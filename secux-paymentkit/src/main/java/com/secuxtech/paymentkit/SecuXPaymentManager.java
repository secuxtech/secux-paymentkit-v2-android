package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-05
 */

import android.content.Context;

import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SecuXPaymentManager extends SecuXPaymentManagerBase{

    public void setSecuXPaymentManagerCallback(SecuXPaymentManagerCallback callback){
        this.mCallback = callback;
    }

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

    public void doPayment(Context context, final SecuXUserAccount account, final String storeInfo, final String paymentInfo){
        this.mContext = context;

        new Thread(new Runnable() {
            @Override
            public void run() {
                doPayment(account, storeInfo, paymentInfo);
            }
        }).start();

    }

    public Pair<Integer, String> getDeviceInfo(String paymentInfo){
        return this.mSecuXSvrReqHandler.getDeviceInfo(paymentInfo);
    }

    public Pair<Integer, String> getPaymentHistory(String token, int pageNum, int count, ArrayList<SecuXPaymentHistory> historyArr){
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
}
