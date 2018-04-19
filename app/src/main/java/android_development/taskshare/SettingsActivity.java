package android_development.taskshare;

import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    View viewDueDateSettings;
    Switch swDueDate;
    Switch swAlarmNotifications;
    NumberPicker npYellow;
    NumberPicker npRed;

    private String[] valuesRed = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    private String[] valuesYellow = {"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    private int posNpRed;
    private int posNpYellow;
    private int daysRed;
    private int daysYellow;
    private Boolean dueDateIsOn;
    private Boolean alarmNotificationIsOn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /***********************************************************************************************
         * INITIALIZE VIEW FOR DUEDATE SETTINGS TO ENABLE IT TO BE GONE (to be used in onCheckedListener)
         **********************************************************************************************/
        viewDueDateSettings = (View) findViewById(R.id.llDueDateOptions);

        /***********************************************************************************************
         * Initialize Switches and set OnSwitchListener
         **********************************************************************************************/

        dueDateIsOn = true;
        swDueDate = (Switch) findViewById(R.id.swDueDate);
        swDueDate.setChecked(true);
        swDueDate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked){
                    //if switch is checked set boolean to true
                    dueDateIsOn = true;
                    swDueDate.setTextAppearance(R.style.AppTheme_SwitchActive);
                    viewDueDateSettings.setVisibility(View.VISIBLE);
                    swAlarmNotifications.setClickable(true);
                    swAlarmNotifications.setTextAppearance(R.style.AppTheme_SwitchActive);

                    if (alarmNotificationIsOn == true){
                            swAlarmNotifications.setChecked(true);
                        }
                        else {
                            swAlarmNotifications.setChecked(false);
                        }

                    Log.d("SettingsActivity", "DuedateOptions: " + dueDateIsOn);
                    Log.d("SettingsActivity", "Alarm Funtions: " + alarmNotificationIsOn);
                }
                else {
                    //if switch is not checked set boolean to false
                    dueDateIsOn = false;
                    viewDueDateSettings.setVisibility(View.GONE);
                    swDueDate.setTextAppearance(R.style.AppTheme_SwitchDeactivated);
                    swAlarmNotifications.setTextAppearance(R.style.AppTheme_SwitchDeactivated);
                    swAlarmNotifications.setChecked(false);
                    swAlarmNotifications.setClickable(false);
                }
            }
        });

        alarmNotificationIsOn = true;
        swAlarmNotifications = (Switch) findViewById(R.id.swAlarmNotifications);
        swAlarmNotifications.setChecked(true);
        swAlarmNotifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //if duedateIsN is false, switching the AlarmNotification wont do anything!
                if (dueDateIsOn) {

                    if(isChecked){
                        //if switch is checked set boolean to true
                        alarmNotificationIsOn = true;
                    }
                    else {
                        //if switch is not checked set boolean to false
                        alarmNotificationIsOn = false;
                    }
                }
            }
        });


        /***********************************************************************************************
         * Initialize Number Pickers for Red and Yellow Deadline Alarm signal
         * TODO: IMPLEMENT THE DEPENDENCY BETWEEN YELLOW ALERT AND RED ALERT
         **********************************************************************************************/

        npYellow = (NumberPicker) findViewById(R.id.npYellow);
        //Disable keyboard popup when focused
        npYellow.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        npYellow.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        npYellow.setMaxValue(valuesYellow.length-1); //to array last value
        //Specify the NumberPicker data source as array elements
        npYellow.setDisplayedValues(valuesYellow);
        //Gets whether the selector wheel wraps when reaching the min/max value.
        npYellow.setWrapSelectorWheel(true);
        //Set a value change listener for NumberPicker
        npYellow.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected value from picker
                posNpYellow = newVal;
                //TODO: THIS IS JUST A WORKAROUND (-1) THERE HAS TO BE A WAY TO GET THE VALUE OF THE ARRAY INSTEAD OF JUST THE INDEX VALUE -1!!!!!!!
                daysYellow = Integer.parseInt(valuesYellow[posNpYellow])-1;

                //posNpYellow = npYellow.getValue();
                //daysYellow = Integer.parseInt(valuesYellow[posNpYellow]);
                //daysYellow = Integer.parseInt(valuesYellow[newVal]);
                Log.d("VariableChange1", "SharedPrefPosYellow: " + posNpYellow);
                Log.d("VariableChange2", "SharedPrefDaysYellow: " + daysYellow);
            }
        });


        npRed = (NumberPicker) findViewById(R.id.npRed);
        //Disable keyboard popup when focused
        npRed.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        //Populate NumberPicker values from String array values
        //Set the minimum value of NumberPicker
        npRed.setMinValue(0); //from array first value
        //Specify the maximum value/number of NumberPicker
        npRed.setMaxValue(valuesRed.length-1); //to array last value
        //Specify the NumberPicker data source as array elements
        npRed.setDisplayedValues(valuesRed);
        //Gets whether the selector wheel wraps when reaching the min/max value.
        npRed.setWrapSelectorWheel(true);
        //Set a value change listener for NumberPicker
        npRed.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal){
                //Display the newly selected value from picker
                posNpRed = newVal;
                daysRed = Integer.parseInt(valuesRed[posNpRed])-1;
                //posNpRed = npRed.getValue();
                //daysRed = Integer.parseInt(valuesRed[posNpRed]);
                //daysRed = Integer.parseInt(valuesRed[newVal]);
                Log.d("VariableChange3", "SharedPrefPosRed: " + posNpRed);
                Log.d("VariableChange4", "SharedPrefDaysRed: " + daysRed);
            }
        });

        // LOAD THE SHARED PREFERENCES AND PUT THE VARIABLES INTO VIEWS
        loadSharedPreferences();

        //UPDATE UI BASED ON VARIABLES
        updateUI();

    }

    @Override
    protected void onStart() {
        super.onStart();

        //UPDATE UI BASED ON VARIABLES
        updateUI();

        if (dueDateIsOn == false){
            swAlarmNotifications.setChecked(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        //SHARED PREFERENCE VALUES WILL BE STORED IN FILE WHEN ACTIVITY IS BEING CLOSED
        saveSharedPreferences();
    }

    //TODO: ERROR: THIS DOES NOT WORK!!!!!!
    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    /***********************************************************************************************
     * LOAD SHARED PREFERENCES FROM FILE
     **********************************************************************************************/
    public void loadSharedPreferences(){

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);

        // 2. Key Reference from SharePrefFile to fields (If key dows not exist, the default value will be loaded
        posNpYellow = (mSharedPref.getInt("KeyINTPosNpYellow", 2));
        posNpRed = (mSharedPref.getInt("KeyINTPosNpRed", 1));
        daysYellow = (mSharedPref.getInt("KeyINTWarningDaysYellow", 1));
        daysRed = (mSharedPref.getInt("KeyINTWarningDaysRed", 0));
        dueDateIsOn = (mSharedPref.getBoolean("KeyBOOLDueDateON", true));
        alarmNotificationIsOn = (mSharedPref.getBoolean("KeyBOOLAlarmNotificationON", true));
        Log.d("SharedPrefLoad1", "SharedPrefPosYellow: " + posNpYellow);
        Log.d("SharedPrefLoad2", "SharedPrefPosRed: " + posNpRed);
        Log.d("SharedPrefLoad3", "SharedPrefDaysYellow: " + daysYellow);
        Log.d("SharedPrefLoad4", "SharedPrefDaysRed: " + daysRed);
        //Log.d("SharedPrefLoad5", "SharedPrefDueDateIsOn: " + dueDateIsOn);
        //Log.d("SharedPrefLoad6", "SharedPrefAlarmIsOn: " + alarmNotificationIsOn);
    }

    /***********************************************************************************************
     * SAVE SHARED PREFERENCES TO FILE
     **********************************************************************************************/
    public void saveSharedPreferences(){

        // 0. Put field values into variables
        //posNpYellow = npYellow.getValue();
        //posNpRed = npRed.getValue();
        //daysYellow = Integer.parseInt(valuesYellow[posNpYellow]);
        //daysRed = Integer.parseInt(valuesRed[posNpRed]);

        // 1. Open Shared Preference File
        SharedPreferences mSharedPref = getSharedPreferences("mSharePrefFile", 0);

        // 2. Initialize Editor Class
        SharedPreferences.Editor editor = mSharedPref.edit();

        // 3. Get Values from fields and store in Shared Preferences
        editor.putInt("KeyINTPosNpYellow", posNpYellow);
        editor.putInt("KeyINTPosNpRed", posNpRed);
        editor.putInt("KeyINTWarningDaysYellow", daysYellow);
        editor.putInt("KeyINTWarningDaysRed", daysRed);

        // 4. Get Boolean values of switches and paste them to respective Keys if they have changed
        if (dueDateIsOn != null) { editor.putBoolean("KeyBOOLDueDateON", dueDateIsOn); }
        if (alarmNotificationIsOn != null) { editor.putBoolean("KeyBOOLAlarmNotificationON", alarmNotificationIsOn); }

        // 5. Store the keys
        editor.commit();

        Log.d("SharedPrefSaved1", "SharedPrefPosYellow: " + posNpYellow);
        Log.d("SharedPrefSaved2", "SharedPrefPosRed: " + posNpRed);
        Log.d("SharedPrefSaved3", "SharedPrefDaysYellow: " + daysYellow);
        Log.d("SharedPrefSaved4", "SharedPrefDaysRed: " + daysRed);
        //Log.d("SharedPrefSaved5", "SharedPrefDueDateIsOn: " + dueDateIsOn);
        //Log.d("SharedPrefSaved6", "SharedPrefAlarmIsOn: " + alarmNotificationIsOn);
    }

    private void updateUI(){
        npYellow.setValue(daysYellow);
        npRed.setValue(daysRed);
        //npYellow.setValue(posNpYellow);
        //npRed.setValue(posNpRed);
        swDueDate.setChecked(dueDateIsOn);
        swAlarmNotifications.setChecked(alarmNotificationIsOn);
    }
}
