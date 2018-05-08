package android_development.taskshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import org.w3c.dom.Document;

import java.security.acl.Group;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;


public class AddGroup extends AppCompatActivity {


    public static final String ID_KEY = "id";
    public static final String GROUPNAME_KEY = "groupname";
    public static final String TAG = "Firestore";
    //define variables
    public String userID;
    public Long userHashCode;

    private FirebaseFirestore mFirestoreRef;
    private DocumentReference mDocumentRef;
    private DocumentReference mMembershipDocRef;
    private CollectionReference mCollectionRef;

    private CollectionReference mMembershipCollectionRef;

    private TextView tvGroupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //DELETE AFTER TUTORIAL
        tvGroupID = (TextView) findViewById(R.id.textView4);

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


        ImageButton ibtnSave = (ImageButton) findViewById(R.id.ibtnSave);

/*        ibtnSave.setOnClickListener(new View.OnClickListener() {
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
        });*/

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
    public void createGroupInFirestore(View view){

        //----------------------------------------------------------------------------------
        //CREATE GROUP ENTRY WITH DEFAULT DATA OF OWNER
        //----------------------------------------------------------------------------------

        //Set Reference to the required path
        mCollectionRef = FirebaseFirestore.getInstance().collection("groups");
        mFirestoreRef = FirebaseFirestore.getInstance();

        // SETTING UP THE DEFAULT GROUP VARIABLES
        String mGroupID = mFirestoreRef.collection("groups").document().getId();
        Log.d(TAG, "The Group Id is: " + mGroupID);

        //String mGroupID = "id_to_be_deleted";
        EditText etGroupName = (EditText) findViewById(R.id.etGroupName);
        String mGroupName = etGroupName.getText().toString();
        String mGroupOwner = userID;
        int mTaskCount = 0;
        //Generated random Integer based on timestamp and user hash value
        int mItemId = generateUniqueItemID();

        //Create Hashmap for default Member Data Objects and add default owner data
        //TODO: Replace hardcoded username with actual username from Firebase Auth
        HashMap<String, MemberData> membersHashMap = new HashMap<>();
        MemberData memberDataGroupOwner = new MemberData(userID, "Lars B.", "owner");
        membersHashMap.put(memberDataGroupOwner.getId() , memberDataGroupOwner);

        //initialize and set new "group" object to be added to the dataset
        GroupData groupData = new GroupData(mGroupID, membersHashMap, mGroupName, mGroupOwner, mItemId, mTaskCount);

        //If groupname field is filled, store dataset in the Database
        if (mGroupName.isEmpty()) {
            Toast.makeText(AddGroup.this, "please type in a groupname", Toast.LENGTH_LONG).show();
        }else {
            //call add method on collection to put data into firestore with a autogenerated ID (specifying "this" in OnSuccessListener will enable automatic deactivation of listener when activity is not active)

            mFirestoreRef.document("groups/" + mGroupID)
                    .set(groupData).addOnSuccessListener(this, new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                }
            }).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            });

            Log.d("GroupData", "ItemID is: " + mItemId);
        }

        //----------------------------------------------------------------------------------
        //ADD MEMBERSHIP OF GROUP INTO USER SPECIFIC USERDATA OBJECT IN DATABASE
        //----------------------------------------------------------------------------------

        //Create new GroupMemberShip Object
        String mGroupMemberShipID = groupData.getId();
        String mGroupMemberShipCategory = "member";
        GroupMemberShip groupMemberShip = new GroupMemberShip(mGroupMemberShipID, mGroupMemberShipCategory);

        mMembershipDocRef = mFirestoreRef.document("users/" + userID + "/memberships/" + mGroupID);

        mMembershipDocRef.set(groupMemberShip).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Usermembership DocumentSnapshot successfully written!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error writing Usermembership document", e);
            }
        });
    }

    public void loadData (View view){

        //set reference for the document to be fetched
        mDocumentRef = FirebaseFirestore.getInstance().document("groups/uniquekey1234");
        //fetch document by calling the "get()" method
        mDocumentRef.get().addOnSuccessListener(this, new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String id = documentSnapshot.getString(ID_KEY);
                    String name = documentSnapshot.getString(GROUPNAME_KEY);

                    //USE THESE METHODS TO FETCH THE DATA IN AN OBJECT FORMAT

                    //Map<String, Object> groupData = documentSnapshot.getData();
                    //GroupData groupDataObject = documentSnapshot.toObject(GroupData.class);

                    tvGroupID.setText(id + " " + name);
                }
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error", e.getCause());
            }
        });
    }


    /***********************************************************************************************
     * METHOD TO CREATE NEW GROUP BASED ON DATA FROM FIELDS
     * ToDo: DELETE THIS METHOD IF FIREBASE WILL BE REPLACED BY FIRESTORE
     **********************************************************************************************/
    public void createGroup(){

        /*// Initialize the Firebase Database instance
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

        //Create Hashmap for default Member Data Objects and add default owner data
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
        dbRef.child("userdata").child(userID).child("groupmemberships").child(mGroupMemberShipID).setValue(groupMemberShip);*/
    }

    @Override
    protected void onStart() {
        super.onStart();

        /***********************************************************************************************
         * THIS CODE CAN BE USED FOR THE MENU (GROUP) BUILDER IN THE MAIN ACTIVITY CLASS
         *
         * //USE A COLLECTION REFERENCE TO QUERY FOR MULTIPLE GROUPS INSTEAD!!!!!!!!!!!!!
         **********************************************************************************************/

        //set reference for the document to be fetched
        mDocumentRef = FirebaseFirestore.getInstance().document("groups/uniquekey1234");
        //set Snapshotlistener to document reference (specifying "this" in OnSuccessListener will enable automatic deactivation of listener when activity is not active)
        mDocumentRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {

                //IF there is an error, show toast and log message
                if (e != null){
                    Toast.makeText(AddGroup.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    //leave this method if there was an exception
                    return;
                }
                if (documentSnapshot.exists()){
                    String id = documentSnapshot.getString(ID_KEY);
                    String name = documentSnapshot.getString(GROUPNAME_KEY);

                    //USE THESE METHODS TO FETCH THE DATA IN AN OBJECT FORMAT

                    //Map<String, Object> groupData = documentSnapshot.getData();
                    //GroupData groupDataObject = documentSnapshot.toObject(GroupData.class);

                    tvGroupID.setText(id + " " + name);
                }
            }
        });
    }
}
