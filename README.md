# secux-paymentkit-v2

[![](https://jitpack.io/v/secuxtech/secux-paymentkit-v2-android.svg)](https://jitpack.io/#secuxtech/secux-paymentkit-v2-android)

## Requirements

* Minimum SDK Version: 24

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
    implementation 'com.github.secuxtech:secux-paymentkit-v2-android:1.0.3'
}
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

## Usage

### Server URL
Set server URL before using the APIs below

#### <u>Declaration</u>
```java
    void setBaseServer(String url)
```

#### <u>Parameters</u>
        Server URL. e.g. https://pmsweb-test.secux.io


### SecuXAccount related operations

Must hava a user account to use the SecuX payment system. After successfully login the user account, the login session shall be validated for 30 minutes. If login session is timeout, relogin is required.

Use SecuXAccountManager object to do the operations below
```java

private SecuXAccountManager mAccountManager = new SecuXAccountManager();

```

1. <b>Get supported coin/token</b>

#### <u>Declaration</u>
```java
    Pair<Integer, String> getSupportedCointokenArray(List<Pair<String, String>>     
                                                     coinTokenList)
```

#### <u>Parameters</u>
        An empty array list for returned coin/token pair list

#### <u>Return value</u>
```
    The first return value shows the operation result, if the result is  
    SecuXServerRequestHandler.SecuXRequestOK, the input coinTokenList parameter contains 
    all the supported coin and token pairs, otherwise the second return value contains 
    an error message.  
```

#### <u>Sample</u>
```java
    List<Pair<String, String>> coinTokenArray = new ArrayList<>();
    ret = mAccountManager.getSupportedCointokenArray(coinTokenArray);
    if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
        for (int i = 0; i < coinTokenArray.size(); i++) {
            Log.i(TAG, coinTokenArray.get(i).toString());
        }
    }
```

2. <b>User account registration</b>

#### <u>Declaration</u>
```java
    Pair<Integer, String> registerUserAccount(SecuXUserAccount userAccount, 
                                              String coinType, String token)
```

#### <u>Parameters</u>
```
    userAccount: A SecuXUserAccount object with name, password, phone number(optional)  
    coinType:    Coin type string  
    token:       Token string
```

#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK,
    registration is successful, otherwise the second return value contains an error message.
```

#### <u>Sample</u>

```java
    SecuXUserAccount newAccount = new SecuXUserAccount("xxxxx", "+886-123456789", 
                                                       "12345678");

    Pair<Integer, String> ret = mAccountManager.registerUserAccount(account, "DCT", "SPC");
    if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {
        showMessageInMain("Account registration successful!");
    }else {
        showMessageInMain("registration failed! Error: " + ret.second);
    }
```

3. <b>User account login</b>

Note: **Login session is valid for 30 minutes**. To continue use after 30 minutes, relogin is required.

#### <u>Declaration</u>
```java
    Pair<Integer, String> loginUserAccount(SecuXUserAccount userAccount)
```
#### <u>Parameter</u>
```
    userAccount: A SecuXUserAccount object with login name and password  
```

#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK,
    registration is successful, otherwise the second return value contains an error message.
```

#### <u>Sample</u>
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

4. <b>Get coin/token account list</b>  
Must successfully login the server before calling the function

#### <u>Declaration</u>
```java
    Pair<Integer, String> getCoinAccountList(SecuXUserAccount userAccount)
```
#### <u>Parameter</u>
```
    userAccount: Successfully logined user account.
```

#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK, 
    getting coin/token account information is successful and coin/token account 
    information is in the user account's coinAccountArray, otherwise the second return 
    value contains an error message.

    Note: if return result is SecuXRequestUnauthorized, the login session is timeout, 
    please relogin the system.
```

#### <u>Sample</u>

```java
    Pair<Integer, String> ret = mAccountManager.getCoinAccountList(account);
    if (ret.first == SecuXServerRequestHandler.SecuXRequestOK){
        for (int i = 0; i < mAccount.mCoinAccountArr.size(); i++) {
            SecuXCoinAccount coinAcc = mAccount.mCoinAccountArr.get(i);
            ...
        }
    }else if (ret.first== SecuXServerRequestHandler.SecuXRequestUnauthorized){
        //Need relogin
    }else{
        showMessageInMain("Get coin token account list failed! Error: " + ret.second);
    }
```
5. <b>Get coin/token account balance</b> 

#### <u>Declaration</u>
```java
    Pair<Integer, String> getAccountBalance(SecuXUserAccount userAccount, String 
                                            coinType, String token)
```
#### <u>Parameter</u>
```
    userAccount: A SecuXUserAccount object with login name and password  
    coinType:    CoinType string  
    token:       Token string
```

#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK, 
    getting coin/token account balance is successful and coin/token account balance can 
    be found in the user account's coinAccountArray, otherwise the second return value 
    contains an error message.

    Note: if return result is SecuXRequestUnauthorized, the login session is timeout, 
    please relogin the system.
```

```java
    for (int i = 0; i < mAccount.mCoinAccountArr.size(); i++) {
        SecuXCoinAccount coinAcc = mAccount.mCoinAccountArr.get(i);

        Set<Map.Entry<String, SecuXCoinTokenBalance>> entrySet = coinAcc.mTokenBalanceMap.entrySet();
        for (Map.Entry<String, SecuXCoinTokenBalance> entry : entrySet) {
            String token = entry.getKey();
            //SecuXCoinTokenBalance balance = entry.getValue();

            //Get account balance for a specified coin and token
            ret = mAccountManager.getAccountBalance(mAccount, coinAcc.mCoinType, token);
            if (ret.first == SecuXServerRequestHandler.SecuXRequestOK) {
                //SecuXCoinAccount coinAcc = mAccount.getCoinAccount("DCT");
                SecuXCoinTokenBalance balance = coinAcc.getBalance(token);

                Log.i(TAG, "Token=" + token + " balance=" + balance.mFormattedBalance.toString() + " usdBalance=" + balance.mUSDBalance.toString());

            } else {
                showMessageInMain("Get account balance failed! Error: " + ret.second);
            }
        }
    }
```


### SecuXPayment related operations

Use SecuXPaymentManager object to do the operations below

```java
    SecuXPaymentManager mPaymentManager = new SecuXPaymentManager();
```

1. <b>Implement SecuXPaymentManagerCallback functions</b>
```
    public void paymentDone(final boolean ret, final String transactionCode, final String errorMsg)
    - Called when payment is completed. Returns payment result and error message.
        
    public void updatePaymentStatus(final String status)
    - Called when payment status is changed. Payment status are: "Device connecting...", "DCT transferring..." and "Device verifying..."
       
    public void getStoreInfoDone(final boolean ret, final String storeInfo, final Bitmap storeLogo)
    - Called when get store information is completed. Returns store info. string and store logo.

    public void userAccountUnauthorized()
    - Called when login session is invalid or timeout, relogin is required when the 
    function is called.
```

2. <b>Parsing payment QRCode / NFC message</b>
#### <u>Declaration</u>
```java
    Pair<Integer, String> getDeviceInfo(String paymentInfo)
```
#### <u>Parameter</u>
```
    paymentInfo: Payment QRCode from P20/P22, or NFC string from P20/P22
```
#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK, 
    parsing payment information is successful, the second return value contains decoded 
    payment information in JSON format , otherwise the second return value contains an 
    error message.

    Note: if the first return result is SecuXRequestUnauthorized, 
    the login session is timeout, please relogin the system.

    Sample return JSON format
    {
        "amount": "10",
        "coinType": "DCT:SPC",
        "deviceID": "4ab10000726b",
        "deviceIDhash": "41193D32D520E114A3730D458F4389B5B9A7114D"
    }
    
    Note: "amount" and "coinType" are optional, QRCode from P20 will not generate 
    these items.
```
#### <u>Sample</u>
```java
    ret = mPaymentManager.getDeviceInfo(rawInfoString);
    if (ret.first==SecuXServerRequestHandler.SecuXRequestOK) {

        try{
            JSONObject replyJson = new JSONObject(ret.second);
            String devIDHash = replyJson.getString("deviceIDhash");
            String paymentInfo = ret.second;

        }catch (Exception e){
            Log.i(TAG, "Invalid store info "+ e.getLocalizedMessage());
        }
    }
```
3. <b>Get store information</b>
#### <u>Declaration</u>
```java
    void getStoreInfo(final String devID)
```
#### <u>Parameter</u>
```
    devID: Hashed device ID from getDeviceInfo function
```
#### <u>Remark</u>
```
    An asynchronized function. The result is in the callback function getStoreInfoDone
```
#### <u>Sample</u>
```java
    mPaymentManager.getStoreInfo("f962639145992d7a710d33dcca503575eb85d759");
```
4. <b>Do payment</b>
#### <u>Declaration</u>
```java
    doPayment(Context context, final SecuXUserAccount account, final String storeInfo, 
              final String paymentInfo)
```
#### <u>Parameter</u>
```
    context:     The current activity context.
    account:     The login user account.
    storeInfo:   Store information JSON string from getStoreInfoDone callback function.
    paymentInfo: Payment information JSON string from getDeviceInfo function.
```

#### <u>Remark</u>
```
    An asynchronized function. Payment progress info. and result is in the callback 
    functions.
```

#### <u>Callback functions sample</u>
```java
    mPaymentManager.setSecuXPaymentManagerCallback(mPaymentMgrCallback);

    private SecuXPaymentManagerCallback mPaymentMgrCallback 
                            = new SecuXPaymentManagerCallback() {

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

5. <b>Get all payment history</b>
#### <u>Declaration</u>
```java
    Pair<Integer, String> getPaymentHistory(String token, int pageNum, int count, 
                                            ArrayList<SecuXPaymentHistory> historyArr)  
```
#### <u>Parameter</u>
```
    token:          Payment token, can be empty
    pageIdx:        History item page index starts from 0, e.g. 0,1,2,3...
    pageItemCount:  Number of history items per request, e.g. 5, 10, 20 ... 
    historyArr:     An empty SecuXPaymentHistory list for returned history items
```

#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK, 
    SecuXPaymentHistory objects are in the input hisotryArr. If number of the history 
    objects in the return array less than the input pageItemCount, there is no 
    more history items. 

    Note: if return result is SecuXRequestUnauthorized, the login session is timeout, please relogin the system.
```
#### <u>Sample</u>

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

    for(int i=0; i<payHisArr.size(); i++){
        SecuXPaymentHistory history = payHisArr.get(i);
        Log.i(TAG, "Store = " + history.mStoreName + " CoinType =" + history.mCoinType +
                " amount=" + history.mAmount.toString() + history.mToken + " timestamp=" + history.mTransactionTime);
    }
```
6. <b>Get payment history from transaction code</b>
#### <u>Declaration</u>
```java
    Pair<Integer, String> getPaymentHistory(String token, String transactionCode, 
                                            SecuXPaymentHistory paymentHistory)
```
#### <u>Parameter</u>
```
    token:           Payment token, e.g. SPC, DCT
    transactionCode: Payment transaction code from SecuXPaymentManagerDelegate when 
                     payment done
    paymentHistory:  The history item
```
#### <u>Return value</u>
```
    The first return value shows the operation result. If the result is SecuXRequestOK, 
    payment history is in the input paymentHistory. Otherwise, the second return value 
    contains an error message.

    Note: if return result is SecuXRequestUnauthorized, the login session is timeout, please relogin the system.
```

## Demo APP

Please find more in our [demo app](https://github.com/secuxtech/secux-paymentdemo-v2-android)

## Author

SecuX, maochunsun@secuxtech.com

## License

SecuXPaymentKit is available under the MIT license. See the LICENSE file for more info.
