package healthcare.cellumed.ble_test2;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import healthcare.cellumed.ble_test2.bluetoothle.BLEDevice;
import healthcare.cellumed.ble_test2.bluetoothle.ManageBluetoothLE;

public class SecondActivity extends AppCompatActivity {

    final String TAG = "SecondActivity";

    TextView tvDisp;

    BLEDevice mBLEDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        tvDisp = (TextView)findViewById(R.id.tv_disp);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        finish();
    }

    public void onClickWrite(View v){
        Log.d(TAG, "onClickWrite");
        ManageBluetoothLE.getInstance().write(mBLEDevice);
    }

    public void onOK(View v){
        Log.d(TAG, "onOK");

    }

    public void onBack(View v){
        Log.d(TAG, "onBack");
        finish();
    }


    Handler mainHandle = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "Second Handle: " + msg. what);
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
