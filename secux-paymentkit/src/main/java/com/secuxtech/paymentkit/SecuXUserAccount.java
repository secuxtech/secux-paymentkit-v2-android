package com.secuxtech.paymentkit;

import java.util.ArrayList;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-19
 */

public class SecuXUserAccount {
    public String mAccountName = "";
    public String mPassword = "";
    public String mEmail = "";
    public String mAlias = "";
    public String mPhoneNum = "";
    public String mUserType = "";

    public ArrayList<SecuXCoinAccount> mCoinAccountArr = new ArrayList<>();

    public SecuXUserAccount(String email, String phone, String password){
        mAccountName = email;
        mPassword = password;
        mEmail = email;
        mAlias = email.substring(0, email.indexOf('@'));
        mPhoneNum = phone;
    }

    public SecuXUserAccount(String email, String password){
        mAccountName = email;
        mPassword = password;
        mEmail = email;
        mAlias = email.substring(0, email.indexOf('@'));
    }
    
    public SecuXCoinAccount getCoinAccount(String coinType){

        for(int i=0; i<mCoinAccountArr.size(); i++){
            SecuXCoinAccount account = mCoinAccountArr.get(i);
            if (account.mCoinType.compareTo(coinType)==0){
                return account;
            }
        }
            
        return null;
    }
}
