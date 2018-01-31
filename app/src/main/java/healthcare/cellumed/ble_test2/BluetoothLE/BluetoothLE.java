package healthcare.cellumed.ble_test2.bluetoothle;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.UUID;

import healthcare.cellumed.ble_test2.BleProfile;
import healthcare.cellumed.ble_test2.util.RingByteBuffer;

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
    private BluetoothLECallback bluetoothLECallback = null;

    public RingByteBuffer readBuffer;

    BluetoothLE(BluetoothDevice dev){
        bluetoothDevice = dev;
        initBuffer();
        Log.d(TAG, "New BluetoothDevice: " + bluetoothDevice.getName() + ", " + bluetoothDevice.getAddress());
    }

    BluetoothLE(BluetoothDevice device, Handler handle){
        this.bluetoothDevice = device;
        this.handler = handle;
        initBuffer();
    }

    BluetoothLE(BluetoothDevice device, BluetoothLECallback bluetoothLECallback){
        this.bluetoothDevice = device;
        this.bluetoothLECallback = bluetoothLECallback;
        initBuffer();
    }

    public void initBuffer(){
        if(readBuffer == null)
            readBuffer = new RingByteBuffer(4096);
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }

    public void setBluetoothLECallback(BluetoothLECallback bluetoothLECallback){
        this.bluetoothLECallback = bluetoothLECallback;
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
                Log.d(TAG, "refreshDeviceCache, is success:  " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e(TAG, "exception occur while refreshing device: " + e.getMessage());
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
            Log.d(TAG, "BluetoothGattCallback：onConnectionStateChange "
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
                } else if (bluetoothLEConnectState == BluetoothLEConnectState.CONNECT_CONNECTED) {
                    bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_DISCONNECT;
                }

                if (isMainThread) { Log.d(TAG, "Main Thread...."); }

                if(handler != null) { handler.obtainMessage(status, gatt); }

                if(bluetoothLECallback != null) {
                    bluetoothLECallback.onConnectionState(bluetoothDevice, bluetoothLEConnectState);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "BluetoothGattCallback：onServicesDiscovered "
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

            } else {
                closeBluetoothGatt();
                bluetoothLEConnectState = BluetoothLEConnectState.CONNECT_CONNECTED;
            }

            if (isMainThread) { Log.d(TAG, "Main Thread...."); }
            if(handler != null) { handler.obtainMessage(status, gatt); }
            if(bluetoothLECallback != null){
                bluetoothLECallback.onConnectionState(bluetoothDevice, bluetoothLEConnectState);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "BluetoothGattCallback：onCharacteristicChanged "
                    + '\n' + "currentThread: " + Thread.currentThread().getId());
            Log.v(TAG, "Read: " + characteristic.getUuid().toString());

            if (isMainThread) { Log.d(TAG, "Main Thread...."); }
            if(handler != null) { handler.obtainMessage(1,gatt); }
            if(bluetoothLECallback != null) {
                bluetoothLECallback.onChanged(bluetoothDevice, characteristic.getValue());
                readBuffer.push(characteristic.getValue());
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
            Log.d(TAG, "BluetoothGattCallback：onCharacteristicWrite "
                    + '\n' + "currentThread: " + Thread.currentThread().getId());
            Log.v(TAG, "write: " + characteristic.getUuid().toString());

            if (isMainThread) { Log.d(TAG, "Main Thread...."); }
            if(handler != null) { handler.obtainMessage(status, gatt); }
            if(bluetoothLECallback != null) {
                bluetoothLECallback.onWrite(bluetoothDevice, status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "BluetoothGattCallback：onCharacteristicRead "
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());
            Log.v(TAG, "Read: " + characteristic.getUuid().toString());

            if (isMainThread) { Log.d(TAG, "Main Thread...."); }
            if(handler != null) { handler.obtainMessage(status, gatt); }
            if(bluetoothLECallback != null) {
                bluetoothLECallback.onRead(bluetoothDevice, characteristic.getValue(), status);
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            super.onReadRemoteRssi(gatt, rssi, status);
            Log.d(TAG, "BluetoothGattCallback：onReadRemoteRssi "
                    + '\n' + "rssi: " + rssi
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if(bluetoothLECallback != null){
                bluetoothLECallback.onReadRssi(bluetoothDevice, rssi, status);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.d(TAG, "BluetoothGattCallback：onMtuChanged "
                    + '\n' + "mtu: " + mtu
                    + '\n' + "status: " + status
                    + '\n' + "currentThread: " + Thread.currentThread().getId());

            if(bluetoothLECallback != null){
                bluetoothLECallback.onReadMtu(bluetoothDevice, mtu, status);
            }
        }
    };

}
