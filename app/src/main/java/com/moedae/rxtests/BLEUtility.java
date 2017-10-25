package com.moedae.rxtests;

/**
 * Created by taun on 10/04/16.
 */

import org.apache.commons.lang3.mutable.MutableInt;

/**
 * These class methods should be in the SSTBLEManager class
 * or a Service/Characteristic class or extensions class
 */
public class BLEUtility {

    private static final String BLUETOOTH_SIG_UUID_BASE = "0000XXXX-0000-1000-8000-00805f9b34fb";
    private static final String HEX_CHARS = "01234567890ABCDEF";

    private final static int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
    private final static int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
    private final static int SHIFT_LEFT_8BITS = 8;
    private final static int SHIFT_LEFT_16BITS = 16;
    private final static int GET_BIT24 = 0x00400000;
    private final static int FIRST_BIT_MASK = 0x01;

    public static long addressStringAsLong(String address) {

        String strippedHex = address.replace(":", "");
        long longID = Long.parseLong(strippedHex, 16);

        return longID;
    }

    public static String addressShortFormFrom(String address) {
        String shortForm = address;

        if (shortForm != null) {
            int length = shortForm.length();
            int beginIndex = length - 5;
            if (beginIndex > 0) {
                shortForm = shortForm.substring(beginIndex);
            }
        }

        return  shortForm;
    }


    public static String normaliseUUID(String uuid) {
        String normalised_128_bit_uuid = uuid;
        if (uuid.length() == 4) {
            normalised_128_bit_uuid = BLUETOOTH_SIG_UUID_BASE.replace("XXXX", uuid);
        }
        if (uuid.length() == 32) {
            normalised_128_bit_uuid = uuid.substring(0, 8) + "-"
                    + uuid.substring(8, 12) + "-"
                    + uuid.substring(12, 16) + "-"
                    + uuid.substring(16, 20) + "-"
                    + uuid.substring(20, 32);
        }
        return normalised_128_bit_uuid;
    }

    public static String extractCharacteristicUuidFromTag(String tag) {
        String uuid = "";
        String[] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[3];
        }
        return uuid;
    }

    public static String extractServiceUuidFromTag(String tag) {
        String uuid = "";
        String[] parts = tag.split("_");
        if (parts.length == 4) {
            uuid = parts[2];
        }
        return uuid;
    }

    public static byte[] getByteArrayFromHexString(String hex_string) {
        String hex = hex_string.replace(" ", "");
        hex = hex.toUpperCase();

        byte[] bytes = new byte[hex.length() / 2];
        int i = 0;
        int j = 0;
        while (i < hex.length()) {
            String h1 = hex.substring(i, i + 1);
            String h2 = hex.substring(i + 1, i + 2);
            try {
                int b = (Integer.valueOf(h1, 16).intValue() * 16) + (Integer.valueOf(h2, 16).intValue());
                bytes[j++] = (byte) b;
                i = i + 2;
            } catch (NumberFormatException e) {
                System.out.println("NFE handling " + h1 + h2 + " with i=" + i);
                throw e;
            }
        }
        return bytes;
    }


    public static String byteArrayAsHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int l = bytes.length;
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < l; i++) {
            if ((bytes[i] >= 0) & (bytes[i] < 16))
                hex.append("0");
            hex.append(Integer.toString(bytes[i] & 0xff, 16).toUpperCase());
        }
        return hex.toString();
    }

    public static boolean isValidHex(String hex_string) {
        System.out.println("isValidHex(" + hex_string + ")");
        String hex = hex_string.replace(" ", "");
        hex = hex.toUpperCase();
        int len = hex.length();
        int remainder = len % 2;
        if (remainder != 0) {
            System.out.println("isValidHex: not even number of chars");
            return false;
        }
        for (int i = 0; i < len; i++) {
            if (!HEX_CHARS.contains(hex.substring(i, i + 1))) {
                return false;
            }
        }
        return true;
    }

    // ----- Utilities from Android Characteristic library

    // FIXME add bounds checking to all methods!
    public static int bytesU8ToInt(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        int value = unsignedByteToInt(bytes[offset]);
        mutableOffset.add(1);
        return value;
    }

    public static int bytesU16ToInt(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        int value = unsignedBytesToInt(bytes[offset], bytes[offset + 1]);
        mutableOffset.add(2);
        return value;
    }

    public static int bytesS8ToInt(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        int value = unsignedToSigned(unsignedByteToInt(bytes[offset]),8);
        mutableOffset.add(1);
        return value;
    }

    public static int bytesS16ToInt(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        int value = unsignedToSigned(unsignedBytesToInt(bytes[offset], bytes[offset + 1]), 16);
        mutableOffset.add(2);
        return value;
    }

    public static int bytesU32ToInt(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        int value = unsignedBytesToInt(bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]);
        mutableOffset.add(4);
        return value;
    }

    public static float bytesF16ToFloat(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();
        float value = bytesToFloat(bytes[offset], bytes[offset + 1]);
        mutableOffset.add(2);
        return value;
    }

    public static float bytesF32ToFloat(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();

        int i = BLEUtility.bytesU32ToInt(bytes, mutableOffset);
        Float f = Float.intBitsToFloat(i);

        return f.floatValue();
    }

    /*

    // convert float to IEEE format, then uint32_t encode

    celcius_ieee.exponent = -2;
    celcius_ieee.mantissa = celciusX100;
    encoded_temp = ( ((celcius_ieee.exponent << 24) & 0xFF000000) |
            ((celcius_ieee.mantissa << 0) & 0x00FFFFFF) );
    len += uint32_encode( encoded_temp,
&p_encoded_buffer[len]);

     */
    public static double bytesF32IEE1073ToDouble(byte[] bytes, MutableInt mutableOffset) {
        int offset = mutableOffset.intValue();

        byte exponential = bytes[4];
        short firstOctet = convertNegativeByteToPositiveShort(bytes[1]);
        short secondOctet = convertNegativeByteToPositiveShort(bytes[2]);
        short thirdOctet = convertNegativeByteToPositiveShort(bytes[3]);
        int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
        mantissa = getTwosComplimentOfNegativeMantissa(mantissa);

        mutableOffset.add(4);

        return  (mantissa * Math.pow(10, exponential));
    }

    private static short convertNegativeByteToPositiveShort(byte octet) {
        if (octet < 0) {
            return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
        } else {
            return octet;
        }
    }

    private static int getTwosComplimentOfNegativeMantissa(int mantissa) {
        if ((mantissa & GET_BIT24) != 0) {
            return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
        } else {
            return mantissa;
        }
    }


    public static long bytesU64ToLong(byte[] bytes, MutableInt mutableOffset) {

        int offset = mutableOffset != null ? mutableOffset.intValue() : 0;

        long value = unsignedBytesToLong(bytes[offset], bytes[offset + 1], bytes[offset + 2], bytes[offset + 3]
                , bytes[offset + 4], bytes[offset + 5], bytes[offset + 6], bytes[offset + 7]);

        if (mutableOffset != null) mutableOffset.add(8);

        return value;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    public static int unsignedByteToInt(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert a signed byte to an unsigned long.
     */
    public static long unsignedByteToLong(byte b) {
        return b & 0xFF;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    public static int unsignedBytesToInt(byte b0, byte b1, byte b2, byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert signed bytes to a 64-bit unsigned int.
     */
    public static long unsignedBytesToLong(byte b0, byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToLong(b2) << 16) + (unsignedByteToLong(b3) << 24)
                + (unsignedByteToLong(b4) << 32) + (unsignedByteToLong(b5) << 40)
                + (unsignedByteToLong(b6) << 48) + (unsignedByteToLong(b7) << 56);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    public static float bytesToFloat(byte b0, byte b1) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
        return (float) (mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    public static float bytesToFloat(byte b0, byte b1, byte b2, byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + (unsignedByteToInt(b1) << 8)
                + (unsignedByteToInt(b2) << 16), 24);
        return (float) (mantissa * Math.pow(10, b3));
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    public static int unsignedToSigned(int unsigned, int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert an integer into the signed bits of a given length.
     */
    public static int intToSignedBits(int i, int size) {
        if (i < 0) {
            i = (1 << size - 1) + (i & ((1 << size - 1) - 1));
        }
        return i;
    }

    public static void main(String args[]) {
        System.out.println("UUID 1802 = " + BLEUtility.normaliseUUID("1802"));
        System.out.println("isValidHex: 01 02 03 04 05 06 A0 B1 C1 D1 E1 F1=" + isValidHex("01 02 03 04 05 06 A0 B1 C1 D1 E1 F1"));
        System.out.println("isValidHex: 01 02 0X 04 05 06 A0 B1 C1 D1 E1 F1=" + isValidHex("01 02 0X 04 05 06 A0 B1 C1 D1 E1 F1"));
    }

}
