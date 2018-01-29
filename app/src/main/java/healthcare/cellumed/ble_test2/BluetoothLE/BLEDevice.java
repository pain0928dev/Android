package healthcare.cellumed.ble_test2.BluetoothLE;

import java.util.Objects;

/**
 * Created by ljh0928 on 2018. 1. 25..
 */

public class BLEDevice {
    private String name;
    private String address;

    BLEDevice(String name, String address){
        this.name = name;
        this.address = address;
    }

    public String getName(){
        return this.name;
    }

    public String getAddress(){
        return this.address;
    }
}
