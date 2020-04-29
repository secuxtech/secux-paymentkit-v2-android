package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-10
 */

import android.graphics.Bitmap;

public abstract class SecuXPaymentManagerCallback {

    public void paymentDone(final boolean ret, final String transactionCode, final String errorMsg){

    }

    public void updatePaymentStatus(final String status){

    }

    public void getStoreInfoDone(final boolean ret, final String storeInfo, final Bitmap storeLogo){

    }

    public void userAccountUnauthorized(){

    }

}
