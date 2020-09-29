package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-05
 */

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;


import com.secuxtech.paymentdevicekit.MachineIoControlParam;
import com.secuxtech.paymentdevicekit.SecuXPaymentUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.secuxtech.paymentdevicekit.PaymentPeripheralManagerV1.SecuX_Peripheral_Operation_OK;
import static com.secuxtech.paymentkit.RestRequestHandler.SecuXRequestFailed;
import static com.secuxtech.paymentkit.RestRequestHandler.SecuXRequestOK;
import static com.secuxtech.paymentkit.RestRequestHandler.TAG;

public class SecuXPaymentManager extends SecuXPaymentManagerBase{

    public void setSecuXPaymentManagerCallback(SecuXPaymentManagerCallback callback){
        this.mCallback = callback;
    }

    /*
    public void getStoreInfo(final String devID){
        new Thread(new Runnable() {
            @Override
            public void run() {
            Integer ret = getPaymentStoreInfo(devID);
            if (ret == SecuXServerRequestHandler.SecuXRequestOK){
                handleGetStoreInfoDone(true);
            }else if (ret == SecuXServerRequestHandler.SecuXRequestUnauthorized){
                handleAccountUnauthorized();
            } else{
                handleGetStoreInfoDone(false);
            }
            }
        }).start();
    }

     */

    public Pair<Pair<Integer, String>, SecuXStoreInfo> getStoreInfo(String devIDHash){
        SecuXPaymentKitLogHandler.Log("getStoreInfo");
        Pair<Integer, String> response = this.mSecuXSvrReqHandler.getStoreInfo(devIDHash);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK) {
            try {
                SecuXStoreInfo storeInfo = new SecuXStoreInfo(new JSONObject(response.second));
                return new Pair<>(response, storeInfo);

            } catch (Exception e) {
                SecuXPaymentKitLogHandler.Log(e.getLocalizedMessage());

                return new Pair<>(new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Inavlid store info."), null);
            }
        }else{
            return new Pair<>(response, null);
        }
    }

    public void doPayment(Context context, final SecuXUserAccount account, final String storeInfo, final String paymentInfo){
        SecuXPaymentKitLogHandler.Log("doPayment");
        this.mContext = context;

        new Thread(new Runnable() {
            @Override
            public void run() {
                doPayment(account, storeInfo, paymentInfo);
            }
        }).start();

    }

    public void doPayment(final String nonce, Context context, final SecuXUserAccount account, final String storeInfo, final String paymentInfo){
        SecuXPaymentKitLogHandler.Log("doPayment wioth nonce");
        this.mContext = context;

        new Thread(new Runnable() {
            @Override
            public void run() {
                doPayment(nonce, account, storeInfo, paymentInfo);
            }
        }).start();

    }


    public Pair<Integer, String> getDeviceInfo(String paymentInfo){
        SecuXPaymentKitLogHandler.Log("getDeviceInfo");
        return this.mSecuXSvrReqHandler.getDeviceInfo(paymentInfo);
    }

    public Pair<Integer, String> getPaymentHistory(String token, int pageNum, int count, ArrayList<SecuXPaymentHistory> historyArr){
        SecuXPaymentKitLogHandler.Log("getPaymentHistory");
        Pair<Integer, String> ret = this.mSecuXSvrReqHandler.getPaymentHistory(token, pageNum, count);
        if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONArray hisJsonArr = new JSONArray(ret.second);
                for(int i=0; i<hisJsonArr.length(); i++){
                    JSONObject itemJson = hisJsonArr.getJSONObject(i);
                    SecuXPaymentHistory historyItem = new SecuXPaymentHistory(itemJson);
                    historyArr.add(historyItem);
                }
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");
            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }
        return ret;
    }

    public Pair<Integer, String> getPaymentHistory(String token, String transactionCode, SecuXPaymentHistory paymentHistory){
        SecuXPaymentKitLogHandler.Log("getPaymentHistory for a specified trans code");
        Pair<Integer, String> ret = this.mSecuXSvrReqHandler.getPaymentHistory(token, transactionCode);
        if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONArray hisJsonArr = new JSONArray(ret.second);
                for(int i=0; i<hisJsonArr.length(); i++){
                    JSONObject itemJson = hisJsonArr.getJSONObject(i);
                    paymentHistory.copyFrom(itemJson);
                    return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");
                }
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "No payment item");
            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }
        return ret;
    }


    public Pair<Integer, String> doRefund(Context context, String devID, String devIDHash){
        SecuXPaymentKitLogHandler.Log("doRefund " + devID);
        //return this.mSecuXSvrReqHandler.refund(devIDHash, ivKey, dataHash);
        this.mContext = context;
        String error = "";
        Pair<Integer, Pair<String, String>> ret = mPaymentPeripheralManager.getRefundRefillInfo(context, devID);
        if (ret.first == SecuX_Peripheral_Operation_OK){
            Pair<Integer, String> refundRet = this.mSecuXSvrReqHandler.refund(devIDHash, ret.second.second, ret.second.first);
            if (refundRet.first == SecuXServerRequestHandler.SecuXRequestOK){
                return sendRefundOrRefillInfoToDevice(refundRet.second);
            }else{
                error = refundRet.second;
            }
        }else{
            error = ret.second.first;
        }
        return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, error);
    }

    public Pair<Integer, String> doRefill(Context context, String devID, String devIDHash){
        SecuXPaymentKitLogHandler.Log("doRefill " + devID);
        //return this.mSecuXSvrReqHandler.refill(devIDHash, ivKey, dataHash);
        this.mContext = context;
        String error = "";
        Pair<Integer, Pair<String, String>> ret = mPaymentPeripheralManager.getRefundRefillInfo(context, devID);
        if (ret.first == SecuX_Peripheral_Operation_OK){
            Pair<Integer, String> refillRet = this.mSecuXSvrReqHandler.refill(devIDHash, ret.second.second, ret.second.first);
            if (refillRet.first == SecuXServerRequestHandler.SecuXRequestOK){
                return sendRefundOrRefillInfoToDevice(refillRet.second);
            }else{
                error = refillRet.second;
            }
        }else{
            error = ret.second.first;
        }
        return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, error);
    }

    public Pair<Integer, String> doRefund(String nonce, Context context, String devID, String devIDHash){
        SecuXPaymentKitLogHandler.Log("doRefund with nonce" + devID);
        //return this.mSecuXSvrReqHandler.refund(devIDHash, ivKey, dataHash);
        this.mContext = context;
        String error = "";
        byte[] code = SecuXPaymentUtility.hexStringToData(nonce);
        Pair<Integer, Pair<String, String>> ret = mPaymentPeripheralManager.getRefundRefillInfo(context, devID, code);
        if (ret.first == SecuX_Peripheral_Operation_OK){
            Pair<Integer, String> refundRet = this.mSecuXSvrReqHandler.refund(devIDHash, ret.second.second, ret.second.first);
            if (refundRet.first == SecuXServerRequestHandler.SecuXRequestOK){
                return sendRefundOrRefillInfoToDevice(refundRet.second);
            }else{
                error = refundRet.second;
            }
        }else{
            error = ret.second.first;
        }
        return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, error);
    }

    public Pair<Integer, String> doRefill(String nonce, Context context, String devID, String devIDHash){
        SecuXPaymentKitLogHandler.Log("doRefill with nonce" + devID);
        //return this.mSecuXSvrReqHandler.refill(devIDHash, ivKey, dataHash);
        this.mContext = context;
        String error = "";
        byte[] code = SecuXPaymentUtility.hexStringToData(nonce);
        Pair<Integer, Pair<String, String>> ret = mPaymentPeripheralManager.getRefundRefillInfo(context, devID, code);
        if (ret.first == SecuX_Peripheral_Operation_OK){
            Pair<Integer, String> refillRet = this.mSecuXSvrReqHandler.refill(devIDHash, ret.second.second, ret.second.first);
            if (refillRet.first == SecuXServerRequestHandler.SecuXRequestOK){
                return sendRefundOrRefillInfoToDevice(refillRet.second);
            }else{
                error = refillRet.second;
            }
        }else{
            error = ret.second.first;
        }
        return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, error);
    }

    public Pair<Integer, String> doActivity(Context context, String userID, String devID, String coin, String token, String transID, String amount, String nonce, String type){
        SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " doActivity " + devID);
        byte[] code = SecuXPaymentUtility.hexStringToData(nonce);
        //Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(code, context, 10,
        //                                                                devID, -75, 30);

        Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(context, 10,
                                                                    devID, -75, 30);
        if (ret.first == SecuX_Peripheral_Operation_OK){
            String ivKey = ret.second;

            Pair<Integer, String> encRet = mSecuXSvrReqHandler.encryptPaymentData(userID, devID, ivKey, coin, token, transID, amount, type);
            if (encRet.first == SecuXRequestOK){
                try {

                    JSONObject payRetJson = new JSONObject(encRet.second);
                    SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " Send server request done ");

                    int statusCode = payRetJson.getInt("statusCode");
                    String statusDesc = payRetJson.getString("statusDesc");

                    if (statusCode != 200){
                        mPaymentPeripheralManager.requestDisconnect();
                        return new Pair<>(SecuXRequestFailed, "Invalide reply status code " + statusCode);
                    }

                    String encryptedStr = payRetJson.getString("encryptedText");
                    final byte[] encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT);

                    //String transCode = payRetJson.getString("transactionCode");

                    android.util.Pair<Integer, String> verifyRet = mPaymentPeripheralManager.doPaymentVerification(encryptedData);
                    if (verifyRet.first == SecuX_Peripheral_Operation_OK){
                        return new Pair<>(SecuXRequestOK, "");
                    }else{
                        return new Pair<>(SecuXRequestFailed, verifyRet.second);
                    }


                }catch (Exception e){
                    SecuXPaymentKitLogHandler.Log(e.getLocalizedMessage());
                    mPaymentPeripheralManager.requestDisconnect();
                    return new Pair<>(SecuXRequestFailed, "Parsing encrypt data from server exception. " + encRet.second);
                }
            }

            return encRet;
        }

        return new Pair<>(SecuXRequestFailed, ret.second);

    }
}
