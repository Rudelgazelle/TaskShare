package android_development.taskshare;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class FirestoreHelper {

    public FirestoreHelper(){

    }

    private static FirebaseFirestore mFirestoreDatabase;

    private static FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build();

    public static  FirebaseFirestore getDatabase(){

        if (mFirestoreDatabase == null){
            mFirestoreDatabase = FirebaseFirestore.getInstance();
            mFirestoreDatabase.setFirestoreSettings(settings);
        }

        return mFirestoreDatabase;
    }
}
