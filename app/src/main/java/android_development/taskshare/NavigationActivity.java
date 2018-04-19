package android_development.taskshare;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //Initialize variables for Userdata
    public String userID ="";
    public String userName;
    public String userMail;
    public String userPassword;
    public Uri userProfilePhotoUrl;

    //Initialize Objects for User data in NavHeader
    public ImageView ivUserProfile;
    public TextView tvUserName;
    public TextView tvUserMail;
    public Button btnUserSetting;
    public Button btnTest;

    //Initialize FirebaseAuth instance
    public FirebaseAuth mAuth;
    FirebaseUser currentUser = null;
    FirebaseAuth.AuthStateListener authStateListener;

    //Define TAG variable for output messages during debug
    private static final String TAG = "EmailPassword";

    // Initialize the Firebase Database instance and query
    FirebaseDatabase database;
    DatabaseReference dbRef;
    DatabaseReference dbRefForMenu;
    Query query;
    String queryContent;

    //Initialize recyclerview that will be filled with data and enable swipelayout
    public RecyclerView recyclerViewTaskData;
    SwipeRefreshLayout mSwipeRefreshLayout;

    public TaskDataViewAdapter adapter;
    public TaskDataViewAdapter filterAdapter;


    //Initialize ArrayList for datasnapshot for recyclerview
    private List<TaskData> taskDataListItems;
    private List<TaskData> filteredList;

    // adding a section and items into menu
    Menu menu;
    SubMenu subMenuGroups;
    int groupID;
    int itemID;
    int itemIdentifyer = 1;
    int itemOrder = 0;
    String itemTitle;
    String activityTitle;
    int index;
    int itemToBeRemovedIndex;

    //VARIABLES FOR MENU GROUP ITEMS
    private List<GroupData> menuGroupItemList;
    private List<Integer> menuIdVariables;

    //Initialize progressdialog object
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Set default Fragment if no Saved Instance is available
        if (savedInstanceState == null) {
            Fragment_NavMenu_AllTasks fragment= new Fragment_NavMenu_AllTasks();
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
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        menu = navigationView.getMenu();
        //subMenuGroups = (SubMenu) findViewById(R.id.groupMenu);
        //subMenuGroups = subMenuGroups.getItem(R.id.groupMenu);
        subMenuGroups = menu.addSubMenu(R.string.navigation_drawer_submenu_group_title).setIcon(R.drawable.ic_action_alarm_yellow);



        //------------------------------------------------------------------------------------------
        //Initialize object fields for User data // SET VIEW FOR HEADER VIEW FIRST!!!!
        View header = navigationView.getHeaderView(0);

        //Initialize Firebase objects
        database = FirebaseHelper.getDatabase();
        dbRef = database.getReference();
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



        /***********************************************************************************************
         *
         * METHOD TO ADD/DELETE/CHANGE MENU ITEMS IN DRAWER MENU
         *
         **********************************************************************************************/

        // Create a new instance of a ArrayList; Initialize the above defined listitem object
        menuGroupItemList = new ArrayList<>();
        // set the database reference to the correct child object (TaskData)
        //TODO: CHANGE THE HARD CODED GROUP CODE TO SOMETHING ELSE
        DatabaseReference dbRefGroup = dbRef.child("group");
        Query query = dbRefGroup;
        //Query query = dbRefGroup.equalTo(userID);

        //groupID = R.id.groupMenu;

        //set a ValueEventlistener to the database reference that listens if changes are being made to the data
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                GroupData groupData = dataSnapshot.getValue(GroupData.class);
                //add the retrieved data to the ArrayList
                menuGroupItemList.add(groupData);
                //generate Item identifier and set as ItemID
                itemID = View.generateViewId();
                //itemID = menuGroupItemList.size()-1;
                //set variables for submenu item
                itemTitle = groupData.getName();
                //add submenu item with the specified variables
                subMenuGroups.add(groupID, itemID, itemOrder, itemTitle);
                //subMenuGroups.add(itemTitle).setIcon(R.drawable.ic_menu_share);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Log.d("Index", "Index: " + dataSnapshot.getKey());
                index = 0;
                String snapShotKey = dataSnapshot.getKey();
                //String snapShotKey = "xxxx";

                Log.d("Menu", "snapShotKey: " + dataSnapshot.getKey());

                for (GroupData groupData : menuGroupItemList) {
                    String mId = groupData.getId();
                    //String mId = "xxxx";
                    Log.d("Menu", "mID: " + groupData.getId());

                    if (mId.equals(snapShotKey)){
                        Log.d("Menu", "ID MATCH!");
                        //IF the correct id is found in the Data snapshot, change the respective object in the array list
                        groupData = dataSnapshot.getValue(GroupData.class);
                        //set the retrieved data to the existing index item of the ArrayList
                        menuGroupItemList.set(index, groupData);
                        //change the Menu Title based on the datachanges
                        itemTitle = groupData.getName();
                        subMenuGroups.getItem(index).setTitle(itemTitle);
                    }
                    //add +1 to the index value for each iteration
                    index ++;
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                int index = 0;
                //get the ID of the datasnapshot (which is the UID of the Group database entry)
                String snapShotKey = dataSnapshot.getKey();

                //Iterate through the existing Arraylist and look for that specific Group entry.
                for (GroupData groupData : menuGroupItemList) {

                    //get the Item ID of the iterated item in the arraylist
                    String arrayItemID = groupData.getId();

                    //If a match of Snapshot ID and ArrayItemID has been found...
                    if (arrayItemID.equals(snapShotKey)){
                        //...set the index of the item that should be removed
                        itemToBeRemovedIndex = index;
                        Log.d("Menu", "The index of the Item to be removed is " + itemToBeRemovedIndex);

                    }
                    //add +1 to the index value for each iteration
                    index ++;
                }

                //remove the respective item from the existing index of the ArrayList
                menuGroupItemList.remove(itemToBeRemovedIndex);
                //remove the item from the menu
                itemID = subMenuGroups.getItem(itemToBeRemovedIndex).getItemId();
                subMenuGroups.removeItem(itemID);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Start the addTask Activity upon click
                Intent addTaskActivityIntent = new Intent(NavigationActivity.this, AddTaskActivity.class);
                addTaskActivityIntent.putExtra("userID", userID);
                addTaskActivityIntent.putExtra("dbRef", dbRef.toString());
                NavigationActivity.this.startActivity(addTaskActivityIntent);
            }
        });

        /***********************************************************************************************
         *
         * Authstate listener will listen to changes in the user authorization state
         *
         **********************************************************************************************/
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                currentUser = mAuth.getCurrentUser();

                if (currentUser == null){
                    Toast.makeText(NavigationActivity.this, "User is logged out", Toast.LENGTH_LONG).show();
                    Intent userLoginActivityIntent = new Intent(NavigationActivity.this, UserLoginStartActivity.class);
                    NavigationActivity.this.startActivity(userLoginActivityIntent);
                }

                //If authorization is positive, refresh userID
                userID = currentUser.getUid();

                //add the variable to SharedPreference
                // 1. Open Shared Preference File
                SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);
                // 2. Initialize Editor Class
                SharedPreferences.Editor editor = mSharedPref.edit();
                // 3. Get Values from fields and store in Shared Preferences
                editor.putString("userID", userID);
                // 5. Store the keys
                editor.commit();
            }
        };

        //Initialize Firebase Authorization and activate AuthStateListener
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(authStateListener);

        /***********************************************************************************************
         * Method to update the user profile UI in the Drawer Menu
         **********************************************************************************************/
        updateUserdataUI(currentUser);

        /***********************************************************************************************
         * Method to update the to update the recyclerview
         **********************************************************************************************/
        prefillRecycleView();

        //TODO: SET FRAGMENT FOR STARTSCREEN
        /* NAVIGATION DRAWER: Show a specific fragment as start screen upon loading (IN THIS CASE THE EXPENSEHISTORY FRAGMENT)*/
        //displaySelectedScreen(R.id.nav_ExpenseHistory);
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

    /***********************************************************************************************
     * Search Menu Tutorial at:
     * https://stackoverflow.com/questions/30398247/how-to-filter-a-recyclerview-with-a-searchview
     **********************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

/*        for (int i= 0; i == menuGroupItemList.size()-1; i++){
            MenuItem item = menu.findItem(i);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //do stuff
                    Toast.makeText(NavigationActivity.this, "You clicked on item: " + itemTitle, Toast.LENGTH_LONG).show();
                    return true;
                }
            });
        }*/
        super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search_view, menu);

        final MenuItem searchItem = menu.findItem(R.id.mSearchView);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {
                // Here is where we are going to implement the filter logic
                Log.d("Looking for: ", queryText);
                executeQuery(queryText);
                return false;
            }
        });

        return true;
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

/*        if (id == subMenuGroups.getItem(0).getItemId()){
            // do stuff
        }*/

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
                //initialize fragment
                fragment = new Fragment_NavMenu_AllTasks();
                break;
            case R.id.nav_favoriteTasks:
                //Set the title of the Activity according to the Fragment
                activityTitle = getResources().getString(R.string.title_fragment_favoriteTasks);
                //initialize fragment
                fragment = new Fragment_NavMenu_FavoriteTasks();
                break;
            case R.id.nav_slideshow:
                // open settings TEST activity
                Intent settingsTestActivityIntent = new Intent(NavigationActivity.this, SettingsActivity2.class);
                NavigationActivity.this.startActivity(settingsTestActivityIntent);
                break;
            case R.id.nav_manage:
                // open settings activity
                Intent settingsActivityIntent = new Intent(NavigationActivity.this, SettingsActivity.class);
                NavigationActivity.this.startActivity(settingsActivityIntent);
                break;
            /*case idVariable1:
                //do stuff
                break;
            case idVariable2:
                //do stuff
                break;
            case idVariable3:
                //do stuff
                break;
            case idVariable4:
                //do stuff
                break;*/
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
            // change icon to arrow drawable
            //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_done);
        }

        //the navigation drawer will be closed after selecting an item
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mAuth.addAuthStateListener(authStateListener);
        adapter.notifyDataSetChanged();
        //updateUserdataUI(currentUser);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(authStateListener);
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


    private void prefillRecycleView(){

        /***********************************************************************************************
         * Initialize the Arraylist, adapter and set the adapter to the recycleView
         **********************************************************************************************/

        // Create a new instance of a ArrayList; Initialize the above defined listitem object
        taskDataListItems = new ArrayList<>();
        // Instanciate a new adapter for the Recycleview and parse the "listitem" and "Context"
        adapter = new TaskDataViewAdapter(taskDataListItems, NavigationActivity.this);
        //set the adapter to the recyclerview
        //recyclerViewTaskData.setAdapter(adapter);

        // set the database reference to the correct child object (TaskData)
        dbRef = dbRef.child("taskdata").child(userID);
        Query query = dbRef.orderByChild("datecreated");

        //set a ValueEventlistener to the database reference that listens if changes are being made to the data
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //delete all items from the list
                taskDataListItems.clear(); //TODO: implement funtion that only single dataset is changed if necessary

                //returns a collection of the children under the set database reference
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                // Shake hands with each of the collected childrens
                //Iterate over the collection of "children" specified above and put it into a variable called "child"
                for (DataSnapshot child : children ) {
                    //child.getValue(TravelExpenseData.class); "VOR STRG + ALT +V"
                    TaskData taskData = child.getValue(TaskData.class);
                    //add the retrieved data to the ArrayList
                    taskDataListItems.add(taskData);
                }

                // notify the adapter that data has been changed and needs to be refreshed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /***********************************************************************************************
     * Method will be called every time a figure has been changed in the search bar
     **********************************************************************************************/
    public void executeQuery(String queryText){
        // 1. Create a new array list for the items matching the query
        ArrayList<TaskData> filteredList = new ArrayList<>();
        // 2. for every Item in the list displayed by the recycleview...
        for (TaskData taskData : taskDataListItems){

            // 3. ...make the data and query case insensitive and search for the query and add it to the newly created
            if (taskData.getContent().toLowerCase().contains(queryText.toLowerCase())){
                filteredList.add(taskData);
            }

            adapter.filterList(filteredList);
        }
    }

    public void getListWithItemIDs(){
        //TODO: CREATE NEW STRING VARIABLES BASED ON ID
        menuIdVariables = new ArrayList<Integer>();
        for (int i = 0; i == menuGroupItemList.size()-1; i++){
            menuIdVariables.add(subMenuGroups.getItem(i).getItemId());
            Log.d("Menu", "ITEM ID TEST: " + subMenuGroups.getItem(i).getItemId());
            Log.d("Menu", "ID COUNT: " + menuIdVariables.size());
        }
    }

}
