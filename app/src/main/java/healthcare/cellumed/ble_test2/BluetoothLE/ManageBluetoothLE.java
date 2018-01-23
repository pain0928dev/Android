package healthcare.cellumed.ble_test2.BluetoothLE;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.BlockingDeque;

import healthcare.cellumed.ble_test2.BleProfile;

/**
 * Created by ljh0928 on 2018. 1. 4..
 */

public class ManageBluetoothLE {

    final static String TAG = "ManageBluetoothLE";

    private Application mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BleScanner mBleScanner;
    private HashMap<String, BluetoothLE> mBle_map = null;

    String mDeviceName;
    String mAddress;
    String mServiceUUID;
    String mWriteUUID;
    String mNotifyUUID;

    public static ManageBluetoothLE getInstance() {
        return ManagerBleHolder.sManagerBLE;
    }

    private static class ManagerBleHolder {
        private static final ManageBluetoothLE sManagerBLE = new ManageBluetoothLE();
    }

    public void init(Application app) {
        if (mContext == null && app != null) {
            mContext = app;
            mBle_map = new HashMap<String, BluetoothLE>();

            BluetoothManager bluetoothManager = (BluetoothManager) mContext
                    .getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager != null)
                mBluetoothAdapter = bluetoothManager.getAdapter();
            //bleExceptionHandler = new DefaultBleExceptionHandler();
            //multipleBluetoothController = new MultipleBluetoothController();
            //bleScanRuleConfig = new BleScanRuleConfig();
            mBleScanner = BleScanner.getInstance(mContext);
        }

        mDeviceName = "";
        mAddress = "";
        mServiceUUID = "";
        mWriteUUID = "";
        mNotifyUUID = "";
    }

    public void addBluetoothLE(BluetoothLE ble){
        if(mBle_map != null && !mBle_map.containsKey(ble.getDeviceKey())){
            Log.d(TAG, "AddbluetoothLE" + ble.getDevice() + ":" + ble.getDeviceKey());
            mBle_map.put(ble.getDeviceKey(), ble);
        }
    }

    public void removeBleutoothLE(BluetoothLE ble){
        if(mBle_map != null && mBle_map.containsKey(ble.getDeviceKey())){
            Log.d(TAG, "removeBluetoothLE" + ble.getDevice() + ":" + ble.getDeviceKey());
            mBle_map.remove(ble.getDeviceKey());
        }
    }

    public static final String SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455";

    public void setmDeviceName(String devName){
        mDeviceName = devName;
    }

    public void setAddress(String address){
        mAddress = address;
    }

    public void setUUID(String service, String write, String notify){
        mServiceUUID = service;
        mWriteUUID = write;
        mNotifyUUID = notify;
    }

    public Context getContext(){
        return mContext;
    }

    public void startScan(BleScanCallback callback){
        if(callback == null) {
            return;
        }

        // scan해서 나오는 리스트를
        //mBleScanner.setFilters(mDeviceName, mAddress, SERVICE_UUID);
        mBleScanner.startScan(callback);
    }

    public void stopScan(){
        mBleScanner.stopScan();
    }

    public boolean isBlueEnable() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public BluetoothGatt connect(DeviceBluetoothLE dev, BleConnectCallback callback){

        // TODO: 2018. 1. 23.
        // 연결 시도후 정상적으로 장치가 연결 되었음을 알려 줘야한다

        if(callback == null){
            throw new IllegalArgumentException("BleConnectCallback is null");
        }

        if (dev == null || dev.getDevice() == null) {
            callback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);
        } else {
            BluetoothLE bleBluetooth = new BluetoothLE(dev);
            //boolean autoConnect = bleScanRuleConfig.isAutoConnect();
            return bleBluetooth.connect(dev, false, callback);
        }
        return null;
    }

    public void disconnect(){

    }

    public void read(){

        if(mBle_map.containsKey("CRD-K100(E2C2)D8:80:39:F5:E2:C2")){
            Log.e(TAG, "read");
            BluetoothLE ble = mBle_map.get("CRD-K100(E2C2)D8:80:39:F5:E2:C2");
            BluetoothGatt gatt = ble.getmBluetoothGatt();

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(UUID.fromString(BleProfile.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BleProfile.NOTIFY_UUID));

            gatt.readCharacteristic(characteristic);


        }

    }

    public void write(){
        if(mBle_map.containsKey("CRD-K100(E2C2)D8:80:39:F5:E2:C2")){
            Log.e(TAG, "write");
            BluetoothLE ble = mBle_map.get("CRD-K100(E2C2)D8:80:39:F5:E2:C2");
            BluetoothGatt gatt = ble.getmBluetoothGatt();

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(UUID.fromString(BleProfile.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BleProfile.NOTIFY_UUID));

            byte[] data = { 0x31, 0x32, 0x33, 0x34, 0x35};

            characteristic.setValue(data);

            gatt.writeCharacteristic(characteristic);

        }
    }

}
