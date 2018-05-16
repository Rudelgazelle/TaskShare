package android_development.taskshare;

import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class UserLoginProviderSelectionActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    public static final String USER_LOGIN_PROVIDER_SELECTION_KEY = "UserLoginProviderSelection";

    ConstraintLayout mConstraintRootView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login_provider_selection);

        //SET ROOTVIEW TO BE USED IN THE SNACKBAR METHOD
        mConstraintRootView = (ConstraintLayout) findViewById(R.id.mConstraintRootView);

        //START SIGN-IN PROCEDURE
        signIn();
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
                        .setIsSmartLockEnabled(false) //TODO:SET THIS TO ENABLED AFTER DEBUGGING
                        .setLogo(R.mipmap.logo)
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
            Intent navigationActivityIntent = new Intent(UserLoginProviderSelectionActivity.this, NavigationActivity.class);
            startActivity(navigationActivityIntent);
            finish();
        }
    }

    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {

            //USER IS LOGGED IN, OPEN THE REGISTRATIONACTIVITY
            Intent navigationActivityIntent = new Intent(UserLoginProviderSelectionActivity.this, NavigationActivity.class);
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
            Log.e(USER_LOGIN_PROVIDER_SELECTION_KEY, "Sign-in error: ", response.getError());
        }
    }

    //generates a Snackbar based on the errorMessage
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mConstraintRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }

}
