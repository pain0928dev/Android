package healthcare.cellumed.ble_test2.cellumedprotocol;


import android.bluetooth.le.ScanFilter;

/**
 * Created by ljh0928 on 2018. 1. 30..
 */

public class CellumedProtocol {

    private String stx = "00";
    private String id = "00";
    private String reserve = "00";
    private String cmd = "00";
    private String data = "0000000000000000000000000000";
    private String chksum = "00";
    private String etx = "00";

    public static class Builder {

        private String stx;
        private String cmd;
        private String reserve;
        private String data;
        private String chksum;
        private String etx;

        public Builder(String cmd){
            this.cmd = cmd;
        }

        public Builder setStx(String stx){
            this.stx = stx;
            return this;
        }

        public Builder setReserve(String reserve){
            this.reserve = reserve;
            return this;
        }

        public Builder setData(String data){
            this.data = data;
            return this;
        }

        public Builder setCheckSum(String chk){
            this.chksum = chk;
            return this;
        }

        public Builder setEtx(String etx){
            this.etx = etx;
            return this;
        }

        public CellumedProtocol build(){
            return new CellumedProtocol(this);
        }
    }

    CellumedProtocol(Builder build){
        stx = build.stx;
        cmd = build.cmd;
        reserve = build.reserve;
        data = build.data;
        chksum = build.chksum;
        etx = build.etx;
    }

    public String makeData(String cmd, String data) {
        final String stx = "21";
        final String etx = "75";

        int length = 20;    // length is always 20
        int checkSum = 0;

        String outdata="C100"+cmd + data + "0000000000000000000000000000".substring(0,28-data.length());
        for (int i = 0; i < outdata.length() ; i += 2) {
            //tmp = Integer.parseInt(data.substring(i, i + 2), 16);
            checkSum += Integer.parseInt(outdata.substring(i, i + 2), 16);

        }

        if (checkSum > 255)
            checkSum = checkSum & 0x00ff;
        return stx + outdata + String.format("%02X", checkSum) + etx;
    }


    class CellumedData{

        // Version
        String fwVersion;
        String hwVersion;

        // Knee Angle
        String minAngle;
        String maxAngle;

        // EMG data
        String numChannel;
        String emgAve;
        String emgMax;
        String emgTotal;

        // Sensor
        String sensorCount;
        String sensorType;
        String sensorPosture;

        // EMG Raw Data
        String emgChannel1;
        String emgChannel2;
        String emgChannel3;
        String emgChannel4;
        String emgChannel5;

        // EMS Info
        String emgProgramNumber;
        String emgPattern;
        String emgFrequncy;
        String emgTime;
        String emgOnTime;
        String emgOffTime;
        String emgRiseTime;
        String emgPulseTime;

        // EMS Level
        String emsChannel1;
        String emsChannel2;
        String emsChannel3;
        String emsChannel4;

        // Battery Info
        String battLevel;
        String battVoltage;
        String battTemperature;

        // Common Data
        String isAckNak;
        String sensorMethod;
        String exerciseType;
        String emsStatus;

    }


}
