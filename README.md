## Synopsis

The is a sample App for how to use GoldenSealSDK

## Code Example

### Configuration
To use this SDK, please add application name in your AndroidManifest.xml file

```xml
<meta-data android:name="com.goldenseal.sdk.ApplicationName"
            android:value="Login Sample" />
```

### Init the SDK
Use **GoldenSealSdk.init()** to init the SDK. In this method, the it will setup the SDK login and payment. After the setup, this method will try to consume the purchased item by using the **IPurchaseConsumer** call back.

```java

skuList = new ArrayList<String>();
skuList.add("sku1");
skuList.add("sku2");
skuList.add("sku3");

// purchaseConsumer use to call game server and consume the purchase
IPurchaseConsumer purchaseConsumer = new IPurchaseConsumer() {
    public boolean consume(Purchase purchase) {
        // TODO: call the game server to consume the SKU
        return true;
    }
};

// Init the SDK with SKU list and purchaseConsumer call back
GoldenSealSdk.init(this, skuList, purchaseConsumer, null);
```
### Sign-In 
In the splash loading activity, it is better to call **tryPreviousSignIn()** to sign in with previous user id.
```java
// Better to do this in Splash Activity
GoldenSealSdk.tryPreviousSignIn(context);
```
To trigger the sign in activity, please use **GoldenSealSdk.signIn()**. For example

```java
GoldenSealSdk.signIn(MainActivity.this, MainActivity.class);
```

### Get user info
```java
GoldenSealSdk.getUserID(new GoldenSealSdk.SDKIdentityHandler() {

    @Override
    public void handleIdentityID(String identityId) {
        clearUserInfo();
        userId = identityId;
        // We have successfully retrieved the user's identity. You can use the
        // user identity value to uniquely identify the user. For demonstration
        // purposes here, we will display the value in a text view.
                userIdTextView.setText(identityId);

        if (GoldenSealSdk.isUserSignedIn()) {

            userNameTextView.setText(GoldenSealSdk.getUserName());

                if (GoldenSealSdk.getUserImage() != null) {
                    userImageView.setImageBitmap(GoldenSealSdk.getUserImage());
                    }
                }
            }

    @Override
    public void handleError(Exception exception) {

        clearUserInfo();

        // We failed to retrieve the user's identity. Set unknown user identifier
        // in text view.
        userIdTextView.setText(unknownUserIdentityText);

        final Context context = MainActivity.this;

        if (context != null) {
           // show error
        }
    }
});
```

### Sign-Out
To sign out the user, please use **GoldenSealSdk.signOut()**. For example:

```java
GoldenSealSdk.signOut(MainActivity.this);
```
### Payment
To purchase an item, please use **GoldenSealSdk.purchase()** API to start the purchase flow. It will use Google Play API to purchase the item first, and then call the **IPurchaseConsumer**, which is passed in **GoldenSealSdk.init()** method, to cunsume the item in game server. If the cunsuming is success, this API will call Google Play API to consume the item.

```java
GoldenSealSdk.purchase(purchaseAct, userId, itemDescription, itemPrice, itemDescription, new IPurchaseResultListener(){
    public void onPurchaseSuccess(Purchase item){
        Toast.makeText(context, "Purchase success: "+item.getSku(), LENGTH_LONG).show();
    }

    public void onPurchaseFail(int errorCode, String message){
        Toast.makeText(context, message, LENGTH_LONG).show();
    }

    public void onConsumeResult(boolean success){
        if (success) {
            Toast.makeText(context, "consume success", LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "consume failed", LENGTH_LONG).show();
        }
    }
});
```

The **GoldenSealSdk.purchase()** will send result to the caller activity. So it need an **onActivityResult()** override method to process the result.
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    // Pass on the activity result to the helper for handling
    if (!GoldenSealSdk.onPurchaseActivityResult(requestCode, resultCode, data)) {
        // not handled, so handle it ourselves (here's where you'd
        // perform any handling of activity results not related to in-app
        // billing...
        super.onActivityResult(requestCode, resultCode, data);
    }
    else {
        Log.d(LOG_TAG, "onActivityResult handled by IABUtil.");
    }
}
```

## API Reference



