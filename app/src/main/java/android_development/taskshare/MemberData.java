package android_development.taskshare;

public class MemberData {

    private String id;
    private String name;
    private String status;


    public MemberData(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public MemberData(String id, String name, String status){
        super();
        this.id = id;
        this.name = name;
        this.status = status;
    }

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
}