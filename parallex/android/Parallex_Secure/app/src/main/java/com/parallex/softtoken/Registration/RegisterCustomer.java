package com.parallex.softtoken.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.entrust.identityGuard.mobile.sdk.example.R;
import com.parallex.softtoken.Activation.ActivateToken;
import com.parallex.softtoken.Dialogs.ProgressDialog;
import com.parallex.softtoken.Model.ApiModel;
import com.parallex.softtoken.Utilities.Delay;
import com.parallex.softtoken.Utilities.Util;

import org.json.JSONObject;

public class RegisterCustomer extends AppCompatActivity implements View.OnClickListener {


    RadioGroup customerType;
    EditText customerId, companyId;
    TextView customerIdLabel;
    TextView companyIdLable;
    Button validateBtn;
    boolean isCorporateCustomer = false;
    boolean displayCompanyCode = false;
    ApiModel model;
    ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_customer);
        model = new ApiModel();
        dialog = new ProgressDialog(this);
        customerId = findViewById(R.id.customer_id);

        customerIdLabel = findViewById(R.id.customer_id_label);
        Toolbar toolbar = findViewById(R.id.toolbar);
        customerType = findViewById(R.id.customer_type);
        companyId = findViewById(R.id.company_id);
        companyIdLable = findViewById(R.id.customer_user_name);

        validateBtn = findViewById(R.id.validate_btn);

        setSupportActionBar(toolbar);

        customerType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                customerId.setText("");
                if (radioGroup.getCheckedRadioButtonId() == R.id.retail_customer) {
                    isCorporateCustomer = false;
                    companyId.setVisibility(View.GONE);
                    companyIdLable.setVisibility(View.GONE);
                    customerId.setHint(R.string.customer_number);
                    customerId.setInputType(InputType.TYPE_CLASS_PHONE);
                    customerIdLabel.setText(R.string.enter_account_number);
                } else if (radioGroup.getCheckedRadioButtonId() == R.id.corporate_customer) {
                    isCorporateCustomer = true;
//                    displayCompanyCode = true;
                    displayCompanyCode = false;
//                    companyId.setVisibility(View.VISIBLE);
                    companyId.setVisibility(View.GONE);
//                    companyIdLable.setVisibility(View.VISIBLE);
                    companyIdLable.setVisibility(View.GONE);
                    customerId.setHint(R.string.enter_username);
                    customerId.setInputType(InputType.TYPE_CLASS_TEXT);
                    customerIdLabel.setText(R.string.enter_username);
                }
            }
        });
        validateBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {


        if (isCorporateCustomer) {
//            if (companyId.getText().toString().isEmpty() || customerId.getText().toString().isEmpty()) {
            if (customerId.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a valid username", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.show();
            dialog.setProgressLabel("Please wait while we validate your information", true);

            model.setAction("4");
            model.setUsername(customerId.getText().toString());
//            model.setCompanyCode(companyId.getText().toString());
            model.validateCorp(this, new ApiModel.InternetCallBack() {
                @Override
                public void OnResponseReturned(String response) {
                    processCorporateValidateApi(response);
                }
            });
        } else {
            if (customerId.getText().toString().isEmpty()) {
                Toast.makeText(this, "Please enter a valid account number", Toast.LENGTH_SHORT).show();
                return;
            }
            dialog.show();
            dialog.setProgressLabel("Please wait while we validate your information", true);

            model.setAction("5");
            model.setCustomerNumber(customerId.getText().toString());
            model.validateRetail(this, new ApiModel.InternetCallBack() {
                @Override
                public void OnResponseReturned(String response) {
                    processValidationApi(response);
                }
            });
        }
    }


    private void processValidationApi(String response) {
        try {
            if (response.isEmpty()) {
                dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                return;
            }

            // ADDED
            String encrypted = response.replace("\n", "");
            String data = model.decrypt(encrypted);

            JSONObject json = new JSONObject(data);
            String responseCode = json.getString("ps_resp_code");
            String respDesc = json.getString("ps_resp_desc");
            if (responseCode.equals("0") && respDesc.toLowerCase().contains("true")) {
                String phoneNumber = json.getString("ps_number");
                String accountNo = json.getString("ps_account");

                model.setAccount(accountNo);
                model.setPhoneNumber(phoneNumber);
                model.setUserId(customerId.getText().toString());
                processSessionBind();
            } else if (respDesc.toLowerCase().contains("false")) {
                dialog.setProgressLabel("User does not exist on CBA, contact System Administrator", false, true);
            } else {
                dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dialog.setProgressLabel("Server Error", false, true);

        }
    }

    private void processCorporateValidateApi(String response) {
        try {
            if (response.isEmpty()) {
                dialog.setProgressLabel("Connection problem, Please check your internet 1", false, true);
                return;
            }

            // ADDED
            String encrypted = response.replace("\n", "");
            String data = model.decrypt(encrypted);

            JSONObject json = new JSONObject(data);
            String responseCode = json.getString("ps_resp_code");
            String respDesc = json.getString("ps_resp_desc");
            if (responseCode.equals("0") && respDesc.toLowerCase().contains("true")) {
                String phoneNumber = json.getString("ps_number");
                String accountNo = json.getString("ps_account");
                String userName = json.getString("ps_username");
                model.setAccount(accountNo);
                model.setPhoneNumber(phoneNumber);
                model.setUserId(userName);
                processSessionBind();
            } else if (respDesc.toLowerCase().contains("false")) {
                dialog.setProgressLabel("User does not exist on CBA, contact System Administrator", false, true);
            } else {
                dialog.setProgressLabel("Connection problem, Please check your internet 2", false, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            dialog.setProgressLabel("Server Error", false, true);
            // dialog.setProgressLabel(e.toString(), false, true);

        }
    }


    private void processSessionBind() {
        dialog.setProgressLabel("Binding Session, Please wait", true);
        model.setAction("0");
        model.sessionBind(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);
                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    if (responseCode.equals("0")) {
                        String sessionId = json.getString("ent_jsession_id");
                        model.setSessionId(sessionId);
                        checkUserExist();
                    } else if (responseCode.equals("1")) {
                        dialog.setProgressLabel("Fail to bind Session, try again", false, true);
                    } else {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("Server Error", false, true);
                }
            }
        });
    }

    private void checkUserExist() {
        dialog.setProgressLabel("Checking user status", true);
        model.setAction("1");
//        model.setUserId(customerId.getText().toString());
        model.checkUserExist(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);


                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    String respDesc = json.getString("ps_resp_desc");
                    if (responseCode.equals("0")) {
                        checkUserTokenExist();
                    } else if (responseCode.equals("1")) {
                        dialog.setProgressLabel("User does not exist on Entrust, Please contact System Administrator", false, true);
                    } else {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("Server Error", false, true);
                }
            }
        });
    }

    private void checkUserTokenExist() {
        dialog.setProgressLabel("Checking token status", true);
        model.setAction("2");
        model.checkUserTokenExist(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);


                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    if (responseCode.equals("0")) {
                        String activationNo = json.getString("ent_activation_no");
                        String serialNo = json.getString("ent_serial_no");
                        if (!serialNo.isEmpty() && activationNo.isEmpty()) {
                            model.setSerialNo(serialNo);
                            generateUserActivationNumber();
                        } else {
                            model.setActivationNo(activationNo);
                            model.setSerialNo(serialNo);
                            sendSmsToUser();
                        }
                    } else if (responseCode.equals("1")) {
                        createUserToken();
                    } else {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("User Token already exist and activated", false, true);
                }
            }
        });
    }


    private void createUserToken() {
        dialog.setProgressLabel("Creating user token", true);
        model.setAction("6");
        model.createUserToken(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);


                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    if (responseCode.equals("0")) {
                        String serialNo = json.getString("ent_serial_no");
                        model.setSerialNo(serialNo);
                        generateUserActivationNumber();
                    } else {
                        checkUserTokenExist();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("Server Error", false, true);
                }
            }
        });
    }


    private void generateUserActivationNumber() {
        dialog.setProgressLabel("Generating user activation number", true);
        model.setAction("7");
        model.generateActivationNo(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);


                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("ps_resp_code");
                    if (responseCode.equals("0")) {
                        String activationNo = json.getString("ent_activation_no");
                        model.setActivationNo(activationNo);
                        sendSmsToUser();
                    } else {
                        checkUserTokenExist();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("User Token already exist and activated", false, true);

                }
            }
        });
    }


    int count = 0;

    public void sendSmsToUser() {
        dialog.setProgressLabel("Sending activation code to your registered number", true);
        model.setAction("8");
        model.sendSms(this, new ApiModel.InternetCallBack() {
            @Override
            public void OnResponseReturned(String response) {
                try {
                    if (response.isEmpty()) {
                        dialog.setProgressLabel("Connection problem, Please check your internet", false, true);
                        return;
                    }
                    // ADDED
                    String encrypted = response.replace("\n", "");
                    String data = model.decrypt(encrypted);

                    JSONObject json = new JSONObject(data);
                    String responseCode = json.getString("responseCode");
                    if (responseCode.equals("00")) {
                        Util.setUserId(RegisterCustomer.this, model.getUserId());
                        Util.setSessionId(RegisterCustomer.this, model.getSessionId());

                        dialog.dismiss();

                        startActivity(new Intent(RegisterCustomer.this, ActivateToken.class));
                        finish();

//                        dialog.setProgressLabel("Activation code has been sent to your registered phone number. Proceed to activate your token", false, true);
//                        dialog.setButtonClicked("Activate Token", new ProgressDialog.ButtonClicked() {
//
//                            @Override
//                            public void onClicked(View v) {
//                                startActivity(new Intent(RegisterCustomer.this, MainActivity.class));
//                                finish();
//                            }
//                        });
//                        dialog.setButtonClicked2("Resend SMS", new ProgressDialog.ButtonClicked() {
//                            @Override
//                            public void onClicked(View v) {
//                                sendSmsToUser();
//                            }
//                        });
                    } else {
                        if (count < 3) {
                            new Delay(new Delay.IDelay() {
                                @Override
                                public void onDelayFinished() {
                                    sendSmsToUser();
                                    count++;
                                }
                            }, 2000);
                        } else {
                            dialog.setProgressLabel("An error occurred during registration, please contact administrator", false, true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dialog.setProgressLabel("Server Error", false, true);


                }
            }
        });
    }
}