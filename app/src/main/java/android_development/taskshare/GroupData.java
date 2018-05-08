package android_development.taskshare;

import com.google.firebase.firestore.Exclude;

import java.util.List;
import java.util.Map;

public class GroupData {

    private String id;
    private String name;
    private String owner;
    public Map<String, MemberData> members;
    private int itemId;
    private int taskCount;

    //private List<String> members;

    public GroupData(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public GroupData (String id, Map<String, MemberData> members, String name, String owner, int itemId, int taskCount){
        this.id = id;
        this.members = members;
        this.name = name;
        this.owner = owner;
        this.itemId = itemId;
        this.taskCount = taskCount;
    }

    //Call exclude to not get redundant ID data in the Firestore, since the document has already the ID in the name.
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Map<String, MemberData> getMembers() {
        return members;
    }

    public void setMembers(Map<String, MemberData> members) {
        this.members = members;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(int taskCount) {
        this.taskCount = taskCount;
    }
}
