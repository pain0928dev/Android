package healthcare.cellumed.ble_test2;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ljh0928 on 2018. 1. 4..
 */

public class ManageBluetoothLE {

    final static String TAG = "ManageBluetoothLE";

    private Application mContext;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BleScanner mBleScanner;

    public static ManageBluetoothLE getInstance() {
        return ManagerBleHolder.sManagerBLE;
    }

    private static class ManagerBleHolder {
        private static final ManageBluetoothLE sManagerBLE = new ManageBluetoothLE();
    }

    public void init(Application app) {
        if (mContext == null && app != null) {
            mContext = app;
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

    String mDeviceName;
    String mAddress;
    String mServiceUUID;
    String mWriteUUID;
    String mNotifyUUID;

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

    }

    public void write(){

    }

}
