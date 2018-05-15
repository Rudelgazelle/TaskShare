package android_development.taskshare;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.provider.ContactsContract;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import org.w3c.dom.Text;

import java.util.List;


public class GroupMemberViewAdapter extends RecyclerView.Adapter<GroupMemberViewAdapter.GroupMemberViewHolder> {

    private List<UserData> mGroupMemberList;
    private Context context;

    private UserData mUserDataItem;

    public GroupMemberViewAdapter(List<UserData> mGroupMemberList, Context context) {
        this.mGroupMemberList = mGroupMemberList;
        this.context = context;
    }

    @Override
    public GroupMemberViewAdapter.GroupMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return the viewholder and inflate the ListItem layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_groupmember, parent, false);
        //return a new instance of the ViewHolder
        return new GroupMemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final GroupMemberViewAdapter.GroupMemberViewHolder holder, int position) {

        //Fetch the data of the mGroupMemberList of the clicked position
        mUserDataItem = mGroupMemberList.get(position);
        //bind the data from the retrieved item to the view objects
        holder.tvMemberName.setText(mUserDataItem.getUserDisplayName());

        //-------------------------------------------------
        //INSERT A CIRCULAR IMAGEVIEW WITH THE USE OF GLIDE
        // ------------------------------------------------

        Glide.with(context)
                .load(mUserDataItem.getUserPhotoUrl())
                .asBitmap()
                .centerCrop()
                .dontAnimate()
                .placeholder(R.drawable.default_profile_pic)
                .error(R.drawable.default_profile_pic)
                .into(new BitmapImageViewTarget(holder.ivMemberProfilePic) {

            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                holder.ivMemberProfilePic.setImageDrawable(circularBitmapDrawable);
            }
        });

//TODO: THIS IS NOT REQUIRED HERE !!!!!!!!!!!!!!!!!!!! IT CAN BE USED IN THE GROUPS INFORMATION PANEL OF EXISTING GROUPS
/*        //--------------------------------------------------------------
        //SET A "Admin" TAG INTO THE RECYCLERVIEW ITEM OF THE GROUPOWNER
        // -------------------------------------------------------------
        SharedPreferences mSharedPref = context.getSharedPreferences("mSharePrefFile", 0);
        String userId = (mSharedPref.getString("userID", ""));

        if (userId.equals(mUserDataItem.getUserId())){
            holder.tvGroupOwner.setVisibility(View.VISIBLE);
        }*/

        Log.d("GroupMemberAdapter", "ViewHolder has been bind to the Adapter");
    }

    @Override
    public int getItemCount() {
        return mGroupMemberList.size();
    }



    public class GroupMemberViewHolder extends RecyclerView.ViewHolder{

        private ImageView ivMemberProfilePic;
        private TextView tvMemberName;
        private TextView tvGroupOwner;

        public GroupMemberViewHolder(View itemView) {
            super(itemView);

            ivMemberProfilePic = (ImageView) itemView.findViewById(R.id.ivMemberProfilePic);
            tvMemberName = (TextView) itemView.findViewById(R.id.tvMemberName);
            tvGroupOwner = (TextView) itemView.findViewById(R.id.tvGroupOwner);
        }
    }
}
