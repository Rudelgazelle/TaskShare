package android_development.taskshare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A placeholder fragment containing a simple view.
 */
public class AddGroupFragment extends Fragment {

    //define variables
    public String userID;

    // Initialize the Firebase Database instance
    public FirebaseDatabase database;
    public DatabaseReference dbRef;




    public AddGroupFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_group, container, false);

        /*//return inflater.inflate(R.layout.fragment_add_group, container, false);

        *//***********************************************************************************************
         * METHOD TO SAVE DATA FROM FIELDS TO DATABASE
         **********************************************************************************************//*

        final EditText etGroupName = (EditText) view.findViewById(R.id.etGroupName);

        ImageButton ibtnSave = (ImageButton) view.findViewById(R.id.ibtnSave);
        ibtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //set variable values for content to be stored
                String mGroupID = dbRef.push().getKey();

                //DUMMY DATA in the MEMBERSLIST
                Map<String, MemberData> memberMap = new HashMap<>();
                //List<String> mMemberList = new ArrayList<>();
                //mMemberList.add("Member 1");
                //mMemberList.add("Member 2");
                //mMemberList.add("Member 3");
                String mGroupName = etGroupName.getText().toString();
                String mGroupOwner = userID;

                //Load Shared preferences from file (e.g. userID)
                loadSharedPreferences();

                //If user is logged in, get the uniqueID of the current user and store dataset in the Database
                if (userID != null) {

                    GroupData groupData = new GroupData(mGroupID, memberMap, mGroupName, mGroupOwner);
                    //GroupData groupData = new GroupData(mGroupID, mMemberList, mGroupName, mGroupOwner);
                    dbRef.child("groupdata").child(mGroupID).setValue(groupData);
                }

                //Show snackbar with message, that data has been stored to the database
                Snackbar.make(view, "Data has been stored in google Firebase", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();


                //After saving procedure the view is navigated back to the main menu
                Intent NavigationActivityIntent = new Intent(getContext(), NavigationActivity.class);
                getContext().startActivity(NavigationActivityIntent);
            }
        });*/

        // Inflate the layout for this fragment
        return view;
    }

    /***********************************************************************************************
     * LOAD SHARED PREFERENCES FROM FILE
     **********************************************************************************************/
    public void loadSharedPreferences(){

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getContext().getSharedPreferences("mSharePrefFile", 0);
        // 2. Key Reference from SharePrefFile to fields (If key dows not exist, the default value will be loaded
        userID = (mSharedPref.getString("userID", null));
    }
}
