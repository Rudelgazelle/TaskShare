package android_development.taskshare;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by lbuer on 22.02.2018.
 */

public class UserData implements Serializable {

    public String userId;
    public String userDisplayName;
    public String userMail;
    public String userPhone;
    public String userPhotoUrl;
    public Long userHashCode;
    public String userStatus;

    public UserData(){
        //Default constructor required for calls to DataSnapshot.getValue(TaskData.class)
    }

    public UserData(String userid, String userdisplayname, String usermail,String userPhone, String userphotourl, Long userHashCode){
        this.userId = userid;
        this.userDisplayName = userdisplayname;
        this.userMail = usermail;
        this.userPhone = userPhone;
        this.userPhotoUrl = userphotourl;
        this.userHashCode = userHashCode;
    }


    //Implemented getters and setters
    @Exclude
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDisplayName() {
        return userDisplayName;
    }

    public void setUserDisplayName(String userDisplayName) {
        this.userDisplayName = userDisplayName;
    }

    @Exclude
    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }

    @Exclude
    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    @Exclude
    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }

    @Exclude
    public Long getUserHashCode() {
        return userHashCode;
    }

    public void setUserHashCode(Long userHashCode) {
        this.userHashCode = userHashCode;
    }
}
