package android_development.taskshare;

public class GroupData {

    private String id;
    private String name;
    private String owner;

    public GroupData(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public GroupData (String id, String name, String owner){
        this.id = id;
        this.name = name;
        this.owner = owner;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
