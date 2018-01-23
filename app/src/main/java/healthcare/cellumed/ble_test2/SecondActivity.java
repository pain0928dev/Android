package healthcare.cellumed.ble_test2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    final String TAG = "SecondActivity";

    TextView tvDisp;

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

    public void onOK(View v){
        Log.d(TAG, "onOK");

    }

    public void onBack(View v){
        Log.d(TAG, "onBack");
        finish();
    }

}
