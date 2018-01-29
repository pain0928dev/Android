package healthcare.cellumed.ble_test2.BluetoothLE;

import android.bluetooth.BluetoothGatt;

/**
 * Created by pain0928 on 2018-01-20.
 */

public abstract class BleConnectCallback extends BaseCallback {

    public abstract void onStartConnect(BluetoothLEConnectState status);
    public abstract void onConnectSuccess(BluetoothGatt gatt, BluetoothLEConnectState status);
    public abstract void onConnectFail(BluetoothLEConnectState status);
    public abstract void onDisConnect(DeviceBluetoothLE dev, BluetoothGatt gatt, BluetoothLEConnectState status);
}
