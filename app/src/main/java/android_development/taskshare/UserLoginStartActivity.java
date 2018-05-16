package android_development.taskshare;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import com.google.android.gms.auth.api.Auth;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Arrays;

public class UserLoginStartActivity extends AppCompatActivity {

    SignInButton googleSignInButton;
    Button btnLoginWithPhone;
    Button loginButton;
    Button registerButton;

    ConstraintLayout mRootView;

    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    private static final String TAG = "GoogleSignIn";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_start);


//<========================================{ INITIALIZATION METHODS }========================================>

        mRootView = (ConstraintLayout) findViewById(R.id.mRootView);


        // Declare sign-in button and set the dimensions
        googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.google_sign_in_button:
                        //sign in with the google account
                        signIn();
                        break;
                    // ...
                }
            }
        });

        btnLoginWithPhone = findViewById(R.id.btnLoginPhone);
        btnLoginWithPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userLoginWithPhoneActivityIntent = new Intent(UserLoginStartActivity.this, LoginWithPhoneActivity.class);
                UserLoginStartActivity.this.startActivity(userLoginWithPhoneActivityIntent);
            }
        });

        loginButton = findViewById(R.id.btnLoginEmailPassword);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent userLoginActivityIntent = new Intent(UserLoginStartActivity.this, UserLoginActivity.class);
                UserLoginStartActivity.this.startActivity(userLoginActivityIntent);
            }
        });

        registerButton = findViewById(R.id.btnRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open Register activity
                Intent userRegistrationActivityIntent = new Intent(UserLoginStartActivity.this, UserRegistrationActivity.class);
                UserLoginStartActivity.this.startActivity(userRegistrationActivityIntent);
            }
        });

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


//<========================================{ FIREBASE UI SIGN-IN }========================================>

    /************************************************************************************************
     * SIGN-IN INTENT:   This method creates an signin intent based on the available signin methods *
     ************************************************************************************************/
    public void signIn() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setTosUrl("https://superapp.example.com/terms-of-service.html")
                        .setPrivacyPolicyUrl("https://superapp.example.com/privacy-policy.html")
                        .setIsSmartLockEnabled(true)
                        .build(),
                RC_SIGN_IN);
    }

    /*******************************************************************************************************
     * ON ACTIVITY RESULT: This method receives the result of the intent and proceeds based on the outcome *
     *******************************************************************************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from the signIn method
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {

            //USER IS LOGGED IN, OPEN THE REGISTRATIONACTIVITY
            Intent navigationActivityIntent = new Intent(UserLoginStartActivity.this, NavigationActivity.class);
            startActivity(navigationActivityIntent);
            finish();
        }
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {

            //USER IS LOGGED IN, OPEN THE REGISTRATIONACTIVITY
            Intent navigationActivityIntent = new Intent(UserLoginStartActivity.this, NavigationActivity.class);
            startActivity(navigationActivityIntent);
            finish();

        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            showSnackbar(R.string.unknown_error);
            Log.e(TAG, "Sign-in error: ", response.getError());
        }
    }

    //generates a Snackbar based on the errorMessage
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
