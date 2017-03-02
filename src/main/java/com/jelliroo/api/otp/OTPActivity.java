package com.jelliroo.api.otp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ProgressBar;
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

/**
 * @author roger cores
 * This activity can called in two modes: firebase and local dbase
 * This activity will verify the users phone number using One Time Password (OTP) and return a result accordingly
 * For firebase mode, the activity returns a custom token
 * For local dbase, custom objects can be returned
 */
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
    public static String ARG_WAIT_MESSAGE = "ARG_WAIT_MESSAGE";
    public static String ARG_SERVER_ERROR_MESSAGE = "ARG_SERVER_ERROR_MESSAGE";
    public static String ARG_VERIFICATION_FAILED_MESSAGE = "ARG_VERIFICATION_FAILED_MESSAGE";
    public static String ARG_EMPTY_PHONE_ERROR_MESSAGE = "ARG_EMPTY_PHONE_MESSAGE";
    public static String ARG_PHONE_LENGTH_ERROR_MESSAGE = "ARG_PHONE_LENGTH_ERROR_MESSAGE";
    public static String ARG_EMPTY_OTP_ERROR_MESSAGE = "ARG_EMPTY_OTP_MESSAGE";


    public static int TYPE_FIREBASE = 1, TYPE_LOCAL = 2;

    private String waitMessage;
    private String serverErrorMessage;
    private String verificationMessage;
    private String emptyPhoneMessage;
    private String phoneLengthMessage;
    private String emptyOTPMessage;
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
    private boolean loadingMode = false;

    private EditText phoneCode, phoneNumber, otpCode;
    private Spinner countrySpinner;
    private Button submit;
    private TextView message, resendCode;
    private RelativeLayout requestOTPContainer, verifyOTPContainer;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;

    private ArrayAdapter<Country> countryArrayAdapter;

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
        progressBar = (ProgressBar) findViewById(R.id.otp_progress_bar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
    }

    public void initViews(){
        phoneNumber.setHint(phoneHint);
        otpCode.setHint(otpHint);
        resendCode.setText(getString(R.string.resend_the_code_container, resendCodeMessage));

        if(otpVerificationMode){
            resendCode.setVisibility(View.VISIBLE);
            message.setText(otpMessage);
            submit.setText(verifySubmitMessage);
            requestOTPContainer.setVisibility(View.GONE);
            verifyOTPContainer.setVisibility(View.VISIBLE);
        } else {
            resendCode.setVisibility(View.GONE);
            message.setText(phoneMessage);
            submit.setText(requestSubmitMessage);
            requestOTPContainer.setVisibility(View.VISIBLE);
            verifyOTPContainer.setVisibility(View.GONE);
        }

        if(loadingMode){
            message.setAlpha(1);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setAlpha(1f);
            coordinatorLayout.setAlpha(0.3f);
            submit.setEnabled(false);
            submit.setAlpha(0.3f);

            if(otpVerificationMode){
                otpCode.setEnabled(false);
                resendCode.setEnabled(false);
                resendCode.setAlpha(0.3f);
            } else {
                countrySpinner.setEnabled(false);
                phoneNumber.setEnabled(false);
                phoneCode.setEnabled(false);
            }
        }

        SpannableString content = new SpannableString(resendCodeMessage);
        content.setSpan(new UnderlineSpan(), 0, resendCodeMessage.length(), 0);
        resendCode.setText(content);


        countryArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, countries);
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
                    phoneNumber.setError(emptyPhoneMessage);
                } else if(phoneNumber.getText().toString().trim().length() != 10) {
                    phoneNumber.setError(phoneLengthMessage);
                } else phoneNumber.setError(null);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        otpCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(otpCode.getText().toString().trim().equals("")){
                    otpCode.setError(emptyOTPMessage);
                } else otpCode.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
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

        waitMessage = getIntent().getExtras().getString(OTPActivity.ARG_WAIT_MESSAGE, getString(R.string.default_wait_message));
        serverErrorMessage = getIntent().getExtras().getString(OTPActivity.ARG_SERVER_ERROR_MESSAGE, getString(R.string.server_error));
        verificationMessage = getIntent().getExtras().getString(OTPActivity.ARG_VERIFICATION_FAILED_MESSAGE, getString(R.string.verification_failed_error));
        emptyPhoneMessage = getIntent().getExtras().getString(OTPActivity.ARG_EMPTY_PHONE_ERROR_MESSAGE, getString(R.string.default_empty_phone_message));
        phoneLengthMessage = getIntent().getExtras().getString(OTPActivity.ARG_PHONE_LENGTH_ERROR_MESSAGE, getString(R.string.default_phone_length_error_message));
        emptyOTPMessage = getIntent().getExtras().getString(OTPActivity.ARG_EMPTY_OTP_ERROR_MESSAGE, getString(R.string.default_empty_otp_message));
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

    public void requestOTP(){
        Country country = (Country) countrySpinner.getSelectedItem();
        String phone = country.getCode().toString() + phoneNumber.getText().toString();

        if(phoneNumber.getText().toString().trim().equals("")){
            phoneNumber.setError(emptyPhoneMessage);
            return;
        } else if(phoneNumber.getText().toString().trim().length() != 10){
            phoneNumber.setError(phoneLengthMessage);
            return;
        }


        animateToLoading();

        if(authType == OTPActivity.TYPE_FIREBASE){
            FirebaseOTPEndPoint endPoint = retrofit.create(FirebaseOTPEndPoint.class);
            Call<Object> callForRequestOTP = endPoint.requestOTP(phone);
            callForRequestOTP.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    if(response.code() == HttpURLConnection.HTTP_OK){
                        if(otpVerificationMode){
                            Toast.makeText(OTPActivity.this, codeResentMessage, Toast.LENGTH_LONG).show();
                            animateToVerify();
                        }
                        else
                            animateToVerify();
                    } else {
                        Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                        animateToRequest();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                    animateToRequest();
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
                            animateToVerify();
                        } else {

                            OTPResponse otpResponse = response.body();
                            sessionId = otpResponse.getOtpSessionId();
                            animateToVerify();
                        }

                    } else {
                        Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                        animateToRequest();
                    }
                }

                @Override
                public void onFailure(Call<OTPResponse> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                    animateToRequest();
                }
            });
        }
    }

    public void verifyOTP(){
        Country country = (Country) countrySpinner.getSelectedItem();
        String phone = country.getCode().toString() + phoneNumber.getText().toString();
        String otp = otpCode.getText().toString();

        if(otp.trim().equals("")){
            otpCode.setError(emptyOTPMessage);
            return;
        }

        animateToLoading();

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
                        Toast.makeText(OTPActivity.this, verificationMessage, Toast.LENGTH_LONG).show();
                        animateToVerify();
                    } else {
                        Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                        animateToVerify();
                    }
                }

                @Override
                public void onFailure(Call<CustomTokenResponse> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                    animateToVerify();
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
                        Toast.makeText(OTPActivity.this, verificationMessage, Toast.LENGTH_LONG).show();
                        animateToVerify();
                    } else {
                        Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                        animateToVerify();
                    }
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    Toast.makeText(OTPActivity.this, serverErrorMessage, Toast.LENGTH_LONG).show();
                    animateToVerify();
                }
            });
        }
    }

    public void resend(View view){
        requestOTP();
    }

    public void submit(View view){
        if(otpVerificationMode){
            verifyOTP();
        } else {
            requestOTP();
        }
    }

    public void animateToLoading(){
        loadingMode = true;
        message.animate()
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        message.setText(waitMessage);
                        message.animate()
                                .alpha(1f)
                                .setDuration(250);
                        animation.removeAllListeners();
                    }
                });

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setAlpha(0f);
        progressBar.animate()
                   .alpha(1f)
                   .setDuration(250)
                   .setListener(new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           super.onAnimationEnd(animation);
                           animation.removeAllListeners();
                           progressBar.setAlpha(1f);
                           progressBar.setVisibility(View.VISIBLE);
                       }
                   });

        coordinatorLayout.animate()
                .alpha(0.3f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animation.removeAllListeners();
                    }
                });


        submit.setEnabled(false);
        submit.animate()
                .alpha(0.3f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        animation.removeAllListeners();
                        submit.setEnabled(false);
                    }
                });

        if(otpVerificationMode){
            otpCode.setEnabled(false);
            resendCode.setEnabled(false);

            resendCode.animate()
                      .alpha(0.3f)
                      .setDuration(250)
                      .setListener(new AnimatorListenerAdapter() {
                          @Override
                          public void onAnimationEnd(Animator animation) {
                              super.onAnimationEnd(animation);
                              animation.removeAllListeners();
                              resendCode.setAlpha(0.3f);
                              resendCode.setEnabled(false);
                          }
                      });

        } else {
            countrySpinner.setEnabled(false);
            phoneNumber.setEnabled(false);
            phoneCode.setEnabled(false);
        }


    }

    public void animateToVerify(){
        loadingMode = false;
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
                        submit.setEnabled(true);
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
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        resendCode.setEnabled(true);
                        resendCode.setAlpha(1f);
                    }
                });
        otpCode.setAlpha(1f);
        otpCode.setEnabled(true);


        progressBar.animate()
                   .alpha(0f)
                   .setDuration(250)
                   .setListener(new AnimatorListenerAdapter() {
                       @Override
                       public void onAnimationEnd(Animator animation) {
                           super.onAnimationEnd(animation);
                           progressBar.setVisibility(View.GONE);
                       }
                   });

        requestOTPContainer.animate()
                .alpha(0f)
                .setDuration(125)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        requestOTPContainer.setVisibility(View.GONE);
                        verifyOTPContainer.setVisibility(View.VISIBLE);
                        verifyOTPContainer.setAlpha(0f);
                        verifyOTPContainer.animate()
                                .alpha(1f)
                                .setDuration(125)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        verifyOTPContainer.setVisibility(View.VISIBLE);
                                        verifyOTPContainer.setAlpha(1f);
                                        coordinatorLayout.setAlpha(1f);
                                        animation.removeAllListeners();
                                    }
                                });
                        animation.removeAllListeners();
                    }
                });
    }

    public void animateToRequest(){
        loadingMode = false;
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
                        submit.setEnabled(true);
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
                        phoneCode.setEnabled(true);
                        phoneNumber.setEnabled(true);
                        countrySpinner.setEnabled(true);
                        verifyOTPContainer.setVisibility(View.GONE);
                        requestOTPContainer.setVisibility(View.VISIBLE);
                        requestOTPContainer.setAlpha(0f);
                        coordinatorLayout.setAlpha(1f);
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



        progressBar.animate()
                .alpha(0f)
                .setDuration(250)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if(loadingMode) return;
        if(otpVerificationMode){
            animateToRequest();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String oldPhoneNumber = phoneNumber.getText().toString();
        String oldOtpCode = otpCode.getText().toString();
        Country country = (Country) countrySpinner.getSelectedItem();
        String oldCountryCode = getString(R.string.phone_code_format, country.getCode().toString());

        setContentView(R.layout.activity_otp);
        loadViews();
        initViews();
        setViewListeners();

        phoneNumber.setText(oldPhoneNumber);
        otpCode.setText(oldOtpCode);

        if(oldPhoneNumber.trim().equals("")){
            phoneNumber.setError(emptyPhoneMessage);
        } else if(oldPhoneNumber.trim().length() != 10){
            phoneNumber.setError(phoneLengthMessage);
        } else {
            phoneNumber.setError(null);
        }

        if(oldOtpCode.trim().equals("")){
            otpCode.setError(emptyOTPMessage);
        } else {
            otpCode.setError(null);
        }

        phoneCode.setText(oldCountryCode);
        countrySpinner.setSelection(countryArrayAdapter.getPosition(country));

    }
}
