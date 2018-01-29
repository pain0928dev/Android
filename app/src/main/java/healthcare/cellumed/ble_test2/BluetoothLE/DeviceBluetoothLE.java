package healthcare.cellumed.ble_test2.BluetoothLE;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by pain0928 on 2018-01-19.
 */

public class DeviceBluetoothLE implements Parcelable {

    private BluetoothDevice mDevice;
    private byte[] mScanRecord;
    private int mRssi;
    private long mTimestampNanos;

    public DeviceBluetoothLE(BluetoothDevice device) {
        mDevice = device;
    }

    public DeviceBluetoothLE(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        mDevice = device;
        mScanRecord = scanRecord;
        mRssi = rssi;
        mTimestampNanos = timestampNanos;
    }

    protected DeviceBluetoothLE(Parcel in) {
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mScanRecord = in.createByteArray();
        mRssi = in.readInt();
        mTimestampNanos = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, flags);
        dest.writeByteArray(mScanRecord);
        dest.writeInt(mRssi);
        dest.writeLong(mTimestampNanos);
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
        if (mDevice != null)
            return mDevice.getName();
        return null;
    }

    public String getMac() {
        if (mDevice != null)
            return mDevice.getAddress();
        return null;
    }

    public String getKey() {
        if (mDevice != null)
            return mDevice.getName() + mDevice.getAddress();
        return "";
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public void setDevice(BluetoothDevice device) {
        this.mDevice = device;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public void setScanRecord(byte[] scanRecord) {
        this.mScanRecord = scanRecord;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        this.mRssi = rssi;
    }

    public long getTimestampNanos() {
        return mTimestampNanos;
    }

    public void setTimestampNanos(long timestampNanos) {
        this.mTimestampNanos = timestampNanos;
    }
}
