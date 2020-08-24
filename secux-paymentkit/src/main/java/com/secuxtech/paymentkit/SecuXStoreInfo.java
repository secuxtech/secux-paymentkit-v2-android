package com.secuxtech.paymentkit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SecuXStoreInfo {

    public String mCode = "";
    public String mName = "";
    public String mDevID = "";
    public Bitmap mLogo = null;
    public ArrayList<Pair<String, String>> mCoinTokenArr = new ArrayList<>();
    public ArrayList<SecuXPromotion> mPromotionArr = new ArrayList<>();

    public String mInfo = "";

    SecuXStoreInfo(JSONObject storeInfoJson) throws Exception{
        try{
            String base64String = storeInfoJson.getString("icon");
            String base64Image = base64String.split(",")[1];
            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
            mLogo = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            mCode = storeInfoJson.getString("storeCode");
            mName = storeInfoJson.getString("name");
            mDevID = storeInfoJson.getString("deviceId");

            JSONArray coinTokenJsonArr = storeInfoJson.getJSONArray("supportedSymbol");
            for(int i=0; i<coinTokenJsonArr.length(); i++) {
                JSONArray itemJsonArr = coinTokenJsonArr.getJSONArray(i);
                if (itemJsonArr.length() == 2){
                    Pair<String, String> coinToken = new Pair<>(itemJsonArr.getString(0), itemJsonArr.getString(1));
                    this.mCoinTokenArr.add(coinToken);
                }
            }

            JSONArray promotionJsonArr = storeInfoJson.getJSONArray("supportedPromotion");
            for(int i=0; i<promotionJsonArr.length(); i++) {
                JSONObject itemJson = promotionJsonArr.getJSONObject(i);
                SecuXPromotion promotionItem = new SecuXPromotion(itemJson);
                this.mPromotionArr.add(promotionItem);
            }

            storeInfoJson.remove("icon");
            mInfo = storeInfoJson.toString();

        }catch (Exception e){
            throw e;
        }
    }

    public SecuXPromotion getPromotionDetails(String code){
        for(SecuXPromotion promotion : this.mPromotionArr){
            if (promotion.mCode.compareToIgnoreCase(code) == 0){
                return promotion;
            }
        }

        return null;
    }

    public class SecuXPromotion{
        public String mType = "";
        public String mCode = "";
        public String mName = "";
        public String mDesc = "";
        public Bitmap mImg = null;

        SecuXPromotion(JSONObject promotionJson){
            mType = promotionJson.optString("type");
            mCode = promotionJson.optString("code");
            mName = promotionJson.optString("name");
            mDesc = promotionJson.optString("description");
            String imgStr = promotionJson.optString("icon");
            byte[] decodedString = Base64.decode(imgStr, Base64.DEFAULT);
            mImg = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
    }
}
