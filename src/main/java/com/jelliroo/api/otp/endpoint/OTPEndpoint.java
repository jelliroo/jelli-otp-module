package com.jelliroo.api.otp.endpoint;

import com.jelliroo.api.otp.serializables.OTPResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by roger on 2/21/2017.
 */

public interface OTPEndpoint {

    @GET("/local/request/{phone}")
    Call<OTPResponse> requestOTP(@Path("phone") String phone);

    @GET("/local/verify/{phone}/session/{otpSessionId}/otp/{input}")
    Call<Object> verifyOTP(@Path("phone") String phone, @Path("otpSessionId") String otpSessionId, @Path("input") String input);

}
