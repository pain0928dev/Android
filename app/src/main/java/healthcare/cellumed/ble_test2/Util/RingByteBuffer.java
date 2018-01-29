package healthcare.cellumed.ble_test2.Util;

/**
 * Created by ljh0928 on 2018. 1. 25..
 */

public class RingByteBuffer {

    private final int DEFAULT_MIN_LENGTH = 256;

    private byte[] mBuffer;
    private int length;
    private int mMax_length = DEFAULT_MIN_LENGTH;
    private int sp;
    private int ep;


    RingByteBuffer(int capacity){
        if(capacity < mMax_length) {
            mBuffer = new byte[mMax_length];
        }
        else{
            mMax_length = capacity;
            mBuffer = new byte[mMax_length];
        }
        sp = 0;
        ep = 0;
    }


    public boolean isEmpty() {
        if(sp == ep) return true;
        return false;
    }

    public void push(byte b) {
        mBuffer[ep] = b;
        ep++;
    }

    public void push(byte[] b, int len) {

        for (int i = 0; i < len; i++) {
            mBuffer[ep] = b[i];
            ep++;
        }
        checkEp();
    }

    public byte[] pop(){
        if(isEmpty()) return null;
        byte[] retB = new byte[1];
        retB[0] = mBuffer[sp+0];
        return retB;
    }

    public byte[] pop(int len){
        if(isEmpty()) return null;
        byte[] retB = new byte[len];
        for(int i=0; i<len; i++){
            retB[i] = mBuffer[sp+i];
        }
        return retB;
    }

    private void checkSp() {
        if(sp == mMax_length) sp = 0;
    }

    private void checkEp() {
        if(ep == mMax_length) ep = 0;
    }
}
