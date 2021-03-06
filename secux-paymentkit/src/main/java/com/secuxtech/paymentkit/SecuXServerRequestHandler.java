package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-10
 */

import android.util.Log;
import android.util.Pair;


import org.json.JSONObject;

import java.util.TimeZone;


public class SecuXServerRequestHandler extends RestRequestHandler {

    static String baseURL = "https://pmsweb-sandbox.secuxtech.com"; //"https://pmsweb-test.secux.io"; //https://pmsweb-sandbox.secuxtech.com";
    static String adminLoginUrl = baseURL + "/api/Admin/Login";
    static String registerUrl = baseURL + "/api/Consumer/Register";
    static String userLoginUrl = baseURL + "/api/Consumer/Login";
    static String changePwdUrl = baseURL + "/api/Consumer/ChangePassword";
    static String transferUrl = baseURL + "/api/Consumer/Transfer";
    static String balanceUrl = baseURL + "/api/Consumer/GetAccountBalance";
    static String balanceListUrl = baseURL + "/api/Consumer/GetAccountBalanceList";
    static String paymentUrl = baseURL + "/api/Consumer/Payment";
    static String fiatPaymentUrl = baseURL + "/api/Consumer/GetFiatPaymentUrl";
    static String checkFiatPaymentUrl = baseURL + "/api/Consumer/CheckFiatPaymentStatus";
    static String paymentHistoryUrl = baseURL + "/api/Consumer/GetPaymentHistory";
    static String getStoreUrl = baseURL + "/api/Terminal/GetStore";
    static String transferHistoryUrl = baseURL + "/api/Consumer/GetTxHistory";
    static String getDeviceInfoUrl = baseURL + "/api/Terminal/GetDeviceInfo";
    static String getSupportedSymbolUrl = baseURL + "/api/Terminal/GetSupportedSymbol";
    static String getChainAccountListUrl = baseURL + "/api/Consumer/GetChainAccountList";
    static String accountOperationUrl = baseURL + "/api/Consumer/BindingChainAccount";
    static String refundUrl = baseURL + "/api/Consumer/Refund";
    static String refillUrl = baseURL + "/api/Consumer/Refill";
    static String encryptPaymentDataUrl = baseURL + "/api/B2B/ProduceCipher";
    static String generateRawTransactionUrl = baseURL + "/api/Consumer/GenerateRawTransaction";
    static String dePayUrl = baseURL + "/api/Consumer/DePay";

    private static String mToken = "";

    private static String mAdminName = "";
    private static String mAdminPassword = "";

    public static void setServerURL(String svrUrl) {
        baseURL = svrUrl;
        adminLoginUrl = baseURL + "/api/Admin/Login";
        registerUrl = baseURL + "/api/Consumer/Register";
        userLoginUrl = baseURL + "/api/Consumer/Login";
        changePwdUrl = baseURL + "/api/Consumer/ChangePassword";
        transferUrl = baseURL + "/api/Consumer/Transfer";
        balanceUrl = baseURL + "/api/Consumer/GetAccountBalance";
        balanceListUrl = baseURL + "/api/Consumer/GetAccountBalanceList";
        paymentUrl = baseURL + "/api/Consumer/Payment";
        fiatPaymentUrl = baseURL + "/api/Consumer/GetFiatPaymentUrl";
        checkFiatPaymentUrl = baseURL + "/api/Consumer/CheckFiatPaymentStatus";
        paymentHistoryUrl = baseURL + "/api/Consumer/GetPaymentHistory";
        getStoreUrl = baseURL + "/api/Terminal/GetStore";
        transferHistoryUrl = baseURL + "/api/Consumer/GetTxHistory";
        getDeviceInfoUrl = baseURL + "/api/Terminal/GetDeviceInfo";
        getSupportedSymbolUrl = baseURL + "/api/Terminal/GetSupportedSymbol";
        getChainAccountListUrl = baseURL + "/api/Consumer/GetChainAccountList";
        accountOperationUrl = baseURL + "/api/Consumer/BindingChainAccount";
        refundUrl = baseURL + "/api/Consumer/Refund";
        refillUrl = baseURL + "/api/Consumer/Refill";
        encryptPaymentDataUrl = baseURL + "/api/B2B/ProduceCipher";

        SecuXPaymentKitLogHandler.Log("setBaseServer " + svrUrl);
    }

    public void setAdminAccount(String name, String password) {
        SecuXPaymentKitLogHandler.Log("setAdminAccount");

        mAdminName = name;
        mAdminPassword = password;
    }

    public String getAdminToken() {
        SecuXPaymentKitLogHandler.Log("getAdminToken");

        String adminName = "", adminPwd = "";
        if (mAdminName.length() > 0 && mAdminPassword.length() > 0) {
            adminName = mAdminName;
            adminPwd = mAdminPassword;
        } else {

            /*
            adminName = "secux_register";
            adminPwd = "!secux_register@123";
            if (SecuXServerRequestHandler.baseURL.compareToIgnoreCase("https://pmsweb.secuxtech.com") == 0){
                adminPwd = "168!Secux@168";
            }

             */

            return "";
        }

        try {
            JSONObject param = new JSONObject();
            param.put("account", adminName);
            param.put("password", adminPwd);
            Pair<Integer, String> response = this.processPostRequest(adminLoginUrl, param);

            if (response.first == SecuXRequestOK) {
                JSONObject responseJson = new JSONObject(response.second);
                String token = responseJson.getString("token");
                return token;
            }

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
        }

        return "";
    }

    public Pair<Integer, String> userRegister(SecuXUserAccount userAccount, String coinType, String token) {
        SecuXPaymentKitLogHandler.Log("userRegister");
        String adminToken = getAdminToken();

        if (adminToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            param.put("account", userAccount.mAccountName);
            param.put("password", userAccount.mPassword);
            param.put("email", userAccount.mEmail);
            param.put("alias", userAccount.mAlias);
            param.put("tel", userAccount.mPhoneNum);
            param.put("coinType", coinType);
            param.put("symbol", token);
            param.put("optional", "{}");

            Pair<Integer, String> result = this.processPostRequest(registerUrl, param, adminToken, 30000);

            return result;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> changePassword(String oldPwd, String newPwd) {
        SecuXPaymentKitLogHandler.Log("changePassword");

        try {
            JSONObject param = new JSONObject();
            param.put("password", oldPwd);
            param.put("newPassword", newPwd);
            Pair<Integer, String> response = this.processPostRequest(changePwdUrl, param, mToken);
            if (response.first == SecuXRequestOK) {
                //JSONObject responseJson = new JSONObject(response.second);
                //String token = responseJson.getString("token");
                //mToken = token;
            }

            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());

            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> userLogin(String account, String pwd) {
        SecuXPaymentKitLogHandler.Log("userLogin");

        try {
            JSONObject param = new JSONObject();
            param.put("account", account);
            param.put("password", pwd);
            Pair<Integer, String> response = this.processPostRequest(userLoginUrl, param);
            if (response.first == SecuXRequestOK) {
                JSONObject responseJson = new JSONObject(response.second);
                String token = responseJson.getString("token");
                mToken = token;
            }

            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());

            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> merchantLogin(String account, String pwd) {
        SecuXPaymentKitLogHandler.Log("merchantLogin");

        try {
            JSONObject param = new JSONObject();
            param.put("account", account);
            param.put("password", pwd);
            Pair<Integer, String> response = this.processPostRequest(adminLoginUrl, param);
            if (response.first == SecuXRequestOK) {
                JSONObject responseJson = new JSONObject(response.second);
                String token = responseJson.getString("token");
                mToken = token;
            }

            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());

            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> getSupportedCoinTokens() {
        SecuXPaymentKitLogHandler.Log("getSupportedCoinTokens");

        String adminToken = getAdminToken();

        if (adminToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        //return this.processPostRequest(getSupportedSymbolUrl);

        return this.processPostRequest(getSupportedSymbolUrl, null, adminToken);
    }

    public Pair<Integer, String> getChainAccountList() {
        SecuXPaymentKitLogHandler.Log("getChainAccountList");

        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        return this.processPostRequest(getChainAccountListUrl, null, mToken);
    }

    public Pair<Integer, String> getAccountBalance(String cointype, String token) {
        SecuXPaymentKitLogHandler.Log("getAccountBalance " + cointype + " " + token);

        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            //param.put("account", account);
            param.put("coinType", cointype);
            param.put("symbol", token);
            Pair<Integer, String> response = this.processPostRequest(balanceUrl, param, mToken);

            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    /*
    public Pair<Integer, String> getAccountBalance(){
        Log.i(TAG, "getAccountBalance ");

        if (mToken.length()==0){
            Log.e(TAG, "No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try{
            JSONObject param = new JSONObject();
            //param.put("account", account);
            Pair<Integer, String> response = this.processPostRequest(balanceListUrl, param, mToken);

            Log.i(TAG, response.second);
            return response;

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }


     */
    public Pair<Integer, String> getStoreInfo(String devID) {
        SecuXPaymentKitLogHandler.Log("getStoreInfo");

        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            param.put("deviceIDhash", devID);

            Pair<Integer, String> response = this.processPostRequest(getStoreUrl, param, mToken);

            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }

    }

    public Pair<Integer, String> doPayment(String sender, String storeName, PaymentInfo payInfo) {
        SecuXPaymentKitLogHandler.Log("doPayment");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();

        try {
            JSONObject param = new JSONObject();
            param.put("ivKey", payInfo.mIVKey);
            param.put("memo", "");
            param.put("symbol", payInfo.mToken);
            param.put("amount", payInfo.mAmount);
            param.put("coinType", payInfo.mCoinType);
            //param.put("account", sender);
            param.put("receiver", payInfo.mDevID);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            //SecuXPaymentKitLogHandler.Log(param.toString());

            Pair<Integer, String> response = this.processPostRequest(paymentUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> doPayment(PaymentInfo payInfo) {
        SecuXPaymentKitLogHandler.Log("doPayment");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();

        try {
            JSONObject param = new JSONObject();
            param.put("ivKey", payInfo.mIVKey);
            param.put("memo", "");
            param.put("symbol", payInfo.mToken);
            param.put("amount", payInfo.mAmount);
            param.put("coinType", payInfo.mCoinType);
            //param.put("account", sender);
            param.put("receiver", payInfo.mDevID);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            //SecuXPaymentKitLogHandler.Log(param.toString());

            Pair<Integer, String> response = this.processPostRequest(paymentUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> getFiatPaymentUrl(PaymentInfo payInfo) {
        SecuXPaymentKitLogHandler.Log("getFiatPaymentUrl");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();

        try {
            JSONObject param = new JSONObject();
            param.put("payChannel", payInfo.mPayChannel);
            param.put("amount", payInfo.mAmount);
            param.put("productName", payInfo.mProductName);
            param.put("deviceId", payInfo.mDevID);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            //SecuXPaymentKitLogHandler.Log(param.toString());

            Pair<Integer, String> response = this.processPostRequest(fiatPaymentUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> checkFiatPayment(PaymentInfo payInfo) {
        SecuXPaymentKitLogHandler.Log("doFiatPayment");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();

        try {
            JSONObject param = new JSONObject();
            param.put("ivKey", payInfo.mIVKey);
            param.put("payChannel", payInfo.mPayChannel);
            param.put("orderId", payInfo.mOrderId);
            param.put("deviceId", payInfo.mDevID);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            Pair<Integer, String> response = this.processPostRequest(checkFiatPaymentUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> generateRawTransaction(String sender, String deviceId, String coinType, String symbol, String amount) {
        SecuXPaymentKitLogHandler.Log("generateRawTransaction");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }
        try {
            JSONObject param = new JSONObject();
            param.put("sender", sender);
            param.put("receiver", deviceId);
            param.put("coinType", coinType);
            param.put("symbol", symbol);
            param.put("amount", amount);

            Pair<Integer, String> response = this.processPostRequest(generateRawTransactionUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> dePay(PaymentInfo payInfo) {
        SecuXPaymentKitLogHandler.Log("dePay");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }
        try {
            JSONObject param = new JSONObject();
            param.put("sender", payInfo.mSender);
            param.put("receiver", payInfo.mDevID);
            param.put("coinType", payInfo.mCoinType);
            param.put("symbol", payInfo.mToken);
            param.put("amount", payInfo.mAmount);
            param.put("ivKey", payInfo.mIVKey);
            param.put("paymentToken", payInfo.mPaymentToken);
            param.put("signHash", payInfo.mSignedMessage);

            Pair<Integer, String> response = this.processPostRequest(dePayUrl, param, mToken, 20000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> doTransfer(String cointype, String token, String feesymbol, String receiver, String amount) {
        SecuXPaymentKitLogHandler.Log("doTransfer");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            param.put("coinType", cointype);
            param.put("symbol", token);
            param.put("feeSymbol", feesymbol);
            //param.put("account", account);
            param.put("receiver", receiver);
            param.put("amount", amount);

            Pair<Integer, String> response = this.processPostRequest(transferUrl, param, mToken, 15000);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> getPaymentHistory(String token, String transactionCode) {
        SecuXPaymentKitLogHandler.Log("getPaymentHistory " + token + " " + transactionCode);
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject subparam = new JSONObject();
            subparam.put("offset", 0);
            subparam.put("limit", 10);
            subparam.put("sort", "transactionTime");
            subparam.put("order", "descending");

            JSONObject param = new JSONObject();
            param.put("keyword", transactionCode);
            param.put("startTime", "");
            param.put("endTime", "");
            param.put("payChannel", "");
            param.put("symbol", token);
            param.put("transactionStatus", "");
            param.put("params", subparam);

            Pair<Integer, String> response = this.processPostRequest(paymentHistoryUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> getPaymentHistory(String token, int pageIdx, int pageItemCount) {
        SecuXPaymentKitLogHandler.Log("getPaymentHistory");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject subparam = new JSONObject();
            subparam.put("offset", pageIdx);
            subparam.put("limit", pageItemCount);
            subparam.put("sort", "transactionTime");
            subparam.put("order", "descending");

            JSONObject param = new JSONObject();
            param.put("keyword", "");
            param.put("startTime", "");
            param.put("endTime", "");
            param.put("payChannel", "");
            param.put("symbol", token);
            param.put("transactionStatus", "");
            param.put("params", subparam);

            Pair<Integer, String> response = this.processPostRequest(paymentHistoryUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> getTransferHistory(String cointype, String symboltype, int page, int count) {
        SecuXPaymentKitLogHandler.Log("getTransferHistory");
        if (mToken.length() == 0) {
            Log.e(TAG, "No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            //param.put("account", account.mAccountName);
            param.put("coinType", cointype);
            param.put("symbol", symboltype);
            param.put("page", page);
            param.put("count", count);

            Pair<Integer, String> response = this.processPostRequest(transferHistoryUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    /*
    public Pair<Integer, String> getDeviceInfo(String coinType, String token, String amount, String deviceID){
        if (mToken.length()==0){
            Log.e(TAG, "No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try{
            JSONObject param = new JSONObject();
            //param.put("account", account.mAccountName);
            param.put("coinType", coinType);
            param.put("symbol", token);
            param.put("amount", amount);
            param.put("deviceID", deviceID);

            Pair<Integer, String> response = this.processPostRequest(getDeviceInfoUrl, param, mToken);
            return response;

        }catch (Exception e){
            Log.e(TAG, e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }
    */

    public Pair<Integer, String> getDeviceInfo(String paymentInfo) {
        SecuXPaymentKitLogHandler.Log("getDeviceInfo");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject(paymentInfo);
            Pair<Integer, String> response = this.processPostRequest(getDeviceInfoUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> accountOperation(String coinType, String accountName, String desc, String type) {
        SecuXPaymentKitLogHandler.Log("accountOperation");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            param.put("coinType", coinType);
            param.put("account", accountName);
            param.put("desc", desc);
            param.put("actionType", type);
            Pair<Integer, String> response = this.processPostRequest(accountOperationUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

    public Pair<Integer, String> refund(String devIDHash, String ivKey, String dataHash) {
        /*
            "deviceIDhash": "592e41d67ee326f82fd6be518fd488d752f5a1b9",
	        "ivKey": "GfJasMOeUhJ6PvJh",
	        "hashTx": "pM4IcNRf7NbaNQ9OPoxjwM3zt7heM4yffG+1TQiawus="
         */

        SecuXPaymentKitLogHandler.Log("refund");

        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();
        try {
            JSONObject param = new JSONObject();
            param.put("deviceIDhash", devIDHash);
            param.put("ivKey", ivKey);
            param.put("hashTx", dataHash);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            Log.i(TAG, "Refund param " + param.toString());
            Pair<Integer, String> response = this.processPostRequest(refundUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }

    }

    public Pair<Integer, String> refill(String devIDHash, String ivKey, String dataHash) {
        /*
            "deviceIDhash": "592e41d67ee326f82fd6be518fd488d752f5a1b9",
	        "ivKey": "GfJasMOeUhJ6PvJh",
	        "hashTx": "pM4IcNRf7NbaNQ9OPoxjwM3zt7heM4yffG+1TQiawus="
         */

        SecuXPaymentKitLogHandler.Log("refill");

        if (mToken.length() == 0) {
            Log.e(TAG, "No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        TimeZone tz = TimeZone.getDefault();
        try {
            JSONObject param = new JSONObject();
            param.put("deviceIDhash", devIDHash);
            param.put("ivKey", ivKey);
            param.put("hashTx", dataHash);
            param.put("timeZone", "" + tz.getRawOffset() / 1000);

            Log.i(TAG, "Refill param: " + param.toString());
            Pair<Integer, String> response = this.processPostRequest(refillUrl, param, mToken);
            return response;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }

    }

    public Pair<Integer, String> encryptPaymentData(String sender, String devID, String ivKey, String coin, String token, String transID, String amount, String memo, String timeZone) {
        SecuXPaymentKitLogHandler.Log("encryptPaymentData");
        if (mToken.length() == 0) {
            SecuXPaymentKitLogHandler.Log("No token");
            return new Pair<>(SecuXRequestFailed, "No token");
        }

        try {
            JSONObject param = new JSONObject();
            param.put("ivKey", ivKey);
            param.put("coinType", coin);
            param.put("symbol", token);
            param.put("sender", sender);
            param.put("deviceId", devID);
            param.put("transactionId", transID);
            param.put("amount", amount);
            param.put("memo", memo);
            param.put("timeZone", timeZone);


            Log.i(TAG, "encryptPaymentData param: " + param.toString());
            Pair<Integer, String> response = this.processPostRequest(encryptPaymentDataUrl, param, mToken);
            return response;

        } catch (Exception e) {
            SecuXPaymentKitLogHandler.Log(e.getMessage());
            return new Pair<>(SecuXRequestFailed, e.getLocalizedMessage());
        }
    }

}
