package android_development.taskshare;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {

    //Variable for date field
    public Date currentDate;
    public Date mDueDate;
    public String content;
    public DateFormat df;

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

    //Textview and edit field for Task
    TextView tvDate;
    EditText etTaskContent;
    EditText etDueDate;
    DatePicker dpDueDate;
    ImageButton ibtnSaveData;

    //Variables for the DatePicker
    private int mYear;
    private int mMonth;
    private int mDay;
    static final int DATE_DIALOG_ID = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //sets the title of the activity and implements the back button
        setTitle("Add task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get UserData from stored object of the PutExtra Method by the NavigationActivity
        userID = getIntent().getStringExtra("userID");

        //initialize the Database instance.
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        //initialize the FirebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        //set defalut image to star_border and implment onclicklistener
        fab.setImageResource(R.drawable.ic_star_border);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if isFavourite is false, set image to star black and bool value to true
                if (!mIsFavorite){
                    mIsFavorite = true;
                    fab.setImageResource(R.drawable.ic_star_black);
                }else {
                    mIsFavorite = false;
                    fab.setImageResource(R.drawable.ic_star_border);
                }
            }
        });

        //Initialize Fields
        tvDate = (TextView) findViewById(R.id.tvDateToday);
        etDueDate = (EditText) findViewById(R.id.etDueDate);
        dpDueDate = (DatePicker) findViewById(R.id.dpDueDate);
        etTaskContent = (EditText) findViewById(R.id.etTaskContent);

        //get current date set date format and set currentDate to textview
        currentDate = Calendar.getInstance().getTime();
        df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
        tvDate.setText(df.format(currentDate));

        etDueDate.setText(df.format(currentDate));
        etDueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dpDueDate.setVisibility(View.VISIBLE);
            }
        });

        dpDueDate.setVisibility(View.GONE);
        dpDueDate.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mYear = year;
                mMonth = monthOfYear;
                mDay = dayOfMonth;
                Log.d("DueDateFromDatePicker: ", "Year:" + mYear + " Month: " + mMonth + " Day: " + mDay);

                Calendar cal = Calendar.getInstance();
                cal.set(mYear, mMonth, mDay);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                mDueDate = cal.getTime();
                //mDueDate = new Date(mYear, mMonth, mDay);
                Log.d("DueDateVariable: ", "Duedate" + mDueDate);

                //set the datepicker to invisible
                dpDueDate.setVisibility(View.GONE);

                //And paste the new date to the edittext.
                etDueDate.setText(df.format(mDueDate));
            }
        });

        /***********************************************************************************************
         * METHOD TO SAVE DATA FROM FIELDS TO DATABASE
         **********************************************************************************************/

        ibtnSaveData = (ImageButton) findViewById(R.id.ibtnSaveData);
        ibtnSaveData.setOnClickListener(new View.OnClickListener() {
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
                    dbRef.child("taskdata").child(userID).child(key).setValue(taskData);
                }

                //Show snackbar with message, that data has been stored to the database
                Snackbar.make(view, "Data has been stored in google Firebase", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                //After saving procedure the view is navigated back to the main menu
                Intent addTaskActivityIntent = new Intent(AddTaskActivity.this, NavigationActivity.class);
                AddTaskActivity.this.startActivity(addTaskActivityIntent);
            }
        });
    }

    //return date picker dialog
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay);
        }
        return null;
    }

    //update month day year
    private void updateDatePicker() {
        etDueDate.setText(//this is the edit text where you want to show the selected date
                new StringBuilder()
                        // Month is 0 based so add 1
                        .append(mYear).append("-")
                        .append(mMonth + 1).append("-")
                        .append(mDay).append(""));
    }

    // the call back received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDatePicker();
                }
            };

}
