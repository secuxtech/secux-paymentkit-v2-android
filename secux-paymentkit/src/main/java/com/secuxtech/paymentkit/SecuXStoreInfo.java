package com.secuxtech.paymentkit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONObject;

public class SecuXStoreInfo {

    public String mCode = "";
    public String mName = "";
    public String mDevID = "";
    public Bitmap mLogo = null;
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

            storeInfoJson.remove("icon");
            mInfo = storeInfoJson.toString();

        }catch (Exception e){
            throw e;
        }
    }
}
