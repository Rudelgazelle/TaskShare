package android_development.taskshare;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Document;
import org.w3c.dom.Text;

import java.net.URL;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class AddGroup extends AppCompatActivity {

    //for Contact picker
    private static final int RESULT_PICK_CONTACT = 65000;

    public static final String ID_KEY = "id";
    public static final String GROUPNAME_KEY = "groupname";
    public static final String TAG = "Firestore";
    public static final String ADD_GROUP_ACTIVITY_TAG = "AddGroupActivity";
    //define variables
    public String userID;
    public Long userHashCode;

    private FirebaseFirestore mFirestoreRef;
    private DocumentReference mDocumentRef;
    private DocumentReference mMembershipDocRef;
    private CollectionReference mCollectionRef;

    private CollectionReference mMembershipCollectionRef;

    private TextView tvGroupID;
    private ListView lvGroupMember;
    private TextView tvMemberName;
    private TextView tvMemberMail;
    private Button btnAddMember;
    ImageButton ibtnSaveData;

    RecyclerView recyclerViewGroupMember;
    GroupMemberViewAdapter adapter;
    private List<UserData> memberDataList;
    private List<UserData> matchingUserList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //DELETE AFTER TUTORIAL
        tvGroupID = (TextView) findViewById(R.id.textView4);

        btnAddMember = (Button) findViewById(R.id.btnAddMember);

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

        //memberDataList = new ArrayList<>();
        matchingUserList = new ArrayList<>();

        //READ ALL CONTACTS AND COMPARE THEM AGAINST ALL REGISTERED USERS
        readAllContacts();

        //Initialize the recyclerView
        adapter = new GroupMemberViewAdapter(matchingUserList, this);
        recyclerViewGroupMember = (RecyclerView) findViewById(R.id.recyclerViewMemberData);
        recyclerViewGroupMember.setHasFixedSize(true);
        recyclerViewGroupMember.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewGroupMember.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        adapter.notifyDataSetChanged();
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
        MemberData memberDataGroupOwner = new MemberData(userID, "Lars B.", "l.buerkner@gmail.com", "owner");
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

                //----------------------------------------------------------------------------------
                //RETURN BACK TO THE NAVIGATION AVTIVITY
                //----------------------------------------------------------------------------------
                Intent addTaskActivityIntent = new Intent(AddGroup.this, NavigationActivity.class);
                AddGroup.this.startActivity(addTaskActivityIntent);
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

    public void pickContact(View v)
    {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    contactPicked(data);
                    break;
            }

        } else {
            Log.e("MainActivity", "Failed to pick contact");
        }
    }

    private void contactPicked(Intent data) {
        Cursor cursor = null;
        try {
            String phoneNo = null ;
            String name = null;
            String mail = null;
            Uri uri = data.getData();
            cursor = getContentResolver().query(uri, null, null, null, null);
            cursor.moveToFirst();

            int  phoneIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int  nameIndex =cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int mailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);

            phoneNo = cursor.getString(phoneIndex);
            name = cursor.getString(nameIndex);

            Log.d(TAG, "contactPicked: Mail is: " + phoneNo);

            //JUST FOR TESTING PURPOSES
            Long userHashCode = 1345678910L;

            String userPictureUrl = "https://firebasestorage.googleapis.com/v0/b/taskshare-300b9.appspot.com/o/Download.jpg?alt=media&token=739983a1-4269-4675-84a5-44916dc5ca56";

            UserData userData = new UserData("1", name, "n.buerkner@gmail.com","015229272023", userPictureUrl, userHashCode );
            memberDataList.add(userData);

            Log.d(TAG, "MemberList is size: " + memberDataList.size());

            //Tell the adapter, that the datasource has been changed
            adapter.notifyDataSetChanged();


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /************************************************************
     * METHOD TO READ ALL CONTACT DATA FROM LOCAL STORAGE       *
     ************************************************************/
    public void readAllContacts(){

        List<String> localContactList = new ArrayList<>();

        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        while (cursor.moveToNext()) {

            String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String contactPhoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA));

            localContactList.add(contactPhoneNumber);
        }
        cursor.close();

        Log.d(ADD_GROUP_ACTIVITY_TAG, "Local Contact Data successfully read! There are " + localContactList.size() + " items in the localContactList");

        //IF THE LIST IS COMPLETE, CALL THE METHOD TO COMPARE THE RETRIEVED LOCAL CONTACT LIST WITH ALL THE USERS OF THE FIRESTORE DATABASE
        compareLocalContactsWithUsersDatabase(localContactList);
    }

    public void compareLocalContactsWithUsersDatabase(final List<String> localContactList){

        //---------------------------------------------------
        //RETRIEVE THE ALL USERS FROM THE FIRESTORE DATABASE
        //---------------------------------------------------

        FirebaseFirestore db;
        CollectionReference userCollectionReference;

        final List<UserData> userDataList = new ArrayList<>();

        db = FirestoreHelper.getDatabase();
        userCollectionReference = db.collection("users");
        userCollectionReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (DocumentSnapshot document : queryDocumentSnapshots){
                            //Create an object and add the user ID (which is not redundant stored in the Database
                            UserData user = document.toObject(UserData.class);
                            user.setUserId(document.getId());
                            userDataList.add(user);
                        }
                        Log.d(ADD_GROUP_ACTIVITY_TAG, "User Data successfully read! There are " + userDataList.size() + " items in the userDataList");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(ADD_GROUP_ACTIVITY_TAG, "Error loading user data! " + e.getMessage());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        Log.d(ADD_GROUP_ACTIVITY_TAG, "Reading Operation completed, proceding with comparing the two lists");

                        //---------------------------------------------------------------------
                        //COMPARE THE RETRIEVED USER DATASETS AGAINST THE LOCAL CONTACT LIST
                        //---------------------------------------------------------------------

                        for (String contactPhone : localContactList){
                            for (UserData user : userDataList){
                                if (contactPhone.equals(user.getUserPhone())) {
                                    //add the user to the matchingList, which will be used in the Activity to see available Users to add to the group
                                    matchingUserList.add(user);
                                }
                            }
                        }
                        //Notify the adapter, that the Dataset has been changed, if the list is updated after the activity has already started.
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
