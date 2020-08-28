package com.secuxtech.paymentkit;

import android.util.Log;

public class SecuXPaymentKitLogHandler{

    static SecuXPaymentKitLogHandlerCallback mLogCallback = null;

    static public void Log(String msg){

        if (msg!=null && msg.length()>0){
            Log.i("SecuXPaymentKit", msg);

            if (mLogCallback != null) {
                mLogCallback.logFromSecuXPaymentKit(msg);
            }
        }
    }

    static public void setCallback(SecuXPaymentKitLogHandlerCallback callback){
        mLogCallback = callback;
    }
}
