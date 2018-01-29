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

    private BluetoothLeScanner bluetoothLeScanner;
    private ScanSettings scanSettings;
    private List<ScanFilter> filterList;
    private WeakReference<Context> contextWeakReference;
    private static BleScanner instance;
    BleScanCallback bleScanCallback;

    BleScanner(Context context){
        contextWeakReference = new WeakReference<Context>(context);
    }

    public static BleScanner getInstance(Context context) {


        if (instance == null) {
            instance = new BleScanner(context);
        } else {
            instance.contextWeakReference = new WeakReference<Context>(context);
        }
        return instance;
    }

    public void setFilters(String name, String address, String uuid){
        filterList = new ArrayList<ScanFilter>();

        ScanFilter sf;
        ParcelUuid pu = new ParcelUuid(UUID.fromString(uuid));
        sf = new ScanFilter.Builder().setServiceUuid(pu).setDeviceName(name).setDeviceAddress(address).build();

        filterList.add(sf);
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();
        //scanSettings = new ScanSettings.Builder().build();
    }

    public void startScan(BleScanCallback callback){

        BluetoothManager bluetoothManager;
        BluetoothAdapter bluetoothAdapter;

        Context context = contextWeakReference.get();

        if(context == null) return;

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return;
        }

        if (!bluetoothAdapter.isEnabled()) bluetoothAdapter.enable();

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (bluetoothLeScanner == null) {
            Log.d(TAG, "Can not find BLE Scanner");
            return;
        }

        bleScanCallback = callback;
        //mBleScanner.startScan(filters, scanSettings, mScanCallback);
        bluetoothLeScanner.startScan(scanCallback);
        bleScanCallback.onStartScan();
        Log.d(TAG, "start scan");

    }

    public void stopScan() {
        if (bluetoothLeScanner == null || scanCallback == null) {
            return;
        }
        bluetoothLeScanner.stopScan(scanCallback);
    }


    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //processResult(result);
            bleScanCallback.onProcessResult(new BLEDevice(result.getDevice().getName(), result.getDevice().getAddress()));
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                //processResult(result);
                bleScanCallback.onProcessResult(new BLEDevice(result.getDevice().getName(), result.getDevice().getAddress()));
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
