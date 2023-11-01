package com.parallex.softtoken.Utilities;

import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Net {

    public static String httpXFormRequest(String apiUrl, HashMap<String,String> headers) {
//        HttpURLConnection urlConnection = null;
         HttpsURLConnection urlConnection = null;
        InputStream inputStream;
        String response = "";
        try {
            Log.d("Result", "Request url: " + apiUrl);
            URL url = new URL(apiUrl);
            urlConnection = (HttpsURLConnection) url.openConnection();
//            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(getGlobalSSlFactory());
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setReadTimeout(60000);
            urlConnection.setConnectTimeout(60000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setRequestProperty("Accept", "*/*");


            StringBuilder urlParameters = new StringBuilder();
            for(Map.Entry<String,String> header : headers.entrySet()){
                urlParameters.append(header.getKey()).append("=").append(header.getValue()).append("&");
            }
            urlParameters.deleteCharAt(urlParameters.length()-1);

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(urlParameters.toString().getBytes());
            out.flush();

            inputStream = urlConnection.getErrorStream();
            if (inputStream == null) {
                inputStream = urlConnection.getInputStream();
            }
            return Util.readStream(inputStream);
        }
        catch (Exception ex){
            ex.printStackTrace();
        } finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }
        return response;
    }


    private static SSLSocketFactory getGlobalSSlFactory() {
        try {
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    X509Certificate[] var3 = chain;
                    int var4 = chain.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        X509Certificate var10000 = var3[var5];
                    }

                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    X509Certificate[] var3 = chain;
                    int var4 = chain.length;

                    for(int var5 = 0; var5 < var4; ++var5) {
                        X509Certificate var10000 = var3[var5];
                    }

                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init((KeyManager[])null, new TrustManager[]{tm}, (SecureRandom)null);
            return sslContext.getSocketFactory();
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }
}
