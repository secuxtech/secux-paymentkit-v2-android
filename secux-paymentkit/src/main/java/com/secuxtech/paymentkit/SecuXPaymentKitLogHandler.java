package com.secuxtech.paymentkit;

import android.util.Log;

public class SecuXPaymentKitLogHandler{

    static SecuXPaymentKitLogHandlerCallback mLogCallback = null;

    static public void Log(String msg){
        Log.i("SecuXPaymentKit", msg);

        if (mLogCallback != null && msg!=null && msg.length()>0){
            mLogCallback.logFromSecuXPaymentKit(msg);
        }
    }

    static public void setCallback(SecuXPaymentKitLogHandlerCallback callback){
        mLogCallback = callback;
    }
}
