package android_development.taskshare;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android_development.taskshare.FirebaseHelper.getDatabase;

/**
 * A login screen that offers login via email/password.
 */

//TODO: Cleanup the code after all fuctionality has been implemented.

public class UserRegistrationActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    //Variables for user registration
    String eMail;
    String password;
    String passwordRepeat;
    String userID;

    //Variables for dummy data creation
    String taskID;
    Date currentDate;
    Date taskDueDate;
    String content;
    Boolean mIsFavorite;

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    public FirebaseAuth.AuthStateListener authStateListener;

    FirebaseDatabase database;
    DatabaseReference dbRef;


    private static final String TAG = "UserRegistration";

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView tvUserMail;
    private EditText etUserPassword;
    private EditText etUserPasswordRepeat;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);
        setupActionBar();
        // Set up the login form.
        tvUserMail = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        etUserPassword = (EditText) findViewById(R.id.password);
        etUserPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        etUserPasswordRepeat = (EditText) findViewById(R.id.passwordRepeat);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //attemptLogin();
                // TODO: Check if the content of both password fields are the same. then process. otherwise prompt error
                //Register user and show progress spinner during creation

                //Check if both password fields have the same values
                password = etUserPassword.getText().toString();
                passwordRepeat = etUserPasswordRepeat.getText().toString();

                if (checkPasswordConsistency(password, passwordRepeat))  {
                    //if values match, register user and show progress spinner
                    registerUserWithEmailAndPassword();
                    showProgress(true);
                }
                else {
                    //if not prompt error message and set focus to pwRepeat field
                    Toast.makeText(UserRegistrationActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
                    etUserPasswordRepeat.requestFocus();
                }
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        //------------------------------------------------------------------------------------------
        // Listen for change in Authentication status
        //------------------------------------------------------------------------------------------

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //IF the login was successful and currentUser is not Null, add the dummy data in the Database based on mew user ID.
                userID = firebaseAuth.getUid();
            }
        };
        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide hide any login buttons */

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(authStateListener);

    }

    @Override
    protected void onStart() {
        super.onStart();
        //start the AuthstateListener on start
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //remove the authstatelistener on Stop
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(tvUserMail, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        tvUserMail.setError(null);
        etUserPassword.setError(null);

        // Store values at the time of the login attempt.
        String email = tvUserMail.getText().toString();
        String password = etUserPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            etUserPassword.setError(getString(R.string.error_invalid_password));
            focusView = etUserPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            tvUserMail.setError(getString(R.string.error_field_required));
            focusView = tvUserMail;
            cancel = true;
        } else if (!isEmailValid(email)) {
            tvUserMail.setError(getString(R.string.error_invalid_email));
            focusView = tvUserMail;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(UserRegistrationActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        tvUserMail.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                etUserPassword.setError(getString(R.string.error_incorrect_password));
                etUserPassword.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    /***********************************************************************************************
     * Boolean to check whether or not the password matches the repeated password
     **********************************************************************************************/
    public boolean checkPasswordConsistency(String pw, String pwRepeat) {
        //if both variables match bool is true, otherwise false

        if (password.equals(passwordRepeat)){
            return true;
        }
        return false;
    }

    public void registerUserWithEmailAndPassword(){

        //Fetch data from field an paste it into variables
        eMail = tvUserMail.getText().toString();
        password = etUserPassword.getText().toString();

        //Get Instance from Firebase Authentication and create new user
        mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(eMail, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            //----------------------------------------------------------------------
                            //After registration, login user and add dummy Task data to the Database and open Main Menu
                            //----------------------------------------------------------------------
                            loginUserWithMailAndPassword();
                            //----------------------------------------------------------------------

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(UserRegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });

    }

    public void loginUserWithMailAndPassword(){

        //--------------------------------------------------------------------------------------
        //Login user with mail and password
        //--------------------------------------------------------------------------------------
        mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(eMail, password)
                .addOnCompleteListener(UserRegistrationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Toast.makeText(UserRegistrationActivity.this, "Authentication successful.", Toast.LENGTH_SHORT).show();

                            //TODO: As long as currentUser = null, wait, when it is not null anymore, carry out method addDummyData;
                            addDummyData();

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(UserRegistrationActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
        //--------------------------------------------------------------------------------------
    }

    /***********************************************************************************************
     * Method for adding dummy data into taskdata to be able to display the data set on first login
     **********************************************************************************************/

    public void addDummyData(){

        //variables for the database reference
        database = getDatabase();
        dbRef = database.getReference();

        //Variables for the dummy data set
        taskID = "dummy_data_key";
        content = "Welcome to the TaskShare app!";
        currentDate = Calendar.getInstance().getTime();

        TaskData taskData = new TaskData(taskID, content, currentDate, taskDueDate, mIsFavorite);
        dbRef.child("taskdata").child(userID).child(taskID).setValue(taskData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                //after the database setValue activity has been completed, open the main Activity and hide progress spinner
                showProgress(false);
                openNavigationActivity();
            }
        });
    }

    /***********************************************************************************************
     * Method for opening the NavigationActivity
     **********************************************************************************************/
    public void openNavigationActivity(){

        Intent navigationActivityIntent = new Intent(UserRegistrationActivity.this, NavigationActivity.class);
        UserRegistrationActivity.this.startActivity(navigationActivityIntent);
    }
}

