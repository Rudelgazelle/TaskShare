package android_development.taskshare;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lbuer on 14.03.2018.
 */

//-------------------------------------------------------------------------------------------------------------------------------------
/* The FirebaseRecyclerAdapter binds a Query to a RecyclerView. When data is added, removed, or changed these updates are automatically
   applied to your UI in real time.
  */
//-------------------------------------------------------------------------------------------------------------------------------------

// 1- implement methods by pressing alt + enter
public class TaskDataViewAdapter extends RecyclerView.Adapter<TaskDataViewAdapter.TaskDataViewHolder> {

    private List<TaskData> mTaskDataList;
    private List<TaskData> mArrayList;
    private Context context;

    DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
    DateFormat df = new SimpleDateFormat("d MMM yyyy");

    //variable for setin due Date Alarm
    private Date mDueDate;
    private Date mDateToday;
    private Date mDateWarningYellow;
    private Date mDateWarningRed;
    private Integer mDaysWarningYellow;
    private Integer mDaysWarningRed;
    private Boolean dueDateIsOn;

    //Create a constructor for List and context
    public TaskDataViewAdapter(List<TaskData> taskDataList, Context context) {
        this.mTaskDataList = taskDataList;
        this.context = context;
    }

    @Override
    public TaskDataViewAdapter.TaskDataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //return the viewholder and inflate the ListItem layout
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.taskdata_taskview, parent, false);
        //return a new instance of the ViewHolder
        return new TaskDataViewHolder(v);
    }

    //this method will bind the data to the viewholder object
    @Override
    public void onBindViewHolder(TaskDataViewAdapter.TaskDataViewHolder holder, final int position) {

        if (mDaysWarningRed == null || mDaysWarningYellow == null || dueDateIsOn == null){
            // GET VARIABLES FOR RED AND YELLOW MARKER OUT OF SHARED PREFERENCES // VORZEICHEN UMDREHEN!!!!!!!!!!!
            SharedPreferences mSharedPref = context.getSharedPreferences("mSharePrefFile", 0);
            mDaysWarningYellow = (mSharedPref.getInt("KeyINTWarningDaysYellow", 1))*-1;
            mDaysWarningRed = (mSharedPref.getInt("KeyINTWarningDaysRed", 0))*-1;
            dueDateIsOn = (mSharedPref.getBoolean("KeyBOOLDueDateON", true));
            Log.d("mDateSharedPrefYellow: ", mDaysWarningYellow.toString());
            Log.d("mDateSharedPrefRed: ", mDaysWarningRed.toString());
            Log.d("mDateDuedateIsON: ", dueDateIsOn.toString());
        }

        //Gets the specific position of the list item
        final TaskData taskDataListItem = mTaskDataList.get(position);
        //Sets the list items to the specific view object
        holder.tvContent.setText(taskDataListItem.getContent());
        holder.tvDateCreated.setText(dateFormat.format(taskDataListItem.getDatecreated()));

        /***********************************************************************************************
         * Activities to check if Due date alarm sign has to be displayed
         **********************************************************************************************/
        mDueDate = taskDataListItem.getDuedate();

        if (mDueDate != null) {
            Log.d("mDueDate: ", df.format(mDueDate));
        }

        if (mDateToday == null) {
            Calendar calToday = Calendar.getInstance();
            calToday.set(Calendar.HOUR_OF_DAY, 0);
            calToday.set(Calendar.MINUTE, 0);
            calToday.set(Calendar.SECOND, 0);
            calToday.set(Calendar.MILLISECOND, 0);
            mDateToday = calToday.getTime();
        }

        //method to set date from on when red warning should appear
        if (mDueDate != null){

            // set mDateWaringRed
            Calendar calRed = Calendar.getInstance();
            calRed.setTime(mDueDate);
            calRed.set(Calendar.HOUR_OF_DAY, 0);
            calRed.set(Calendar.MINUTE, 0);
            calRed.set(Calendar.SECOND, 0);
            calRed.set(Calendar.MILLISECOND, 0);
            calRed.add(Calendar.DATE, mDaysWarningRed);
            //cal2.add(Calendar.DATE, 0);
            mDateWarningRed = calRed.getTime();
            Log.d("mDateWarningRed: ", mDateWarningRed.toString());

            //Set mDateWaringYellow
            Calendar calYellow = Calendar.getInstance();
            calYellow.setTime(mDueDate);
            calYellow.set(Calendar.HOUR_OF_DAY, 0);
            calYellow.set(Calendar.MINUTE, 0);
            calYellow.set(Calendar.SECOND, 0);
            calYellow.set(Calendar.MILLISECOND, 0);
            calYellow.add(Calendar.DATE, mDaysWarningYellow);
            //cal1.add(Calendar.DATE, -1);
            mDateWarningYellow = calYellow.getTime();
            Log.d("mDateWarningYellow: ", mDateWarningYellow.toString());
        }

        if (dueDateIsOn == true){

            //1. if a dueday is set...
            if (mDueDate != null) {
                //2. check if the dueday is after mDateWarningRed
                Log.d("mDateDueDate: ", mDueDate.toString());
                Log.d("mDateToday: ", mDateToday.toString());

                if (mDateToday.compareTo(mDateWarningRed) >= 0) {
                    holder.ivDueDateAlarm.setImageResource(R.drawable.ic_action_alarm_red);
                    holder.ivDueDateAlarm.setVisibility(View.VISIBLE);
                }

                else if (mDateToday.compareTo(mDateWarningYellow) >= 0) {
                    holder.ivDueDateAlarm.setImageResource(R.drawable.ic_action_alarm_yellow);
                    holder.ivDueDateAlarm.setVisibility(View.VISIBLE);
                }

                //if no alarm day has been passed, set DueDateAlarm to invisible
                else {
                    holder.ivDueDateAlarm.setVisibility(View.INVISIBLE);
                }

            }
            //IF no duedate is set, set DuedadeAlarm invisible.
            else {
                holder.ivDueDateAlarm.setVisibility(View.INVISIBLE);
            }
        }
        //IF DUEDATE FUNCTION IS DISABLED, HIDE THE SYMBOL
        else {
            holder.ivDueDateAlarm.setVisibility(View.INVISIBLE);
        }

        /***********************************************************************************************
         ***********************************************************************************************
         **********************************************************************************************/


        //initialize linear layout and attach OnClickListener to enable item click
        //whenever an Item with the position "position" is clicked, this method is being executed
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Define variables to be mapped to values of clicked item
                String taskID = taskDataListItem.getId();
                String taskDateCreated = df.format(taskDataListItem.getDatecreated());
                String taskContent = taskDataListItem.getContent();

                //Get application context and initiate intent to open the TaskDetailsActivity
                Context context = v.getContext();
                Intent intent = new Intent(context, TaskDataDetailsActivity.class);
                intent.putExtra("taskID", taskID);
                intent.putExtra("taskDateCreated", taskDateCreated);
                intent.putExtra("taskContent", taskContent);

                context.startActivity(intent);
            }
        });
    }

    //returns the number of items covered in the List
    @Override
    public int getItemCount() {
        return mTaskDataList.size();
    }

    // 2 - create constructor (alt + enter)
    public class TaskDataViewHolder extends RecyclerView.ViewHolder {

        // Define the Textviews of the Card View
        private TextView tvContent;
        private TextView tvDateCreated;
        private ImageView ivDueDateAlarm;
        //Define a Linear Layout to enable item click
        public LinearLayout linearLayout;


        public TaskDataViewHolder(final View itemView) {
            super(itemView);
            tvContent = (TextView) itemView.findViewById(R.id.content);
            tvDateCreated = (TextView) itemView.findViewById(R.id.tvDateCreated);
            ivDueDateAlarm = (ImageView) itemView.findViewById(R.id.ivDueDateAlarm);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.recyclerViewTaskData);
        }
    }

    //method sets the List used by the adapter to the list of filtered items
    public void filterList(ArrayList<TaskData> filteredList){
        mTaskDataList = filteredList;
        notifyDataSetChanged();
    }

    public Filter getFilter(){

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                mTaskDataList.clear();
                final FilterResults results = new FilterResults();

                //returns a collection of the children under the set database reference
                Iterable<DataSnapshot> children;
                children = new Iterable<DataSnapshot>() {
                    @NonNull
                    @Override
                    public Iterator<DataSnapshot> iterator() {
                        return null;
                    }
                };

                if (constraint.length() == 0) {
                    for (DataSnapshot child : children ) {
                        //child.getValue(TravelExpenseData.class); "VOR STRG + ALT +V"
                        TaskData taskData = child.getValue(TaskData.class);
                        //add the retrieved data to the ArrayList
                        mTaskDataList.add(taskData);
                    }
                } else {
                    final String filterText = constraint.toString().toLowerCase().trim();
                    for (DataSnapshot child : children) {
                        TaskData taskData = child.getValue(TaskData.class);
                        if (taskData.getContent().toLowerCase().startsWith(filterText)) {
                            mTaskDataList.add(taskData);
                        }
                    }
                }
                System.out.println("Count Number " + mTaskDataList.size());
                results.values = mTaskDataList;
                results.count = mTaskDataList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mTaskDataList = (List<TaskData>) results.values;
                notifyDataSetChanged();
            }
        };
    }
}