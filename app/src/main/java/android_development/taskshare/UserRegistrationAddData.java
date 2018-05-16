package android_development.taskshare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRegistrationAddData extends AppCompatActivity {

    public static final String USER_REGISTRATION_ADD_DATA_TAG = "UserRegistrationAddData";

    //View objects
    EditText etName;
    EditText etMail;
    Button btnSave;
    ProgressBar progressbar;

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration_add_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        etName = (EditText) findViewById(R.id.etName);
        etMail = (EditText) findViewById(R.id.etMail);
        btnSave = (Button) findViewById(R.id.btnSave);
        progressbar = (ProgressBar) findViewById(R.id.progressBar);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //EXECUTE SAVE METHOD ON CLICK
                saveUserData();
            }
        });
    }

    public void saveUserData(){

        if (currentUser != null){

            //DEACTIVATE THE EDITTET FIELDS AND SHOW PROGRESS BAR
            etName.setEnabled(false);
            etMail.setEnabled(false);
            progressbar.setVisibility(View.VISIBLE);

            //Retrieve the values of the editText fields
            String mUserName = etName.getText().toString();
            String mUserMail = etMail.getText().toString();

            String mUserId = currentUser.getUid();
            String mUserPhone = currentUser.getPhoneNumber();
            String mUserProfile = "empty";
            Long mUserHash = 1L;


            //update the users mail based on the provided email
            currentUser.updateEmail(mUserMail);

            //UPDATE THE USERS PROFILE (DISPLAYNAME) BASED ON PROVIDED DATA
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(mUserName)
                    .build();

            currentUser.updateProfile(profileUpdates);

            //STORE THE USERS DATASET IN A SEPARATE DATABASE
            UserData user = new UserData(mUserId, mUserName, mUserMail, mUserPhone, mUserProfile, mUserHash);

            FirebaseFirestore db = FirestoreHelper.getDatabase();
            DocumentReference userDocument = db.collection("users").document(mUserId);

            Log.d(USER_REGISTRATION_ADD_DATA_TAG, "The user is: " + mUserId);
            Log.d(USER_REGISTRATION_ADD_DATA_TAG, "The Path for the user document is: " + userDocument);
            Log.d(USER_REGISTRATION_ADD_DATA_TAG, "The users new mailadress is: " + currentUser.getEmail());


            userDocument.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(USER_REGISTRATION_ADD_DATA_TAG, "The user data have been stored in the database.");

                    //ON SUCCESS, NAVIGATE THE USER BACK TO THE NAVIGATION ACTIVITY
                    Intent navigationActivityIntent = new Intent(UserRegistrationAddData.this, NavigationActivity.class);
                    startActivity(navigationActivityIntent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    etName.setEnabled(true);
                    etMail.setEnabled(true);
                    progressbar.setVisibility(View.INVISIBLE);

                    Toast.makeText(UserRegistrationAddData.this, "Error saving data. Please try again", Toast.LENGTH_LONG).show();

                }
            });
        }

    }

}
