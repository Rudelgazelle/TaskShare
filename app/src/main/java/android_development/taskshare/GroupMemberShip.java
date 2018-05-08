package android_development.taskshare;

import com.google.firebase.firestore.Exclude;

public class GroupMemberShip {

    private String groupId;
    private String category;

    public GroupMemberShip(){
        //Default constructor required for Firebase calls to DataSnapshot.getValue(TaskData.class)
    }

    public GroupMemberShip(String groupId, String category){
        this.groupId = groupId;
        this.category = category;
    }

    //Call exclude to not get redundant ID data in the Firestore, since the document has already the ID in the name.
    @Exclude
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
