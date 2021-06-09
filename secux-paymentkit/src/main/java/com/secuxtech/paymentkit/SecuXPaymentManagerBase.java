package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-10
 */

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Numeric;

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



import com.google.gson.Gson;
import com.secuxtech.paymentdevicekit.MachineIoControlParam;
import com.secuxtech.paymentdevicekit.PaymentPeripheralManager;
import com.secuxtech.paymentdevicekit.SecuXPaymentUtility;


import java.math.BigInteger;
import java.util.ArrayList;

import static com.secuxtech.paymentdevicekit.PaymentPeripheralManager.SecuX_Peripheral_Operation_OK;
import static com.secuxtech.paymentkit.RestRequestHandler.TAG;

class PaymentInfo{
    String mCoinType;
    String mToken;
    String mAmount;
    String mDevID;
    String mIVKey;
    String mProductName;
    String mOrderId;
    String mPayChannel;
}

class PaymentDevConfigInfo{
    String mName;
    int mScanTimeout;
    int mConnTimeout;
    int mRssi;
}

class RawTransactionInfo{
    String mPaymentToken;
    BigInteger mNonce;
    BigInteger mGasPrice;
    BigInteger mGas;
    BigInteger mValue;
    String mTo;
    String mData;
}

public class SecuXPaymentManagerBase {

    protected SecuXPaymentManagerCallback mCallback = null;
    protected Context mContext = null;

    protected SecuXServerRequestHandler mSecuXSvrReqHandler = new SecuXServerRequestHandler();
    protected PaymentPeripheralManager mPaymentPeripheralManager = new PaymentPeripheralManager(mContext, 10, -80, 30);

    private SecuXUserAccount mAccount = null;
    private PaymentInfo mPaymentInfo = new PaymentInfo();
    private RawTransactionInfo mRawTransactionInfo = new RawTransactionInfo();
    private PaymentDevConfigInfo mPaymentDevConfigInfo = new PaymentDevConfigInfo();

    private String mStoreInfo = "";
    private Bitmap mStoreLogo = null;
    private ArrayList<String> mStoreSupportedCoinTokenArr = new ArrayList<>();
    public static boolean mIsCompleted = false;

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

    protected  void doDePay(String nonce, String address, String storeInfo, String privateKey, String paymentInfo){
        SecuXPaymentKitLogHandler.Log("doDePay");
        if (getPaymentInfo(paymentInfo) && getPaymentDevConfigInfo(storeInfo)){
            //generate raw transaction
            handlePaymentStatus("Generate transaction ...");
            Pair<Integer, String> payRet = mSecuXSvrReqHandler.generateRawTransaction(address, mPaymentInfo);
            if(payRet.first != SecuXServerRequestHandler.SecuXRequestOK){
                handlePaymentDone(false, payRet.second);
                return;
            }
            //signature
            getRawTransactionsInfo(payRet.second);
            SecuXPaymentKitLogHandler.Log("mRawTransactionInfo : " + new Gson().toJson(mRawTransactionInfo));
            // Create the Transaction
            RawTransaction rawTransaction = RawTransaction.createTransaction(
                    mRawTransactionInfo.mNonce,
                    mRawTransactionInfo.mGasPrice,
                    mRawTransactionInfo.mGas,
                    mRawTransactionInfo.mTo,
                    mRawTransactionInfo.mValue,
                    mRawTransactionInfo.mData);

            // Sign the Transaction
            handlePaymentStatus("Transaction signing ...");
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Credentials.create(privateKey));
//
            String hexValue = Numeric.toHexString(signedMessage);
            SecuXPaymentKitLogHandler.Log("signedMessage : " + hexValue);
            SecuXPaymentKitLogHandler.Log("pay to device " + mPaymentInfo.mDevID);
            handlePaymentStatus("Device connecting ...");
            byte[] code = SecuXPaymentUtility.hexStringToData(nonce);
            Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(code, mContext, mPaymentDevConfigInfo.mScanTimeout,
                    mPaymentInfo.mDevID, mPaymentDevConfigInfo.mRssi, mPaymentDevConfigInfo.mConnTimeout);
            if (ret.first == SecuX_Peripheral_Operation_OK){
                mPaymentInfo.mIVKey = ret.second;
                sendDePayInfoToDevice(address, hexValue);
            }else{
                handlePaymentDone(false, ret.second);
            }

        }else {
            handlePaymentDone(false, "Wrong payment information");
        }
    }

    protected void doPayment(SecuXUserAccount account, String storeInfo, String paymentInfo){

        SecuXPaymentKitLogHandler.Log("doPayment");

        this.mAccount = account;

        if (getPaymentInfo(paymentInfo) && getPaymentDevConfigInfo(storeInfo)){
            SecuXPaymentKitLogHandler.Log("pay to device " + mPaymentInfo.mDevID);
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

    protected void doPayment(String nonce, SecuXUserAccount account, String storeInfo, String paymentInfo){

        SecuXPaymentKitLogHandler.Log("doPayment");

        this.mAccount = account;

        if (getPaymentInfo(paymentInfo) && getPaymentDevConfigInfo(storeInfo)){
            SecuXPaymentKitLogHandler.Log("pay to device " + mPaymentInfo.mDevID);
            handlePaymentStatus("Device connecting ...");

            byte[] code = SecuXPaymentUtility.hexStringToData(nonce);
            Pair<Integer, String> ret = mPaymentPeripheralManager.doGetIVKey(code, mContext, mPaymentDevConfigInfo.mScanTimeout,
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

    protected void doPayment(String paymentInfo){

        SecuXPaymentKitLogHandler.Log("doPayment");

        if (getPaymentInfo(paymentInfo)){
            SecuXPaymentKitLogHandler.Log("pay to device " + mPaymentInfo.mDevID);

            handlePaymentStatus(mPaymentInfo.mToken + " transferring...");
            Pair<Integer, String> payRet = mSecuXSvrReqHandler.doPayment(mPaymentInfo);
            if (payRet.first == SecuXServerRequestHandler.SecuXRequestUnauthorized){
                //mPaymentPeripheralManager.requestDisconnect();
                handleAccountUnauthorized();
                return;
            }else if (payRet.first != SecuXServerRequestHandler.SecuXRequestOK){
                //mPaymentPeripheralManager.requestDisconnect();
                handlePaymentDone(false, payRet.second);
                return;
            }

            try {

                JSONObject payRetJson = new JSONObject(payRet.second);
                SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " Send server request done ");

                int statusCode = payRetJson.getInt("statusCode");
                String statusDesc = payRetJson.getString("statusDesc");

                if (statusCode != 200){
                    //mPaymentPeripheralManager.requestDisconnect();
                    handlePaymentDone(false, statusDesc);
                    return;
                }

                String transCode = payRetJson.getString("transactionCode");
                handlePaymentDone(true, transCode);

            }catch (Exception e){
                SecuXPaymentKitLogHandler.Log( e.getLocalizedMessage());
                //mPaymentPeripheralManager.requestDisconnect();
                handlePaymentDone(false, mPaymentInfo.mCoinType.toString() + " transfer failed!");
            }

        }else {
            handlePaymentDone(false, "Wrong payment information");
        }

    }

    protected boolean getRawTransactionsInfo(String rawTxInfo){
        try {
            SecuXPaymentKitLogHandler.Log(" getRawTransactionsInfo : " + rawTxInfo);
            JSONObject jsonInfo = new JSONObject(rawTxInfo);
            mRawTransactionInfo.mPaymentToken = jsonInfo.getString("paymentToken");
            JSONObject dataInfo = new JSONObject(jsonInfo.getString("data"));
            SecuXPaymentKitLogHandler.Log(" getRawTransactionsInfo　dataInfo : " + jsonInfo.getString("data"));
            mRawTransactionInfo.mNonce = Numeric.toBigInt(dataInfo.getString("nonce"));
            mRawTransactionInfo.mGasPrice = Numeric.toBigInt(dataInfo.getString("gasPrice"));
            mRawTransactionInfo.mGas = Numeric.toBigInt(dataInfo.getString("gas"));
            mRawTransactionInfo.mValue = Numeric.toBigInt(dataInfo.getString("value"));
            mRawTransactionInfo.mTo = dataInfo.getString("to");
            mRawTransactionInfo.mData = dataInfo.getString("data");
        }catch (Exception e){
            return false;
        }
        return true;
    }

    protected boolean getPaymentInfo(String paymentInfo){
        try {
            JSONObject jsonInfo = new JSONObject(paymentInfo);
            mPaymentInfo.mAmount = jsonInfo.getString("amount");
            mPaymentInfo.mDevID = jsonInfo.getString("deviceID");
            mPaymentInfo.mCoinType = jsonInfo.getString("coinType");
            mPaymentInfo.mToken = jsonInfo.getString("token");
            mPaymentInfo.mProductName = jsonInfo.getString("productName");
            mPaymentInfo.mPayChannel = jsonInfo.getString("payChannel");
//            mPaymentInfo.mOrderId = jsonInfo.getString("orderId");

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

        SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " sendRefundOrRefillInfoToDevice ");
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
            SecuXPaymentKitLogHandler.Log(e.getLocalizedMessage());
            mPaymentPeripheralManager.requestDisconnect();

            ret = new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Parsing server reply exception!");
        }
        return ret;
    }

    protected void sendDePayInfoToDevice(String address, String hexValue){
        SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " sendDePayInfoToDevice amount=" + mPaymentInfo.mAmount);
        handlePaymentStatus(mPaymentInfo.mToken + " transferring...");
        Pair<Integer, String> payRet = mSecuXSvrReqHandler.dePay(address, mRawTransactionInfo.mPaymentToken, hexValue, mPaymentInfo);
        if (payRet.first != SecuXServerRequestHandler.SecuXRequestOK){
            mPaymentPeripheralManager.requestDisconnect();
            handlePaymentDone(false, payRet.second);
            return;
        }

        try {

            JSONObject payRetJson = new JSONObject(payRet.second);
            SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " Send server request done ");

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

            android.util.Pair<Integer, String> verifyRet = mPaymentPeripheralManager.doPaymentVerification(encryptedData, machineIoControlParam);
            if (verifyRet.first == SecuX_Peripheral_Operation_OK){
                handlePaymentDone(true, transCode);
            }else{
                handlePaymentDone(false, verifyRet.second);
            }

        }catch (Exception e){
            SecuXPaymentKitLogHandler.Log( e.getLocalizedMessage());
            mPaymentPeripheralManager.requestDisconnect();
            handlePaymentDone(false, mPaymentInfo.mCoinType.toString() + " transfer failed!");
        }
    }
    protected void sendInfoToDevice(){

        SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " sendInfoToDevice amount=" + mPaymentInfo.mAmount);

        handlePaymentStatus(mPaymentInfo.mToken + " transferring...");
        Pair<Integer, String> payRet;
        if(mPaymentInfo.mCoinType.equalsIgnoreCase("TWD")){
            payRet = mSecuXSvrReqHandler.getFiatPaymentUrl(mPaymentInfo);
            try {
                JSONObject payRetJson = new JSONObject(payRet.second);

                mPaymentInfo.mOrderId = payRetJson.getString("orderId");
//                String url = "http://www.example.com";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(payRetJson.getString( "paymentUrl")));
                mContext.startActivity(intent);
                int time = 1;
                while(!mIsCompleted){
                    if(time > mPaymentDevConfigInfo.mConnTimeout){
                        payRet = new Pair<Integer, String>(SecuXServerRequestHandler.SecuXRequestFailed, "consumer payment time out");
                        throw new InterruptedException("consumer payment time out");
                    }
                    Thread.sleep(1*1000);
                    time++;
                }
                payRet = mSecuXSvrReqHandler.checkFiatPayment(mPaymentInfo);
            } catch (JSONException | InterruptedException e) {
                e.printStackTrace();
                handlePaymentDone(false, payRet.second);
            }
        }else {
            payRet = mSecuXSvrReqHandler.doPayment(mAccount.mAccountName, mPaymentDevConfigInfo.mName, mPaymentInfo);
        }
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
            SecuXPaymentKitLogHandler.Log(SystemClock.uptimeMillis() + " Send server request done ");

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
            SecuXPaymentKitLogHandler.Log( e.getLocalizedMessage());
            mPaymentPeripheralManager.requestDisconnect();
            handlePaymentDone(false, mPaymentInfo.mCoinType.toString() + " transfer failed!");
        }
    }


    protected void handlePaymentStatus(final String status){
        SecuXPaymentKitLogHandler.Log("Payment status " + status);
        if (mCallback!=null){
            mCallback.updatePaymentStatus(status);
        }
    }

    protected void handlePaymentDone(final boolean ret, final String errorMsg){
        SecuXPaymentKitLogHandler.Log("Payment done " + String.valueOf(ret) + " msg:" + errorMsg);
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
        SecuXPaymentKitLogHandler.Log("Account unauthorized!");
        if (mCallback!=null){
            mCallback.userAccountUnauthorized();
        }
    }

}
