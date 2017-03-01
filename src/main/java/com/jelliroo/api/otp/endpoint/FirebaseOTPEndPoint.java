package com.jelliroo.api.otp.endpoint;

import com.jelliroo.api.otp.serializables.CustomTokenResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by roger on 2/28/2017.
 */

public interface FirebaseOTPEndPoint {

    @GET("/firebase/request/{phone}")
    Call<Object> requestOTP(@Path("phone") String phone);

    @GET("/firebase/verify/{phone}/otp/{input}")
    Call<CustomTokenResponse> verifyOTP(@Path("phone") String phone, @Path("input") String input);

}
