package healthcare.cellumed.ble_test2.bluetoothle;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import healthcare.cellumed.ble_test2.BleProfile;
import healthcare.cellumed.ble_test2.util.HexaDump;
import healthcare.cellumed.ble_test2.util.RingBuffer;
import healthcare.cellumed.ble_test2.util.RingByteBuffer;

/**
 * Created by ljh0928 on 2018. 1. 4..
 */

public class ManageBluetoothLE {

    private static final String TAG = "ManageBluetoothLE";
    private static final int MAX_BLUETOOTHLE = 4;

    private Application application;
    private BleScanner bleScanner;
    private HashMap<String, BluetoothLE> bluetoothLEHashMap = null;
    private RingBuffer<Send2Gatt> sendBuffer = new RingBuffer<Send2Gatt>(16);


    public static ManageBluetoothLE getInstance() {
        return ManagerBleHolder.manageBluetoothLE;
    }

    private static class ManagerBleHolder {
        private static final ManageBluetoothLE manageBluetoothLE = new ManageBluetoothLE();
    }

    public void init(Application app) {
        if (application == null && app != null) {
            application = app;
            bluetoothLEHashMap = new HashMap<String, BluetoothLE>();


            eableBluetooth();
            //bleScanRuleConfig = new BleScanRuleConfig();
            bleScanner = BleScanner.getInstance(application.getApplicationContext());
        }

        new Thread() {

            private final String TAG = "ManageBluetoothLEThread";

            // Todo: if There is data in buffer
            public void run(){

                while(true){
                    synchronized (this) {
                        if (sendBuffer.size() > 0) {
                            Send2Gatt s2g = sendBuffer.pop();
                            s2g.gatt.writeCharacteristic(s2g.characteristic);
                            Log.d(TAG, "Write Gatt: " + s2g.gatt.getDevice());
                        }
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }.start();
    }

    public Context getContext(){
        return application.getApplicationContext();
    }

    public void addBluetoothLE(BluetoothLE ble){

        if(bluetoothLEHashMap != null && !bluetoothLEHashMap.containsKey(ble.getBluetoothDeviceKey())){
            Log.d(TAG, "AddbluetoothLE" + ble.getBluetoothDevice() + ":" + ble.getBluetoothDeviceKey());
            bluetoothLEHashMap.put(ble.getBluetoothDeviceKey(), ble);
        }
    }

    public void removeBleutoothLE(BluetoothLE ble){
        if(bluetoothLEHashMap != null && bluetoothLEHashMap.containsKey(ble.getBluetoothDeviceKey())){
            Log.d(TAG, "removeBluetoothLE" + ble.getBluetoothDevice() + ":" + ble.getBluetoothDeviceKey());
            bluetoothLEHashMap.remove(ble.getBluetoothDeviceKey());
        }
    }

    public void startScan(BleScanCallback callback){
        if(callback == null) {
            return;
        }

        //mBleScanner.setFilters(mDeviceName, mAddress, SERVICE_UUID);
        bleScanner.startScan(callback);
    }

    public void stopScan(){
        bleScanner.stopScan();
    }

    public void eableBluetooth() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) application.getApplicationContext()
                .getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
            if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
            }
        }
    }

    public BluetoothGatt connect(BLEDevice ble_dev, BleConnectCallback callback){

        // TODO: 2018. 1. 23.
        // 연결 시도후 정상적으로 장치가 연결 되었음을 알려 줘야한다
        if(bluetoothLEHashMap.size() == MAX_BLUETOOTHLE) {
            callback.onConnectFail(0);
            return null;
        }

        if(callback == null){
            throw new IllegalArgumentException("BleConnectCallback is null");
        }

        if (ble_dev == null) {
            callback.onConnectFail(0);
        } else {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(ble_dev.getAddress());
            BluetoothLE bleBluetooth = new BluetoothLE(device);
            bleBluetooth.setHandler(handler);
            bleBluetooth.setBluetoothLECallback(bluetoothLECallback);

            //boolean autoConnect = bleScanRuleConfig.isAutoConnect();
            callback.onStartConnect();
            return bleBluetooth.connect(device, false);
        }
        return null;
    }

    public void disconnect(BLEDevice dev){

        // 연결된 리스트중에 전달받은 인자와 같은걸 disconnect 시킨다
        String key = makeKey(dev);
        if(bluetoothLEHashMap.containsKey(key)){
            BluetoothLE ble = bluetoothLEHashMap.get(key);
            ble.disconnect();
        }
    }

    public byte[] read(BLEDevice dev){

        /*
        // 연결된 리스트중에 전달받은 인자와 같은걸 disconnect 시킨다
        String key = makeKey(dev);
        if(bluetoothLEHashMap.containsKey(key)){
            Log.e(TAG, "read");
            BluetoothLE ble = bluetoothLEHashMap.get(key);
            BluetoothGatt gatt = ble.getBluetoothGatt();

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(UUID.fromString(BleProfile.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BleProfile.NOTIFY_UUID));

            gatt.readCharacteristic(characteristic);
        }
        */

        String key = makeKey(dev);
        if(bluetoothLEHashMap.containsKey(key)) {
            Log.e(TAG, "read");
            BluetoothLE ble = bluetoothLEHashMap.get(key);

            if(ble.readBuffer.getSize() > 0){
                return ble.readBuffer.pop(ble.readBuffer.getSize());
            }
        }

        return null;
    }

    public void write(BLEDevice dev){

        String key = makeKey(dev);
        if(bluetoothLEHashMap.containsKey(key)){
            Log.e(TAG, "write");
            BluetoothLE ble = bluetoothLEHashMap.get(key);
            BluetoothGatt gatt = ble.getBluetoothGatt();

            BluetoothGattCharacteristic characteristic =
                    gatt.getService(UUID.fromString(BleProfile.SERVICE_UUID))
                    .getCharacteristic(UUID.fromString(BleProfile.NOTIFY_UUID));

            if (characteristic == null) {
                Log.e(TAG, "characteristic is null");
                return;
            }

            String s = makeData("01", "");
            byte data[] = HexaDump.toByteArray(s);
            characteristic.setValue(data);
            sendBuffer.push(new Send2Gatt(gatt, characteristic));
            /*
            boolean status = gatt.writeCharacteristic(characteristic);
            Log.e(TAG, "status:" + status);
            */
        }
    }

    public String makeKey(BLEDevice dev){
        return (dev.getName() + dev.getAddress());
    }
    private class Send2Gatt{
        BluetoothGatt gatt;
        BluetoothGattCharacteristic characteristic;

        public Send2Gatt(BluetoothGatt _gatt, BluetoothGattCharacteristic _characteristic){
            gatt = _gatt;
            characteristic = _characteristic;
        }
    }

    private String makeData(String cmd, String data) {
        String header = "21";
        String footer = "75";

        int length = 20;    // length is always 20
        int checkSum = 0;

        String outdata="C100"+cmd + data + "0000000000000000000000000000".substring(0,28-data.length());
        for (int i = 0; i < outdata.length() ; i += 2) {
            //tmp = Integer.parseInt(data.substring(i, i + 2), 16);
            checkSum += Integer.parseInt(outdata.substring(i, i + 2), 16);

        }

        if (checkSum > 255)
            checkSum = checkSum & 0x00ff;
        return header + outdata + String.format("%02X", checkSum) + footer;
    }

    public Handler getHandler(){
        return handler;
    }

    public BluetoothLECallback getCallback(){
        return bluetoothLECallback;
    }

    public Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "Msg: " + msg.what);

            switch (msg.what){

            }
        }
    };

    public BluetoothLECallback bluetoothLECallback = new BluetoothLECallback() {

        @Override
        public void onConnectionState(BluetoothDevice bluetoothDevice, BluetoothLEConnectState bluetoothLEConnectState) {
            Log.i(TAG, "onConnectionState:: " + bluetoothLEConnectState.toString());
            switch (bluetoothLEConnectState){
                case CONNECT_CONNECTED:
                    break;
                case CONNECT_DISCONNECT:
                    break;
                case CONNECT_FAILURE:
                    break;
            }
        }

        @Override
        public void onChanged(BluetoothDevice bluetoothDevice, byte[] inBytes) {
            Log.i(TAG, "onChanged::[" + bluetoothDevice.getName() + "] -> " +
                    HexaDump.toString(inBytes, inBytes.length));

        }

        @Override
        public void onRead(BluetoothDevice bluetoothDevice, byte[] inBytes, int status) {
            String hexaStr = bytes2String(inBytes, inBytes.length);
            Log.i(TAG, "onRead:: " + hexaStr);
            Log.i(TAG, "status: " + status);

        }

        @Override
        public void onReadRssi(BluetoothDevice bluetoothDevice, int rssi, int status) {
            Log.i(TAG, "onReadRssi:: " + rssi + ", " + status );
        }

        @Override
        public void onReadMtu(BluetoothDevice bluetoothDevice, int mtu, int status) {
            Log.i(TAG, "onReadRssi:: " + mtu + ", " + status );
        }

        @Override
        public void onWrite(BluetoothDevice bluetoothDevice, int status) {
            Log.i(TAG, "onWrite:: " + status);
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
    //private RingBuffer<byte> recvByteBuffer = new RingBuffer<byte>(8192);

    /*
    private class ManageThread extends Thread {

        private final String TAG = "ManageThread";

        // Todo: if There is data in buffer

        public ManageThread(){

        }

        public void run(){

            while(true){
                if(sendBuffer.size() > 0){
                    Send2Gatt s2g = sendBuffer.pop();
                    s2g.gatt.writeCharacteristic(s2g.characteristic);
                    Log.d(TAG, "Write Gatt: " + s2g.gatt.getDevice());
                }
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }
    */


    public class ReadBluetoothData{


        private RingByteBuffer readBuffer;
    }

}
