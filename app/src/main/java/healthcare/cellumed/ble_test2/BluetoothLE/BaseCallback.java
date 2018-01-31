package healthcare.cellumed.ble_test2.bluetoothle;

import android.os.Handler;

/**
 * Created by pain0928 on 2018-01-19.
 */

public abstract class BaseCallback {
    private String key;
    private Handler handler;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }
}
