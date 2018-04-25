package android_development.taskshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class AddGroup extends AppCompatActivity {

    //define variables
    public String userID;
    public Long userHashCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Load Shared preferences from file (e.g. userID)
        loadSharedPreferences();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // TEST METHOD TO CREATE A RANDOM INTEGER BASED ON TIMESTAMP AND RANDOM NUMBER
                int uniqueID = generateUniqueItemID();
                Log.d("UniqeID", "uniqueID is: " + uniqueID);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /***********************************************************************************************
         * METHOD TO SAVE DATA FROM FIELDS TO DATABASE
         **********************************************************************************************/

        final EditText etGroupName = (EditText) findViewById(R.id.etGroupName);

        ImageButton ibtnSave = (ImageButton) findViewById(R.id.ibtnSave);
        ibtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Initialize the Firebase Database instance
                FirebaseDatabase database;
                database = FirebaseHelper.getDatabase();

                DatabaseReference dbRef;
                dbRef = database.getReference();

                //DEFAULT DATA FOR DATABASE---------------------------------------------------------
                HashMap<String, GroupData> groupsHashmap = null;
                String mGroupID = dbRef.push().getKey();
                String mGroupName = etGroupName.getText().toString();
                String mGroupOwner = userID;
                //Generated random Integer based on timestamp and user hash value
                int mItemId = generateUniqueItemID();

                //Create Hashmap for Member Data Objects and fill from snapshot
                HashMap<String, MemberData> membersHashMap = new HashMap<>();
                MemberData memberData1 = new MemberData("memberIDxxx", "Lars B.", "member");
                MemberData memberData2 = new MemberData("memberIDxyy", "Christina", "member");
                membersHashMap.put(memberData1.getId() , memberData1);
                membersHashMap.put(memberData2.getId(), memberData2);

                //If user is logged in, get the uniqueID of the current user and store dataset in the Database
                if (userID != null) {
                    //Create set of Hashmap, to be populated into Firebase Database
                    GroupData groupData1 = new GroupData(mGroupID, membersHashMap, mGroupName, mGroupOwner, mItemId);
                    //Add the groupData1 object to the database
                    dbRef.child("groupdata").child(mGroupID).setValue(groupData1);
                    Log.d("GroupData", "ItemID is: " + mItemId);
                }

                //Show snackbar with message, that data has been stored to the database
                Snackbar.make(view, "Data has been stored in google Firebase", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                //After saving procedure the view is navigated back to the main menu
                Intent NavigationActivityIntent = new Intent(AddGroup.this, NavigationActivity.class);
                AddGroup.this.startActivity(NavigationActivityIntent);
            }
        });

    }

    /***********************************************************************************************
     * LOAD SHARED PREFERENCES FROM FILE
     **********************************************************************************************/
    public void loadSharedPreferences(){
        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = this.getSharedPreferences("mSharePrefFile", 0);
        // 2. Key Reference from SharePrefFile to fields (If key dows not exist, the default value will be loaded
        userID = (mSharedPref.getString("userID", null));
        userHashCode = (mSharedPref.getLong("userHashCode", -9));
    }

    public int generateUniqueItemID(){

        // TEST METHOD TO CREATE A RANDOM INTEGER BASED ON TIMESTAMP AND RANDOM NUMBER
        //TODO: THIS METHOD HAS TO BE REWORKED TO MAKE SURE THAT THE ITEM ID IS NEVER REDUNDANT
        UniqueIdGenerator uniqueIdGenerator = new UniqueIdGenerator();
        int id = uniqueIdGenerator.createID();
        int additionalId = (int) (long) userHashCode;

        int uniqueID = id+additionalId;

        return uniqueID;
    }

}
