package android_development.taskshare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class TaskDataDetailsActivity extends AppCompatActivity {

    //Define variables to be mapped to values of clicked item
    String taskID;
    String taskDateCreated;
    String taskContent;
    Date taskDueDate;
    Boolean mIsFavorite;

    public TextView tvTaskUpdatedDate;
    public TextView tvTaskOwner;
    public EditText etTaskContent;
    public ImageButton ibtnSaveData;

    //Firebase Database
    DatabaseReference dbRef;
    String currentUserID;

    //System variables
    Date currentDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_data_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //sets the title of the activity
        setTitle("Task");

        //Get data from stored variables of the PutExtra Method by the TaskDataViewAdapter
        taskID = getIntent().getStringExtra("taskID");
        taskDateCreated = getIntent().getStringExtra("taskDateCreated");
        taskContent = getIntent().getStringExtra("taskContent");

        //Initialize fields
        tvTaskUpdatedDate = (TextView) findViewById(R.id.tvTaskUpdatedDate);
        tvTaskOwner = (TextView) findViewById(R.id.tvTaskOwner);
        etTaskContent = (EditText) findViewById(R.id.etTaskContent);
        ibtnSaveData = (ImageButton) findViewById(R.id.ibtnSaveData);

        //map variables to field values
        tvTaskUpdatedDate.setText(taskDateCreated);
        tvTaskOwner.setText(taskID);
        etTaskContent.setText(taskContent);
        etTaskContent.isFocused();

        ibtnSaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Call UpdateItem method upon click to save the new data values in Firebase Database
                updateItem();

                //After saving procedure the view is navigated back to the main menu
                Intent addTaskActivityIntent = new Intent(TaskDataDetailsActivity.this, NavigationActivity.class);
                TaskDataDetailsActivity.this.startActivity(addTaskActivityIntent);
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    //This method fetches the ItemID and updates the item values
    // in firebase Database based on new values in fields
    //----------------------------------------------------------------------------------------------
    public void updateItem(){
        currentUserID = FirebaseHelper.getCurrentUser().getUid();
        dbRef = FirebaseHelper.getDatabase().getReference().child("taskdata").child(currentUserID).child(taskID);

        //update the variables based on (updated) field values
        currentDate = Calendar.getInstance().getTime();
        taskContent = etTaskContent.getText().toString();

        //update the specific task in FirebaseDatabase based on new values
        TaskData taskData = new TaskData(taskID, taskContent, currentDate, taskDueDate, mIsFavorite);
        dbRef.setValue(taskData);
    }

}
