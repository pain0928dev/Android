package healthcare.cellumed.ble_test2;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import healthcare.cellumed.ble_test2.bluetoothle.BLEDevice;
import healthcare.cellumed.ble_test2.bluetoothle.BleConnectCallback;
import healthcare.cellumed.ble_test2.bluetoothle.BleScanCallback;
import healthcare.cellumed.ble_test2.bluetoothle.ManageBluetoothLE;
import healthcare.cellumed.ble_test2.util.HexaDump;


public class MainActivity extends AppCompatActivity {

    Button btnScan;
    Button btnConnect;

    private final static String TAG = "MainActivity";

    ArrayList<BLEDevice> scanBLEDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnScan = (Button)findViewById(R.id.bt_scan);
        btnConnect = (Button)findViewById(R.id.bt_connect1);

        scanBLEDeviceList = new ArrayList<BLEDevice>();

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
                } else {
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

    public boolean checkEqualObject(BLEDevice dev){
        for(int i=0; i< scanBLEDeviceList.size(); i++){
            if(scanBLEDeviceList.get(i).getName().equals(dev.getName())){
                Log.d(TAG, "same name.....");
                return true;
            }
        }
        return false;
    }

    final static int MAX_BLE_DEVICE_COUNT = 2;
    public void AddDevice(BLEDevice dev){

        if(checkEqualObject(dev)){
            ManageBluetoothLE.getInstance().stopScan();
            return;
        } else {
            Log.i(TAG, "Add BLE Device (" + dev.getName() + ", " + dev.getAddress() + ")");
            scanBLEDeviceList.add(dev);
        }

        if(scanBLEDeviceList.size() == MAX_BLE_DEVICE_COUNT){
            Log.i(TAG, "Couldn't Add because of Maximum");
            if(btnScan.getText().equals("stop")){
                btnScan.setText("scan");
                ManageBluetoothLE.getInstance().stopScan();
            }
        }
    }

    BluetoothDevice mBluetoothDevice;
    public void onClickScan(View v){
        Log.d(TAG, "onClickScan");

        if(btnScan.getText().equals("scan")){
            btnScan.setText("stop");
        } else if (btnScan.getText().equals("stop")){
            btnScan.setText("scan");
            ManageBluetoothLE.getInstance().stopScan();
            return;
        }

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
            public void onProcessResult(BLEDevice dev) {
                if(dev.getName() == null) return;
                //if(scanResult.getName().contains("CRD-K100")) {
                if(dev.getName().contains("CRD-K100")) {
                    Log.d(TAG, "Scan Device : " + dev.getName() + ", " + dev.getAddress());
                    AddDevice(dev);
                }
            }
        });
    }

    /*
    public void onClickConnect(View v){

        BLEDevice bleDev = null;
        for(int i=0; i < scanBLEDeviceList.size(); i++){
            bleDev = scanBLEDeviceList.get(i);
            Log.i(TAG, "Connect: " + bleDev.getName());
        }

        ManageBluetoothLE.getInstance().connect(bleDev, new BleConnectCallback() {

            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect");
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess");
            }

            @Override
            public void onConnectFail(int status) {
                Log.d(TAG, "onConnectFail");
            }

            @Override
            public void onDisConnect(BluetoothGatt bluetoothGatt, int status) {
                Log.d(TAG, "onDisConnect");
            }
        });
    }

    public void onClickRead(View v){
        Log.d(TAG, "onClickRead");
        BLEDevice bleDev = null;
        for(int i=0; i < scanBLEDeviceList.size(); i++){
            bleDev = scanBLEDeviceList.get(i);

            byte[] bytes = ManageBluetoothLE.getInstance().read(bleDev);
            Log.i(TAG, "Device(" + bleDev.getName() + ") -> " +
                    HexaDump.toString(bytes, bytes.length));
        }
    }

    public void onClickWrite(View v){
        Log.d(TAG, "onClickWrite");
        BLEDevice bleDev = null;
        for(int i=0; i < scanBLEDeviceList.size(); i++){
            bleDev = scanBLEDeviceList.get(i);
            ManageBluetoothLE.getInstance().write(bleDev);
            Log.i(TAG, "Device: " + bleDev.getName());
        }
    }
    */

    public void onClickConnect1(View v){

        BLEDevice bleDev = scanBLEDeviceList.get(0);
            Log.i(TAG, "Connect: " + bleDev.getName());

        startConect(bleDev);
    }
    public void onClickConnect2(View v){

        BLEDevice bleDev = scanBLEDeviceList.get(1);
        Log.i(TAG, "Connect: " + bleDev.getName());

        startConect(bleDev);
    }

    public void startConect(final BLEDevice bleDev){
        ManageBluetoothLE.getInstance().connect(bleDev, new BleConnectCallback() {

            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect");
                Log.d(TAG, "Name: " + bleDev.getName());
            }

            @Override
            public void onConnectSuccess(BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess");
            }

            @Override
            public void onConnectFail(int status) {
                Log.d(TAG, "onConnectFail");
            }

            @Override
            public void onDisConnect(BluetoothGatt bluetoothGatt, int status) {
                Log.d(TAG, "onDisConnect");
            }
        });
    }

    public void onClickRead1(View v){
        Log.d(TAG, "onClickRead");
        BLEDevice bleDev = scanBLEDeviceList.get(0);
        byte[] bytes = ManageBluetoothLE.getInstance().read(bleDev);
        if(bytes != null)
            Log.i(TAG, "Device(" + bleDev.getName() + ") -> " +
                    HexaDump.toString(bytes, bytes.length));
    }

    public void onClickRead2(View v){
        Log.d(TAG, "onClickRead");
        BLEDevice bleDev = scanBLEDeviceList.get(1);
        byte[] bytes = ManageBluetoothLE.getInstance().read(bleDev);
        if(bytes != null)
            Log.i(TAG, "Device(" + bleDev.getName() + ") -> " +
                    HexaDump.toString(bytes, bytes.length));
    }

    public void onClickWrite1(View v){
        Log.d(TAG, "onClickWrite");
        BLEDevice bleDev = scanBLEDeviceList.get(0);
            ManageBluetoothLE.getInstance().write(bleDev);
            Log.i(TAG, "Device: " + bleDev.getName());
    }

    public void onClickWrite2(View v){
        Log.d(TAG, "onClickWrite");
        BLEDevice bleDev = scanBLEDeviceList.get(1);
        ManageBluetoothLE.getInstance().write(bleDev);
        Log.i(TAG, "Device: " + bleDev.getName());
    }

    public void onClickDisconnect(View v){
        Log.d(TAG, "onClickDisconnect");

        BLEDevice bleDev = null;
        for(int i=0; i < scanBLEDeviceList.size(); i++){
            bleDev = scanBLEDeviceList.get(i);
            ManageBluetoothLE.getInstance().disconnect(bleDev);
            Log.i(TAG, "Device: " + bleDev.getName());
        }
    }

    public void onMove(View v){

        Intent i = new Intent(this, SecondActivity.class);
        startActivity(i);
    }

    Handler mainHandle = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Main Handle: " + msg. what);
            Log.d(TAG, "data: " + (String)msg.obj);

            switch (msg.what) {
                case 1:
                    break;
                default:
                    break;
            }
        }
    };

}
