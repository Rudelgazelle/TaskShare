package android_development.taskshare;

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
