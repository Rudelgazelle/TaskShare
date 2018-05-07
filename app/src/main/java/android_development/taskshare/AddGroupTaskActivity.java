package android_development.taskshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;

import java.text.DateFormat;
import java.util.Date;

public class AddGroupTaskActivity extends AppCompatActivity {

    //Variable for date field
    public Date currentDate;
    public Date mDueDate;
    public String content;
    public DateFormat df;
    public int mTaskCount;

    //variable for Task Object
    public Boolean mIsFavorite = false;

    //unique key under which the data is stored in Firebase
    public String key;
    public String userID;

    // Initialize the Firebase Database instance
    public FirebaseDatabase database;
    public DatabaseReference dbRef;

    //Initialize FirebaseAuth instance
    public FirebaseAuth mAuth;

    //Variables for the DatePicker
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;

    //Textview and edit field for Task
    TextView tvDate;
    EditText etTaskContent;
    EditText etDueDate;
    DatePicker dpDueDate;
    ImageButton ibtnSaveData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get UserData from stored object of the PutExtra Method by the NavigationActivity
        userID = getIntent().getStringExtra("userID");

        //initialize the Database instance.
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        //initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //set variable values for content to be stored
                key = dbRef.push().getKey();
                content = etTaskContent.getText().toString();

                //If user is logged in, get the uniqueID of the current user and store dataset in the Database
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Name, email address, and profile photo Url
                    userID = currentUser.getUid();

                    TaskData taskData = new TaskData(key, content, currentDate, mDueDate, mIsFavorite);
                    dbRef.child("groupdata").child("groupID").child("tasks").child(key).setValue(taskData);

                    dbRef.child("groupdata").child("groupID").child("taskCount");
//TODO: Add +1 to taskCount of Group Data
                }

                //Show snackbar with message, that data has been stored to the database
                Snackbar.make(view, "Data has been stored in google Firebase", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                //After saving procedure the view is navigated back to the main menu
                Intent addGroupTaskActivityIntent = new Intent(AddGroupTaskActivity.this, NavigationActivity.class);
                AddGroupTaskActivity.this.startActivity(addGroupTaskActivityIntent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
