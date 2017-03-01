package com.jelliroo.api.otp.serializables;

import java.io.Serializable;

/**
 * Created by roger on 2/11/2017.
 */

public class CustomTokenResponse implements Serializable {

    private String customToken;

    public String getCustomToken() {
        return customToken;
    }

    public void setCustomToken(String customToken) {
        this.customToken = customToken;
    }

}
