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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

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

}
