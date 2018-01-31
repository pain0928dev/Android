package healthcare.cellumed.ble_test2.bluetoothle;

import android.bluetooth.BluetoothDevice;

/**
 * Created by ljh0928 on 2018. 1. 29..
 */

public abstract class BluetoothLECallback {
    public abstract void onConnectionState(BluetoothDevice bluetoothDevice, BluetoothLEConnectState bluetoothLEConnectState);
    public abstract void onChanged(BluetoothDevice bluetoothDevice, byte[] inBytes);
    public abstract void onRead(BluetoothDevice bluetoothDevice, byte[] inBytes, int status);
    public abstract void onReadRssi(BluetoothDevice bluetoothDevice, int rssi, int status);
    public abstract void onReadMtu(BluetoothDevice bluetoothDevice, int mtu, int status);
    public abstract void onWrite(BluetoothDevice bluetoothDevice, int status);
    //public abstract void onRead(int status);
}
