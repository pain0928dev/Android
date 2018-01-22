package healthcare.cellumed.ble_test2;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.util.Map;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Created by ljh0928 on 2018. 1. 18..
 */

public class BluetoothLE {

    final String TAG = "BluetoothLE";

    private BluetoothLEConnectState mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_IDLE;
    private boolean isActiveDisconnect = false;
    private DeviceBluetoothLE mDeviceBluetoothLE;
    private BluetoothGatt mBluetoothGatt;
    private BleConnectCallback mBleConnectCallback;
    private boolean isMainThread = false;
    private MainHandler handler = new MainHandler();

    BluetoothLE(DeviceBluetoothLE dev){
        mDeviceBluetoothLE = dev;
    }

    public synchronized BluetoothGatt connect(DeviceBluetoothLE devBle,
                                              boolean autoConnect,
                                              BleConnectCallback callback) {
        Log.i(TAG, "connect device: " + devBle.getName()
                + "\nmac: " + devBle.getMac()
                + "\nautoConnect: " + autoConnect
                + "\ncurrentThread: " + Thread.currentThread().getId());


        isMainThread = Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper();

        BluetoothGatt gatt;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = devBle.getDevice().connectGatt(ManageBluetoothLE.getInstance().getContext(),
                    autoConnect, coreGattCallback, TRANSPORT_LE);
        } else {
            gatt = devBle.getDevice().connectGatt(ManageBluetoothLE.getInstance().getContext(),
                    autoConnect, coreGattCallback);
        }
        if (gatt != null) {
            mBleConnectCallback = callback;
            if (callback != null)
                callback.onStartConnect(BluetoothLEConnectState.CONNECT_CONNECTING);

            mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_CONNECTING;
        }
        return gatt;
    }

    private BluetoothGatt getBluetoothGatt(){
        return mBluetoothGatt;
    }
    private DeviceBluetoothLE getDeviceBluetoothLE(){
        return mDeviceBluetoothLE;
    }

    private synchronized boolean refreshDeviceCache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null) {
                boolean success = (Boolean) refresh.invoke(getBluetoothGatt());
                Log.i(TAG, "refreshDeviceCache, is success:  " + success);
                return success;
            }
        } catch (Exception e) {
            Log.i(TAG, "exception occur while refreshing device: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public synchronized void disconnect() {
        if (mBluetoothGatt != null) {
            isActiveDisconnect = true;
            mBluetoothGatt.disconnect();
        }
    }

    private synchronized void closeBluetoothGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    public void destroy() {
        mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_IDLE;
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        if (mBluetoothGatt != null) {
            refreshDeviceCache();
        }
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    private static final class MainHandler extends Handler {

        final String TAG = "MainHandler";

        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "handleMessage: " + msg. what);
            switch (msg.what) {

                case BleMsg.MSG_CONNECT_FAIL: {
                    /*
                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                    BleGattCallback callback = para.getCallback();
                    BluetoothGatt gatt = para.getGatt();
                    int status = para.getStatus();
                    if (callback != null)
                        callback.onConnectFail(new ConnectException(gatt, status));
                    */
                    break;
                }

                case BleMsg.MSG_DISCONNECTED: {
                    /*
                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                    BleGattCallback callback = para.getCallback();
                    BluetoothGatt gatt = para.getGatt();
                    boolean isActive = para.isAcitive();
                    BleDevice bleDevice = para.getBleDevice();
                    int status = para.getStatus();
                    if (callback != null)
                        callback.onDisConnected(isActive, bleDevice, gatt, status);
                    */
                    break;
                }

                case BleMsg.MSG_CONNECT_SUCCESS: {
                    /*
                    BleConnectStateParameter para = (BleConnectStateParameter) msg.obj;
                    BleGattCallback callback = para.getCallback();
                    BluetoothGatt gatt = para.getGatt();
                    BleDevice bleDevice = para.getBleDevice();
                    int status = para.getStatus();
                    if (callback != null)
                        callback.onConnectSuccess(bleDevice, gatt, status);
                    */
                    break;
                }

                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }
    private BluetoothGattCallback coreGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(TAG, "BluetoothGattCallback：onConnectionStateChange "
                    + '\n' + "status: " + status
                    + '\n' + "newState: " + newState
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if (newState == BluetoothGatt.STATE_CONNECTED) {
                gatt.discoverServices();

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                closeBluetoothGatt();

                //ManageBluetoothLE.getInstance().getMultipleBluetoothController().removeBleBluetooth(BluetoothLE.this);

                if (mBluetoothLEConnectState == BluetoothLEConnectState.CONNECT_CONNECTING) {
                    mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_FAILURE;

                    if (isMainThread) {
                        Message message = handler.obtainMessage();
                        message.what = BleMsg.MSG_CONNECT_FAIL;
                        //message.obj = new BleConnectStateParameter(mBleConnectCallback, gatt, status);
                        handler.sendMessage(message);
                    } else {
                        if (mBleConnectCallback != null)
                            mBleConnectCallback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);
                    }

                } else if (mBluetoothLEConnectState == BluetoothLEConnectState.CONNECT_CONNECTED) {
                    mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_DISCONNECT;

                    if (isMainThread) {
                        Message message = handler.obtainMessage();
                        message.what = BleMsg.MSG_DISCONNECTED;
                        //BleConnectStateParameter para = new BleConnectStateParameter(mBleConnectCallback, gatt, status);
                        //para.setAcitive(isActiveDisconnect);
                        //para.setBleDevice(getDeviceBluetoothLE());
                        //message.obj = para;
                        handler.sendMessage(message);
                    } else {
                        if (mBleConnectCallback != null)
                            mBleConnectCallback.onDisConnect(getDeviceBluetoothLE(), gatt, BluetoothLEConnectState.CONNECT_DISCONNECT);
                    }
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.i(TAG, "BluetoothGattCallback：onServicesDiscovered "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBluetoothGatt = gatt;
                mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_CONNECTED;
                isActiveDisconnect = false;
                //ManageBluetoothLE.getInstance().getMultipleBluetoothController().addBleBluetooth(BluetoothLE.this);

                if (isMainThread) {
                    Message message = handler.obtainMessage();
                    message.what = BleMsg.MSG_CONNECT_SUCCESS;
                    //BleConnectStateParameter para = new BleConnectStateParameter(mBleConnectCallback, gatt, status);
                    //para.setBleDevice(getDeviceBluetoothLE);
                    //message.obj = para;
                    handler.sendMessage(message);
                    mBleConnectCallback.onConnectSuccess(gatt, BluetoothLEConnectState.CONNECT_CONNECTED);
                } else {
                    if (mBleConnectCallback != null)
                        mBleConnectCallback.onConnectSuccess(gatt, BluetoothLEConnectState.CONNECT_CONNECTED);
                }
            } else {
                closeBluetoothGatt();

                mBluetoothLEConnectState = BluetoothLEConnectState.CONNECT_FAILURE;

                if (isMainThread) {
                    Message message = handler.obtainMessage();
                    message.what = BleMsg.MSG_CONNECT_FAIL;
                    //message.obj = new BleConnectStateParameter(mBleConnectCallback, gatt, status);
                    handler.sendMessage(message);
                } else {
                    if (mBleConnectCallback != null)
                        mBleConnectCallback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.i(TAG, "BluetoothGattCallback：onCharacteristicRead ");
            Log.e(TAG, "Read" + characteristic.getUuid().toString());

                            Message message = handler.obtainMessage();
                            message.what = BleMsg.MSG_CHA_NOTIFY_DATA_CHANGE;
                            //message.obj = bleNotifyCallback;
                            //Bundle bundle = new Bundle();
                            //bundle.putByteArray(BleMsg.KEY_NOTIFY_BUNDLE_VALUE, characteristic.getValue());
                            //message.setData(bundle);
                            handler.sendMessage(message);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            descriptor.getCharacteristic().getUuid().toString();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            characteristic.getUuid().toString();

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "BluetoothGattCallback：onCharacteristicRead "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            Log.e(TAG, "Read" + characteristic.getUuid().toString());

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            Log.i(TAG, "rssi: " + rssi + ", status: " + status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "mtu: " + mtu + ", status: " + status);
        }
    };
}
