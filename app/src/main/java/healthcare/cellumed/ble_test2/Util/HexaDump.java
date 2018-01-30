package healthcare.cellumed.ble_test2.Util;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by ljh0928 on 2018. 1. 25..
 */

public class HexaDump {

    public static String toString(byte[] b, int size){
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            String myInt = Integer.toHexString((int) (b[i] & 0xFF));
            result.add(myInt);
        }
        return TextUtils.join(" ", result);
    }

    public static byte[] toByteArray(String hexaString) {
        int len = hexaString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len - 1; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexaString.charAt(i), 16) << 4)
                    + Character.digit(hexaString.charAt(i + 1), 16));
        }
        return data;
    }
}
