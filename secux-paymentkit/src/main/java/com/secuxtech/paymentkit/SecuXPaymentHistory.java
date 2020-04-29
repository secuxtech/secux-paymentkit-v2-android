package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-19
 */

import org.json.JSONObject;

import java.math.BigDecimal;

public class SecuXPaymentHistory {
    public Integer      mID                 = -1;
    public Integer      mStoreID            = -1;
    public String       mStoreName          = "";
    public String       mStoreTel           = "";
    public String       mStoreAddress       = "";

    public String       mUserAccountName    = "";
    public String       mTransactionCode    = "";
    public String       mTransactionType    = "";

    public String       mPayPlatform        = "";
    public String       mPayChannel         = "";
    public String       mCoinType           = "";
    public String       mToken              = "";
    public BigDecimal   mAmount             = null;
    public String       mTransactionStatus  = "";
    public String       mTransactionTime    = "";
    public String       mRemark             = "";
    public String       mDetailsUrl         = "";

    public SecuXPaymentHistory(){

    }

    SecuXPaymentHistory(JSONObject hisJson) throws Exception{
        try{
            mID = hisJson.getInt("id");
            mStoreID = hisJson.getInt("storeID");
            mStoreName = hisJson.getString("storeName");
            mStoreTel = hisJson.getString("storeTel");
            mStoreAddress = hisJson.getString("storeAddress");

            mUserAccountName = hisJson.getString("account");
            mTransactionCode = hisJson.getString("transactionCode");
            mTransactionType = hisJson.getString("transactionType");

            mPayPlatform = hisJson.getString("payPlatform");
            mPayChannel = hisJson.getString("payChannel");
            mCoinType = hisJson.getString("coinType");
            mToken = hisJson.getString("symbol");
            mAmount = new BigDecimal(hisJson.getString("amount"));
            mTransactionStatus = hisJson.getString("transactionStatus");
            mTransactionTime = hisJson.getString("transactionTime");
            mRemark = hisJson.getString("remark");
            mDetailsUrl = hisJson.getString("detailsUrl");

        }catch (Exception e){
            throw e;
        }
    }

    void copyFrom(JSONObject hisJson) throws Exception {
        try {
            mID = hisJson.getInt("id");
            mStoreID = hisJson.getInt("storeID");
            mStoreName = hisJson.getString("storeName");
            mStoreTel = hisJson.getString("storeTel");
            mStoreAddress = hisJson.getString("storeAddress");

            mUserAccountName = hisJson.getString("account");
            mTransactionCode = hisJson.getString("transactionCode");
            mTransactionType = hisJson.getString("transactionType");

            mPayPlatform = hisJson.getString("payPlatform");
            mPayChannel = hisJson.getString("payChannel");
            mCoinType = hisJson.getString("coinType");
            mToken = hisJson.getString("symbol");
            mAmount = new BigDecimal(hisJson.getString("amount"));
            mTransactionStatus = hisJson.getString("transactionStatus");
            mTransactionTime = hisJson.getString("transactionTime");
            mRemark = hisJson.getString("remark");
            mDetailsUrl = hisJson.getString("detailsUrl");

        } catch (Exception e) {
            throw e;
        }
    }
}
