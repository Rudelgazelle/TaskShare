package android_development.taskshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddGroupTaskActivity extends AppCompatActivity {

    public static final String ADD_GROUP_TASK_TAG = "AddGroupTask";
    // FIRESTORE VARIABLES
    FirebaseFirestore db;
    CollectionReference userTasksCollection;
    DocumentReference groupDocRef;

    //Variable for date field
    public String groupTaskID;
    public Date currentDate;
    public Date dueDate;
    public String content;
    public DateFormat df;
    public int mTaskCount;

    //variable for Task Object
    public Boolean taskIsFavorite = false;

    //unique key under which the data is stored in Firebase
    public String groupID;
    public String userID;

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

        //Set Firestore Database
        db = FirestoreHelper.getDatabase();

        //Get UserData from stored object of the PutExtra Method by the NavigationActivity
        userID = getIntent().getStringExtra("userID");
        groupID = getIntent().getStringExtra("groupID");

        Log.d(ADD_GROUP_TASK_TAG, "PutExtra for GroupID is: " + groupID);

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
                dueDate = cal.getTime();
                //mDueDate = new Date(mYear, mMonth, mDay);
                Log.d("DueDateVariable: ", "Duedate" + dueDate);

                //set the datepicker to invisible
                dpDueDate.setVisibility(View.GONE);

                //And paste the new date to the edittext.
                etDueDate.setText(df.format(dueDate));
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /***********************************************************************************************
     * METHOD TO SAVE DATA FROM FIELDS TO DATABASE
     **********************************************************************************************/
    public void saveGroupTask(View view){

        createGroupTaskDocument();
        updateTaskCount();

        //After saving procedure the view is navigated back to the main menu
        Intent addGroupTaskActivityIntent = new Intent(AddGroupTaskActivity.this, NavigationActivity.class);
        AddGroupTaskActivity.this.startActivity(addGroupTaskActivityIntent);
    }

    public void createGroupTaskDocument(){

        //Reference to user specific "userTaskCollection" and set the new task object
        userTasksCollection = db.collection("groups").document(groupID).collection("grouptasks");

        //set the required variables for a TaskData object
        groupTaskID = userTasksCollection.document().getId();
        Log.d(ADD_GROUP_TASK_TAG, "New Task ID IS: " + groupTaskID);
        content = etTaskContent.getText().toString();

        if (content.equals("")){
            Toast.makeText(this, "Please enter a task description", Toast.LENGTH_SHORT).show();
            return;
        }else {
            //proceed with generation object to be stored in Database
            TaskData taskData = new TaskData(groupTaskID, content, currentDate, dueDate, taskIsFavorite);

            userTasksCollection.add(taskData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d(ADD_GROUP_TASK_TAG, "Group Task Document successfully written!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(ADD_GROUP_TASK_TAG, "Error writing groupTask Document to database", e.fillInStackTrace());
                }
            });
        }
    }

    public void updateTaskCount(){

        groupDocRef = db.collection("groups").document(groupID);

        db.runTransaction(new com.google.firebase.firestore.Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull com.google.firebase.firestore.Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(groupDocRef);
                double updatedTaskCount = snapshot.getDouble("taskCount") + 1;
                transaction.update(groupDocRef, "taskCount", updatedTaskCount);

                //SUCCESS
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(ADD_GROUP_TASK_TAG, "Transaction success!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(ADD_GROUP_TASK_TAG, "Transaction failure.", e);
            }
        });

    }
}
