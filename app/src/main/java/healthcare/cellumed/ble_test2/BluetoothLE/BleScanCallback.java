package healthcare.cellumed.ble_test2.bluetoothle;

/**
 * Created by pain0928 on 2018-01-19.
 */

public abstract class BleScanCallback extends BaseCallback {

    public abstract void onStartScan();
    public abstract void onScanning();
    public abstract void onProcessResult(BLEDevice dev);
}
