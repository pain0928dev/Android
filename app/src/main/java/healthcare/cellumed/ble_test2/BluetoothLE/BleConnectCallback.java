package healthcare.cellumed.ble_test2.bluetoothle;

import android.bluetooth.BluetoothGatt;

/**
 * Created by pain0928 on 2018-01-20.
 */

public abstract class BleConnectCallback extends BaseCallback {

    public abstract void onStartConnect();
    public abstract void onConnectSuccess(BluetoothGatt gatt, int status);
    public abstract void onConnectFail(int status);
    public abstract void onDisConnect(BluetoothGatt bluetoothGatt, int status);
}
