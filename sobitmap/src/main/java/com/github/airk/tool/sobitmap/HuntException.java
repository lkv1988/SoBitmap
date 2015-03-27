/*
 * Copyright 2015 Kevin Liu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.airk.tool.sobitmap;

import android.text.TextUtils;

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
    public static final int REASON_CANT_DECODE = 0x06;
    public static final int REASON_NETWORK_ERROR = 0x07;

    private int reason;
    private String extra;

    public HuntException(int reason) {
        this.reason = reason;
    }

    public void setExtra(String extra) {
        this.extra = extra;
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
            case REASON_CANT_DECODE:
                ret = "Can't decode this request.";
                break;
            case REASON_NETWORK_ERROR:
                ret = "Network error.";
                break;
            default:
                ret = "unknown";
        }
        return ret + (TextUtils.isEmpty(extra) ? "" : (" Extra: " + extra));
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }
}
