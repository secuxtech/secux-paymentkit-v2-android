package com.secuxtech.paymentkit;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-12
 */

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SecuXCoinAccount {
    public String mCoinType = "";
    public String mAccountName = "";
    public String mAccountType = "";

    public Map<String, SecuXCoinTokenBalance> mTokenBalanceMap = new HashMap<>();

    SecuXCoinAccount(String accName, String coinType, String acctype, Map<String, SecuXCoinTokenBalance> symbolBalance){
        mAccountName = accName;
        mCoinType = coinType;
        mAccountType = acctype;
        mTokenBalanceMap.putAll(symbolBalance);
    }

    public boolean updateTokenBalance(String token, BigDecimal balance, BigDecimal formattedBalance, BigDecimal usdBalance){
        SecuXCoinTokenBalance accBalance = mTokenBalanceMap.get(token);
        if (accBalance != null) {
            accBalance.mBalance = balance;
            accBalance.mFormattedBalance = formattedBalance;
            accBalance.mUSDBalance = usdBalance;

            return true;
        }
        return false;
    }


    public SecuXCoinTokenBalance getBalance(String token){
        Set<Map.Entry<String, SecuXCoinTokenBalance>> entrySet = mTokenBalanceMap.entrySet();
        for (Map.Entry<String, SecuXCoinTokenBalance> entry: entrySet){
            if (entry.getKey().compareTo(token)==0){
                return entry.getValue();
            }
        }
        return null;
    }
}
