package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-05
 */

import android.util.Log;

import androidx.core.util.Pair;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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

    public void processURLRequest(){

        HttpURLConnection connection = null;
        try{
            URL url = new URL("https://www.xxx.com/");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            connection.setDoOutput(true);// 使用 URL 連線進行輸出
            connection.setDoInput(true);// 使用 URL 連線進行輸入
            connection.setUseCaches(false);// 忽略快取
// 建立輸出流，並寫入資料
            OutputStream outputStream = connection.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes("username=admin&password=888888");
            dataOutputStream.close();
            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
// 當正確響應時處理資料
                StringBuffer response = new StringBuffer();
                String line;
                BufferedReader responseReader =
                        new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
// 處理響應流，必須與伺服器響應流輸出的編碼一致
                while (null != (line = responseReader.readLine())) {
                    response.append(line);
                }
                responseReader.close();
                Log.d("secux-paymentkit", response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null!= connection) {
                connection.disconnect();
            }
        }
    }

    public Pair<Integer, String> processGetRequest(String path, String authorization) {
        Integer result = SecuXRequestFailed;
        String response = "";
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestProperty("Authorization", "Basic " + authorization);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(mConnectTimeout);
            connection.setReadTimeout(mConnectTimeout);
            connection.connect();

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;
                if (mLogReply) {
                    Log.i(TAG, response);
                }
            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();
                Log.e(TAG, "Server request response code = " + response);
            }

            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
        }
        return new Pair<>(result, response);
    }

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
                    Log.i(TAG, response);
                }
            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();
                Log.e(TAG, "Server request response code = " + response);
            }
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
            response = e.getMessage();
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
            connection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            DataOutputStream out = new DataOutputStream(connection.getOutputStream());

            out.writeBytes(paramStr);
            out.flush();
            out.close();

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;

                if (mLogReply) {
                    Log.i(TAG, response);
                }

            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();
                Log.e(TAG, "Server request response code = " + response);
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.i(TAG, "connection timeout = " + mConnectTimeout);
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
            connection.setRequestProperty("Content-Length", String.valueOf(paramStr.length()));
            connection.connect();

            if (paramStr.length() > 0) {
                DataOutputStream out = new DataOutputStream(connection.getOutputStream());
                out.writeBytes(paramStr);
                out.flush();
                out.close();
            }

            Integer responseCode = connection.getResponseCode();
            if (responseCode == HttpsURLConnection.HTTP_OK) {
                InputStream in = connection.getInputStream();
                response = getResponse(in);
                result = SecuXRequestOK;

                if (mLogReply) {
                    Log.i(TAG, response);
                }
            }else if (responseCode == HttpsURLConnection.HTTP_UNAUTHORIZED){
                response = "Unauthorized";
                result = SecuXRequestUnauthorized;
            }else{
                InputStream errIn = connection.getErrorStream();
                response = getResponse(errIn);
                //String errormsg = connection.getResponseMessage();
                Log.e(TAG, "Server request response code = " + response);
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
