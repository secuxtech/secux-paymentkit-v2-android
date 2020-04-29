package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-03
 */

import android.util.Log;

import androidx.core.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecuXAccountManager {

    private static String TAG = "secux-paymentkit";
    private SecuXServerRequestHandler mSecuXSvrReqHandler = new SecuXServerRequestHandler();

    public void setBaseServer(String url){
        SecuXServerRequestHandler.baseURL = url;
    }

    public Pair<Integer, String> registerUserAccount(SecuXUserAccount userAccount, String coinType, String token){
        Pair<Integer, String> response = mSecuXSvrReqHandler.userRegister(userAccount, coinType, token);

        if (response.first==SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONObject responseJson = new JSONObject(response.second);

                /*
                String coinType = responseJson.getString("coinType");
                String token = responseJson.getString("symbol");

                BigDecimal balance = new BigDecimal(responseJson.getString("balance"));
                BigDecimal formattedBalance = new BigDecimal(responseJson.getString("formattedBalance"));
                BigDecimal usdBlance = new BigDecimal(responseJson.getString("balance_usd"));

                SecuXCoinTokenBalance tokenBalance = new SecuXCoinTokenBalance(balance, formattedBalance, usdBlance);
                Map<String, SecuXCoinTokenBalance> tokenBalanceMap = new HashMap<>();
                tokenBalanceMap.put(token, tokenBalance);

                SecuXCoinAccount coinAccount = new SecuXCoinAccount(coinType, tokenBalanceMap);

                userAccount.mCoinAccountArr.add(coinAccount);
                */
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");

            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        return response;
    }

    public Pair<Integer, String> loginUserAccount(SecuXUserAccount userAccount){
        Pair<Integer, String>  response = mSecuXSvrReqHandler.userLogin(userAccount.mAccountName, userAccount.mPassword);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK) {
            try {
                JSONObject responseJson = new JSONObject(response.second);
                //String coinType = responseJson.getString("coinType");
                //String token = responseJson.getString("symbol");

                if (responseJson.getString("name")!=null){
                    userAccount.mAlias = responseJson.getString("name");
                }

                if (responseJson.getString("email")!=null){
                    userAccount.mEmail = responseJson.getString("email");
                }

                if (responseJson.getString("tel")!=null){
                    userAccount.mPhoneNum = responseJson.getString("tel");
                }

                if (responseJson.getString("userType")!=null){
                    userAccount.mUserType = responseJson.getString("userType");
                }

                /*
                BigDecimal balance = new BigDecimal(responseJson.getString("balance"));
                BigDecimal formattedBalance = new BigDecimal(responseJson.getString("formattedBalance"));
                BigDecimal usdBlance = new BigDecimal(responseJson.getString("balance_usd"));

                SecuXCoinTokenBalance tokenBalance = new SecuXCoinTokenBalance(balance, formattedBalance, usdBlance);
                Map<String, SecuXCoinTokenBalance> tokenBalanceMap = new HashMap<>();
                tokenBalanceMap.put(token, tokenBalance);

                SecuXCoinAccount coinAccount = new SecuXCoinAccount(coinType, tokenBalanceMap);

                userAccount.mCoinAccountArr.add(coinAccount);

                 */
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");

            } catch (Exception e) {
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        return response;
    }

    public Pair<Integer, String> getSupportedCointokenArray(List<Pair<String, String>> coinTokenList){
        Pair<Integer, String> response = this.mSecuXSvrReqHandler.getSupportedCoinTokens();
        if (response.first == SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONArray responseJsonArr = new JSONArray(response.second);
                for (int i=0; i<responseJsonArr.length(); i++){
                    JSONArray itemJsonArr = responseJsonArr.getJSONArray(i);

                    if (itemJsonArr.length() == 2){
                        coinTokenList.add(new Pair<String, String>(itemJsonArr.getString(0), itemJsonArr.getString(1)));
                    }else{
                        Log.i(TAG, "Invalid coin token info.");
                    }
                }

            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        if (coinTokenList.size() == 0){
            return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "No supported coin & token info. from server");
        }

        return response;
    }

    public Pair<Integer, String> getCoinAccountList(SecuXUserAccount userAccount){
        Pair<Integer, String> response = this.mSecuXSvrReqHandler.getChainAccountList();
        if (response.first == SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONObject responseJson = new JSONObject(response.second);
                JSONArray accountJsonArray = responseJson.getJSONArray("dataList");

                userAccount.mCoinAccountArr.clear();
                for(int i=0; i<accountJsonArray.length(); i++){
                    JSONObject itemJson = accountJsonArray.getJSONObject(i);

                    String accName = itemJson.getString("account");
                    String coinType = itemJson.getString("coinType");
                    JSONArray tokenJsonArr = itemJson.getJSONArray("symbol");
                    String action = itemJson.getString("actionType");

                    SecuXCoinTokenBalance zeroBalance = new SecuXCoinTokenBalance(new BigDecimal(0), new BigDecimal(0), new BigDecimal(0));
                    Map<String, SecuXCoinTokenBalance> tokenBalanceMap = new HashMap<>();
                    for(int j=0; j<tokenJsonArr.length(); j++){
                        tokenBalanceMap.put(tokenJsonArr.getString(j), zeroBalance);
                    }
                    SecuXCoinAccount coinAccount = new SecuXCoinAccount(accName, coinType, action, tokenBalanceMap);
                    userAccount.mCoinAccountArr.add(coinAccount);
                }

            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        return response;
    }

    public Pair<Integer, String> changePassword(String oldPwd, String newPwd) {
        return mSecuXSvrReqHandler.changePassword(oldPwd, newPwd);
    }

    public Pair<Integer, String> getAccountBalance(SecuXUserAccount userAccount, String coinType, String token){
        Pair<Integer, String>  response = this.mSecuXSvrReqHandler.getAccountBalance(coinType, token);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK) {
            try {
                JSONObject responseJson = new JSONObject(response.second);
                BigDecimal balance = new BigDecimal(responseJson.getString("balance"));
                BigDecimal formattedBalance = new BigDecimal(responseJson.getString("formattedBalance"));
                BigDecimal usdBlance = new BigDecimal(responseJson.getString("balance_usd"));
                String accName = responseJson.getString("accountName");

                SecuXCoinAccount coinAcc = userAccount.getCoinAccount(coinType);
                if (coinAcc != null) {
                    coinAcc.mAccountName = accName;
                    Boolean ret = coinAcc.updateTokenBalance(token, balance, formattedBalance, usdBlance);
                    if (ret){
                        return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");
                    }else{
                        return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid token type");
                    }
                }else{
                    return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid coin type");
                }

            } catch (Exception e) {
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        return response;
    }

    /*
    public Pair<Integer, String>  getAccountBalance(SecuXUserAccount userAccount){
        Pair<Integer, String>  response = this.mSecuXSvrReqHandler.getAccountBalance();
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK) {
            try {
                JSONArray responseJsonArr = new JSONArray(response.second);
                for (int i = 0; i < responseJsonArr.length(); i++) {
                    JSONObject itemJson = responseJsonArr.getJSONObject(i);
                    String cointype = itemJson.getString("coinType");
                    String token = itemJson.getString("symbol");
                    BigDecimal balance = new BigDecimal(itemJson.getString("balance"));
                    BigDecimal formattedBalance = new BigDecimal(itemJson.getString("formattedBalance"));
                    BigDecimal usdBlance = new BigDecimal(itemJson.getString("balance_usd"));
                    String accName = itemJson.getString("accountName");

                    SecuXCoinAccount coinAcc = userAccount.getCoinAccount(cointype);
                    if (coinAcc != null) {
                        coinAcc.mAccountName = accName;
                        if (!coinAcc.updateTokenBalance(token, balance, formattedBalance, usdBlance)){
                            SecuXCoinTokenBalance tokenBalance = new SecuXCoinTokenBalance(balance, formattedBalance, usdBlance);
                            coinAcc.mTokenBalanceMap.put(token, tokenBalance);
                        }
                    }else{
                        SecuXCoinTokenBalance tokenBalance = new SecuXCoinTokenBalance(balance, formattedBalance, usdBlance);
                        Map<String, SecuXCoinTokenBalance> tokenBalanceMap = new HashMap<>();
                        tokenBalanceMap.put(cointype, tokenBalance);

                        SecuXCoinAccount coinAccount = new SecuXCoinAccount(accName, cointype, "Add", tokenBalanceMap);
                        coinAccount.mAccountName = accName;
                        userAccount.mCoinAccountArr.add(coinAccount);
                    }
                }

                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");

            } catch (Exception e) {
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }
        return response;
    }
    */
    public Pair<Integer, String> doTransfer(String cointype, String token, String feeSymbol,
                                            String amount, String receiver, SecuXTransferResult transRet){

        Pair<Integer, String> response = mSecuXSvrReqHandler.doTransfer(cointype, token, feeSymbol, receiver, amount);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK){
            try{
                JSONObject transRetJson = new JSONObject(response.second);
                int statusCode = transRetJson.getInt("statusCode");
                String statusDesc = transRetJson.getString("statusDesc");

                if (statusCode != 200){
                    return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, statusDesc);
                }
                transRet.mTxID = transRetJson.getString("txId");
                transRet.mDetailsUrl = transRetJson.getString("detailsUrl");
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");
            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }
        return response;
    }

    public Pair<Integer, String> getTransferHistory(String cointype, String token,
                                                    int page, int count, ArrayList<SecuXTransferHistory> historyArr){

        Pair<Integer, String> response = mSecuXSvrReqHandler.getTransferHistory(cointype, token, page, count);
        if (response.first==SecuXServerRequestHandler.SecuXRequestOK){
            try {
                JSONArray responseJsonArr = new JSONArray(response.second);
                for (int i = 0; i < responseJsonArr.length(); i++) {
                    JSONObject itemJson = responseJsonArr.getJSONObject(i);
                    SecuXTransferHistory history = new SecuXTransferHistory(itemJson);
                    historyArr.add(history);
                }
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestOK, "");
            }catch (Exception e){
                return new Pair<>(SecuXServerRequestHandler.SecuXRequestFailed, "Invalid return value");
            }
        }

        return response;
    }



    /*
    public Map<String, Double> getCoinUSDRate(){
        Map<String, Double> rateMap = new HashMap<String, Double>();
        String ret = mSecuXSvrReqHandler.getCoinCurrency();
        try{
            JSONArray rateJsonArr = new JSONArray(ret);
            for(int i=0; i<rateJsonArr.length(); i++){
                JSONObject rateJsonObj = rateJsonArr.getJSONObject(i);

                @SecuXCoinType.CoinType String type = rateJsonObj.getString("coinType");
                Double rate = Double.valueOf(rateJsonObj.getString("usdPrice"));

                rateMap.put(type, rate);
            }
            return rateMap;
        }catch (Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return rateMap;
    }

    private boolean getDCTAccountBalance(SecuXAccount account, SecuXAccountBalance balance){
        if (account.mName.length()==0)
            return false;

        try{
            JSONObject param = new JSONObject();
            param.put("coinType", account.mCoinType.toString());
            param.put("pubKey", account.mName);

            String strRet = mSecuXSvrReqHandler.getAccountBalance(param);
            return handleAccountBalanceData(strRet, balance);

        }catch(Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return false;
    }

    private boolean getLBRAccountBalance(SecuXAccount account, SecuXAccountBalance balance){
        if (account.mName.length()==0)
            return false;

        try{
            JSONObject param = new JSONObject();
            param.put("coinType", account.mCoinType.toString());
            param.put("pubKey", account.mAddress);

            String strRet = mSecuXSvrReqHandler.getAccountBalance(param);
            return handleAccountBalanceData(strRet, balance);

        }catch(Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return false;
    }

    private boolean getDCTAccountHistory(SecuXAccount account, ArrayList<SecuXAccountHisotry> historyList){
        if (account.mName.length()==0)
            return false;

        try{
            JSONObject param = new JSONObject();
            param.put("coinType", account.mCoinType.toString());
            param.put("pubKey", account.mName);

            String strRet = mSecuXSvrReqHandler.getAccountHistory(param);
            return handleAccountHistoryData(strRet, historyList);
        }catch(Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return false;
    }

    private boolean getLBRAccountHistory(SecuXAccount account, ArrayList<SecuXAccountHisotry> historyList){
        if (account.mName.length()==0)
            return false;

        try{
            JSONObject param = new JSONObject();
            param.put("coinType", account.mCoinType.toString());
            param.put("pubKey", account.mAddress);

            String strRet = mSecuXSvrReqHandler.getAccountHistory(param);
            return handleAccountHistoryData(strRet, historyList);
        }catch(Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return false;
    }

    private boolean handleAccountBalanceData(String accBalanceStr, SecuXAccountBalance balance){
        try{
            JSONObject accBalanceJson = new JSONObject(accBalanceStr);
            balance.mBalance = accBalanceJson.getDouble("balance");
            balance.mFormatedBalance = accBalanceJson.getDouble("formattedBalance");
            balance.mUSDBalance = accBalanceJson.getDouble("balance_usd");
            return true;
        }catch (Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }
        return false;
    }

    private boolean handleAccountHistoryData(String accHistoryStr, ArrayList<SecuXAccountHisotry> histryList){
        try{
            JSONArray accHisJsonArr = new JSONArray(accHistoryStr);
            for (int i=0; i<accHisJsonArr.length(); i++){
                JSONObject historyItem = accHisJsonArr.getJSONObject(i);

                SecuXAccountHisotry history = new SecuXAccountHisotry();
                history.address = historyItem.getString("address");
                history.tx_type = historyItem.getString("tx_type");
                history.amount = historyItem.getDouble("amount");
                history.amount_symbol = historyItem.getString("amount_symbol");
                history.formatted_amount = historyItem.getDouble("formatted_amount");
                history.amount_usd = historyItem.getDouble("amount_usd");
                history.timestamp = historyItem.getString("timestamp");
                history.detailslUrl = historyItem.getString("detailsUrl");

                histryList.add(history);

            }
            return true;
        }catch (Exception e){
            Log.e("secux-paymentkit", e.getLocalizedMessage());
        }

        return false;
    }
    */
}
