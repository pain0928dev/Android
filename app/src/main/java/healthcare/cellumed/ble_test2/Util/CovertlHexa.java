package healthcare.cellumed.ble_test2.Util;

import android.text.TextUtils;

import java.util.ArrayList;

/**
 * Created by ljh0928 on 2018. 1. 25..
 */

public class CovertlHexa {

    public static String toString(byte[] b, int size){
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            String myInt = Integer.toHexString((int) (b[i] & 0xFF));
            result.add(myInt);
        }
        return TextUtils.join(" ", result);
    }
}
