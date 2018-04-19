package android_development.taskshare;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class TaskOverviewActivity extends AppCompatActivity {

    public String content;
    public Date dateCreated;
    public String key;
    public Date mDueDate;
    public Boolean misFavorite;

    public String userID;
    public String userName;
    public String userMail = "l.buerkner@gmail.com";
    public String userPassword = "test123";

    //TextViews for User data
    public TextView textViewUserID;
    public TextView textViewUserName;
    public TextView textViewUserMail;

    //TextViews for Data from database
    public TextView textViewID;
    public TextView textViewContent;
    public TextView textViewDate;


    //Initialize FirebaseAuth instance
    public FirebaseAuth mAuth;

    private static final String TAG = "EmailPassword";

    // Initialize the Firebase Database instance
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    public DatabaseReference myRef = database.getReference();
    public DatabaseReference specificRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_overview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        // Initialize the Firebase Database instance
        //FirebaseDatabase database = FirebaseDatabase.getInstance();
        //final DatabaseReference myRef = database.getReference();
        //final DatabaseReference specificRef;

        //Initialize editText field for TaskData Content
        final EditText editTextContent = (EditText) findViewById(R.id.editTextContent);

        //Initialize editText field for User data
        textViewUserID = (TextView) findViewById(R.id.textViewUserID);
        textViewUserName = (TextView) findViewById(R.id.textViewName);
        textViewUserMail = (TextView) findViewById(R.id.textViewMail);
        final Button btnLogin = (Button) findViewById(R.id.btnLogin);
        final Button btnGetData = (Button) findViewById(R.id.btnGetData);

        //Initialize editText fields for TaskData Read from Database
        textViewID = (TextView) findViewById(R.id.textViewID);
        textViewContent = (TextView) findViewById(R.id.textViewContent);
        textViewDate = (TextView) findViewById(R.id.textViewDate);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabAdd);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //set variable values for content to be stored
                key = myRef.push().getKey();
                content = editTextContent.getText().toString();
                dateCreated = new Date(System.currentTimeMillis());

                //If user is logged in, get the uniqueID of the current user and store dataset in the Database
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Name, email address, and profile photo Url
                    userID = currentUser.getUid();

                    TaskData taskData = new TaskData(key, content, dateCreated, mDueDate, misFavorite);
                    myRef.child("taskdata").child(userID).child(key).setValue(taskData);
                }

                //Show snackbar with message, that data has been stored to the database
                Snackbar.make(view, "Data has been stored in google Firebase", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //implement onClicklistener for Login Button
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Login User with Mail and Password
                mAuth.signInWithEmailAndPassword(userMail, userPassword)
                        .addOnCompleteListener(TaskOverviewActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUserdata();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(TaskOverviewActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    updateUserdata();
                                }

                                // ...
                            }
                        });
            }
        });

        //implement onClicklistener for GetData Button
        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get data from database
                readSpecificEntry();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        updateUserdata();
    }

    public void updateUserdata(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Name, email address, and profile photo Url
            userID = currentUser.getUid();
            userName = currentUser.getDisplayName();
            userMail = currentUser.getEmail();
            Uri photoUrl = currentUser.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = currentUser.isEmailVerified();

            //update ui
            textViewUserID.setText(userID);
            textViewUserName.setText(userName);
            textViewUserMail.setText(userMail);
        }
    }

    public void readSpecificEntry(){
        // Read from specific Entry reference (for test purposes with fixed entry Key
        //TODO: Replace hardcoded ID with variable
        specificRef = myRef.child("taskdata").child(userID).child("randomID");
        specificRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                TaskData data = dataSnapshot.getValue(TaskData.class);

                textViewID.setText(data.id);
                textViewContent.setText(data.content);
                textViewDate.setText(data.datecreated.toString());

                Log.d(TAG, "Value is: " + data);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read data.", error.toException());
            }
        });
    }

}
