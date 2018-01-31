package healthcare.cellumed.ble_test2.cellumedprotocol;

/**
 * Created by ljh0928 on 2018. 1. 30..
 */

public class CellumedCommand {

    public String stx = "01";

    public enum CommandE {
    /*
    CMD_REQ_VER(0x01),
    CMD_START_SENS(0x11),
    CMD_STOP_SENS(0x12),
    CMD_RESP_NOR_SENS(0x13),
    CMD_RESP_RAW_SENS(0x14),
    CMD_RESP_SENS_PST(0x16),
    CMD_REQ_REPORT_SENS(0x15),
    CMD_REQ_START_EMS(0x21),
    CMD_REQ_STOP_EMS(0x22),
    CMD_EMS_INFO(0x02),
    CMD_EMS_LEVEL(0x03),
    CMD_REQ_BATT_INFO(0x04),
    CMD_REQ_START_CAL(0x31),
    CMD_EMS_STATUS(0x41);

    private int code;
    CellumedCommand(int code) {
        this.code = code;
    }
    public int getCode() {
        return code;
    }
    */

        CMD_REQ_VER("01"),

        CMD_START_SENS("11"),

        CMD_STOP_SENS("12"),

        CMD_RESP_NOR_SENS("13"),

        CMD_RESP_RAW_SENS("14"),

        CMD_RESP_SENS_PST("16"),

        CMD_REQ_REPORT_SENS("15"),

        CMD_REQ_START_EMS("21"),

        CMD_REQ_STOP_EMS("22"),

        CMD_EMS_INFO("02"),

        CMD_EMS_LEVEL("03"),

        CMD_REQ_BATT_INFO("04"),

        CMD_REQ_START_CAL("31"),

        CMD_EMS_STATUS("41");

        private String code;
        CommandE(String code) {
            this.code = code;
        }
        public String getCode() {
            return code;
        }
    }

}
