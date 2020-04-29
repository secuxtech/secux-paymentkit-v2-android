package com.secuxtech.paymentkit;

import java.math.BigDecimal;

/**
 * Created by maochuns.sun@gmail.com on 2020-02-19
 */
public class SecuXCoinTokenBalance {

    public BigDecimal mBalance;
    public BigDecimal mFormattedBalance;
    public BigDecimal mUSDBalance;

    SecuXCoinTokenBalance(BigDecimal balance, BigDecimal formattedBalance, BigDecimal usdBalance){
        mBalance = balance;
        mFormattedBalance = formattedBalance;
        mUSDBalance = usdBalance;
    }

}
