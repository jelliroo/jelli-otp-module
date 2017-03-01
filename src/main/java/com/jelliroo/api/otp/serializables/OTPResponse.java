package com.jelliroo.api.otp.serializables;

import java.io.Serializable;

/**
 * Created by roger on 2/21/2017.
 */

public class OTPResponse implements Serializable{

    String otpSessionId;

    public String getOtpSessionId() {
        return otpSessionId;
    }

    public void setOtpSessionId(String otpSessionId) {
        this.otpSessionId = otpSessionId;
    }
}
