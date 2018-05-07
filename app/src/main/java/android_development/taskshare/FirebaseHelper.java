package android_development.taskshare;

/**
 * Created by lbuer on 20.03.2018.
 */

import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import static android.content.ContentValues.TAG;


public class FirebaseHelper {


    public FirebaseHelper() {

    }

    private static FirebaseDatabase mDatabase;
    private static DatabaseReference mDbRef;
    private static FirebaseUser mCurrentUser;
    private static FirebaseAuth mAuth;

    //Variables for uploading default data
    private static String userID;
    private static String key;
    private static String content;
    private static Date currentDate;


    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }
        return mDatabase;
    }

    public static FirebaseUser getCurrentUser() {
        if (mAuth == null) {
            mAuth = FirebaseAuth.getInstance();}

        if (mCurrentUser == null) {
            mCurrentUser = mAuth.getCurrentUser();}

        return mCurrentUser;
    }

    public ArrayList<GroupMemberShip> getGroupMembershipList (String mUserID) {

        final ArrayList<GroupMemberShip> mGrouMembershipList = new ArrayList<GroupMemberShip>();

        mDatabase.getReference();
        mDbRef = mDatabase.getReference();

        mDbRef = mDbRef.child("userdata").child(mUserID).child("groupmemberships");
        mDbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                GroupMemberShip newGroupMemberShip = dataSnapshot.getValue(GroupMemberShip.class);
                mGrouMembershipList.add(newGroupMemberShip);
                Log.d("Firebasehelper", "Size of retrieved list is: " + mGrouMembershipList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return mGrouMembershipList;
    }

}
