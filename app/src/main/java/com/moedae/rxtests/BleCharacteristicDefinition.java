package com.moedae.rxtests;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;

import java.util.UUID;

/**
 * Created by taun on 07/29/17.
 *
 *
 *  Use the class to declare a characteristic or just a service 16 or 128 bit
 private BleCharacteristicDefinition mTemperatureGenericDefinition = new BleCharacteristicDefinition("GenericTemperature", "1809","2A1C");
 private BleCharacteristicDefinition mTemperatureHistoryDefinition = new BleCharacteristicDefinition("CustomCharacteristic", "12345678-1234-5678-abcd-0123456789AB","12345678-1234-5678-abcd-0123456789AC");

 * Easier clearer comparisons when getting a characteristic or service callback
 *
 if (mActivitySummaryHistoryDefinition.equals(characteristic)) {
    showMsg(String.format("%s *** %s Characteristic Notification Data: %s",this , mTemperatureGenericDefinition.toString(), BLEUtility.byteArrayAsHexString(characteristic.getValue())));

    final byte[] bytes = characteristic.getValue().clone();

    doSomethingWithData(bytes);

 }

 */

public class BleCharacteristicDefinition {
    String mLabel;
    String mDescription;
    UUID mServiceUUID;
    UUID mCharacteristicUUID;

    public BleCharacteristicDefinition() {
    }

    public BleCharacteristicDefinition(String label, String service, String characteristic) {
        setLabel(label);
        setServiceUUID(service);
        setCharacteristicUUID(characteristic);
    }

    public BleCharacteristicDefinition(UUID service, UUID characteristic) {
        setServiceUUID(service);
        setCharacteristicUUID(characteristic);
    }

    /*
    If service and mServiceUUID are both null, still returns false
     */
    public boolean equals(BluetoothGattService bluetoothGattService){
        if (getServiceUUID() != null && bluetoothGattService != null && getServiceUUID().equals(bluetoothGattService.getUuid())){
            return true;
        } else {
            return false;
        }
    }

    public boolean equals(BluetoothGattCharacteristic bluetoothGattCharacteristic){
        if (getCharacteristicUUID() != null && bluetoothGattCharacteristic != null && getCharacteristicUUID().equals(bluetoothGattCharacteristic.getUuid())){
            return equals(bluetoothGattCharacteristic.getService());
        } else {
            return false;
        }
    }

    public void setLabel(String label) {
        mLabel = label;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getLabel() {
        return mLabel;
    }

    public String getDescription() {
        return mDescription;
    }

    public UUID getServiceUUID() {
        return mServiceUUID;
    }

    public void setServiceUUID(String serviceUUID) {
        if (serviceUUID != null){
            mServiceUUID = UUID.fromString(BLEUtility.normaliseUUID(serviceUUID));
        }
    }

    public void setServiceUUID(UUID service) {
        mServiceUUID = service;
    }

    public UUID getCharacteristicUUID() {
        return mCharacteristicUUID;
    }

    public void setCharacteristicUUID(String characteristicUUID) {
        if (characteristicUUID != null){
            mCharacteristicUUID = UUID.fromString(BLEUtility.normaliseUUID(characteristicUUID));
        }
    }

    public void setCharacteristicUUID(UUID characteristic) {
        mCharacteristicUUID = characteristic;
    }

    @Override
    public String toString() {
        return getLabel();
    }
}
