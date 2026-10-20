package com.secuxtech.paymentkitexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import android.util.Pair;
import android.view.Gravity;
import android.widget.Toast;

import com.secuxtech.paymentkit.*;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
    private SecuXAccountManager mAccountManager = new SecuXAccountManager();
    private SecuXUserAccount    mAccount;

    private String mPaymentInfo = "{\"amount\":\"16.5\", \"coinType\":\"DCT\", \"deviceID\":\"41193D32D520E114A3730D458F4389B5B9A7114D\",\"token\":\"SPC\"}";
    private final Context mContext = this;

    private final static String TAG = "secux_paymentkit_exp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        // No automatic account or payment demo runs on launch.
        // Configure disposable test credentials explicitly before calling a demo action.
    }

    public boolean configureDemo(SecuXUserAccount account, String adminName, String adminPassword) {
        if (account == null || account.mAccountName == null || account.mAccountName.isEmpty()
                || account.mPassword == null || account.mPassword.isEmpty()
                || adminName == null || adminName.trim().isEmpty()
                || adminPassword == null || adminPassword.trim().isEmpty()) {
            return false;
        }
        mAccount = account;
        mAccountManager.setAdminAccount(adminName, adminPassword);
        return true;
    }

    public void testAccount() {
        if (mAccount == null) { return; }
        Pair<Integer, String> ret = mAccountManager.loginUserAccount(mAccount);

        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {


            List<Pair<String, String>> coinTokenArray = new ArrayList<>();
            ret = mAccountManager.getSupportedCointokenArray(coinTokenArray);
            if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
                for (int i = 0; i < coinTokenArray.size(); i++) {
                    Log.i(TAG, coinTokenArray.get(i).toString());
                }
            }

            //Get account all balance
            ret = mAccountManager.getCoinAccountList(mAccount);
            if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
                for (int i = 0; i < mAccount.mCoinAccountArr.size(); i++) {
                    SecuXCoinAccount coinAcc = mAccount.mCoinAccountArr.get(i);

                    Set<Map.Entry<String, SecuXCoinTokenBalance>> entrySet = coinAcc.mTokenBalanceMap.entrySet();
                    for (Map.Entry<String, SecuXCoinTokenBalance> entry : entrySet) {
                        String token = entry.getKey();
                        //SecuXCoinTokenBalance balance = entry.getValue();

                        //Get account balance for a specified coin and token
                        ret = mAccountManager.getAccountBalance(mAccount, coinAcc.mCoinType, token);
                        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
                            //SecuXCoinAccount coinAcc = mAccount.getCoinAccount("DCT");
                            SecuXCoinTokenBalance balance = coinAcc.getBalance(token);

                            Log.i(TAG, "Token=" + token + " balance=" + balance.mFormattedBalance.toString() + " usdBalance=" + balance.mUSDBalance.toString());

                        } else {
                            showMessageInMain("Get account balance failed! Error: " + ret.second);
                        }


                    }
                }
            } else {
                showMessageInMain("Get account balance failed! Error: " + ret.second);
            }

        }
    }

    public void testPayment(){
        if (mAccount == null) { return; }
        //Payment operations
        Pair<Integer, String>  ret = mAccountManager.loginUserAccount(mAccount);
        if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {

            SecuXPaymentHistory payhistory = new SecuXPaymentHistory();
            ret = mPaymentManager.getPaymentHistory("SPC", "49296508e4714a8dbdf16fd98dd21f10", payhistory);
            if (ret.first!=SecuXServerRequestHandler.SecuXRequestOK){
                showMessageInMain("Get payment history failed!");
            }

                //Get payment history
            ArrayList<SecuXPaymentHistory> payHisArr = new ArrayList<>();
            int idx = 0;
            int hisItemCount = 20;
            while (true){
                int preHisItemCount = payHisArr.size();
                ret = mPaymentManager.getPaymentHistory("SPC", idx, hisItemCount, payHisArr);
                if (ret.first!=SecuXServerRequestHandler.SecuXRequestOK){
                    showMessageInMain("Get payment history failed!");
                    break;
                }else if (payHisArr.size() - preHisItemCount < hisItemCount){
                    Log.i(TAG, "Get all history items");
                    break;
                }
                idx += 1;
            }

            for(int i=0; i<payHisArr.size(); i++){
                SecuXPaymentHistory history = payHisArr.get(i);
                Log.i(TAG, "Store = " + history.mStoreName + " CoinType =" + history.mCoinType +
                        " amount=" + history.mAmount.toString() + history.mToken + " timestamp=" + history.mTransactionTime);
            }

            //Must set the callback for the SecuXPaymentManager
            mPaymentManager.setSecuXPaymentManagerCallback(mPaymentMgrCallback);

            //User SecuXPaymentManager to get valid payment info. from the QRCode string;
            String paymentInfo = "{\"amount\":\"2\", \"coinType\":\"DCT:SPC\",\"deviceIDhash\":\"4afff62e0b314266d9e1b3a48158d56134331a9f\"}";
            ret = mPaymentManager.getDeviceInfo(paymentInfo);
            //ret = mPaymentManager.getDeviceInfo("{\"amount\":\"2\", \"coinType\":\"LBR:LBR\",\"deviceIDhash\":\"592e41d67ee326f82fd6be518fd488d752f5a1b9\"}");
            if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {

                try{
                    JSONObject replyJson = new JSONObject(ret.second);
                    String devID = replyJson.getString("deviceIDhash");
                    mPaymentInfo = ret.second;

                    //Use SecuXPaymentManager to get store info.
                    Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfoRet = mPaymentManager.getStoreInfo(devID);
                    if (storeInfoRet.first.first == SecuXServerRequestHandler.SecuXRequestOK) {
                        mPaymentManager.doPayment(mContext, mAccount, storeInfoRet.second.mInfo, ret.second);
                    }
                }catch (Exception e){
                    Log.i(TAG, "Invalid store info "+e.getLocalizedMessage());
                }
            }
        }
    }


    public void testRefundRefill() {
        if (mAccount == null) { return; }
        Pair<Integer, String> ret = mAccountManager.loginUserAccount(mAccount);
        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
            //mPaymentManager.doRefund(mContext, "811c000009c5", "592e41d67ee326f82fd6be518fd488d752f5a1b9");

            Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo("592e41d67ee326f82fd6be518fd488d752f5a1b9");
            mPaymentManager.doRefill(mContext, "811c000009c5", "592e41d67ee326f82fd6be518fd488d752f5a1b9");
        }
    }

    public void testSpringTreesAPIs(){
        if (mAccount == null) { return; }
        mAccountManager.setBaseServer("https://pmsweb-test.secux.io");
        Pair<Integer, String> ret = mAccountManager.loginMerchantAccount(mAccount.mAccountName, mAccount.mPassword);
        if (ret.first == SecuXServerRequestHandler.SecuXRequestOK){
            Pair<Pair<Integer, String>, SecuXStoreInfo> storeInfo = mPaymentManager.getStoreInfo("296ec7daa1dd7460d384471ec52127492bf45f9f");
            if (storeInfo.first.first == SecuXServerRequestHandler.SecuXRequestOK){
                Pair<Integer, String> encRet = mPaymentManager.doActivity(mContext, "secuxdemo", storeInfo.second.mDevID,
                        "LBR", "LBR", "Test1234", "1", "41395f47", "payment", "8");

                Log.i("", encRet.second);
            }
        }
    }

    private void showMessageInMain(final String msg){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });
    }

    //Callback for SecuXPaymentManager
    private SecuXPaymentManagerCallback mPaymentMgrCallback = new SecuXPaymentManagerCallback() {

        //Called when payment is completed. Returns payment result and error message.
        @Override
        public void paymentDone(final boolean ret, final String transactionCode, final String errorMsg) {
            if (ret){
                SecuXPaymentHistory payhistory = new SecuXPaymentHistory();
                Pair<Integer, String> hisret = mPaymentManager.getPaymentHistory("SPC", transactionCode, payhistory);
                if (hisret.first == SecuXServerRequestHandler.SecuXRequestOK) {

                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ret){
                        Toast toast = Toast.makeText(mContext, "Payment successful! " + transactionCode, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }else{
                        Toast toast = Toast.makeText(mContext, "Payment failed! Error: " + errorMsg, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }
            });

        }

        //Called when payment status is changed. Payment status are: "Device connecting...", "DCT transferring..." and "Device verifying..."
        @Override
        public void updatePaymentStatus(final String status){
            Log.i(TAG, "Update payment status: " + status);
        }


        //Called when the user login account token is timeout. Just login in the account again.
        @Override
        public void userAccountUnauthorized(){
        if (mAccount == null) { return; }
            Log.i(TAG, "account unauthorized login again");

            Pair<Integer, String> ret = mAccountManager.loginUserAccount(mAccount);

            if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
                Log.i(TAG, "Login successfully");
            }else{
                Log.i(TAG, "Login failed!");
            }
        }

    };
}
