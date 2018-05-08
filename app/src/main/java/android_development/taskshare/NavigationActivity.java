package android_development.taskshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
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
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String FIRESTORE_TAG = "Firestore";
    public static final String USER_AUTH_TAG = "UserAuth";
    //Initialize variables for Userdata
    public String userID = "";
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
    public FirebaseUser currentUser = null;
    public FirebaseAuth.AuthStateListener mAuthstateListener;
    public FirebaseAuth.AuthStateListener authStateListener;

    //Define TAG variable for output messages during debug
    private static final String TAG = "EmailPassword";

    //Initialize Firestore Database instances
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference groupRef = db.collection("groups");
    DocumentReference groupDocRef;
    CollectionReference membershipCollectionRef;
    CollectionReference taskRef = db.collection("tasks");
    DocumentReference docRef;

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

    int groupID;
    String mGroupIDforFragment;
    int itemID;
    int itemOrder = 0;
    String itemTitle;
    String activityTitle;
    int index;
    int itemToBeRemovedIndex;

    //VARIABLES FOR MENU GROUP ITEMS
    public Map<String, GroupData> groupHashMap = new HashMap<String, GroupData>();
    public Map<String, MemberData> memberHashMap = new HashMap<String, MemberData>();
    private List<String> mGroupMembershipList;
    private List<GroupData> menuGroupItemList;
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
                    Intent userLoginStartActivityIntent = new Intent(NavigationActivity.this, UserLoginStartActivity.class);
                    NavigationActivity.this.startActivity(userLoginStartActivityIntent);
                }

                if (mCurrentUser != null) {
                    //If authorization is positive, refresh userID
                    String mUserID = mCurrentUser.getUid();
                    Long mUserHashCode = Long.valueOf(mCurrentUser.hashCode());

                    saveSharedPreferences(mUserID, mUserHashCode);

                    Log.d("User1", "ActivUser: " + mUserID);
                    Log.d("User2", "ActivUserHash: " + mUserHashCode);
                }
            }
        };

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthstateListener);

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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //load shared preference, to get the updated variable of listsize
                loadSharedPreferencesListSizes();

                //set a new title with the list size of each Menu item at the end
                String mNewTitleAll = getResources().getString(R.string.navigation_drawer_submenu_general_allTasks) + " (" +taskDataListAllSize.toString()+")";
                menu.findItem(R.id.nav_allTasks).setTitle(mNewTitleAll);

                String mNewTitleFavorite = getResources().getString(R.string.navigation_drawer_submenu_general_favoriteTasks) + " (" +taskDataListFavoriteSize.toString()+")";
                menu.findItem(R.id.nav_favoriteTasks).setTitle(mNewTitleFavorite);

                String mNewTitleOverdue = getResources().getString(R.string.navigation_drawer_submenu_general_overdueTasks) + " (" +taskDataListOverdueSize.toString()+")";
                menu.findItem(R.id.nav_overdueTasks).setTitle(mNewTitleOverdue);
            }
        };
        // Set the drawer toggle as the DrawerListener
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //declare menu variable to be used in different methods
        menu = navigationView.getMenu();

        //------------------------------------------------------------------------------------------
        //Initialize object fields for User data // SET VIEW FOR HEADER VIEW FIRST!!!!
        View header = navigationView.getHeaderView(0);

        //Initialize Firebase objects
        database = FirebaseHelper.getDatabase();
        mDatabaseMembership = FirebaseHelper.getDatabase();
        dbRef = database.getReference();
        mMembershipDBReference = mDatabaseMembership.getReference();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        //Initialize view objects
        ivUserProfile = header.findViewById(R.id.ivUserProfile);
        tvUserName = header.findViewById(R.id.tvUserName);
        tvUserMail = header.findViewById(R.id.tvUserMail);
        btnUserSetting = header.findViewById(R.id.btnUserSetting);
        btnUserSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //On click the new activity is opened and the userData values are stored in a Object, which can be called upon in the new activity.
                Intent userSettingActivityIntent = new Intent(NavigationActivity.this, UserSettingsActivity.class);

                // TODO: Check whats going on with userProfilePhotoUrl a Null reference is thrown for newly registered users if URL is not passed hardcoded.
                //url hardcoded to avoid null reference.
                userProfilePhotoUrl = Uri.parse("https://www.google.de/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Fe%2Fe0%2FSNice.svg%2F1200px-SNice.svg.png&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSmiley&docid=EB-7l6d3ePZ1CM&tbnid=2ibD-NzxUXqVVM%3A&vet=10ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg..i&w=1200&h=1200&client=firefox-b-ab&bih=750&biw=1536&q=smiley&ved=0ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg&iact=mrc&uact=8");
                UserData userData = new UserData(userID, userName, userMail, userProfilePhotoUrl.toString());
                userSettingActivityIntent.putExtra("userData", userData);
                NavigationActivity.this.startActivity(userSettingActivityIntent);
            }
        });

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
                addGroupTaskActivityIntent.putExtra("dbRef", dbRef.toString());
                NavigationActivity.this.startActivity(addGroupTaskActivityIntent);
            }
        });

        /***********************************************************************************************
         * Method to update the user profile UI in the Drawer Menu
         **********************************************************************************************/
        updateUserdataUI(currentUser);
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
        if (id == R.id.action_settings) {
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
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(mAuthstateListener);


        /***********************************************************************************************
         * JUST FOR TESTING PURPOSES
         * TODO: REMOVE AFTER TEST
         **********************************************************************************************/
        if (userID == null){
            Log.d(USER_AUTH_TAG, "Error, user is null!");
        }else {
            loadMemberships();
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
    public void updateUserdataUI(FirebaseUser currentUser){
        if (currentUser != null) {
            // Name, email address, and profile photo Url

            userID = currentUser.getUid();
            userName = currentUser.getDisplayName();
            userMail = currentUser.getEmail();
            userProfilePhotoUrl = currentUser.getPhotoUrl();
            //TODO: Implement default profile picture if photoURL is Null

            // Check if user's email is verified
            boolean emailVerified = currentUser.isEmailVerified();

            //update ui
            ivUserProfile.setImageURI(userProfilePhotoUrl);
            tvUserName.setText(userName);
            tvUserMail.setText(userMail);

            //Downloads UserPic and updates ImageView
            downloadUserPic();
        }
    }

    public void downloadUserPic(){

        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Reference to an image file in Firebase Storage
        //TODO: Implement a Query string based on variables; not hard coded
        StorageReference storageReferencePicURL = storageRef.child("images/OQ7CjskosBasyXoBB5BtkkfJ92G2/787A5361.jpg");

        // Load the image using Glide
        Glide.with(this /* context */)
                .using(new FirebaseImageLoader())
                .load(storageReferencePicURL)
                .into(ivUserProfile);
    }

    /***********************************************************************************************
     * LOAD SHARED PREFERENCES FROM FILE
     **********************************************************************************************/
    public void loadSharedPreferencesListSizes(){

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);
        // 2. Key Reference from SharePrefFile to fields (If key dows not exist, the default value will be loaded
        taskDataListAllSize = (mSharedPref.getInt("listSizeTasksAll", 0));
        taskDataListFavoriteSize = (mSharedPref.getInt("listSizeTasksFavorite", 0));
        taskDataListOverdueSize = (mSharedPref.getInt("listSizeTasksOverdue", 0));
    }

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
     **********************************************************************************************/
    public void loadMemberships(){

        Log.d(FIRESTORE_TAG, "Fetch method has been executed");

        membershipCollectionRef = db.collection("users").document("acSIBra6pUcemLPBlHFnZLNuchy2").collection("memberships");
        membershipCollectionRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(NavigationActivity.this, "Error loading membership data!", Toast.LENGTH_SHORT).show();
                    Log.d(NavigationActivity.FIRESTORE_TAG, e.toString());
                } else {
                    Log.d(FIRESTORE_TAG, "Snapshot has been retrieved: " + queryDocumentSnapshots.size());

                    // get data from snapshot
                    mGroupMembershipList = new ArrayList();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        //add id of group Dokument and post it into array list.
                        mGroupMembershipList.add(queryDocumentSnapshot.getId());
                    }
                }

                //ITERATE OVER THE ARRAY AND SETUP QUERIES TO FIRESTORE ACCORDINGLY
                for (String groupId : mGroupMembershipList) {
                    Log.d("ArrayTest", "Array: " + groupId);

                    groupDocRef = db.collection("groups").document(groupId);
                    groupDocRef
                            .get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    GroupData groupData = documentSnapshot.toObject(GroupData.class);

                                    //This sets the document id as the group id to not store the data redundantly in a document field
                                    groupData.setId(documentSnapshot.getId());

                                    /***********************************************************************************************
                                     * BUILD THE MENU OUT OF THE FETCHED ITEM
                                     **********************************************************************************************/

                                    //Create new arraylist
                                    menuGroupItemList = new ArrayList<>();

                                    //add the retrived object to Arraylist
                                    menuGroupItemList.add(groupData);

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
                                                            mGroupIDforFragment = String.valueOf(itemID);
                                                        }
                                                    }

                                                    //Put the Activity title based on the Group name listed in the menus item title
                                                    activityTitle = itemClicked.getTitle().toString();
                                                    //Set the title of the activity
                                                    getSupportActionBar().setTitle(activityTitle);

                                                    //Open Fragment and parse the required itemID for the query of groups
                                                    Fragment_NavMenu_GroupTasks fragment = new Fragment_NavMenu_GroupTasks();
                                                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                                                    fragmentTransaction.add(R.id.content_main_navigation, fragment);
                                                    fragmentTransaction.commit();

                                                    //Make the AddGroupTask Button Visible
                                                    //TODO: THERE COULD BE A NICE TRANSITION ANIMATION OF THE TWO BUTTONS

                                                    fabAddGroupTask.setVisibility(View.VISIBLE);

                                                    //TODO: PUTEXTRA METHOD WITH PARSING THE GroupID to be used in the opened GroupActivity


                                                    return false;
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(FIRESTORE_TAG, "Error retieving group data: " + e.toString());
                                }
                            });
                }
            }
        });
    }

    /***********************************************************************************************
     * THIS METHOD RETRIEVES ALL GROUP DATA FROM THE DATABASE BASED ON THE USERS MEMBERSHIP
     **********************************************************************************************/
    public void loadGroupData(){

        //THE ORDER BY METHOD CAN ONLY BE EXECUTED ON THE SAME FIELD AS THE WHERE STATEMENT!!!!!!!!
        groupRef.whereEqualTo("owner", "useridxxyx")
                .whereEqualTo("groupId", "groupidxyxy")
                //The first order by has to match the first where statement field
                .orderBy("owner", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .orderBy("taskCount", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //Iterate trough the documentsnapshot and retrieve the group objects (no need to check if the QueryDocumentSnapshot exists, because it always exists)
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                            GroupData group = documentSnapshot.toObject(GroupData.class);
                            //This sets the document id as the group id to not store the data redundantly in a document field
                            group.setId(documentSnapshot.getId());

                            //build the menu out of the fetched objects


                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(FIRESTORE_TAG, e.toString());
                    }
                });

        /***********************************************************************************************
         * TODO: ADD THIS TO THE "ON START" METHOD
         **********************************************************************************************/
        groupRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                //check if there is an error
                if (e != null){
                    Toast.makeText(NavigationActivity.this, "Error loading groups!", Toast.LENGTH_SHORT).show();
                    Log.d(FIRESTORE_TAG, e.toString());
                    //Important to step out of this method if there is an error, to not run into an app crash
                    return;
                }else {
                    //Iterate trough the documentsnapshot and retrieve the group objects (no need to check if the QueryDocumentSnapshot exists, because it always exists)
                    for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
                        GroupData group = documentSnapshot.toObject(GroupData.class);
                        //This sets the document id as the group id to not store the data redundantly in a document field
                        group.setId(documentSnapshot.getId());
                    }
                }
            }
        });
    }
}
