package healthcare.cellumed.ble_test2;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

public class BLeScanActivity extends AppCompatActivity {

    final String TAG ="BLeScanActivity";

    String mName;
    String mAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scan);

        Intent intent = getIntent();
        mName = intent.getStringExtra("test1234");
        mAddress = intent.getStringExtra("test");

        Log.d(TAG, "name: " + mName + ", address: " + mAddress);

        checkPermission();

        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        // Checks if Bluetooth LE Scanner is available.
        if (mBLEScanner == null) {
            Log.d(TAG, "Can not find BLE Scanner");
            finish();
            return;
        }

        mBLEScanner.startScan(mScanCallback);
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.d(TAG, "onResume");

        mBLEScanner.startScan(mScanCallback);
    }

    @Override
    protected void onPause(){
        super.onPause();
        Log.d(TAG, "onPause");

        mBLEScanner.stopScan(mScanCallback);
    }

    @Override
    protected void onStop(){
        super.onStop();
        Log.d(TAG, "onStop");

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;

    public void checkPermission(){

        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        Log.d("BluetoothLeService",  "initialize is " + mBluetoothManager);
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
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

                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                }
                else{
                    finish();
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

    public void findDevice(String name, String address){

        mBLEScanner.stopScan(mScanCallback);

        Intent intent = new Intent(this, MainActivity.class);
        //intent.putExtra(ManageBluetoothLE.BLE_NAME, name);
        //intent.putExtra(ManageBluetoothLE.BLE_ADDRESS, address);

        setResult(RESULT_OK, intent);

        finish();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
        }

        private void processResult(final ScanResult result) {
            if(result.getDevice().getName() == null) return;

            Log.e(TAG, "Scan Device : " + result.getDevice().getName());

            if(result.getDevice().getName().contains(mName)){
                findDevice(result.getDevice().getName(), result.getDevice().getAddress());
            }
        }
    };
}
