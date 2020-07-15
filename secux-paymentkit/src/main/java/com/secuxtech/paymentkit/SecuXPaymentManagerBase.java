package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-10
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.json.JSONObject;

/*
import com.secux.payment.sdk.BoxError;
import com.secux.payment.sdk.DiscoveredDevice;
import com.secux.payment.sdk.MachineIoControlParam;
import com.secux.payment.sdk.PaymentPeripheralManager;
import com.secux.payment.sdk.listener.OnConnectCompleteListener;
import com.secux.payment.sdk.listener.OnErrorListener;
import com.secux.payment.sdk.listener.OnGetDataMapCompleteListener;
import com.secux.payment.sdk.listener.OnScanCompleteListener;
import com.secux.payment.sdk.listener.OnSendStringCompleteListener;


 */



import com.secuxtech.paymentdevicekit.MachineIoControlParam;
import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXBLEManager;


import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static com.secuxtech.paymentdevicekit.PaymentPeripheralManager.SecuX_Peripheral_Operation_OK;
import static com.secuxtech.paymentdevicekit.PaymentPeripheralManager.SecuX_Peripheral_Operation_fail;
import static com.secuxtech.paymentkit.RestRequestHandler.TAG;

class PaymentInfo{
    String mCoinType;
    String mToken;
    String mAmount;
    String mDevID;
    String mIVKey;
}

class PaymentDevConfigInfo{
    String mName;
    int mScanTimeout;
    int mConnTimeout;
    int mRssi;
}

public class SecuXPaymentManagerBase {

    protected SecuXPaymentManagerCallback mCallback = null;
    protected Context mContext = null;

    protected SecuXServerRequestHandler mSecuXSvrReqHandler = new SecuXServerRequestHandler();
    protected PaymentPeripheralManager mPaymentPeripheralManager = new PaymentPeripheralManager(mContext, 10, -80, 30);

    private SecuXUserAccount mAccount = null;
    private PaymentInfo mPaymentInfo = new PaymentInfo();
    private PaymentDevConfigInfo mPaymentDevConfigInfo = new PaymentDevConfigInfo();

    private String mStoreInfo = "";
    private Bitmap mStoreLogo = null;
    private ArrayList<String> mStoreSupportedCoinTokenArr = new ArrayList<>();

    SecuXPaymentManagerBase(){
        Log.i(TAG, "SecuXPaymentManagerBase");
    }

    /*
    protected Integer getPaymentStoreInfo(String deviceID){
        Log.i(TAG, "getStoreInfo");

        mStoreInfo = "";
        mStoreLogo = null;

        Pair<Integer, String> response = this.mSecuXSvrReqHandler.getStoreInfo(deviceID);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK) {
            try {
                mStoreInfo = response.second;
                JSONObject storeInfoJson = new JSONObject(mStoreInfo);

                String base64String = storeInfoJson.getString("icon");
                String base64Image = base64String.split(",")[1];
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                mStoreLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                storeInfoJson.remove("icon");
                mStoreInfo = storeInfoJson.toString();

                return SecuXServerRequestHandler.SecuXRequestOK;
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }else{
            return response.first;
        }

        return SecuXServerRequestHandler.SecuXRequestFailed;
    }
    */

    protected void doPayment(SecuXUserAccount account, String storeInfo, String paymentInfo){

        Log.i(TAG, "doPayment");

        this.mAccount = account;

        if (getPaymentInfo(paymentInfo) && getPaymentDevConfigInfo(storeInfo)){
            Log.i(TAG, "pay to device " + mPaymentInfo.mDevID);
            handlePaymentStatus("Device connecting ...");

            Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(mContext, mPaymentDevConfigInfo.mScanTimeout,
                    mPaymentInfo.mDevID, mPaymentDevConfigInfo.mRssi, mPaymentDevConfigInfo.mConnTimeout);
            if (ret.first == SecuX_Peripheral_Operation_OK){
                mPaymentInfo.mIVKey = ret.second;
                sendInfoToDevice();
            }else{
                handlePaymentDone(false, ret.second);
            }

        }else {
            handlePaymentDone(false, "Wrong payment information");
        }

    }

    protected boolean getPaymentInfo(String paymentInfo){
        try {
            JSONObject jsonInfo = new JSONObject(paymentInfo);
            mPaymentInfo.mAmount = jsonInfo.getString("amount");
            mPaymentInfo.mDevID = jsonInfo.getString("deviceID");
            mPaymentInfo.mCoinType = jsonInfo.getString("coinType");
            mPaymentInfo.mToken = jsonInfo.getString("token");

        }catch (Exception e){
            return false;
        }
        return true;
    }

    protected boolean getPaymentDevConfigInfo(String storeInfo){
        try {
            JSONObject jsonInfo = new JSONObject(storeInfo);
            mPaymentDevConfigInfo.mName = jsonInfo.getString("name");
            mPaymentDevConfigInfo.mConnTimeout = jsonInfo.getInt("connectionTimeout");
            mPaymentDevConfigInfo.mScanTimeout = jsonInfo.getInt("scanTimeout");
            mPaymentDevConfigInfo.mRssi = jsonInfo.getInt("checkRSSI");

        }catch (Exception e){
            return false;
        }
        return true;
    }

    protected Pair<Integer, String> sendRefundOrRefillInfoToDevice(String info){

        Log.i(TAG, SystemClock.uptimeMillis() + " sendRefundOrRefillInfoToDevice " + info);
        Pair<Integer, String> ret = new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Unknown error");
        try {

            JSONObject payRetJson = new JSONObject(info);
            int statusCode = payRetJson.getInt("statusCode");
            String statusDesc = payRetJson.getString("statusDesc");

            if (statusCode != 200){
                mPaymentPeripheralManager.requestDisconnect();
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid status code!");
            }

            String ioControlParams = payRetJson.getString("machineControlParam");
            JSONObject ioCtrlParamJson = new JSONObject(ioControlParams);

            final MachineIoControlParam machineIoControlParam=new MachineIoControlParam();
            machineIoControlParam.setGpio1(ioCtrlParamJson.getString("gpio1"));
            machineIoControlParam.setGpio2(ioCtrlParamJson.getString("gpio2"));
            machineIoControlParam.setGpio31(ioCtrlParamJson.getString("gpio31"));
            machineIoControlParam.setGpio32(ioCtrlParamJson.getString("gpio32"));
            machineIoControlParam.setGpio4(ioCtrlParamJson.getString("gpio4"));
            machineIoControlParam.setGpio4c(ioCtrlParamJson.getString("gpio4c"));
            machineIoControlParam.setGpio4cCount(ioCtrlParamJson.getString("gpio4cCount"));
            machineIoControlParam.setGpio4cInterval(ioCtrlParamJson.getString("gpio4cInterval"));
            machineIoControlParam.setGpio4dOn(ioCtrlParamJson.getString("gpio4dOn"));
            machineIoControlParam.setGpio4dOff(ioCtrlParamJson.getString("gpio4dOff"));
            machineIoControlParam.setGpio4dInterval(ioCtrlParamJson.getString("gpio4dInterval"));
            machineIoControlParam.setUart(ioCtrlParamJson.getString("uart"));
            machineIoControlParam.setRunStatus(ioCtrlParamJson.getString("runStatus"));
            machineIoControlParam.setLockStatus(ioCtrlParamJson.getString("lockStatus"));

            String encryptedStr = payRetJson.getString("encryptedTransaction");
            final byte[] encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT);

            String transCode = payRetJson.getString("transactionCode");


            Pair<Integer, String> verifyRet = mPaymentPeripheralManager.doPaymentVerification(encryptedData, machineIoControlParam);
            if (verifyRet.first == SecuX_Peripheral_Operation_OK){

                ret = new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, transCode);
            }else{

                ret = new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, verifyRet.second);
            }


        }catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
            mPaymentPeripheralManager.requestDisconnect();

            ret = new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Parsing server reply exception!");
        }
        return ret;
    }

    protected void sendInfoToDevice(){

        Log.i(TAG, SystemClock.uptimeMillis() + " sendInfoToDevice amount=" + mPaymentInfo.mAmount);

        handlePaymentStatus(mPaymentInfo.mToken + " transferring...");
        Pair<Integer, String> payRet = mSecuXSvrReqHandler.doPayment(mAccount.mAccountName, mPaymentDevConfigInfo.mName, mPaymentInfo);
        if (payRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
            mPaymentPeripheralManager.requestDisconnect();
            handleAccountUnauthorized();
            return;
        }else if (payRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            mPaymentPeripheralManager.requestDisconnect();
            handlePaymentDone(false, payRet.second);
            return;
        }

        try {

            JSONObject payRetJson = new JSONObject(payRet.second);
            Log.i(TAG, SystemClock.uptimeMillis() + " Send server request done " + payRetJson.toString());

            int statusCode = payRetJson.getInt("statusCode");
            String statusDesc = payRetJson.getString("statusDesc");

            if (statusCode != 200){
                mPaymentPeripheralManager.requestDisconnect();
                handlePaymentDone(false, statusDesc);
                return;
            }

            String ioControlParams = payRetJson.getString("machineControlParam");
            JSONObject ioCtrlParamJson = new JSONObject(ioControlParams);

            final MachineIoControlParam machineIoControlParam=new MachineIoControlParam();
            machineIoControlParam.setGpio1(ioCtrlParamJson.getString("gpio1"));
            machineIoControlParam.setGpio2(ioCtrlParamJson.getString("gpio2"));
            machineIoControlParam.setGpio31(ioCtrlParamJson.getString("gpio31"));
            machineIoControlParam.setGpio32(ioCtrlParamJson.getString("gpio32"));
            machineIoControlParam.setGpio4(ioCtrlParamJson.getString("gpio4"));
            machineIoControlParam.setGpio4c(ioCtrlParamJson.getString("gpio4c"));
            machineIoControlParam.setGpio4cCount(ioCtrlParamJson.getString("gpio4cCount"));
            machineIoControlParam.setGpio4cInterval(ioCtrlParamJson.getString("gpio4cInterval"));
            machineIoControlParam.setGpio4dOn(ioCtrlParamJson.getString("gpio4dOn"));
            machineIoControlParam.setGpio4dOff(ioCtrlParamJson.getString("gpio4dOff"));
            machineIoControlParam.setGpio4dInterval(ioCtrlParamJson.getString("gpio4dInterval"));
            machineIoControlParam.setUart(ioCtrlParamJson.getString("uart"));
            machineIoControlParam.setRunStatus(ioCtrlParamJson.getString("runStatus"));
            machineIoControlParam.setLockStatus(ioCtrlParamJson.getString("lockStatus"));

            String encryptedStr = payRetJson.getString("encryptedTransaction");
            final byte[] encryptedData = Base64.decode(encryptedStr, Base64.DEFAULT);

            String transCode = payRetJson.getString("transactionCode");

            handlePaymentStatus("Device verifying...");

            /*
            mPaymentPeripheralManager.setOnGetDataMapCompleteListener(new OnGetDataMapCompleteListener() {
                @Override
                public void onComplete(final Map<String, Object> dataMap) {
                    handlePaymentDone(true, "");
                }
            });

             */

            android.util.Pair<Integer, String> verifyRet = mPaymentPeripheralManager.doPaymentVerification(encryptedData, machineIoControlParam);
            if (verifyRet.first == SecuX_Peripheral_Operation_OK){
                handlePaymentDone(true, transCode);
            }else{
                handlePaymentDone(false, verifyRet.second);
            }

            /*
            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                   mPaymentPeripheralManager.doPaymentVerification(encryptedData, machineIoControlParam);
                }
            });

             */

        }catch (Exception e){
            Log.e(TAG, e.getLocalizedMessage());
            mPaymentPeripheralManager.requestDisconnect();
            handlePaymentDone(false, mPaymentInfo.mCoinType.toString() + " transfer failed!");
        }
    }

    protected void handleGetStoreInfoDone(final boolean ret){
        Log.i(TAG, "Get store info. done " + String.valueOf(ret) + " " + mStoreInfo);
        if (mCallback!=null){
            mCallback.getStoreInfoDone(ret, mStoreInfo, mStoreLogo);
        }
    }

    protected void handlePaymentStatus(final String status){
        Log.i(TAG, "Payment status " + status);
        if (mCallback!=null){
            mCallback.updatePaymentStatus(status);
        }
    }

    protected void handlePaymentDone(final boolean ret, final String errorMsg){
        Log.i(TAG, "Payment done " + String.valueOf(ret) + " error:" + errorMsg);
        if (mCallback!=null){
            String transCode = "";
            String error = errorMsg;
            if (ret){
                transCode = errorMsg;
                error = "";
            }
            mCallback.paymentDone(ret, transCode, error);
        }
    }

    protected void handleAccountUnauthorized(){
        Log.i(TAG, "Account unauthorized!");
        if (mCallback!=null){
            mCallback.userAccountUnauthorized();
        }
    }

}
