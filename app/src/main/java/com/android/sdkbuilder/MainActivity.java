package com.android.sdkbuilder;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.sdklibrary.admin.GoldenSealSdk;


public class MainActivity extends AppCompatActivity implements GoldenSealSdk.SDKSignInStateChangeListener{
    private static final String LOG_TAG = MainActivity.class.getSimpleName();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Init the SDK
        GoldenSealSdk.init(this);

        // Better to do this in Splash Activity
        GoldenSealSdk.tryPreviousSignIn(this);

        setContentView(R.layout.activity_main);

        initUserInfo();
        initGoldenSealSdkLogin();
    }

    private void initUserInfo() {
        userNameTextView = (TextView) findViewById(R.id.userName);
        userIdTextView = (TextView) findViewById(R.id.userId);
        userImageView = (ImageView) findViewById(R.id.userImage);

        fetchUserIdentity();
    }

    private void initGoldenSealSdkLogin() {


        final Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Main", "User sign-in");

                GoldenSealSdk.signIn(MainActivity.this, MainActivity.class);
            }
        });

        final Button signOutButton = (Button) findViewById(R.id.sign_out_button);
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
                            .setTitle(R.string.identity_demo_error_dialog_title)
                            .setMessage(getString(R.string.identity_demo_error_message_failed_get_identity)
                                    + exception.getMessage())
                            .setNegativeButton(R.string.identity_demo_dialog_dismiss_text, null)
                            .create()
                            .show();
                }
            }
        });
    }

    private void clearUserInfo() {

        clearUserImage();


        try {
            userNameTextView.setText(getString(R.string.unknown_user));
        } catch (final IllegalStateException e) {
            // This can happen when app shuts down and activity is gone
            Log.w(LOG_TAG, "Unable to reset user name back to default.");
        }

    }

    private void clearUserImage() {


        try {
            userImageView.setImageResource(R.mipmap.user);
        } catch (final IllegalStateException e) {
            // This can happen when app shuts down and activity is gone
            Log.w(LOG_TAG, "Unable to reset user image back to default image.");
        }

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
}
