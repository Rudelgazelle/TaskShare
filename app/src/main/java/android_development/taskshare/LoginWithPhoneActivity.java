package android_development.taskshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginWithPhoneActivity extends AppCompatActivity {

    public static final String LOGIN_WITH_PHONE_ACTIVITY_TAG = "LoginWithPhoneActivity";
    //Reverences for the View obejcts in the Activity
    private EditText etPhoneNumber;
    private EditText etCode;
    private LinearLayout linearLayoutCode;
    private ProgressBar prBarPhone;
    private ProgressBar prBarCode;
    private Button btnSend;

    //CALL BACK VARIABLE FOR THE PHONEATHPROVIDER
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    private FirebaseAuth mAuth;

    //VERIFICATION VARIABLES
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    //Helper variable to give the Send verification button a second method for Sending the Confirmation Code
    private int btnFunction = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_with_phone);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etPhoneNumber = (EditText) findViewById(R.id.etPhoneNumber);
        etCode = (EditText) findViewById(R.id.etCode);

        linearLayoutCode = (LinearLayout) findViewById(R.id.linearLayoutCode);
        linearLayoutCode.setVisibility(View.INVISIBLE);

        prBarPhone = (ProgressBar) findViewById(R.id.prBarPhone);
        prBarCode = (ProgressBar) findViewById(R.id.prBarCode);

        //Initialize Firebase Auth Object
        mAuth = FirebaseAuth.getInstance();

        btnSend = (Button) findViewById(R.id.btnSendVerification);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (btnFunction == 0) {

                    //Activate the progressbar and disable the textfield for the phonenumber
                    prBarPhone.setVisibility(View.VISIBLE);
                    etPhoneNumber.setEnabled(false);
                    //disable button, so that the method cannot be triggered again
                    btnSend.setEnabled(false);

                    String phoneNumber = etPhoneNumber.getText().toString();

                    //Check if Phone number has been provided
                    if (phoneNumber.equals("")) {
                        Toast.makeText(LoginWithPhoneActivity.this, "Please provide a phone number", Toast.LENGTH_LONG).show();
                    } else {

                        //Prompt PhoneAutchprovider to send a verification code via SMS
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                phoneNumber,
                                60,
                                TimeUnit.SECONDS,
                                LoginWithPhoneActivity.this,
                                mCallbacks
                        );
                    }
                } else {

                    //WHEN VERIFICATION CODE HAS BEEN SEND FROM THE SYSTEM THIS CODE IS EXECUTED WHEN CLICKING THE BUTTON
                    btnSend.setEnabled(false);
                    prBarCode.setVisibility(View.VISIBLE);

                    String mCode = etCode.getText().toString();

                    //CREATE THE LOGIN CREDENTIALS "BY HAND" AND LOGIN WITH THE SAME METHOD AS DONE WITH THE AUTOMATIC VERIFICATION
                    PhoneAuthCredential manualCredential = PhoneAuthProvider.getCredential(mVerificationId, mCode);
                    signInWithPhoneAuthCredential(manualCredential);
                }


            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

                //If code has been (automativally) retreaved by the phone, start signin process
                signInWithPhoneAuthCredential(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                Toast.makeText(LoginWithPhoneActivity.this, "failed to verify number " + e.getMessage(), Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(LOGIN_WITH_PHONE_ACTIVITY_TAG, "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                //WHEN THE CODE HAS BEEN SEND FROM THE SYSTEM, THE USER IS ENABLED TO SEND THE VARIFICATION CODE IF THE PHONE IS NOT DOING THIS AUTOMATICALLY
                btnFunction = 1;

                //HIDE THE PROGRESS BAR FOR PHONE AND MAKE LAYOUT FOR CODE VERIFICATION VISIBLE
                prBarPhone.setVisibility(View.INVISIBLE);
                linearLayoutCode.setVisibility(View.VISIBLE);
                btnSend.setText("Verify Code");
                btnSend.setEnabled(true);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String mUserDisplayName = mAuth.getCurrentUser().getDisplayName();
                            String mUserMail = mAuth.getCurrentUser().getDisplayName();

                            if (!mUserDisplayName.equals("") && (!mUserMail.equals(""))){

                                //USER IS LOGGED IN, OPEN THE REGISTRATIONACTIVITY
                                Intent navigationActivityIntent = new Intent(LoginWithPhoneActivity.this, NavigationActivity.class);
                                startActivity(navigationActivityIntent);
                                finish();

                            } else {
                                //USER IS LOGGED IN, OPEN THE REGISTRATIONACTIVITY
                                Intent registrationAddDataIntent = new Intent(LoginWithPhoneActivity.this, UserRegistrationAddData.class);
                                startActivity(registrationAddDataIntent);
                                finish();
                            }


                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w(LOGIN_WITH_PHONE_ACTIVITY_TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(LoginWithPhoneActivity.this, "The entered code is invalid", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }


}
