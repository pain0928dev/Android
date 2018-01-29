package healthcare.cellumed.ble_test2.BluetoothLE;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

import healthcare.cellumed.ble_test2.BleProfile;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Created by ljh0928 on 2018. 1. 18..
 */

public class BluetoothLE {

    private static final String TAG = "BluetoothLE";

    private BluetoothLEConnectState bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_IDLE;
    private BluetoothDevice bluetoothDevice;
    private BluetoothGatt bluetoothGatt;
    private boolean isActiveDisconnect = false;
    private boolean isMainThread = false;


    private Handler handler = null;
    private Objects callBack = null;

    BluetoothLE(BluetoothDevice dev){
        bluetoothDevice = dev;
    }

    BluetoothLE(BluetoothDevice device, Handler handle){
        this.bluetoothDevice = device;
        this.handler = handle;
    }

    BluetoothLE(BluetoothDevice device, Objects callback){
        this.bluetoothDevice = device;
        this.callBack = callback;
    }

    public BluetoothDevice getBluetoothDevice(){
        return this.bluetoothDevice;
    }
    public String getBluetoothDeviceName() { return this.bluetoothDevice.getName(); }
    public String getBluetoothDeviceAddress() { return this.bluetoothDevice.getAddress(); }
    public String getBluetoothDeviceKey() { return (getBluetoothDeviceName() + getBluetoothDeviceAddress()); }

    public BluetoothGatt getBluetoothGatt(){
        return this.bluetoothGatt;
    }

    public BluetoothLEConnectState getConnectState() { return this.bluetoothLEConnectState; }

    public synchronized BluetoothGatt connect(BluetoothDevice device, boolean autoConnect) {
        Log.i(TAG, "connect Name: " + device.getName()
                + "\nAddress: " + device.getAddress()
                + "\nautoConnect: " + autoConnect
                + "\ncurrentThread: " + Thread.currentThread().getId());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.bluetoothGatt = device.connectGatt(ManageBluetoothLE.getInstance().getContext(),
                    autoConnect, coreGattCallback, TRANSPORT_LE);
        } else {
            this.bluetoothGatt = device.connectGatt(ManageBluetoothLE.getInstance().getContext(),
                    autoConnect, coreGattCallback);
        }

        this.bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_CONNECTING;
        return this.bluetoothGatt;
    }

    public synchronized void disconnect() {
        if (this.bluetoothGatt != null) {
            this.isActiveDisconnect = true;
            this.bluetoothGatt.disconnect();
        }
    }

    private synchronized void closeBluetoothGatt() {
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.close();
        }
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

    public void destroy() {
        this.bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_IDLE;
        if (this.bluetoothGatt != null) {
            this.bluetoothGatt.disconnect();
            this.bluetoothGatt.close();
            refreshDeviceCache();
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

                ManageBluetoothLE.getInstance().removeBleutoothLE(BluetoothLE.this);

                if (bluetoothLEConnectState == BluetoothLEConnectState.CONNECT_CONNECTING) {
                    bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_FAILURE;

                    if (isMainThread) {
                        Log.i(TAG, "Main Thread....");
                    }
                    //mBleConnectCallback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);

                    /*
                    if (isMainThread) {
                        Message message = handler.obtainMessage();
                        message.what = BleMsg.MSG_CONNECT_FAIL;
                        //message.obj = new BleConnectStateParameter(mBleConnectCallback, gatt, status);
                        handler.sendMessage(message);
                    } else {
                        if (mBleConnectCallback != null)
                            mBleConnectCallback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);
                    }
                    */

                } else if (bluetoothLEConnectState == BluetoothLEConnectState.CONNECT_CONNECTED) {
                    bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_DISCONNECT;

                    if (isMainThread) {
                        Log.i(TAG, "Main Thread....");
                    }
                    //mBleConnectCallback.onDisConnect(getDeviceBluetoothLE(), gatt, BluetoothLEConnectState.CONNECT_DISCONNECT);
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
                bluetoothGatt = gatt;
                bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_CONNECTED;
                isActiveDisconnect = false;
                ManageBluetoothLE.getInstance().addBluetoothLE(BluetoothLE.this);

                BluetoothGattCharacteristic characteristic =
                        bluetoothGatt.getService(UUID.fromString(BleProfile.SERVICE_UUID)).getCharacteristic(UUID.fromString(BleProfile.NOTIFY_UUID));
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                }

                bluetoothGatt.setCharacteristicNotification(characteristic, true);

                if (isMainThread) {
                    Log.i(TAG, "Main Thread....");
                }
                //mBleConnectCallback.onConnectSuccess(gatt, BluetoothLEConnectState.CONNECT_CONNECTED);

            } else {
                closeBluetoothGatt();
                bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_FAILURE;

                if (isMainThread) {
                    Log.i(TAG, "Main Thread....");
                }
                //mBleConnectCallback.onConnectFail(BluetoothLEConnectState.CONNECT_FAILURE);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            Log.i(TAG, "BluetoothGattCallback：onCharacteristicChanged ");
            Log.e(TAG, "Read: " + characteristic.getUuid().toString());

            final byte[] d = characteristic.getValue();
            String hexaStr = bytes2String(d,d.length);
            Log.e(TAG, "rcv=" + hexaStr);
            //handler.sendMessage(handler.obtainMessage(1, hexaStr));

            if (isMainThread) {
                Log.d(TAG, "Main Thread....");
            } else {
                Log.d(TAG, "Callback Handler....");
            }
        }

        /*
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

            descriptor.getCharacteristic().getUuid().toString();
        }
        */

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(TAG, "BluetoothGattCallback：onCharacteristicWrite "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if (isMainThread) {
                Log.d(TAG, "Main Thread....");
            } else {
                Log.d(TAG, "Callback Handler....");
            }



        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "BluetoothGattCallback：onCharacteristicRead "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if (isMainThread) {
                Log.d(TAG, "Main Thread....");
            } else {
                Log.d(TAG, "Callback Handler....");
            }

            Log.e(TAG, "Read: " + characteristic.getUuid().toString());

        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);

            Log.i(TAG, "BluetoothGattCallback：onReadRemoteRssi "
                    + '\n' + "rssi: " + rssi
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i(TAG, "BluetoothGattCallback：onMtuChanged "
                    + '\n' + "mtu: " + mtu
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());
        }
    };



    private String bytes2String(byte[] b, int count) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            String myInt = Integer.toHexString((int) (b[i] & 0xFF));
            result.add(myInt);
        }
        return TextUtils.join(" ", result);
    }
}
