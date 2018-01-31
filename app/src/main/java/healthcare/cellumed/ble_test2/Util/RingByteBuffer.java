package healthcare.cellumed.ble_test2.util;

/**
 * Created by ljh0928 on 2018. 1. 25..
 */

public class RingByteBuffer {

    private final static int DEFAULT_MIN_LENGTH = 1024;

    private byte[] byteBuffers;
    private int bufferLength = DEFAULT_MIN_LENGTH;
    private int head;
    private int tail;


    public RingByteBuffer(int len){
        if(bufferLength < len) {
            byteBuffers = new byte[len];
        }
        clear();
    }

    public void push(byte b) {
        byteBuffers[tail++] = b;
        tail = (tail % bufferLength);
    }

    public void push(byte[] b) {

        System.arraycopy(b, 0, byteBuffers, head, b.length);
        tail += b.length;
        tail = (tail % bufferLength);
    }

    public byte[] pop(int len){
        if(isEmpty()) return null;
        if(getSize() < len) return null;

        byte[] bytes = new byte[len];
        System.arraycopy(byteBuffers, head, bytes, 0, len);
        head += len;
        head = (head % bufferLength);

        return bytes;
    }

    public boolean isEmpty() {

        if(head == tail) return true;
        return false;
    }

    public int getSize(){
        return (tail - (head + bufferLength) % bufferLength);
    }

    public void clear(){
        head = 0;
        tail = 0;
    }

}
