package android_development.taskshare;

import com.google.firebase.firestore.Exclude;

public class MemberData {

    private String id;
    private String name;
    private String mail;
    private String status;


    public MemberData(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public MemberData(String id, String name, String mail, String status){
        super();
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.status = status;
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }
}