package android_development.taskshare;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_NavMenu_GroupTasks.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class Fragment_NavMenu_GroupTasks extends Fragment {

    public static final String GROUP_TASK_FRAGMENT_TAG = "GroupTaskFragment";
    private OnFragmentInteractionListener mListener;

    //Initialize FirebaseAuth instance
    public String userID;
    public String mGroupID;
    public int mItemId;

    public FirebaseAuth mAuth;
    FirebaseUser currentUser = null;
    FirebaseAuth.AuthStateListener authStateListener;

    //Initialize the recyclerView
    RecyclerView recyclerViewTaskData;
    TaskDataViewAdapter adapter;
    List<TaskData> taskDataListItems;

    //Initialize FireStore References
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference groupTaskCollectionRef;

    public Fragment_NavMenu_GroupTasks() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Define the view object
        // LAYOUT FROM "ALL TASKS" CAN BE REUSED, BECAUSE THEY LOOK IDENTICAL
        View view = inflater.inflate(R.layout.fragment__nav_menu__all_tasks, container, false);
        //return inflater.inflate(R.layout.fragment_nav_menu_group_tasks, container, false);

        //retrieve the userID and Group IF from NavigationActivity
        retrieveDataFromNavigationActivity();

        /***********************************************************************************************
         * Initialize the Arraylist, adapter and set the adapter to the recycleView
         **********************************************************************************************/
        // Create a new instance of a ArrayList; Initialize the above defined listitem object
        taskDataListItems = new ArrayList<>();
        //Initiate the RecyclerView object //map the Recyclerview object to the xml RecyclerView
        // Instanciate a new adapter for the Recycleview and parse the "listitem" and "Context"
        adapter = new TaskDataViewAdapter(taskDataListItems, getContext());

        recyclerViewTaskData = (RecyclerView) view.findViewById(R.id.recyclerViewTaskData);
        //Every item of the recyclerview will have a fixed size
        recyclerViewTaskData.setHasFixedSize(true);
        recyclerViewTaskData.setLayoutManager(new LinearLayoutManager(getContext()));
        //set the adapter to the recyclerview
        recyclerViewTaskData.setAdapter(adapter);


        /***********************************************************************************************
         * SET COLLECTION REFERENCE
         **********************************************************************************************/
        groupTaskCollectionRef = db.collection("groups").document(mGroupID).collection("grouptasks");


        // Inflate the layout for this fragment
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        //CLEAR THE LIST, AS THERE WOULD BE A DUBLICATION OF DATA IF THE METHOD WOULD BE CALLED AGAIN, E.G AFTER A WAKEUP OF THE DEVICE
        taskDataListItems.clear();

        /***********************************************************************************************
         * SET COLLECTION REFERENCE LISTENER TO GROUPTASKDATA, TO LISTEN TO CHANGES IN THE DATACOLLECTION
         **********************************************************************************************/
        groupTaskCollectionRef.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                //Check if there is an error
                if (e != null) {
                    Log.d(Fragment_NavMenu_GroupTasks.GROUP_TASK_FRAGMENT_TAG, "Error!" + e.getMessage());
                }

                // Iterate through the QueryDocumentSnapshot
                for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {

                    //TODO: THIS COULD BETTER BE HANDLD IN A CASE INSTEAD OF IF STATEMENTS!!!!!!!!!!!!!!!
                    //IF A NEW DOCUMENT IS ADDED, EXECUTE THIS CODE
                    if (documentChange.getType() == DocumentChange.Type.ADDED) {

                        Log.d(GROUP_TASK_FRAGMENT_TAG, "ADDED is triggered");

                        //retrieve data as a TaskData object
                        TaskData taskData = documentChange.getDocument().toObject(TaskData.class);
                        //ad the Id to the taskdata object, that has not been stored as fieldvalue
                        taskData.setId(documentChange.getDocument().getId());
                        //add the retrived object to Arraylist
                        taskDataListItems.add(taskData);

                        // After every object from the QueryDocumentSnapshot has been retrieved, notify the Adapter, that the dataset has changed
                        adapter.notifyDataSetChanged();
                    } else {

                        //IF AN EXISTING DOCUMENT IS MODIFIED, EXECUTE THIS CODE
                        if (documentChange.getType() == DocumentChange.Type.MODIFIED) {

                            Log.d(GROUP_TASK_FRAGMENT_TAG, "MODIFIED is triggered");

                            int mListIndex = 0;
                            TaskData taskDataUpdated = documentChange.getDocument().toObject(TaskData.class);
                            //ad the Id to the taskdata object, that has not been stored as fieldvalue
                            taskDataUpdated.setId(documentChange.getDocument().getId());

                            for (TaskData taskData : taskDataListItems) {
                                // retrive the id of the task that has been changed
                                String mTaskID = taskData.getId();

                                Log.d(GROUP_TASK_FRAGMENT_TAG, "mID: " + mTaskID);

                                if (mTaskID.equals(taskDataUpdated.getId())){
                                    Log.d(GROUP_TASK_FRAGMENT_TAG, "ID MATCH!");
                                    //IF the correct id is found in the Data snapshot, change the respective object in the array list
                                    //set the retrieved data to the existing index item of the ArrayList
                                    taskDataListItems.set(mListIndex, taskDataUpdated);
                                }

                                //add +1 to the index value for each iteration
                                mListIndex ++;
                            }

                            // After every object from the QueryDocumentSnapshot has been retrieved, notify the Adapter, that the dataset has changed
                            adapter.notifyDataSetChanged();

                        } else {

                            //IF A DOCUMENT IS DELETED, EXECUTE THIS CODE
                            if (documentChange.getType() == DocumentChange.Type.REMOVED){

                                //THE DELETE FUNCTION HAS TO USE AN ITERATOR, AS THERE CANNOT BE CALLED AN REMOVE OPTION WHEN LIST IS ITERATED OVER.
                                String taskIdDelete = documentChange.getDocument().getId();
                                Log.d(GROUP_TASK_FRAGMENT_TAG, "REMOVED is triggered");

                                for (Iterator<TaskData> iterator = taskDataListItems.iterator(); iterator.hasNext(); ) {
                                    TaskData taskData = iterator.next();
                                    if (taskData.getId().equals(taskIdDelete)) {
                                        iterator.remove();
                                    }
                                }

                                // After every object from the QueryDocumentSnapshot has been retrieved, notify the Adapter, that the dataset has changed
                                adapter.notifyDataSetChanged();
                            }
                        }

                    }
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public void retrieveDataFromNavigationActivity(){

        NavigationActivity navigationActivity;
        navigationActivity = (NavigationActivity) getActivity();

        //attach public variable to local variable
        userID = navigationActivity.userID;
        mGroupID = navigationActivity.mGroupIDforPutExtra;

        Log.d(GROUP_TASK_FRAGMENT_TAG, "Group ID is: " + mGroupID);
    }
}
