package healthcare.cellumed.ble_test2.BluetoothLE;

/**
 * Created by ljh0928 on 2018. 1. 18..
 */

public enum BluetoothLEConnectState {


        CONNECT_IDLE(0x00),
        CONNECT_CONNECTING(0x01),
        CONNECT_CONNECTED(0x02),
        CONNECT_FAILURE(0x03),
        CONNECT_TIMEOUT(0x04),
        CONNECT_DISCONNECT(0x05);

        private int code;

        BluetoothLEConnectState(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

}
