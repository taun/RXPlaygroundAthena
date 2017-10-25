package com.moedae.rxtests;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

import static android.bluetooth.BluetoothProfile.GATT;

interface BleEventHandler {
    public void handle(Integer event);
}

interface TestClassInterface {
//    public Integer doSomethingWith(Integer anInt);
    public void setBleHandler(BleEventHandler handler);
    public void handleEvent(Integer event);
}

class BLETestClass implements  TestClassInterface {

//    public Integer doSomethingWith(Integer anInt){
//        return 2*anInt;
//    }

    BleEventHandler mHandler;

    public void setBleHandler(BleEventHandler handler){
        mHandler = handler;
    }

    public void handleEvent(Integer event){
        mHandler.handle(event);
    }
}

public class MainActivity extends Activity {

    final static String TAG = "RXPlayground";

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    public static final UUID UUID_CHARACTERISTIC_DESCRIPTOR_EXTENDED = UUID.fromString(BLEUtility.normaliseUUID("2900"));

    /**
     * BT SIG 3.3.3.2 Characteristic User Description
     */
    public static final UUID UUID_CHARACTERISTIC_DESCRIPTOR_USER_DESC_R = UUID.fromString(BLEUtility.normaliseUUID("2901"));

    /**
     * BT SIG 3.3.3.3 Client Characteristic Configuration
     */
    public static final UUID UUID_CHARACTERISTIC_DESCRIPTOR_CONFIG_RW = UUID.fromString(BLEUtility.normaliseUUID("2902"));

    /**
     * BT SIG 3.3.3.5 Characteristic Presentation Format
     */
    public static final UUID UUID_CHARACTERISTIC_DESCRIPTOR_FORMAT_R = UUID.fromString(BLEUtility.normaliseUUID("2903"));


    private BleCharacteristicDefinition mAthenaServiceDefinition =     new BleCharacteristicDefinition("AthenaService",
            "000048CC-3CE7-CE73-48CC-AFBCBF17CD4B",null);

    private BleCharacteristicDefinition mAthenaStatusDefinition =     new BleCharacteristicDefinition("AthenaStatus",
            "000048CC-3CE7-CE73-48CC-AFBCBF17CD4B","000048CE-3CE7-CE73-48CC-AFBCBF17CD4B");

    private BleCharacteristicDefinition mAthenaControlDefinition =     new BleCharacteristicDefinition("AthenaControl",
            "000048CC-3CE7-CE73-48CC-AFBCBF17CD4B","000048CD-3CE7-CE73-48CC-AFBCBF17CD4B");


    private BleCharacteristicDefinition mDeviceInfoDefinition =     new BleCharacteristicDefinition("DeviceInfo",
            "180A",null);

    private BleCharacteristicDefinition mDeviceInfoSerialNumberDefinition =     new BleCharacteristicDefinition("DeviceInfoSerial",
            "180A","2A25");

    private BleCharacteristicDefinition mDeviceInfoFirmwareDefinition =     new BleCharacteristicDefinition("DeviceInfoFirmware",
            "180A","2A26");

    private BleCharacteristicDefinition mDeviceInfoHardwareDefinition =     new BleCharacteristicDefinition("DeviceInfoHardware",
            "180A","2A27");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    Maps of Service,Characteristic commands for dispatching
    Is there a way to pass arguments?

    Use an Observable.interval rather than cancelling and repeating a one shot?
    Hmmm use an interval and subscribe/unsubscribe doesn't get observed when unsubsribed!

    Map by way of concatentation of strings

    Keys:
    Service1
    Service1Characteristic1
    Service1Characteristic1Read
    Service1Characteristic1Written
    Service1Characteristic1Changed
    Service2
    Service2Characteristic2
    .....


     */

    private void multiplyEvent(Integer event){
        Integer result = event * 3;
        Integer newResult = result * event;
    }

    Integer mOutput;

    protected void onResume() {
        super.onResume();


        BiFunction<TestClassInterface, Integer, Integer> anFunction;

//        anFunction = TestClassInterface::doSomethingWith;

        BLETestClass sampleInstance = new BLETestClass();

//        Integer result = anFunction.apply(sampleInstance, 3);


        Map<String,BiFunction<TestClassInterface, Integer, Integer>> functionMap = new HashMap<>();

//        functionMap.put("Six", TestClassInterface::doSomethingWith);

//        Integer result2 = functionMap.get("Six").apply(sampleInstance, 4);

        Integer input = 5;
        Integer output;

        sampleInstance.setBleHandler(event -> {
            Integer result = event * 3;
            Integer newResult = result * event;
            mOutput = newResult;
        });

        sampleInstance.handleEvent(6);

        Map<String, Runnable> commands = new HashMap<>();

        // Populate commands map
        commands.put("delay", () -> restartNotificationTimeout());

        // Invoke some command
        String cmd = "delay";
        commands.get(cmd).run();

        restartNotificationTimeout();

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                differentPath();
            }
        }, 5000);

        Timer thirdRail = new Timer();
        thirdRail.schedule(new TimerTask() {
            @Override
            public void run() {
                thirdRailPath();
             }
        }, 10000);

        boolean persisted = false;
        if (persisted){
            //"78:4A:0C:D6:EA:CF"
            // bonded address
            getBluetoothAdapter().getRemoteDevice("78:4A:0C:D6:EA:CF").connectGatt(getApplicationContext(), false, mGattCallback);
        } else {
            scan();
        }
    }

    @Override
    protected void onPause() {
        notificationSubscription.unsubscribe();
        super.onPause();
    }

    private Subscription notificationSubscription;

    Observer notificationTimeoutObserver = new Observer() {
        @Override
        public void onCompleted() {
            Log.v(TAG,"Timer completed");
        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            Log.v(TAG,"Timer went");
        }
    };

    int notificationTimeout = 16;

//    Observable watchDogObservable = Observable.interval(notificationTimeout, TimeUnit.SECONDS);

    void restartNotificationTimeout(){
        if (notificationSubscription != null && !notificationSubscription.isUnsubscribed()){
            notificationSubscription.unsubscribe();
        }

//        notificationSubscription = watchDogObservable.subscribe(notificationTimeoutObserver);

        notificationSubscription = Observable.timer(notificationTimeout, TimeUnit.SECONDS).subscribe(notificationTimeoutObserver);
    }

    private void differentPath(){
        restartNotificationTimeout();
        Log.v(TAG,"Runnable went");
    }

    private void thirdRailPath(){
        restartNotificationTimeout();
        Log.v(TAG,"TimerTask went");
    }

    private void scan() {

        BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
        if (bluetoothAdapter != null) {
//                if (isScanning()){
//                    getBluetoothLeScanner().stopScan(mScanCallback);
//                    if (mSstBleCallback != null) mSstBleCallback.discoveryStopped(this);
//                }

            UUID[] services = new UUID[1];
            services[0] = mAthenaServiceDefinition.getServiceUUID();
            bluetoothAdapter.startLeScan(services, mLEScanCallback);
        }

    }

    public BluetoothManager getBluetoothManager() {

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        }

        return mBluetoothManager;
    }

    public BluetoothAdapter getBluetoothAdapter() {

        if (mBluetoothAdapter == null) {
            if (getBluetoothManager() != null)
                mBluetoothAdapter = getBluetoothManager().getAdapter();
        }

        return mBluetoothAdapter;
    }

    private final Object mLock = new Object();

    public void connectToDevice(final String address) {

        synchronized (mLock) {
            Log.v(TAG, "entering connectToDevice - starting connect" + " address: " + address);

            // null gatt
            boolean autoConnect = false;

            List<BluetoothDevice> connectedDevices = getBluetoothManager().getConnectedDevices(GATT);
            Log.v(TAG, String.format("connectToDevice - found %d connected devices", connectedDevices.size()));

            for (BluetoothDevice device : connectedDevices) {
                if (device.getAddress().equals(address)) {
                    Log.v(TAG, String.format("connectToDevice - device %s already connected, doing nothing", address));
                    device.connectGatt(getApplicationContext(), autoConnect, mGattCallback);
                    return;
                }
            }

            if (getBluetoothAdapter().getRemoteDevice(address) != null) {
                Log.v(TAG, String.format("connectToDevice - initiate new gatt connection to address: %s, auto: %s", address, Boolean.valueOf(autoConnect)));
                getBluetoothAdapter().getRemoteDevice(address).connectGatt(getApplicationContext(), autoConnect, mGattCallback);
            }
        }
    }

    private final BluetoothAdapter.LeScanCallback mLEScanCallback =
        new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecordBytes) {
                ScanRecord scanRecord = ScanRecord.parseFromBytes(scanRecordBytes);

                Log.v(TAG, String.format("advertising onScanResult: ScanRecord %s", scanRecord.toString()));
                int adFlags = scanRecord.getAdvertiseFlags();
                SparseArray<byte[]> manuData = scanRecord.getManufacturerSpecificData();
                Log.v(TAG, String.format("onScanResult: RSSI: %d advertisement flag: %d, manu data: %s", rssi, adFlags, manuData));

                // should check keys range, ...
                // TODO: no debounce is implemented. There will be multiple sequential and redundant connection attempts
                // Also need timeouts started when the connection attempt is started, bonding, ...
                if (manuData.get(89)[0] == 1 && manuData.get(89)[1] != 0) {
                    getBluetoothAdapter().stopLeScan(mLEScanCallback);
                    connectToDevice(device.getAddress());
                } else {
                    // check address and if correct, connect
                    // for now with only one athena, just connect anyhow
                    connectToDevice(device.getAddress());
                }
            }
        };


    // ------------------------------------- GATT CALLBACKS -----------------------------------------
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        private final Handler mHandler = new Handler();

        public void successfulConnection(final BluetoothGatt gatt) {

            Log.v(TAG, "Connected - connection attempt duration (s)");

            final boolean bonded = gatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED;

            int delay = 0; // around 1600 ms is required when connection interval is ~45ms.

            if (!bonded) {
                gatt.getDevice().createBond();
                delay = 1900;
            } else {


            }

            Log.v(TAG, "Connection bonded status: " + bonded + " Services Discovery delay: " + delay);

            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Some proximity tags (e.g. nRF PROXIMITY) initialize bonding automatically when connected.
//                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING) {
                    boolean discoveryStarted = gatt.discoverServices();
                    Log.v(TAG,String.format("gatt: %s service discovery start status: %b", gatt, discoveryStarted));
//                }
                }
            }, delay);
        }

        private void writeCharacteristicDefinitionBytes(final BluetoothGatt gatt, BleCharacteristicDefinition definition, byte[] bytes) {
            final BluetoothGattService service = gatt.getService(definition.getServiceUUID());

            if (service == null){
                return;
            }

            final BluetoothGattCharacteristic characteristic = service.getCharacteristic(definition.getCharacteristicUUID());
            characteristic.setValue(bytes);
            Log.v(TAG, String.format("writeCharacteristicDefinitionBytes: %s, %s",definition , BLEUtility.byteArrayAsHexString(bytes)));
            gatt.writeCharacteristic(characteristic);
        }

        private void readCharacteristicDefinition(final BluetoothGatt gatt, BleCharacteristicDefinition definition) {
            if (gatt == null || definition == null){
                return;
            }

            BluetoothGattService service = gatt.getService(definition.getServiceUUID());

            if (service == null || definition.getCharacteristicUUID() == null){
                return;
            }

            BluetoothGattCharacteristic characteristic1 = service.getCharacteristic(definition.getCharacteristicUUID());
            Log.v(TAG, String.format("readCharacteristicDefinition: %s",definition));
            gatt.readCharacteristic(characteristic1);
        }

        private void enableNotificationsOrIndicationsOnCharacteristicUUID(final boolean indicate, final BluetoothGatt gatt, BleCharacteristicDefinition definition) {

            final BluetoothGattService deviceService = gatt.getService(definition.getServiceUUID());

            if (deviceService == null || definition == null || definition.getCharacteristicUUID() == null){
                return;
            }

            final BluetoothGattCharacteristic deviceCharacteristic = deviceService.getCharacteristic(definition.getCharacteristicUUID());

            BluetoothGattDescriptor descriptor = deviceCharacteristic.getDescriptor(UUID_CHARACTERISTIC_DESCRIPTOR_CONFIG_RW);

            boolean notifySetSuccess;
            if (indicate){
                notifySetSuccess = setIndicateForCharacteristic(gatt, deviceCharacteristic, true);
            } else {
                notifySetSuccess = setNotifyForCharacteristic(gatt, deviceCharacteristic, true);
            }
            // *** May be false due to device busy
            Log.v(TAG, String.format("%s *** enableNotifications on %s indicate: %b, setNotify success: %b", this, definition, indicate, notifySetSuccess));
        }

        public boolean setNotifyForCharacteristic(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean shouldNotify) {
            return setNotifyOrIndicateForCharacteristic(gatt, characteristic, shouldNotify, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }

        public boolean setIndicateForCharacteristic(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean shouldNotify) {
            return setNotifyOrIndicateForCharacteristic(gatt, characteristic, shouldNotify, BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }

        private boolean setNotifyOrIndicateForCharacteristic(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean shouldNotify, byte[] value) {
            if (gatt == null) {
                return false;
            } else {

                boolean notifyOn = gatt.setCharacteristicNotification(characteristic, shouldNotify);

                // TODO This should not be necessary! Test without once working with
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_CHARACTERISTIC_DESCRIPTOR_CONFIG_RW);

                if (shouldNotify) {
                    descriptor.setValue(value);
                } else {
                    descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                }

                return gatt.writeDescriptor(descriptor);

            }
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String address = gatt.getDevice().getAddress();

            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) { // && status == BluetoothGatt.GATT_SUCCESS
                Log.v(TAG, "connected:" + address);

                long delay = 0;
                long minInterval = 36000;

                long currentConnection = System.currentTimeMillis();
                successfulConnection(gatt);

            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.v(TAG, "disconnected:" + address);
                gatt.connect(); // uses autoConnect = true

            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // TODO: removed restartDiscovery from here. Does Novartis app need it?
                String errorMsg;

                // if status == 133 means no advertisements from device available.
                // if status == 34 0x22 ?
                // if status == 22 0x16 means?
                Log.e(TAG, "connection failed status: " + status + " address: " + address);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(TAG, "service discovery for gatt" + gatt + " status " + status);
                readCharacteristicDefinition(gatt, mDeviceInfoSerialNumberDefinition);

            } else {
                Log.v(TAG, "service discovery failed for gatt" + gatt + " status " + status);
            }
        }

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(TAG, String.format("onCharacteristicRead: %s", characteristic.getUuid().toString()));

                if (mDeviceInfoSerialNumberDefinition.equals(characteristic)) {
                    byte[] bytes = characteristic.getValue();
                    String serial = new String(bytes);
                    Log.v(TAG, String.format("Athena Serial Number: %s", serial));
                    enableNotificationsOrIndicationsOnCharacteristicUUID(true, gatt, mAthenaStatusDefinition);
                }

            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                Log.e(TAG, String.format("GATT_INSUFFICIENT_AUTHENTICATION onCharacteristicRead: %s", characteristic.getUuid().toString()));
            }

        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.v(TAG, String.format("onCharacteristicWrite: %s", characteristic.getUuid().toString()));

            } else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
                Log.e(TAG, String.format("GATT_INSUFFICIENT_AUTHENTICATION onCharacteristicWrite: %s", characteristic.getUuid().toString()));
            }
        }

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.v(TAG, String.format("onCharacteristicChanged: %s", characteristic.getUuid().toString()));

            if (mAthenaStatusDefinition.equals(characteristic)) {
                byte[] statusBytes = characteristic.getValue();
                int status = statusBytes[0] & 0x0f;
                Log.v(TAG, String.format("Athena Status: %s", BLEUtility.byteArrayAsHexString(statusBytes)));
                if (status != 0) {
                    // reset athena
                    byte[] bytes = new byte[1];
                    bytes[0] = 0;
                    writeCharacteristicDefinitionBytes(gatt, mAthenaControlDefinition, bytes);
                }
            }

        }


        @Override
        public void onDescriptorRead(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

        }

        @Override
        public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.v(TAG, String.format("onDescriptorWrite: %s", descriptor.getCharacteristic().getUuid().toString()));

        }

        @Override
        public void onReliableWriteCompleted(final BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(final BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.v(TAG, "onReadRemoteRssi");
        }

        @Override
        public void onMtuChanged(final BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.v(TAG, "onMtuChanged");
        }
    };

}
