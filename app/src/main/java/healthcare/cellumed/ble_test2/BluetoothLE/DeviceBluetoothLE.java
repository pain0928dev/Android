package healthcare.cellumed.ble_test2.bluetoothle;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pain0928 on 2018-01-19.
 */

public class DeviceBluetoothLE implements Parcelable {

    private BluetoothDevice bluetoothDevice;
    private byte[] scanRecord;
    private int rssi;
    private long timestampNanos;

    public DeviceBluetoothLE(BluetoothDevice device) {
        bluetoothDevice = device;
    }

    public DeviceBluetoothLE(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        this.bluetoothDevice = device;
        this.scanRecord = scanRecord;
        this.rssi = rssi;
        timestampNanos = timestampNanos;
    }

    protected DeviceBluetoothLE(Parcel in) {
        bluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        scanRecord = in.createByteArray();
        rssi = in.readInt();
        timestampNanos = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(bluetoothDevice, flags);
        dest.writeByteArray(scanRecord);
        dest.writeInt(rssi);
        dest.writeLong(timestampNanos);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceBluetoothLE> CREATOR = new Creator<DeviceBluetoothLE>() {
        @Override
        public DeviceBluetoothLE createFromParcel(Parcel in) {
            return new DeviceBluetoothLE(in);
        }

        @Override
        public DeviceBluetoothLE[] newArray(int size) {
            return new DeviceBluetoothLE[size];
        }
    };

    public String getName() {
        if (bluetoothDevice != null)
            return bluetoothDevice.getName();
        return null;
    }

    public String getMac() {
        if (bluetoothDevice != null)
            return bluetoothDevice.getAddress();
        return null;
    }

    public String getKey() {
        if (bluetoothDevice != null)
            return bluetoothDevice.getName() + bluetoothDevice.getAddress();
        return "";
    }

    public BluetoothDevice getDevice() {
        return bluetoothDevice;
    }

    public void setDevice(BluetoothDevice device) {
        this.bluetoothDevice = device;
    }

    public byte[] getScanRecord() {
        return scanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.scanRecord = scanRecord;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public long getTimestampNanos() {
        return timestampNanos;
    }

    public void setTimestampNanos(long timestampNanos) {
        this.timestampNanos = timestampNanos;
    }
}
