package android_development.taskshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_NavMenu_OverdueTasks.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_NavMenu_OverdueTasks#newInstance} factory method to
 * create an instance of this fragment.
 */

public class Fragment_NavMenu_OverdueTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Initialize the recyclerView
    RecyclerView recyclerViewTaskData;
    TaskDataViewAdapter adapter;
    List<TaskData> taskDataListItems;

    //Firebase User ID of current User
    String userID;

    //for storing the listsize of the tasklist array
    int mListSize;

    //Date field for comparison of DueDate and today
    Date mDateToday = null;

    private OnFragmentInteractionListener mListener;

    public Fragment_NavMenu_OverdueTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_NavMenu_OverdueTasks.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_NavMenu_OverdueTasks newInstance(String param1, String param2) {
        Fragment_NavMenu_OverdueTasks fragment = new Fragment_NavMenu_OverdueTasks();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //This enables the search menu function --> further implementation in "onPrepareOptionsMenu"
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Define the view object
        View view = inflater.inflate(R.layout.fragment__nav_menu__all_tasks, container, false);

        //retreive todays Date if mDateToday is null
        if (mDateToday == null) {
            Calendar calToday = Calendar.getInstance();
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);
            mDateToday = calToday.getTime();
        }

        /***********************************************************************************************
         * Initialize the Arraylist, adapter and set the adapter to the recycleView
         **********************************************************************************************/
        //Initiate the RecyclerView object //map the Recyclerview object to the xml RecyclerView
        recyclerViewTaskData = (RecyclerView) view.findViewById(R.id.recyclerViewTaskData);
        //Every item of the recyclerview will have a fixed size
        recyclerViewTaskData.setHasFixedSize(true);
        recyclerViewTaskData.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create a new instance of a ArrayList; Initialize the above defined listitem object
        taskDataListItems = new ArrayList<>();
        // Instanciate a new adapter for the Recycleview and parse the "listitem" and "Context"
        adapter = new TaskDataViewAdapter(taskDataListItems, getContext());
        //set the adapter to the recyclerview
        recyclerViewTaskData.setAdapter(adapter);

        // set the database reference to the correct child object (TaskData)
        //Initialize Firebase objects
        FirebaseDatabase database = FirebaseHelper.getDatabase();
        DatabaseReference dbRef = database.getReference();

        //retrieve UserId of current logged in User
        retrieveUserID();

        dbRef = dbRef.child("userdata").child(userID).child("tasks");
        Query query = dbRef.orderByChild("datecreated");

        // add Child eventlistener to the database reference, this updates the Arraylist and notifies the Adapter of the recyclerview, that the data has been changed
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //retrieve data as a TaskData object
                TaskData taskData = dataSnapshot.getValue(TaskData.class);
                Date dueDate = taskData.getDuedate();

                if (dueDate != null){
                    //if duedate is equal or after today add the object to the list
                    if (mDateToday.compareTo(dueDate) >= 0){
                        //add the retrieved data to the ArrayList
                        taskDataListItems.add(taskData);
                    }
                }

                // notify the adapter that data has been changed and needs to be refreshed
                adapter.notifyDataSetChanged();

                //save the listsize to shared preferences
                mListSize = taskDataListItems.size();
                saveSharedPreferencesListSize(mListSize);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                int mListIndex = 0;
                TaskData taskDataUpdated = dataSnapshot.getValue(TaskData.class);
                String snapShotKey = dataSnapshot.getKey();

                for (TaskData taskData : taskDataListItems) {
                    // retrive the id of the task that has been changed
                    String mTaskID = taskData.getId();

                    if (mTaskID.equals(snapShotKey)){
                        //IF the correct id is found in the Data snapshot, change the respective object in the array list
                        //set the retrieved data to the existing index item of the ArrayList
                        taskDataListItems.set(mListIndex, taskDataUpdated);
                    }

                    //add +1 to the index value for each iteration
                    mListIndex ++;
                }

                // notify the adapter that data has been changed and needs to be refreshed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                int mListIndex = 0;
                String snapShotKey = dataSnapshot.getKey();

                for (TaskData taskData : taskDataListItems) {
                    String mTaskID = taskData.getId();

                    if (mTaskID.equals(snapShotKey)){
                        //IF the correct id is found in the Data snapshot, remove the respective object in the array list
                        taskDataListItems.remove(mListIndex);
                    }

                    //add +1 to the index value for each iteration
                    mListIndex ++;
                }

                // notify the adapter that data has been changed and needs to be refreshed
                adapter.notifyDataSetChanged();

                //save the listsize to shared preferences
                mListSize = taskDataListItems.size();
                saveSharedPreferencesListSize(mListSize);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    //This picks the searchview from Resources.
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem mSearchMenuItem = menu.findItem(R.id.mSearchView);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.search_view, menu);
        //getMenuInflater().inflate(R.menu.search_view, menu);

        MenuItem searchItem = menu.findItem(R.id.mSearchView);
        SearchView searchView = (SearchView) searchItem.getActionView();

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
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
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

    /***********************************************************************************************
     * SAVE SHARED PREFERENCES TO FILE
     **********************************************************************************************/
    public void saveSharedPreferencesListSize(int mListSize){
        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getContext().getSharedPreferences("mSharePrefFile", 0);
        // 2. Initialize Editor Class
        SharedPreferences.Editor editor = mSharedPref.edit();
        // 3. Get Values from fields and store in Shared Preferences
        editor.putInt("listSizeTasksOverdue", mListSize);
        // 4. Store the keys
        editor.commit();
    }

    public void retrieveUserID(){

        NavigationActivity navigationActivity;
        navigationActivity = (NavigationActivity) getActivity();

        //attach public variable to local variable
        userID = navigationActivity.userID;
    }

    /***********************************************************************************************
     * Update Data in Recyclerview based on refreshed List of NavigationActivity                   *
     ***********************************************************************************************/
    public void updateRecyclerviewWithFreshData(List<TaskData> updatedTaskDataList){
        taskDataListItems = updatedTaskDataList;
        adapter.notifyDataSetChanged();
    }
}
