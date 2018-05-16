package android_development.taskshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FIRESTORE_TAG = "Firestore";
    public static final String USER_AUTH_TAG = "UserAuth";
    public static final String GROUP_MENU_CREATION_TAG = "GroupMenuCreation";
    public static final String NAVIGATION_ACTIVITY_TAG = "NavigationActivity";
    public static final String MENU_GROUP_ITEM_LIST_TAG = "MenuGroupItemList";

    //Initialize variables for Userdata
    public UserData currentUserData;

    public String userID;
    public String userName;
    public String userMail;
    public Uri userProfilePhotoUrl;
    public Long userHashCode; //to be used for generation of unique IDs


    //Initialize Objects for User data in NavHeader
    public ImageView ivUserProfile;
    public TextView tvUserName;
    public TextView tvUserMail;
    public Button btnUserSetting;
    public ImageButton ibtnToggleUserSettings;

    public FloatingActionButton fabAddTask;
    public FloatingActionButton fabAddGroupTask;

    //Initialize FirebaseAuth instance
    public FirebaseAuth mAuth;
    public FirebaseAuth.AuthStateListener mAuthstateListener;

    //Define TAG variable for output messages during debug
    private static final String TAG = "EmailPassword";

    //Initialize Firestore Database instances
    FirebaseFirestore db;
    CollectionReference groupRef;
    DocumentReference groupDocRef;
    CollectionReference membershipCollectionRef;
    CollectionReference userTasksCollectionRef;
    CollectionReference taskRef;
    DocumentReference docRef;
    Boolean fetchGroupDataIsSuccessfull = false;
    Boolean menuShouldBeUpdated = false;

    // Initialize the Firebase Database instance and query
    FirebaseDatabase database;
    FirebaseDatabase mDatabaseMembership;
    DatabaseReference mMembershipDBReference;
    DatabaseReference dbRef;

    // adding a section and items into menu
    Menu menu;
    SubMenu subMenuGroups;
    MenuItem menuItem;
    Boolean mActionSettingsAreToggled;

    //User Object that is used by different Methods
    //UserData user;

    int groupID;
    String mGroupIDforPutExtra;
    int itemID;
    int itemOrder = 0;
    String itemTitle;
    String activityTitle;
    int index;
    int itemToBeRemovedIndex;

    //VARIABLES FOR MENU GROUP ITEMS
    public Map<String, GroupData> groupHashMap = new HashMap<String, GroupData>();
    public Map<String, MemberData> memberHashMap = new HashMap<String, MemberData>();
    public List<String> mGroupMembershipList;
    public List<GroupData> menuGroupItemList;
    private Integer taskDataListAllSize;
    private Integer taskDataListFavoriteSize;
    private Integer taskDataListOverdueSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Boolean required for use of the UserSettingsToggle Button
        mActionSettingsAreToggled = false;

//<========================================{ INITIALIZATION OF VIEWS }========================================>

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //declare menu variable to be used in different methods
        menu = navigationView.getMenu();

        //------------------------------------------------------------------------------------------
        //Initialize object fields for User data // SET VIEW FOR HEADER VIEW FIRST!!!!
        View header = navigationView.getHeaderView(0);
        //Initialize view objects
        ivUserProfile = header.findViewById(R.id.ivUserProfile);
        tvUserName = header.findViewById(R.id.tvUserName);
        tvUserMail = header.findViewById(R.id.tvUserMail);

//<========================================{ INITIALIZATION METHODS }========================================>

        // 1. RETRIEVE THE CURRENT USER DATA IN A ONE TIME EVENT (IMPORTANT, AS AUTHSTATELISTENER WILL TAKE A WHILE TO RETRIEVE UPDATED DATA!)
        fetchCurrentUserData();

        // 2. Update the NavDrawer Header with the CurrentUser Data (Name, Mail and Profile picture if available)
        //TODO: Implement that the profile picture is rounded via GLIDE
        updateUserdataUI(currentUserData);

        //FETCH GroupData in Memberships as one time event

        if (userID != null){
            fetchMemberShipDataforMenuCreation();
        }

        /***********************************************************************************************
         *
         * Authstate listener will listen to changes in the user authorization state
         *
         **********************************************************************************************/
        mAuthstateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser mCurrentUser = firebaseAuth.getCurrentUser();

                if (mCurrentUser == null) {
                    Toast.makeText(NavigationActivity.this, "User is logged out", Toast.LENGTH_LONG).show();

                    Intent userLoginProviderSelectionIntent = new Intent(NavigationActivity.this, UserLoginProviderSelectionActivity.class);
                    NavigationActivity.this.startActivity(userLoginProviderSelectionIntent);


                    /*Intent userLoginStartActivityIntent = new Intent(NavigationActivity.this, UserLoginStartActivity.class);
                    NavigationActivity.this.startActivity(userLoginStartActivityIntent);*/
                }

                if (mCurrentUser != null) {

                    fetchCurrentUserData();

                    String mUserID = currentUserData.getUserId();
                    Long mUserHashCode = currentUserData.getUserHashCode();

                    saveSharedPreferences(mUserID, mUserHashCode);

                    Log.d("User1", "ActivUser: " + mUserID);
                    Log.d("User2", "ActivUserHash: " + mUserHashCode);
                }
            }
        };

        //mAuth = FirebaseAuth.getInstance();
        //mAuth.addAuthStateListener(mAuthstateListener);

        if (userID != null){
            //Set default Fragment if no Saved Instance is null
            if (savedInstanceState == null) {
                Fragment_NavMenu_AllTasks fragment = new Fragment_NavMenu_AllTasks();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.content_main_navigation, fragment);
                fragmentTransaction.commit();

                //Set the title of the Activity according to the Fragment
                activityTitle = getResources().getString(R.string.title_fragment_allTasks);
                //Set the title of the activity
                getSupportActionBar().setTitle(activityTitle);
            }
        }else{
            Toast.makeText(this, "The user is null, fragment cannot be loaded!", Toast.LENGTH_SHORT).show();
            Log.d(NAVIGATION_ACTIVITY_TAG, "The user is null, fragment cannot be loaded!");
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //UPDATE THE DISPLAYED USER DATA
//TODO: REACTIVATE IF REQUIRED, BUT THIS SHOULD NOT BE THE CASE
                //updateUserdataUI(currentUserData);

                // update the menu items if there is new Group data loaded from the database
                if (menuShouldBeUpdated){

                    Log.d(GROUP_MENU_CREATION_TAG, "Nav Drawer is opened and Menu should be updated!");
                    updateGroupMenuItems();

                    menuShouldBeUpdated = false;
                }
            }
        };
        // Set the drawer toggle as the DrawerListener
        drawer.addDrawerListener(toggle);
        toggle.syncState();




        //Initialize Firestore Database of offline persistancy
        db = FirestoreHelper.getDatabase();
        groupRef = db.collection("groups");
        taskRef = db.collection("tasks");

        //Initialize Firebase objects
        database = FirebaseHelper.getDatabase();
        mDatabaseMembership = FirebaseHelper.getDatabase();
        dbRef = database.getReference();
        mMembershipDBReference = mDatabaseMembership.getReference();
        //mAuth = FirebaseAuth.getInstance();
        //currentUser = mAuth.getCurrentUser();



        //IMPLEMENTATION OF TOGGLE BUTTON IN THE NAVHEADER TO TOGGLE BETWEEN TWO MENU GROUPS (general and user Settings)
        ibtnToggleUserSettings = header.findViewById(R.id.ibtnToggleAccountSettings);
        ibtnToggleUserSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mActionSettingsAreToggled){
                    mActionSettingsAreToggled = true;
                }else {
                    mActionSettingsAreToggled = false;
                }

                //change the image of button from arrow down, to arrow up and visa versa based on the boolean
                if (mActionSettingsAreToggled){
                    //set image for button
                    ibtnToggleUserSettings.setBackgroundResource(R.drawable.ic_action_toggle_account_settings_up);
                    //hide the Task menu and make Account settings menu visible
                    menu.setGroupVisible(R.id.groupTaskMenu, false);
                    menu.setGroupVisible(R.id.groupUserAccountMenu, true);

                } else {
                    ibtnToggleUserSettings.setBackgroundResource(R.drawable.ic_action_toggle_account_settings_down);
                    //hide the Account Settings menu and make Task menu visible
                    menu.setGroupVisible(R.id.groupTaskMenu, true);
                    menu.setGroupVisible(R.id.groupUserAccountMenu, false);
                }
            }
        });

        fabAddTask = (FloatingActionButton) findViewById(R.id.fabAddTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start the addTask Activity upon click
                Intent addTaskActivityIntent = new Intent(NavigationActivity.this, AddTaskActivity.class);
                addTaskActivityIntent.putExtra("userID", userID);
                addTaskActivityIntent.putExtra("dbRef", dbRef.toString());
                NavigationActivity.this.startActivity(addTaskActivityIntent);
            }
        });

        fabAddGroupTask = (FloatingActionButton) findViewById(R.id.fabAddGroupTask);
        fabAddGroupTask.setVisibility(View.GONE);
        fabAddGroupTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start the addGroupTask Activity upon click
                Intent addGroupTaskActivityIntent = new Intent(NavigationActivity.this, AddGroupTaskActivity.class);
                addGroupTaskActivityIntent.putExtra("userID", userID);
                addGroupTaskActivityIntent.putExtra("groupID", mGroupIDforPutExtra);
                NavigationActivity.this.startActivity(addGroupTaskActivityIntent);
            }
        });

        /***********************************************************************************************
         * Method to update the user profile UI in the Drawer Menu
         **********************************************************************************************/
        updateUserdataUI(currentUserData);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.nav_allTasks) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //TODO: Implement Filter on own tasks and tasks shared in a group (e.g. via fragment)
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //call the method and parse the id of the selected item
        displaySelectedScreen(id);
        return true;
    }

    public void displaySelectedScreen(int id){
        //Initialize a new instance of fragment
        android.support.v4.app.Fragment fragment = null;
        //FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        //define cases to determine item(navigation) ID // TODO: THIS CAN BE EXPANDED WITH FURTHER ITEMS OF THE NAVIGATIONBAR
        switch (id){
            case R.id.nav_allTasks:
                //Set the title of the Activity according to the Fragment
                activityTitle = getResources().getString(R.string.title_fragment_allTasks);
                //Set the fab button to be gone
                fabAddGroupTask.setVisibility(View.GONE);
                //initialize fragment
                fragment = new Fragment_NavMenu_AllTasks();
                break;
            case R.id.nav_favoriteTasks:
                //Set the title of the Activity according to the Fragment
                activityTitle = getResources().getString(R.string.title_fragment_favoriteTasks);
                //Set the fab button to be gone
                fabAddGroupTask.setVisibility(View.GONE);
                //initialize fragment
                fragment = new Fragment_NavMenu_FavoriteTasks();
                break;
            case R.id.nav_overdueTasks:
                //Set the title of the Activity according to the Fragment
                activityTitle = getResources().getString(R.string.title_fragment_overdueTasks);
                //Set the fab button to be gone
                fabAddGroupTask.setVisibility(View.GONE);
                //initialize fragment
                fragment = new Fragment_NavMenu_OverdueTasks();
                break;
            case R.id.nav_addGroup:
                // open settings activity
                Intent addGroupActivityIntent = new Intent(NavigationActivity.this, AddGroup.class);
                NavigationActivity.this.startActivity(addGroupActivityIntent);
                break;
            case R.id.nav_settings:
                // open settings activity
                Intent settingsActivityIntent = new Intent(NavigationActivity.this, SettingsActivity.class);
                NavigationActivity.this.startActivity(settingsActivityIntent);
                break;
            case R.id.nav_slideshow:
                // open settings TEST activity
                Intent settingsTestActivityIntent = new Intent(NavigationActivity.this, SettingsActivity2.class);
                NavigationActivity.this.startActivity(settingsTestActivityIntent);
                break;
            case R.id.nav_userAccount:
                //OPEN THE USER SETTINGS ACTIVITY
                openUserSettings();
                break;
            case R.id.nav_userLogout:
                //LOGOUT USER FROM AUTHENTICATION SYSTEM/APP
                logOutUser();
                break;
        }

        if (fragment != null){
            //create a FragmentTransaction that changes the displayed fragment // This initiates the screenswitching action
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.addToBackStack(null);
            //Replace the fragment within the main navigation layout
            ft.replace(R.id.content_main_navigation, fragment);
            //commit the changes
            ft.commit();

            //Set the title of the activity
            getSupportActionBar().setTitle(activityTitle);
        }

        //the navigation drawer will be closed after selecting an item
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //ATTACH AUTH STATE LISTENER ON START OF THE APP
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthstateListener);

        /***********************************************************************************************
         * LOAD THE MENU WITH GROUP DATA IF THE USER IS NOT NULL
         **********************************************************************************************/
        if (userID == null){
            Log.d(USER_AUTH_TAG, "Error, user is null!");
        }else {
            Log.d(USER_AUTH_TAG, "UserID for GroupDataListener (Menu) is: " + userID);

            listenToChangesOnUserTasks();

            //listenForChangesInGroupDataForMenu();

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthstateListener != null){
        mAuth.removeAuthStateListener(mAuthstateListener);
        }
    }

    //----------------------------------------------------------------------------------------------
    // If the user is logged in, the userdata will be called from the Firebase authentication server
    //----------------------------------------------------------------------------------------------
    public void updateUserdataUI(UserData userDataObject){
        if (userDataObject != null) {

            //BIND VALUES OF userDataObject TO LOCAL VARIABLES
            String userId = userDataObject.getUserId();
            String userDisplayName = userDataObject.getUserDisplayName();
            String userMail = userDataObject.getUserMail();
            String userPhotoUrl = userDataObject.getUserPhotoUrl();

            Log.d(NAVIGATION_ACTIVITY_TAG, "The username for Navdrawer is: " + userDisplayName);
            Log.d(NAVIGATION_ACTIVITY_TAG, "The usermail for Navdrawer is: " + userMail);

            //update name and mail
            tvUserName.setText(userDisplayName);
            tvUserMail.setText(userMail);




            //TODO: USE LOCAL BITMAP INSTEAD ONLINE RESOURCE

            // update ImageView for Profilepicture if a Picture URL has been provided
            if (userPhotoUrl != null){
                ivUserProfile.setImageResource(R.drawable.default_profile_pic);
            }
        }
    }

    public void downloadUserPic(){

        StorageReference storageReferencePicURL = FirebaseStorage.getInstance().getReference();

        // Reference to an image file in Firebase Storage
//TODO: Implement a Query string based on variables; not hard coded
        try {
            // Create a storage reference from our app
            storageReferencePicURL = storageReferencePicURL.child("images/" + userID + "/787A5361.jpg");;
        }catch (NullPointerException e){
            Log.d(NAVIGATION_ACTIVITY_TAG, "User has no uploaded User Photo!!!");
            //STEP OUT OF THE METHOD
            return;
        }

        // Load the image using Glide
        Glide.with(this /* context */)
                .using(new FirebaseImageLoader())
                .load(storageReferencePicURL)
                .into(ivUserProfile);
    }

    /***********************************************************************************************
     * LOAD SHARED PREFERENCES FROM FILE
     **********************************************************************************************/
    //TODO: METHOD CAN BE DELETED, AS THE LISTSIZE IS UPDATED VIA A DATABASE LISTENER
/*    public void loadSharedPreferencesListSizes(){

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);
        // 2. Key Reference from SharePrefFile to fields (If key dows not exist, the default value will be loaded
        taskDataListAllSize = (mSharedPref.getInt("listSizeTasksAll", 0));
        taskDataListFavoriteSize = (mSharedPref.getInt("listSizeTasksFavorite", 0));
        taskDataListOverdueSize = (mSharedPref.getInt("listSizeTasksOverdue", 0));
    }*/

    /***********************************************************************************************
     * SAVE SHARED PREFERENCES TO FILE
     **********************************************************************************************/
    public void saveSharedPreferences(String userID, Long userHashCode){
        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);
        // 2. Initialize Editor Class
        SharedPreferences.Editor editor = mSharedPref.edit();
        // 3. Get Values from fields and store in Shared Preferences
        editor.putString("userID", userID);
        editor.putLong("userHashCode", userHashCode);
        // 4. Store the keys
        editor.commit();

        Log.d("User3", "ActivUser-Stored: " + userID);
        Log.d("User4", "ActivUserHash-stored: " + userHashCode);
    }

    /***********************************************************************************************
     * THIS METHOD RETRIEVES ALL MEMBERSHIPS DATA FROM THE DATABASE BASED ON THE CURRENT USER ID
     * AND CREATES MENU ITEMS WITH GROUPS ACCORDINGLY
     **********************************************************************************************/
    public void listenForChangesInGroupDataForMenu(){

        /***********************************************************************************************
         * THIS METHOD RETRIEVES ALL MEMBERSHIPS DATA FROM THE DATABASE BASED ON THE CURRENT USER ID
         **********************************************************************************************/
        membershipCollectionRef = db.collection("users").document(userID).collection("memberships");
        membershipCollectionRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(NavigationActivity.this, "Error loading membership data!", Toast.LENGTH_SHORT).show();
                    Log.d(GROUP_MENU_CREATION_TAG, e.toString());
                } else {
                    Log.d(GROUP_MENU_CREATION_TAG, "MembershipSnapshot has been retrieved: " + queryDocumentSnapshots.size());

                    //mGroupMembershipList = new ArrayList();
                    //mGroupMembershipList.clear();

                    // get only document changes out of the queryDocumentSnapshot
                    for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()){

                        switch (documentChange.getType()){

                            case ADDED: //IF A NEW DOCUMENT IS ADDED, EXECUTE THIS CODE

                                if (queryDocumentSnapshots.size() == mGroupMembershipList.size()){
                                    //IF THE OLD LIST HAS THE SAME SIZE AS THE NEW SNAPSHOT, THEN DO NOTHING
                                } else {
                                    //IF THERE ARE ADDITIONAL ITEMS DELETE OLD LIST AND ADD DOCUMENTS
                                    //put the ID (GroupID) of the document in the Array for groupMemberships
                                    mGroupMembershipList.add(documentChange.getDocument().getId());
                                    Log.d(GROUP_MENU_CREATION_TAG, "Membership: ADDED is triggered: " + documentChange.getDocument().getId());
                                }
                                break;


                            case MODIFIED: //IF A DOCUMENT IS MODIFIED, EXECUTE THIS CODE
                                //Get the Membership Object from the snapshot
                                String modifiedId = documentChange.getDocument().getId();
                                Log.d(GROUP_MENU_CREATION_TAG, "Membership: MODIFIED is triggered: " + modifiedId);

                                int mListIndex = 0;

                                //Iterate over "mGroupMembershipList" and search for the Item which has been modified.
                                for (String memberShipId : mGroupMembershipList){

                                    memberShipId = mGroupMembershipList.get(mListIndex);

                                    //if match has been found replace the id of the listitem with the new updated id
                                    if (memberShipId.equals(modifiedId)){
                                        Log.d(GROUP_MENU_CREATION_TAG, "Membership: ID MATCH!");
                                        mGroupMembershipList.set(mListIndex, modifiedId);
                                    }

                                    //add +1 to the index value for each iteration
                                    mListIndex++;
                                }
                                break;

                            case REMOVED: //IF A DOCUMENT IS REMOVED, EXECUTE THIS CODE
                                //THE DELETE FUNCTION HAS TO USE AN ITERATOR, AS THERE CANNOT BE CALLED AN REMOVE OPTION WHEN LIST IS ITERATED OVER.
                                String deletedId = documentChange.getDocument().getId();
                                Log.d(GROUP_MENU_CREATION_TAG, "REMOVED is triggered: " + deletedId);

                                for (Iterator<String> iterator = mGroupMembershipList.iterator(); iterator.hasNext(); ){
                                    String memberShipId = iterator.next();
                                    if (memberShipId.equals(deletedId)){
                                        iterator.remove();
                                    }
                                }
                            break;
                        }
                    }

                    Log.d(MENU_GROUP_ITEM_LIST_TAG, " (listenForChangesInGroupDataForMenu) The membershipItemList outgoing size was: " + mGroupMembershipList.size());

                    //After the fill of MembershipList has been finished, call method to fetch the ralating GroupData
                    fetchGroupDataForMenuCreation();
                }
            }
        });
    }

    /***********************************************************************************************
     * THIS METHOD RETRIEVES ALL GROUP DATA FROM THE DATABASE BASED ON THE MEMBERSHIP LIST
     **********************************************************************************************/
//TODO: IN THIS METHOD, THERE IS SOMETHING GOING WONG AND MENU IS POPULATED 3 TIMES
    public void fetchGroupDataForMenuCreation(){

        menuGroupItemList = new ArrayList<>();

        Log.d(GROUP_MENU_CREATION_TAG, "Method 'fetchGroupDataForMenuCreation' has been triggered!");

        for (String groupId : mGroupMembershipList) {

            groupDocRef = db.collection("groups").document(groupId);
            groupDocRef
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Log.d(GROUP_MENU_CREATION_TAG, "Snapshot of GroupData has been retrieved");
                            GroupData groupData = documentSnapshot.toObject(GroupData.class);
                            //This sets the document id as the group id to not store the data redundantly in a document field
                            try{
                                groupData.setId(documentSnapshot.getId());
                            }catch (NullPointerException e){
                                Log.d(GROUP_MENU_CREATION_TAG, "FetchGroup has returned Null " + e.toString());
                                return;
                            }

                            //add the retrived object to Arraylist
                            menuGroupItemList.add(groupData);

                            //TODO: DER BOOLEAN MÜSSTE EIGENDLICH AUSLEÖST WERDEN WENN DIE LETZTE ERFOLGREICHE ITERATION DURCHGEFÜHRT WIRD
                            //set menu to be updated
                            menuShouldBeUpdated = true;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(GROUP_MENU_CREATION_TAG, "Error loading GroupData Snapshot: " + e.toString());
                        }
                    });
        }
    }

    /*********************************************************************************************************************
     * THIS METHOD ITERATES "menuGroupItemList" AND CREATES MENU ITEMS ACCORDINGLY                                       *
     *********************************************************************************************************************/
    public void updateGroupMenuItems(){

        // DELETES THE SHARED GROUPS FROM MENU SO THEY WILL NOT BE ADDED MULTIPLE TIMES ON REFRESH
        menu.findItem (R.id.submenu_groups).getSubMenu().removeGroup(R.id.groups);

        // Iterate over the List of menuGroupItems and create Menu from it.
        for (GroupData groupData : menuGroupItemList){

            Log.d(MENU_GROUP_ITEM_LIST_TAG, " (UpdateGroupMenuItems) The menuGroupItemList size was: " + menuGroupItemList.size());

            //set groupid to the Group for GroupTasks defined in "activity_navigation_drawer"
            groupID = R.id.groups;
            //generate Item identifier and set as ItemID

            //set variables for submenu item
            itemID = groupData.getItemId();
            //itemID = View.generateViewId();
            //set the order to NONE
            itemOrder = Menu.NONE;

            String groupName = groupData.getName();
            int taskCount = groupData.getTaskCount();
            itemTitle = groupName + " (" + taskCount + ")";


            //Add menu items under the "Groups" Submenu and add a menuitemclicklistener
            menuItem = menu.findItem(R.id.submenu_groups).getSubMenu().add(groupID, itemID, itemOrder, itemTitle).setIcon(R.drawable.ic_menu_share)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem itemClicked) {

                            //get the item ID of the clicked menu item
                            int mItemIdPicked = itemClicked.getItemId();
                            Log.d("Menu1", "The ID of the clicked item is: " + mItemIdPicked);

                            // iterate throug the Arraylist and search for an entry with the specific itemID as value of itemID field
                            for (GroupData groupData : menuGroupItemList) {
                                int itemID = groupData.getItemId();
                                //Log.d("Menu", "mID: " + groupData.getId());

                                if (mItemIdPicked == itemID){
                                    Log.d("Menu2", "The ID of the group is: " + itemID);
                                    //Pass group ID to mSelectedGroupID to be used in other activities/Fragments.
                                    mGroupIDforPutExtra = groupData.getId();
                                }
                            }

                            //Put the Activity title based on the Group name listed in the menus item title
                            activityTitle = itemClicked.getTitle().toString();
                            //Set the title of the activity
                            getSupportActionBar().setTitle(activityTitle);

                            //Open Fragment and parse the required itemID for the query of groups
                            Fragment_NavMenu_GroupTasks fragment = new Fragment_NavMenu_GroupTasks();
                            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                            fragmentTransaction.replace(R.id.content_main_navigation, fragment);
                            fragmentTransaction.addToBackStack(null);
                            fragmentTransaction.commit();

                            //Make the AddGroupTask Button Visible
                            //TODO: THERE COULD BE A NICE TRANSITION ANIMATION OF THE TWO BUTTONS

                            fabAddGroupTask.setVisibility(View.VISIBLE);

                            return false;
                        }
                    });


        }
    }

    /*******************************************************************************************
     * THIS METHOD DELETES SELECTED ITEM FROM FIRESTORE DATABASE (IS CALLED BY VIEWADAPTER     *
     *******************************************************************************************/
    public void deleteUserTaskFromDatabase(String taskId){
        Log.d(NAVIGATION_ACTIVITY_TAG, "The provided taskID is: " + taskId);
        Log.d(NAVIGATION_ACTIVITY_TAG, "The userID is: " + userID);

        FirebaseFirestore db = FirestoreHelper.getDatabase();
        DocumentReference taskDocRef = db.collection("users").document("acSIBra6pUcemLPBlHFnZLNuchy2").collection("tasks").document(taskId);
        taskDocRef.delete();
    }

    public void openUserSettings(){
        //On click the new activity is opened and the userData values are stored in a Object, which can be called upon in the new activity.
        Intent userSettingActivityIntent = new Intent(NavigationActivity.this, UserSettingsActivity.class);

        // TODO: Check whats going on with userProfilePhotoUrl a Null reference is thrown for newly registered users if URL is not passed hardcoded.
        //url hardcoded to avoid null reference.
        userProfilePhotoUrl = Uri.parse("https://www.google.de/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Fe%2Fe0%2FSNice.svg%2F1200px-SNice.svg.png&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSmiley&docid=EB-7l6d3ePZ1CM&tbnid=2ibD-NzxUXqVVM%3A&vet=10ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg..i&w=1200&h=1200&client=firefox-b-ab&bih=750&biw=1536&q=smiley&ved=0ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg&iact=mrc&uact=8");
        userSettingActivityIntent.putExtra("userData", currentUserData);
        NavigationActivity.this.startActivity(userSettingActivityIntent);
    }

    public void logOutUser(){
        //unauthorise user from firebase
        mAuth.signOut();

        //After logging out the user the view is navigated back to the login activity
        Intent loginActivityIntent = new Intent(NavigationActivity.this, UserLoginStartActivity.class);
        NavigationActivity.this.startActivity(loginActivityIntent);
    }

    /*******************************************************************************************
     *                                  INITIALIZATION METHODS!                                *
     *******************************************************************************************/
    public void fetchCurrentUserData(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            //If authorization is positive, refresh userID
            String mUserID = user.getUid();
            String mUserDisplayName = user.getDisplayName();
            String mUserMail = user.getEmail();
            String mUserPhone = user.getPhoneNumber();
            String mUserPhotoURL ="";
            try{
                mUserPhotoURL = user.getPhotoUrl().toString();
            }catch (NullPointerException e){
                Log.d(NAVIGATION_ACTIVITY_TAG, "User has no PhotoURL Set");
            }
            Long mUserHashCode = Long.valueOf(user.hashCode());

            //CREATE A NEW USERDATA OBJECT FROM DATA OF THE AUTHLISTENER
            currentUserData = new UserData(mUserID, mUserDisplayName, mUserMail, mUserPhone, mUserPhotoURL, mUserHashCode);
            Log.d(NAVIGATION_ACTIVITY_TAG, "THE RETRIEVED USER ID IS: " + mUserID);
            Log.d(NAVIGATION_ACTIVITY_TAG, "THE RETRIEVED NAME IS: " + mUserDisplayName);
            Log.d(NAVIGATION_ACTIVITY_TAG, "THE RETRIEVED MAIL IS: " + mUserMail);


            //SET THE GLOBAL VARIABLE FOR FREQUENTLY USED "userID"
            userID = currentUserData.getUserId();

            //STORE THE USERID IN SHAREDPREFERENCES
            saveSharedPreferencesUserId(userID);

        }else{
            Toast.makeText(NavigationActivity.this, "User is logged out", Toast.LENGTH_LONG).show();

            Intent userLoginProviderSelectionIntent = new Intent(NavigationActivity.this, UserLoginProviderSelectionActivity.class);
            NavigationActivity.this.startActivity(userLoginProviderSelectionIntent);


            /*Intent userLoginStartActivityIntent = new Intent(NavigationActivity.this, UserLoginStartActivity.class);
            NavigationActivity.this.startActivity(userLoginStartActivityIntent);*/
        }
    }

    /***********************************************************************************************
     * SAVE SHARED PREFERENCES TO FILE
     **********************************************************************************************/
    public void saveSharedPreferencesUserId(String mUserId){
        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = this.getSharedPreferences("mSharePrefFile", 0);
        // 2. Initialize Editor Class
        SharedPreferences.Editor editor = mSharedPref.edit();
        // 3. Get Values from fields and store in Shared Preferences
        editor.putString("userID", mUserId);
        // 4. Store the keys
        editor.apply();
    }

    /***********************************************************************************************
     * THIS METHOD RETRIEVES ALL MEMBERSHIPS DATA FROM THE DATABASE BASED ON THE CURRENT USER ID
     **********************************************************************************************/
    public void fetchMemberShipDataforMenuCreation(){

        //Create new arraylist
        mGroupMembershipList = new ArrayList<>();
        mGroupMembershipList.clear();

        Log.d(GROUP_MENU_CREATION_TAG, "UserID for MembershipSnapshot is: " + userID);

        //Set reference to users membership Collection
        FirebaseFirestore db = FirestoreHelper.getDatabase();
        CollectionReference membershipCollectionRef = db.collection("users").document(userID).collection("memberships");
        membershipCollectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                Log.d(GROUP_MENU_CREATION_TAG, "MembershipSnapshot has been retrieved: " + queryDocumentSnapshots.size());

                for (QueryDocumentSnapshot document : queryDocumentSnapshots){

                    Log.d(GROUP_MENU_CREATION_TAG, "Document id is " + document.getId());

                    //put the ID (GroupID) of the document in the Array for groupMemberships
                    mGroupMembershipList.add(document.getId());

                }

                //After the fill of MembershipList has been finished, call method to fetch the relating GroupData
                fetchGroupDataForMenuCreation();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NavigationActivity.this, "Error loading membership data!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*******************************************************************************************************************************
     * THIS METHOD ENABLES A LISTENER, THAT LISTENS FOR CHANGES IN THE USERS TASKS LIST AND UPDATES THE LIST USED IN EACH FRAGMENT *
     *******************************************************************************************************************************/
    public void listenToChangesOnUserTasks(){

        userTasksCollectionRef = db.collection("users").document(userID).collection("tasks");
        userTasksCollectionRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

               //If there is not error, iterate trough the "changed" documents in the collection and veryfy if they have been added, changed or deleted
               if (e != null){
                   Log.d(NAVIGATION_ACTIVITY_TAG, "ERROR, Could not retrieve user tasks form Firestore! " + e.getMessage());
               }else{

                   List<TaskData> mUpdatedUserTasksList = new ArrayList<>();
                   int index = 0;

                   for (DocumentChange document : queryDocumentSnapshots.getDocumentChanges()){

                       //create a new object of the retrieved document
                       TaskData updatedTask = document.getDocument().toObject(TaskData.class);
                       //set the ID of the document to the ID variable, as this field is not locally stored in the database
                       updatedTask.setId(document.getDocument().getId());

                       switch (document.getType()){

                           case ADDED:
                               //Add the object to the list
                               mUpdatedUserTasksList.add(updatedTask);
                               break;

                           case MODIFIED:
                               //Update the existing object in the list according to the documentChange

                               for (TaskData task : mUpdatedUserTasksList){

                                   //get the id of the iterated existing ListItem
                                   String taskID = task.getId();

                                   //if a the correct existing item has been found, update the set with new data
                                   if (taskID.equals(updatedTask.getId())){
                                       mUpdatedUserTasksList.set(index, updatedTask);
                                   }
                                   // add 1 to the index at each iteration
                                   index++;
                               }
                               break;

                           case REMOVED:
                               //Delete the existing object in the list according to the documentChange
                               for (TaskData task : mUpdatedUserTasksList){

                                   //get the id of the iterated existing ListItem
                                   String taskID = task.getId();

                                   //if a the correct existing item has been found, remove this item based on the index
                                   if (taskID.equals(updatedTask.getId())){
                                       mUpdatedUserTasksList.remove(index);
                                   }
                                   //add 1 to the index at each iteration
                                   index++;
                               }
                               break;
                       }
                   }

                   //After all changes have been retrieved from the database, update all fragments with the new Data
//TODO: implement this!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                   //updateAllFragments(mUpdatedUserTasksList);

                   //Update the menu titles, with the new amount of present tasks
                   updateMenuItemTitlesForUserTasks(mUpdatedUserTasksList);
               }
            }
        });
    }

    /*********************************************************************************************
     * THIS METHOD UPDATES THE MENU TITLES BASED ON THE NEW TASK COUNT IN EACH CATEGORY          *
     *********************************************************************************************/
    public void updateMenuItemTitlesForUserTasks(List<TaskData> mUpdatedUserTaskList){

        //----------------------------------------------
        //UPDATE THE TASK COUNT FOR THE THREE CATEGORIES
        //----------------------------------------------


        //Set the updated counter for the "All tasks" menu-----------------------------------------
        int mCountAllTasks = mUpdatedUserTaskList.size();

        //set the counter for the "favorite tasks" menu--------------------------------------------
        int mCountFavoriteTasks = 0;

        for (TaskData taskData : mUpdatedUserTaskList){
            //retrieve the favorite variable from the iterated object in the list
            Boolean mIsfavorite = taskData.getFavorite();

            if (mIsfavorite != null && mIsfavorite){
                //add the retrieved data to the ArrayList if it fits the requirements
                mCountFavoriteTasks++;
            }
        }

        //set the counter for the "overdue" menu---------------------------------------------------
        int mCountOverdueTasks = 0;
        Date mDateToday;

        Calendar calToday = Calendar.getInstance();
        calToday.set(Calendar.HOUR_OF_DAY, 0);
        calToday.set(Calendar.MINUTE, 0);
        calToday.set(Calendar.SECOND, 0);
        calToday.set(Calendar.MILLISECOND, 0);
        mDateToday = calToday.getTime();

        for (TaskData taskData : mUpdatedUserTaskList){
            //retrieve the favorite variable from the iterated object in the list
            Date mDueDate = taskData.getDuedate();

            //if the retrieved date is not null and earlier or equal than today, add one to the counter
            if (mDueDate != null && mDueDate.compareTo(mDateToday) <= 0){
                //add the retrieved data to the ArrayList if it fits the requirements
                mCountOverdueTasks++;
            }
        }

        //-----------------------------------------------------------
        //UPDATE THE COUNT IN MENU BASED ON THE UPDATED COUNT FIGURES
        //-----------------------------------------------------------

        //set a new title with the list size of each Menu item at the end
        String mNewTitleAll = getResources().getString(R.string.navigation_drawer_submenu_general_allTasks) + " (" + mCountAllTasks + ")";
        menu.findItem(R.id.nav_allTasks).setTitle(mNewTitleAll);

        String mNewTitleFavorite = getResources().getString(R.string.navigation_drawer_submenu_general_favoriteTasks) + " (" + mCountFavoriteTasks + ")";
        menu.findItem(R.id.nav_favoriteTasks).setTitle(mNewTitleFavorite);

        String mNewTitleOverdue = getResources().getString(R.string.navigation_drawer_submenu_general_overdueTasks) + " (" + mCountOverdueTasks + ")";
        menu.findItem(R.id.nav_overdueTasks).setTitle(mNewTitleOverdue);
    }

    /*********************************************************************************************
     * THIS METHOD SENDS A SIGNAL TO ALL FRAGMENTS, TO UPDATE THE CONTENT OF THE RECYCLERVIEWS   *
     *********************************************************************************************/
    public void updateAllFragments(List<TaskData> mUpdatedUserTaskList){
//TODO: CHECK IF FRAGMENT IS ACTIVE; THEN FIRE THE CODE BELOW
        //Update All tasks Fragment
        Fragment_NavMenu_AllTasks fragment_navMenu_allTasks = new Fragment_NavMenu_AllTasks();
        fragment_navMenu_allTasks.updateRecyclerviewWithFreshData(mUpdatedUserTaskList);

        //Update Favorite Tasks Fragment
        Fragment_NavMenu_FavoriteTasks fragment_navMenu_favoriteTasks = new Fragment_NavMenu_FavoriteTasks();
        fragment_navMenu_favoriteTasks.updateRecyclerviewWithFreshData(mUpdatedUserTaskList);

        //Update Overdue Tasks Fragment
        Fragment_NavMenu_OverdueTasks fragment_navMenu_overdueTasks = new Fragment_NavMenu_OverdueTasks();
        fragment_navMenu_overdueTasks.updateRecyclerviewWithFreshData(mUpdatedUserTaskList);
    }
}
