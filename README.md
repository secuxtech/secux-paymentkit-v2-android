# SecuXPaymentKit

[![](https://jitpack.io/v/secuxtech/secux-paymentkit-android.svg)](https://jitpack.io/#secuxtech/secux-paymentkit-android)

## Usage

### Add JitPack repository

```java
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Add dependency secux-paymentkit-android

```java
dependencies {
    implementation 'com.github.secuxtech:secux-paymentkit-android:1.2.8'
}
```

### Add dependency secux-paymentdevicekit.aar

Download the [secux-peripheralkit-1.0.0.aar](https://github.com/secuxtech/secux-peripheralkit-android/tree/master/repository/com/secuxtech/secux-peripheralkit/1.0.0)

Copy the secux-paymentdevicekit.aar to ~/app/libs

Add dependency
```java
implementation fileTree(dir: 'libs', include: ['secux-paymentdevicekit.aar'])
```

### Add bluetooth privacy permissions

Add permission to the AndroidManifest.xml

    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>

Request permission

```java
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
```

### Import the the module

```java 
import com.secuxtech.paymentkit.*;
```

### Use SecuXAccountManager to do account operations

Must hava a user account to use the SecuX payment system. After successfully login the user account, the login session shall be validated for 30 minutes. If login session is timeout, relogin is required.

```java

private SecuXAccountManager mAccountManager = new SecuXAccountManager();

```

* User account registration

```java
SecuXUserAccount newAccount = new SecuXUserAccount("xxxxx", "+886-123456789", 
                                                   "12345678");

Pair<Integer, String> ret = mAccountManager.registerUserAccount(newAccount);
if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {
    showMessageInMain("Account registration successful!");
}else {
    showMessageInMain("registration failed! Error: " + ret.second);
}
```

* User account login

```java
SecuXUserAccount account = new SecuXUserAccount("maochuntest9@secuxtech.com", "", 
                                                "12345678");

Pair<Integer, String> ret = mAccountManager.loginUserAccount(mAccount);
if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
    showMessageInMain("Login successful!");
}else{
    showMessageInMain("Login failed! Error: " + ret.second);
}

```

* Get all coin/token balance

```java
ret = mAccountManager.getAccountBalance(mAccount);
if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
    for(int i=0; i<mAccount.mCoinAccountArr.size(); i++){
        SecuXCoinAccount coinAcc = mAccount.mCoinAccountArr.get(i);

        Set<Map.Entry<String, SecuXCoinTokenBalance>> entrySet = coinAcc.mTokenBalanceMap.entrySet();
        for (Map.Entry<String, SecuXCoinTokenBalance> entry: entrySet){
            String symbol = entry.getKey();
            SecuXCoinTokenBalance balance = entry.getValue();

            Log.i(TAG, "Symbol=" + symbol + " balance=" + balance.mFormattedBalance.toString() + " usdBalance=" + balance.mUSDBalance.toString());
        }
    }
}else if (ret.first== SecuXServerRequestHandler.SecuXRequestUnauthorized){
    //Need relogin
}else{
    showMessageInMain("Get account balance failed! Error: " + ret.second);
}
```

* Get a specified coin/token balance

```java
ret = mAccountManager.getAccountBalance(mAccount, "DCT", "SPC");
if (ret.first==SecuXServerRequestHandler.SecuXRequestOK){
    SecuXCoinAccount coinAcc = mAccount.getCoinAccount("DCT");
    SecuXCoinTokenBalance balance = coinAcc.getBalance("SPC");
    Log.i(TAG, "balance=" +  balance.mFormattedBalance.toString() + " usdBalance=" + balance.mUSDBalance.toString());

}else if (ret.first== SecuXServerRequestHandler.SecuXRequestUnauthorized){
    //Need relogin
}else{
    showMessageInMain("Get account balance failed! Error: " + ret.second);
}
```

### Use SecuXPaymentManager to do payment operations

* Implement SecuXPaymentManagerCallback functions

    **public void paymentDone(final boolean ret, final String transactionCode, final String errorMsg)**
    Called when payment is completed. Returns payment result and error message.
        
    **public void updatePaymentStatus(final String status)**
    Called when payment status is changed. Payment status are: "Device connecting...", "DCT transferring..." and "Device verifying..."
       
    **public void getStoreInfoDone(final boolean ret, final String storeInfo, final Bitmap storeLogo)**   
    Called when get store information is completed. Returns store info. string and store logo.
      
    
```java
 mPaymentManager.setSecuXPaymentManagerCallback(mPaymentMgrCallback);

 private SecuXPaymentManagerCallback mPaymentMgrCallback = new SecuXPaymentManagerCallback() {

        @Override
        public void paymentDone(final boolean ret, final String transactionCode, final String errorMsg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ret){
                        Toast toast = Toast.makeText(mContext, "Payment successful!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }else{
                        Toast toast = Toast.makeText(mContext, "Payment failed! Error: " + errorMsg, Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }
            });
        }

        @Override
        public void updatePaymentStatus(final String status){
            Log.i("secux-paymentkit-exp", "Update payment status: " + status);
        }

        @Override
        public void getStoreInfoDone(final boolean ret, final String storeInfo, final Bitmap storeLogo){
            Log.i("secux-paymentkit-exp", "Get store info. done ret=" + String.valueOf(ret) + ",name=" + storeName);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ret){
                        String storeName = "";
                        try{
                            JSONObject storeInfoJson = new JSONObject(mStoreInfo);
                            storeName = storeInfoJson.getString("name");
                        }catch (Exception e){
                        }

                    }else{
                        Toast toast = Toast.makeText(mContext, "Get store info. failed!", Toast.LENGTH_LONG);
                        toast.setGravity(Gravity.CENTER,0,0);
                        toast.show();
                    }
                }
            });
        }

    };
```

* Get store information

```java
mPaymentManager.getStoreInfo("f962639145992d7a710d33dcca503575eb85d759");
```

* Do payment

```java
 String mPaymentInfo = "{\"amount\":\"7\", \"coinType\":\"DCT:SPC\",\"deviceIDhash\":\"f962639145992d7a710d33dcca503575eb85d759\"}";

ret = mPaymentManager.getDeviceInfo(mPaymentInfo);
if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {

    mPaymentManager.doPayment(mContext, mAccount, storeInfo, mPaymentInfo);

}
```

* Get payment history

```java
ArrayList<SecuXPaymentHistory> payHisArr = new ArrayList<>();
int idx = 1;
int hisItemCount = 10;
while (true){
    int preHisItemCount = payHisArr.size();
    ret = mPaymentManager.getPaymentHistory("SPC", idx, hisItemCount, payHisArr);
    if (ret.first!=SecuXServerRequestHandler.SecuXRequestOK){
        showMessageInMain("Get payment history failed!");
        break;
    }else if (payHisArr.size() - preHisItemCount < hisItemCount){
        Log.i(TAG, "Get all history items");
        break;
    }
    idx += 1;
}
```

## Demo APP

Please find more in our [demo app](https://github.com/secuxtech/secux-paymentdemo-android)

## Author

SecuX, maochunsun@secuxtech.com

## License

SecuXPaymentKit is available under the MIT license. See the LICENSE file for more info.
