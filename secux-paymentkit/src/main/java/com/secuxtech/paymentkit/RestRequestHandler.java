package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-05
 */


import android.util.Pair;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

class RestRequestHandler {

    final public static String TAG = "secux-paymentkit";

    private Integer mConnectTimeout = 10000;
    private Boolean mLogReply = false;

    public static final Integer SecuXRequestOK = 0;
    public static final Integer SecuXRequestFailed = 1;
    public static final Integer SecuXRequestUnauthorized = 2;
    public static final Integer SecuXRequestForbiddened = 3;



    public Pair<Integer, String> processPostRequest(String path) {
        Integer result = SecuXRequestFailed;
        String response = "";
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mConnectTimeout);
            connection.setRequestProperty("Charset", "UTF-8");
            connection.setRequestProperty("Content-Length", "0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;

                if (mLogReply) {
                }
            }else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){

                response = "Unauthorized";
                result = SecuXRequestUnauthorized;

                SecuXPaymentKitLogHandler.Log("Server request failed! " + path + "Param: null");

            }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){

                response = "Forbidden operation";
                result = SecuXRequestForbiddened;

                SecuXPaymentKitLogHandler.Log("Server request failed! " + path + "Param: null");

            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();
                SecuXPaymentKitLogHandler.Log("Server request failed! " + path + "Param: null");
            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();

            SecuXPaymentKitLogHandler.Log("Server request failed! " + path + "Param: null");
        }
        return new Pair<>(result, response);
    }

    public Pair<Integer, String> processPostRequest(String path, JSONObject param) {
        String paramStr = param.toString();
        Integer result = SecuXRequestFailed;
        String response = "";
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mConnectTimeout);
            connection.setRequestProperty("Charset", "UTF-8");
            //connection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(out, "UTF-8"));
            writer.write(paramStr);
            writer.close();

            //out.writeBytes(paramStr);
            //out.flush();
            out.close();

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;

                if (mLogReply) {
                }

            }else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){

                response = "Unauthorized";
                result = SecuXRequestUnauthorized;

            }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){

                response = "Forbidden operation";
                result = SecuXRequestForbiddened;

            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();


            }

            connection.disconnect();

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log("connection timeout = " + mConnectTimeout);
            e.printStackTrace();
            response = e.getMessage();

        }

        return new Pair<>(result, response);
    }

    Pair<Integer, String> processPostRequest(String path, JSONObject param, String token) {
        return processPostRequest(path, param, token, mConnectTimeout);
    }

    public Pair<Integer, String> processPostRequest(String path, JSONObject param, String token, Integer timeout) {
        String paramStr = "{}";
        if (param != null) {
            paramStr = param.toString();
        }
        Integer result = SecuXRequestFailed;
        String response = "";
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setRequestProperty("Charset", "UTF-8");

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + token);
            //connection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));
            connection.connect();

            if (paramStr.length() > 0) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                BufferedWriter writer = new BufferedWriter( new OutputStreamWriter(out, "UTF-8"));
                writer.write(paramStr);
                writer.close();
                //out.writeBytes(paramStr);
                //out.flush();
                out.close();
            }

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;

                if (mLogReply) {
                }
            }else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){

                response = "Unauthorized";
                result = SecuXRequestUnauthorized;

            }else if (responseCode == HttpsURLConnection.HTTP_FORBIDDEN){

                response = "Forbidden operation";
                result = SecuXRequestForbiddened;


            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();

            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        }

        return new Pair<>(result, response);
    }

    private String getResponse(InputStream in) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

}
