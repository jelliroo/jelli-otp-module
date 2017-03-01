package com.jelliroo.api.otp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jelliroo.api.otp.endpoint.FirebaseOTPEndPoint;
import com.jelliroo.api.otp.endpoint.OTPEndpoint;
import com.jelliroo.api.otp.parcelables.Country;
import com.jelliroo.api.otp.serializables.CustomTokenResponse;
import com.jelliroo.api.otp.serializables.OTPResponse;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OTPActivity extends AppCompatActivity {

    private Retrofit retrofit;

    public static String ARG_AUTH_TYPE = "ARG_AUTH_TYPE";
    public static String ARG_INPUT_PHONE = "ARG_INPUT_PHONE";
    public static String ARG_INPUT_OTP = "ARG_INPUT_OTP";
    public static String ARG_BASE_URL = "ARG_BASE_URL";
    public static String ARG_COUNTRIES = "ARG_COUNTRIES";
    public static String ARG_PHONE_HINT = "ARG_PHONE_HINT";
    public static String ARG_OTP_HINT = "ARG_OTP_HINT";
    public static String ARG_SUBMIT_SMS = "ARG_SUBMIT_SMS";
    public static String ARG_SUBMIT_VERIFY = "ARG_SUBMIT_VERIFY";
    public static String ARG_TOKEN = "ARG_TOKEN";
    public static String ARG_CODE_RESENT = "ARG_CODE_RESENT";
    public static String ARG_RESEND_CODE_MESSAGE = "ARG_RESEND_CODE_MESSAGE";

    public static int TYPE_FIREBASE = 1, TYPE_LOCAL = 2;

    private String phoneMessage;
    private String otpMessage;
    private String requestSubmitMessage;
    private String verifySubmitMessage;
    private String codeResentMessage;
    private String sessionId;
    private String phoneHint;
    private String otpHint;
    private String resendCodeMessage;
    private String baseUrl;
    private int authType;
    private ArrayList<Country> countries;
    private boolean otpVerificationMode = false;

    private EditText phoneCode, phoneNumber, otpCode;
    private Spinner countrySpinner;
    private Button submit;
    private TextView message, resendCode;
    private RelativeLayout requestOTPContainer, verifyOTPContainer;

    public void loadViews(){
        submit = (Button) findViewById(R.id.submit);
        message = (TextView) findViewById(R.id.message);
        resendCode = (TextView) findViewById(R.id.resend_code);
        phoneNumber = (EditText) findViewById(R.id.phone_number);
        otpCode = (EditText) findViewById(R.id.otp_code);
        requestOTPContainer = (RelativeLayout) findViewById(R.id.requestOTP);
        verifyOTPContainer = (RelativeLayout) findViewById(R.id.verifyOTP);
        countrySpinner = (Spinner) findViewById(R.id.spinner_countries);
        phoneCode = (EditText) findViewById(R.id.phone_code);
    }

    public void initViews(){
        phoneNumber.setHint(phoneHint);
        otpCode.setHint(otpHint);
        resendCode.setText(getString(R.string.resend_the_code_container, resendCodeMessage));
        resendCode.setVisibility(View.GONE);
        SpannableString content = new SpannableString(resendCodeMessage);
        content.setSpan(new UnderlineSpan(), 0, resendCodeMessage.length(), 0);
        resendCode.setText(content);
        message.setText(phoneMessage);
        submit.setText(requestSubmitMessage);
        ArrayAdapter<Country> countryArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
        countrySpinner.setAdapter(countryArrayAdapter);
        phoneCode.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
    }

    public void setViewListeners(){
        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(phoneNumber.getText().toString().trim().equals("")){
                    phoneNumber.setError(getString(R.string.empty_phone_message));
                } else phoneNumber.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        phoneCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        countrySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                phoneCode.setText(getString(R.string.phone_code_format, countries.get(position).getCode().toString()));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void loadExtras(){
        authType = getIntent().getExtras().getInt(OTPActivity.ARG_AUTH_TYPE, OTPActivity.TYPE_LOCAL);
        phoneMessage = getIntent().getExtras().getString(OTPActivity.ARG_INPUT_PHONE, getString(R.string.default_message_input_phone));
        otpMessage = getIntent().getExtras().getString(OTPActivity.ARG_INPUT_OTP, getString(R.string.default_message_input_otp));
        requestSubmitMessage = getIntent().getExtras().getString(OTPActivity.ARG_SUBMIT_SMS, getString(R.string.default_submit_sms));
        verifySubmitMessage = getIntent().getExtras().getString(OTPActivity.ARG_SUBMIT_VERIFY, getString(R.string.default_submit_verify));
        phoneHint = getIntent().getExtras().getString(OTPActivity.ARG_PHONE_HINT, getString(R.string.default_phone_hint));
        otpHint = getIntent().getExtras().getString(OTPActivity.ARG_OTP_HINT, getString(R.string.default_otp_hint));
        codeResentMessage = getIntent().getExtras().getString(OTPActivity.ARG_CODE_RESENT, getString(R.string.default_code_resent_message));
        resendCodeMessage = getIntent().getExtras().getString(OTPActivity.ARG_RESEND_CODE_MESSAGE, getString(R.string.default_resend_code_message));
        baseUrl = getIntent().getExtras().getString(OTPActivity.ARG_BASE_URL);
        if(baseUrl == null || baseUrl.trim().equals("")){
            throw new IllegalArgumentException(getString(R.string.illegal_arguments, getString(R.string.argument_base_url_name)));
        }
        countries = getIntent().getExtras().getParcelableArrayList(OTPActivity.ARG_COUNTRIES);
        if(countries == null || countries.size() == 0){
            throw new IllegalArgumentException(getString(R.string.illegal_arguments, getString(R.string.argument_countries_name)));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        loadExtras();
        loadViews();
        initViews();
        setViewListeners();
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public void resend(View view){
        requestOTP();
    }

    public void requestOTP(){
        Country country = (Country) countrySpinner.getSelectedItem();
        String phone = country.getCode().toString() + phoneNumber.getText().toString();

        if(phoneNumber.getText().toString().trim().equals("")){
            phoneNumber.setError(getString(R.string.empty_phone_message));
            return;
        }

        if(authType == OTPActivity.TYPE_FIREBASE){
            FirebaseOTPEndPoint endPoint = retrofit.create(FirebaseOTPEndPoint.class);
            Call<Object> callForRequestOTP = endPoint.requestOTP(phone);
            callForRequestOTP.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){
                        if(otpVerificationMode){
                            Toast.makeText(OTPActivity.this, codeResentMessage, Toast.LENGTH_LONG).show();
                        }
                        else
                            animateToVerify();
                    } else {
                        Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            OTPEndpoint endpoint = retrofit.create(OTPEndpoint.class);
            Call<OTPResponse> callForRequestOTP = endpoint.requestOTP(phone);

            callForRequestOTP.enqueue(new Callback<OTPResponse>() {
                @Override
                public void onResponse(Call<OTPResponse> call, Response<OTPResponse> response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){

                        if(otpVerificationMode){
                            Toast.makeText(OTPActivity.this, codeResentMessage, Toast.LENGTH_LONG).show();
                        } else {

                            OTPResponse otpResponse = response.body();
                            sessionId = otpResponse.getOtpSessionId();
                            animateToVerify();
                        }

                    } else {
                        Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<OTPResponse> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void verifyOTP(){
        Country country = (Country) countrySpinner.getSelectedItem();
        String phone = country.getCode().toString() + phoneNumber.getText().toString();
        String otp = otpCode.getText().toString();

        if(authType == TYPE_FIREBASE){
            FirebaseOTPEndPoint endPoint = retrofit.create(FirebaseOTPEndPoint.class);
            Call<CustomTokenResponse> callForVerify = endPoint.verifyOTP(phone, otp);
            callForVerify.enqueue(new Callback<CustomTokenResponse>() {
                @Override
                public void onResponse(Call<CustomTokenResponse> call, Response<CustomTokenResponse> response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){
                        CustomTokenResponse tokenResponse = response.body();
                        Intent intent = new Intent();
                        intent.putExtra(OTPActivity.ARG_TOKEN, tokenResponse.getCustomToken());
                        setResult(RESULT_OK, intent);
                        finish();
                    } else if(response.code() == HttpURLConnection.HTTP_NOT_FOUND || response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Toast.makeText(OTPActivity.this, R.string.verification_failed_error, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<CustomTokenResponse> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            OTPEndpoint endpoint = retrofit.create(OTPEndpoint.class);
            Call<Object> callForVerify = endpoint.verifyOTP(phone, sessionId, otp);
            callForVerify.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){
                        setResult(RESULT_OK);
                        finish();
                    } else if(response.code() == HttpURLConnection.HTTP_NOT_FOUND || response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Toast.makeText(OTPActivity.this, R.string.verification_failed_error, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, R.string.server_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void submit(View view){
        if(otpVerificationMode){
            verifyOTP();
        } else {
            requestOTP();
        }
    }

    public void animateToVerify(){
        otpVerificationMode = true;
        message.animate()
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        message.setText(otpMessage);
                        message.animate()
                                .alpha(1f)
                                .setDuration(250);
                        animation.removeAllListeners();
                    }
                });

        submit.animate()
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        submit.setText(verifySubmitMessage);
                        submit.animate()
                                .alpha(1f)
                                .setDuration(250);
                        animation.removeAllListeners();
                    }
                });
        resendCode.setVisibility(View.VISIBLE);
        resendCode.setAlpha(0f);
        resendCode.animate()
                .alpha(1f)
                .setDuration(500);

        requestOTPContainer.animate()
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        requestOTPContainer.setVisibility(View.GONE);
                        verifyOTPContainer.setVisibility(View.VISIBLE);
                        verifyOTPContainer.setAlpha(0f);
                        verifyOTPContainer.animate()
                                .alpha(1f)
                                .setDuration(250)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        verifyOTPContainer.setVisibility(View.VISIBLE);
                                        verifyOTPContainer.setAlpha(1f);
                                        animation.removeAllListeners();
                                    }
                                });
                        animation.removeAllListeners();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(otpVerificationMode){

            otpVerificationMode = false;

            message.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            message.setText(phoneMessage);
                            message.animate()
                                    .alpha(1f)
                                    .setDuration(250);
                            animation.removeAllListeners();
                        }
                    });

            submit.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            submit.setText(requestSubmitMessage);
                            submit.animate()
                                    .alpha(1f)
                                    .setDuration(250);
                            animation.removeAllListeners();
                        }
                    });


            resendCode.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            resendCode.setVisibility(View.VISIBLE);
                            animation.removeAllListeners();
                        }
                    });

            verifyOTPContainer.animate()
                    .alpha(0f)
                    .setDuration(250)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            verifyOTPContainer.setVisibility(View.GONE);
                            requestOTPContainer.setVisibility(View.VISIBLE);
                            requestOTPContainer.setAlpha(0f);
                            requestOTPContainer.animate()
                                    .alpha(1f)
                                    .setDuration(250)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            super.onAnimationEnd(animation);
                                            requestOTPContainer.setVisibility(View.VISIBLE);
                                            requestOTPContainer.setAlpha(1f);
                                            animation.removeAllListeners();
                                        }
                                    });
                            animation.removeAllListeners();
                        }
                    });



        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
