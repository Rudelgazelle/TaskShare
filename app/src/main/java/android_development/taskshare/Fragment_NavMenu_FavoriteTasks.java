package android_development.taskshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Fragment_NavMenu_FavoriteTasks.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Fragment_NavMenu_FavoriteTasks#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Fragment_NavMenu_FavoriteTasks extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Initialize the recyclerView
    RecyclerView recyclerViewTaskData;

    public Fragment_NavMenu_FavoriteTasks() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Fragment_NavMenu_FavoriteTasks.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment_NavMenu_FavoriteTasks newInstance(String param1, String param2) {
        Fragment_NavMenu_FavoriteTasks fragment = new Fragment_NavMenu_FavoriteTasks();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Define the view object
        View view = inflater.inflate(R.layout.fragment__nav_menu__favorite_tasks, container, false);
        //Initiate the RecyclerView object //map the Recyclerview object to the xml RecyclerView
        recyclerViewTaskData = (RecyclerView) view.findViewById(R.id.recyclerViewTaskData);

        /***********************************************************************************************
         * Initialize the Arraylist, adapter and set the adapter to the recycleView
         **********************************************************************************************/

        //Every item of the recyclerview will have a fixed size
        recyclerViewTaskData.setHasFixedSize(true);
        recyclerViewTaskData.setLayoutManager(new LinearLayoutManager(getContext()));

        // Create a new instance of a ArrayList; Initialize the above defined listitem object
        final List<TaskData> taskDataListItems = new ArrayList<>();
        // Instanciate a new adapter for the Recycleview and parse the "listitem" and "Context"
        final TaskDataViewAdapter adapter = new TaskDataViewAdapter(taskDataListItems, getContext());
        //set the adapter to the recyclerview
        recyclerViewTaskData.setAdapter(adapter);

        // set the database reference to the correct child object (TaskData)
        //Initialize Firebase objects
        FirebaseDatabase database = FirebaseHelper.getDatabase();
        DatabaseReference dbRef = database.getReference();

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getContext().getSharedPreferences("mSharePrefFile", 0);

        // 2. ID Reference from SharePrefFile to fields (If id does not exist, the default value will be loaded)
        String userID = (mSharedPref.getString("userID", null));

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
                    Boolean mIsfavorite = taskData.getFavorite();

                    if (mIsfavorite != null && mIsfavorite){
                        //add the retrieved data to the ArrayList if it fits the requirements
                        taskDataListItems.add(taskData);
                    }
                }

                // notify the adapter that data has been changed and needs to be refreshed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


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
}
