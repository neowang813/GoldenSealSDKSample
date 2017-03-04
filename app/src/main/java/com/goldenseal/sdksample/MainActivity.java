package com.goldenseal.sdksample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import com.android.sdklibrary.admin.GoldenSealSdk;
import com.android.sdklibrary.admin.IPurchaseConsumer;
import com.android.sdklibrary.admin.IPurchaseResultListener;
import com.android.sdklibrary.admin.iab.Purchase;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

import static android.widget.Toast.LENGTH_LONG;


public class MainActivity extends AppCompatActivity implements GoldenSealSdk.SDKSignInStateChangeListener, AdapterView.OnItemSelectedListener {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static Pattern PATTERN = Pattern.compile("(.+) \\(\\$(.+)\\)");

    /**
     * Text view for showing the user identity.
     */
    private TextView userIdTextView;

    /**
     * Text view for showing the user name.
     */
    private TextView userNameTextView;

    /**
     * Image view for showing the user image.
     */
    private ImageView userImageView;

    private int itemPrice;
    private String itemDescription;
    private String userId;

    private List<String> skuList;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        skuList = new ArrayList<String>();
        skuList.add("cardboard");
        skuList.add("kindle");
        skuList.add("macbook");

        context = this;

        // purchaseConsumer use to call game service and consume the purchase
        IPurchaseConsumer purchaseConsumer = new IPurchaseConsumer() {
            public boolean consume(Purchase purchase) {
                CharSequence msg = "Game consuming " + purchase.getSku();
                Toast.makeText(context, msg, LENGTH_LONG).show();
                return true;
            }
        };

        // Init the SDK
        GoldenSealSdk.init(this, skuList, purchaseConsumer, null);

        // Better to do this in Splash Activity
        GoldenSealSdk.tryPreviousSignIn(this);

        setContentView(com.goldenseal.sdksample.R.layout.activity_main);

        initUserInfo();
        initGoldenSealSdkLogin();
        initGoldenSealSdkPayment();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initUserInfo() {
        userNameTextView = (TextView) findViewById(com.goldenseal.sdksample.R.id.userName);
        userIdTextView = (TextView) findViewById(com.goldenseal.sdksample.R.id.userId);
        userImageView = (ImageView) findViewById(com.goldenseal.sdksample.R.id.userImage);

        fetchUserIdentity();
    }

    private void initGoldenSealSdkLogin() {


        final Button signInButton = (Button) findViewById(com.goldenseal.sdksample.R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Main", "User sign-in");

                GoldenSealSdk.signIn(MainActivity.this, MainActivity.class);
            }
        });

        final Button signOutButton = (Button) findViewById(com.goldenseal.sdksample.R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Main", "User sign-out");

                GoldenSealSdk.signOut(MainActivity.this);
                // Show the sign-in button and hide the sign-out button.
                signOutButton.setVisibility(View.INVISIBLE);
                signInButton.setVisibility(View.VISIBLE);
                fetchUserIdentity();
            }
        });

        final boolean isUserSignedIn = GoldenSealSdk.isUserSignedIn();
        signOutButton.setVisibility(isUserSignedIn ? View.VISIBLE : View.INVISIBLE);
        signInButton.setVisibility(!isUserSignedIn ? View.VISIBLE : View.INVISIBLE);
    }

    private void initGoldenSealSdkPayment() {
        Spinner spinner = (Spinner) findViewById(com.goldenseal.sdksample.R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                com.goldenseal.sdksample.R.array.products_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Button checkoutButton = (Button) findViewById(com.goldenseal.sdksample.R.id.checkout_button);
        final Activity purchaseAct = this;
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Main", "User checkout");
                GoldenSealSdk.purchase(purchaseAct, userId, itemDescription, itemPrice, itemDescription, new IPurchaseResultListener(){
                    public void onPurchaseSuccess(Purchase var1){
                        Toast.makeText(context, "Purchase success: "+var1.getSku(), LENGTH_LONG).show();
                    }

                    public void onPurchaseFail(int var1, String var2){
                        Toast.makeText(context, var2, LENGTH_LONG).show();
                    }

                    public void onConsumeResult(boolean var1){
                        if (var1) {
                            Toast.makeText(context, "consume success", LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, "consume failed", LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }

    /**
     * Fetches the user identity safely on the background thread.  It may make a network call.
     */
    private void fetchUserIdentity() {
        Log.d(LOG_TAG, "fetchUserIdentity");

        // Pre-fetched to avoid race condition where fragment is no longer active.
        final String unknownUserIdentityText = "Unknow";

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
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(com.goldenseal.sdksample.R.string.identity_demo_error_dialog_title)
                            .setMessage(getString(com.goldenseal.sdksample.R.string.identity_demo_error_message_failed_get_identity)
                                    + exception.getMessage())
                            .setNegativeButton(com.goldenseal.sdksample.R.string.identity_demo_dialog_dismiss_text, null)
                            .create()
                            .show();
                }
            }
        });
    }

    private void clearUserInfo() {

        clearUserImage();


        try {
            userNameTextView.setText(getString(com.goldenseal.sdksample.R.string.unknown_user));
        } catch (final IllegalStateException e) {
            // This can happen when app shuts down and activity is gone
            Log.w(LOG_TAG, "Unable to reset user name back to default.");
        }

    }

    private void clearUserImage() {


        try {
            userImageView.setImageResource(com.goldenseal.sdksample.R.mipmap.user);
        } catch (final IllegalStateException e) {
            // This can happen when app shuts down and activity is gone
            Log.w(LOG_TAG, "Unable to reset user image back to default image.");
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String product = (String) parent.getItemAtPosition(position);

        Matcher matcher = PATTERN.matcher(product);
        matcher.find();
        this.itemDescription = matcher.group(1);
        this.itemPrice = (int) Math.round(Double.parseDouble(matcher.group(2).replace(",", "")) * 100);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public void onUserSignedIn() {
        // Update the user identity to account for the user signing in.
        fetchUserIdentity();
    }

    @Override
    public void onUserSignedOut() {
        // Update the user identity to account for the user signing out.
        fetchUserIdentity();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LOG_TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        // Pass on the activity result to the helper for handling
        if (GoldenSealSdk.onPurchaseActivityResult(requestCode, resultCode, data)) {
            Log.d(LOG_TAG, "onActivityResult handled by IABUtil.");
        }
        else if (GoldenSealSdk.onSignInActivityResult(requestCode, resultCode, data)){
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "SIGN IN SUCCESS", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "SIGN IN CANCEL", Toast.LENGTH_LONG).show();
            }
        }
        else {

            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
