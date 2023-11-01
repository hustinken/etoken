package com.parallex.softtoken.Model;


import android.app.Activity;
import android.os.Build;

import com.parallex.softtoken.Utilities.Net;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ApiModel {

    // private final String API_URL = "https://entrustselfservice.providusbank.com:8013/pmapi/api/";
//    private String API_URL = "https://etoken.parallexbank.com:443/pamapi/api/";
    private String API_URL = "https://uatetoken.parallexbank.com/pamapi/api/";
//    private String API_URL = "http://102.129.39.87/pamapi/api/";
    // public final String API_URL = " http://102.129.39.41/pmapi/api/";

    // ADDED
    public final String SEC_KEY = "12345678901234567890123456789012";
    public final String IVString = "0000000000000000";
    private Cipher encryptionCipher;
    // ADDED
    public String action, username, companyCode, customerNumber, sessionId, userId, account, registrationNo, phoneNumber;
    public static String activationNo, serialNo;
    public String encryptedData;
    String returnedResp = "";

    public String getAction() {
        return action;
    }

    //ADDED
    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public void setReturnedResp(String returnedResp) {
        this.returnedResp = returnedResp;
    }
    //ADDED

    public void setAction(String action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }



    public String getCustomerNumber() {
        return customerNumber;
    }

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        ApiModel.serialNo = serialNo.replace("-", "");
    }

    public String getActivationNo() {
        return activationNo;
    }

    public void setActivationNo(String activationNo) {
        ApiModel.activationNo = activationNo.replace("-", "");
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo.replace("-", "");
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void validateRetail(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("customernumber", customerNumber);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void validateCorp(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("username", username);
//                headers.put("companycode",companyCode);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void sessionBind(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void checkUserExist(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ent_user_id", userId);
                headers.put("ent_jsession_id", sessionId);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void checkUserTokenExist(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ent_user_id", userId);
                headers.put("ent_jsession_id", sessionId);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void createUserToken(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ent_user_id", userId);
                headers.put("ent_jsession_id", sessionId);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void generateActivationNo(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ent_user_id", userId);
                headers.put("ent_serial_no", serialNo);
                headers.put("ent_jsession_id", sessionId);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void sendSms(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ps_number", phoneNumber);
                headers.put("ent_serial_no", serialNo);
                headers.put("ent_activation_no", activationNo);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }

    public void activateToken(final Activity activity, final InternetCallBack callBack) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("action", action);
                headers.put("ent_user_id", userId);
                headers.put("ent_serial_no", serialNo);
                headers.put("ent_registration_no", registrationNo);
                headers.put("ent_jsession_id", sessionId);
                final String response = Net.httpXFormRequest(API_URL, headers);
                log(headers, response);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callBack.OnResponseReturned(response);
                    }
                });
            }
        }).start();
    }


    // ADDED
    private String encode(byte[] data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Base64.getEncoder().encodeToString(data);
        }
        return null;
    }

    private byte[] decode(String data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return Base64.getDecoder().decode(data);
             return Base64.getMimeDecoder().decode(data);
        }
        return new byte[0];
    }


    public String decrypt(String encrypted) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException {

        SecretKeySpec skeySpec = new SecretKeySpec(SEC_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(IVString.getBytes());

        Cipher ecipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        ecipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec);

        byte[] raw = decode(encrypted);
        byte[] originalBytes = ecipher.doFinal(raw);
        String original = new String(originalBytes, "UTF8");
        return original;

    }


    // Post response for data decryption


    //ADDED

    public void log(HashMap<String, String> headers, String response) {
//        for(Map.Entry<String,String> header : headers.entrySet()){
//            Log.d("Result", header.getKey() + ":" + header.getValue());
//        }
//        Log.d("Result", "Response: " + response);
    }

    public interface InternetCallBack {
        void OnResponseReturned(String response);
    }
}
