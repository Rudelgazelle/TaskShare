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

    //Define Field variable
    EditText etGroupName;

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

        //Initialize the EditText field for groupname
        etGroupName = (EditText) findViewById(R.id.etGroupName);

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


        ImageButton ibtnSave = (ImageButton) findViewById(R.id.ibtnSave);
        ibtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //create group and store in database
                createGroup();

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


    /***********************************************************************************************
     * METHOD TO CREATE NEW GROUP BASED ON DATA FROM FIELDS
     **********************************************************************************************/
    public void createGroup(){

        // Initialize the Firebase Database instance
        FirebaseDatabase database;
        database = FirebaseHelper.getDatabase();

        DatabaseReference dbRef;
        dbRef = database.getReference();

        //----------------------------------------------------------------------------------
        //CREATE GROUP ENTRY WITH DEFAULT DATA OF OWNER
        //----------------------------------------------------------------------------------

        String mGroupID = dbRef.push().getKey();
        String mGroupName = etGroupName.getText().toString();
        String mGroupOwner = userID;
        int mTaskCount = 0;
        //Generated random Integer based on timestamp and user hash value
        int mItemId = generateUniqueItemID();

        //Create Hashmap for default Member Data Objects and add defaul owner data
        HashMap<String, MemberData> membersHashMap = new HashMap<>();
        MemberData memberDataGroupOwner = new MemberData(userID, "Lars B.", "owner");
        membersHashMap.put(memberDataGroupOwner.getId() , memberDataGroupOwner);

        //initialize and set new "group" object to be added to the dataset
        GroupData groupData = new GroupData(mGroupID, membersHashMap, mGroupName, mGroupOwner, mItemId, mTaskCount);

        //If user is logged in, get the uniqueID of the current user and store dataset in the Database
        if (userID != null) {
            //Add the groupData object to the database
            dbRef.child("groupdata").child(mGroupID).setValue(groupData);
            Log.d("GroupData", "ItemID is: " + mItemId);
        }

        //----------------------------------------------------------------------------------
        //ADD MEMBERSHIP OF GROUP INTO USER SPECIFIC USERDATA OBJECT IN DATABASE
        //----------------------------------------------------------------------------------

        //Create new GroupMemberShip Object
        String mGroupMemberShipID = groupData.getId();
        String mGroupMemberShipCategory = "owner";
        GroupMemberShip groupMemberShip = new GroupMemberShip(mGroupMemberShipID, mGroupMemberShipCategory);

        //set DB reference to user specific entry and set Value of group ID into the child location
        dbRef.child("userdata").child(userID).child("groupmemberships").child(mGroupMemberShipID).setValue(groupMemberShip);
    }

}
