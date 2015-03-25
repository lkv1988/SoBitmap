package com.github.airk.tool.sobitmap;

/**
 * Created by kevin on 15/3/24.
 * <p/>
 * Custom HuntException
 */
public class HuntException extends Exception {
    public static final int REASON_FILE_NOT_FOUND = 0x01;
    public static final int REASON_TOO_LARGE = 0x02;
    public static final int REASON_OOM = 0x03;
    public static final int REASON_IO_EXCEPTION = 0x04;
    public static final int REASON_UNSUPPORT_TYPE = 0x05;

    private int reason;

    public HuntException(int reason) {
        this.reason = reason;
    }

    public int getReason() {
        return reason;
    }

    @Override
    public String getMessage() {
        String ret;
        switch (reason) {
            case REASON_FILE_NOT_FOUND:
                ret = "File not found.";
                break;
            case REASON_TOO_LARGE:
                ret = "Input file is too large, please check your options.";
                break;
            case REASON_OOM:
                ret = "OOM occurred while hunting bitmap.";
                break;
            case REASON_IO_EXCEPTION:
                ret = "IOException occurred while hunting bitmap.";
                break;
            default:
                ret = "unknown";
        }
        return ret;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
