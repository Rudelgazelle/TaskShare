package android_development.taskshare;

import android.os.AsyncTask;

import com.google.firebase.firestore.Exclude;

import java.util.Date;

/**
 * Created by lbuer on 21.02.2018.
 */

public class TaskData {

    //TODO: Implement share function via group.

    public String id;
    private String category;
    public String content;
    public Date datecreated;
    private Date dateupdated;
    private Date duedate;
    private Boolean favorite;
    private Boolean Status;
    private String sharedwith;


    public TaskData(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public TaskData(String id, String content, Date datecreated, Date duedate, Boolean favorite){
        this.id = id;
        this.content = content;
        this.datecreated = datecreated;
        this.duedate = duedate;
        this.favorite = favorite;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDatecreated() {
        return datecreated;
    }

    public void setDatecreated(Date datecreated) {
        this.datecreated = datecreated;
    }

    public Date getDateupdated() {
        return dateupdated;
    }

    public void setDateupdated(Date dateupdated) {
        this.dateupdated = dateupdated;
    }

    public Date getDuedate() {
        return duedate;
    }

    public void setDuedate(Date duedate) {
        this.duedate = duedate;
    }

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }

    public Boolean getStatus() {
        return Status;
    }

    public void setStatus(Boolean status) {
        Status = status;
    }

    public String getSharedwith() {
        return sharedwith;
    }

    public void setSharedwith(String sharedwith) {
        this.sharedwith = sharedwith;
    }




}
