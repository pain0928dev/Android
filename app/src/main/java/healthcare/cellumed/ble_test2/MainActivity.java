package healthcare.cellumed.ble_test2;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import healthcare.cellumed.ble_test2.BluetoothLE.BleConnectCallback;
import healthcare.cellumed.ble_test2.BluetoothLE.BleScanCallback;
import healthcare.cellumed.ble_test2.BluetoothLE.BluetoothLE;
import healthcare.cellumed.ble_test2.BluetoothLE.BluetoothLEConnectState;
import healthcare.cellumed.ble_test2.BluetoothLE.DeviceBluetoothLE;
import healthcare.cellumed.ble_test2.BluetoothLE.ManageBluetoothLE;


public class MainActivity extends AppCompatActivity {

    Button btn_connect;

    final static String TAG = "MainActivity";

    HashMap<String, DeviceBluetoothLE>  scan_dev;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_connect = (Button)findViewById(R.id.bt_connect);

        scan_dev = new HashMap<String, DeviceBluetoothLE>();

        checkPermission();

        ManageBluetoothLE.getInstance().init(getApplication());
    }

    // Check permission ---------------------------------------------------
    public void checkPermission(){
        int PERMISSION_ALL = 1;
        /*
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS};
        */
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_COARSE_LOCATION
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions( this, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    //private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    //private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 2;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.d(TAG, "requestCode:" + requestCode);
        int inx = 0;
        for(String i : permissions) {
            Log.d(TAG, "permission: " + i + ", " + grantResults[inx]);
            if(i.equals(Manifest.permission.ACCESS_COARSE_LOCATION)){
                if(grantResults[inx] == 0){
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

                    //mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                else{
                    //finish();
                }
            }
        }
        /*
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        */
    }
    // --------------------------------------------------------------------

    final static int MAX_BLE_DEVICE_COUNT = 1;
    public void AddDevice(String name, DeviceBluetoothLE ble_dev){
        if(!scan_dev.containsKey(name)){
            scan_dev.put(name, ble_dev);
            Log.e(TAG, "Add Device : " + name);
        }

        if(scan_dev.size() == MAX_BLE_DEVICE_COUNT){
            ManageBluetoothLE.getInstance().stopScan();
        }
    }

    DeviceBluetoothLE mDeviceBluetoothLE;
    public void onClickScan(View v){
        Log.d(TAG, "onClickScan");
        ManageBluetoothLE.getInstance().startScan(new BleScanCallback() {
            @Override
            public void onStartScan() {
                Toast.makeText(getApplicationContext(),"onStartScan",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onScanning() {
                Toast.makeText(getApplicationContext(),"onScanning",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProcessResult(BluetoothDevice scanResult) {
                if(scanResult.getName() == null) return;
                if(scanResult.getName().contains("CRD-K100")) {
                    Log.e(TAG, "Scan Device : " + scanResult.getName());
                    mDeviceBluetoothLE = new DeviceBluetoothLE(scanResult);
                    AddDevice(scanResult.getName(), new DeviceBluetoothLE(scanResult));
                    //ManageBluetoothLE.getInstance().stopScan();
                }
            }
        });
    }

    public void onClickConnect(View v){

        Iterator iterator = scan_dev.entrySet().iterator();
        DeviceBluetoothLE devBle = null;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Object obj = entry.getValue();
            devBle = (DeviceBluetoothLE)obj;
            Log.d(TAG, "Get.............................");
        }

        ManageBluetoothLE.getInstance().connect(devBle, new BleConnectCallback() {

            @Override
            public void onStartConnect(BluetoothLEConnectState status) {
                Log.d(TAG, "onStartConnect");
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, BluetoothLEConnectState status) {
                Log.d(TAG, "onConnectSuccess");
            }

            @Override
            public void onConnectFail(BluetoothLEConnectState status) {
                Log.d(TAG, "onConnectFail");
            }

            @Override
            public void onDisConnect(DeviceBluetoothLE dev, BluetoothGatt gatt, BluetoothLEConnectState status) {
                Log.d(TAG, "onDisConnect");
            }
        });
    }

    public void onClickDisconnect(View v){
        Log.e(TAG, "onClickDisconnect");
    }

    public void onClickRead(View v){
        Log.e(TAG, "onClickRead");
        ManageBluetoothLE.getInstance().read();

    }

    public void onClickWrite(View v){
        Log.e(TAG, "onClickWrite");
        ManageBluetoothLE.getInstance().write();

    }

    public void onMove(View v){

        Intent i = new Intent(this, SecondActivity.class);
        startActivity(i);
    }
}
