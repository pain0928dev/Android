package healthcare.cellumed.ble_test2.BluetoothLE;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by ljh0928 on 2018. 1. 9..
 */

public class BleScanner {

    private static final String TAG = BleScanner.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBleScanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filters;

    private WeakReference<Context> mContextWeakReference;
    private static BleScanner instance;

    BleScanner(Context context){
        mContextWeakReference = new WeakReference<Context>(context);
    }

    public static BleScanner getInstance(Context context) {


        if (instance == null) {
            instance = new BleScanner(context);
        } else {
            instance.mContextWeakReference = new WeakReference<Context>(context);
        }
        return instance;
    }

    public void setFilters(String name, String address, String uuid){
        filters = new ArrayList<ScanFilter>();

        ScanFilter sf;
        ParcelUuid pu = new ParcelUuid(UUID.fromString(uuid));
        sf = new ScanFilter.Builder().setServiceUuid(pu).setDeviceName(name).setDeviceAddress(address).build();

        filters.add(sf);
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        //scanSettings = new ScanSettings.Builder().build();
    }

    BleScanCallback mCallBack;
    public void startScan(BleScanCallback callback){

        Context context = mContextWeakReference.get();
        if(context == null) return;

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.d("BluetoothLeService",  "initialize is " + mBluetoothManager);
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) mBluetoothAdapter.enable();

        mBleScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (mBleScanner == null) {
            Log.d(TAG, "Can not find BLE Scanner");
            return;
        }

        mCallBack = callback;

        Log.d(TAG, "start scan");
        //mBleScanner.startScan(filters, scanSettings, mScanCallback);
        mBleScanner.startScan(mScanCallback);
        mCallBack.onStartScan();
    }

    public void stopScan() {
        if (mBleScanner == null || mScanCallback == null) {
            return;
        }
        mBleScanner.stopScan(mScanCallback);
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //processResult(result);
            mCallBack.onProcessResult(result.getDevice());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                //processResult(result);
                mCallBack.onProcessResult(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan Failed: " + errorCode);
        }

        private void processResult(final ScanResult result) {
            if(result.getDevice().getName() == null) return;
            BluetoothDevice btDev = result.getDevice();
            Log.e(TAG, "Scan Device : " + result.getDevice().getName());
        }
    };




}
