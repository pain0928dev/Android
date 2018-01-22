package healthcare.cellumed.ble_test2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BLeService extends Service {

    final String TAG ="BLeService";

    public static final UUID SERVICE_UUID = UUID.fromString("49535343-fe7d-4ae5-8fa9-9fafd205e455");
    public static final UUID WRITE_UUID = UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616");   // android입장에서write.
    public static final UUID NOTIFY_UUID = UUID.fromString("49535343-1e4d-4bd9-ba61-23c647249616");   // serial

    BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;

    private HashMap<String, BluetoothGatt> service_list;

    public class LocalBinder extends Binder {
        BLeService getService() {
            return BLeService.this;
        }
    }

    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        service_list = new HashMap<String, BluetoothGatt>();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBinder;
    }

    public void connect(String address){

        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

        synchronized (this) {
            device.connectGatt(this, false, mGattCallback);
        }

        Log.d(TAG, "Trying to create a new connection");
    }

    public void disconnect(String address){
    }

    public void sendData(String address, byte b[]){
        if(service_list.containsKey(address)){
            mBluetoothGatt = service_list.get(address);

            BluetoothGattService service = mBluetoothGatt.getService(SERVICE_UUID);
            if (service == null) {
                Log.e("Tag", "Connection State error");
                return ;
            } else {
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(WRITE_UUID);
                if (characteristic == null) return;
                characteristic.setValue(b);

                mGattCallback.onCharacteristicWrite(mBluetoothGatt, characteristic, 1);
            }
        }
    }

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            Log.d(TAG, "onConnectionStateChange: " + status + ", " + newState);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        gatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                //broadcastUpdate1(intentAction, null);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {

            Log.d(TAG, "onServicesDiscovered: " + status);
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString() + ", " + status);

            // notify uuid enable
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattCharacteristic characteristic = gatt.getService(SERVICE_UUID).getCharacteristic(NOTIFY_UUID);
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                }

                if(!service_list.containsKey(gatt.getDevice().getName())){
                    service_list.put(gatt.getDevice().getName(), gatt);
                }
                gatt.setCharacteristicNotification(characteristic, true);

            } else {
                Log.w(TAG, "onServicesDiscoverd receive : " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            //this block should be synchronized to prevent the function overloading
            synchronized(this)
            {
                Log.e(TAG, "onCharacteristicWrite mBluetoothGatt1 :" + status);

                if(status == BluetoothGatt.GATT_SUCCESS)
                {
                }

                /*
                else if(status == WRITE_NEW_CHARACTERISTIC)
                {
                    gatt.writeCharacteristic(characteristic);
                    Log.e(TAG, "write CharacteristicWrite 1");
                }
                */
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i("OnCharacteristicRead", characteristic.toString());

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            // buno ble. 시리얼uuid를 통한 noti
            Log.i("OnCharacteristicChanged", characteristic.toString());
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                broadcastUpdate(data);
            }
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

    private void broadcastUpdate(final byte[] b_data){
        Log.e(TAG,"Recv : " + bytes2String(b_data,b_data.length));
    }
}
