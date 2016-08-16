package de.mpg.mpdl.labcam.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.HashMap;
import java.util.Map;

import de.mpg.mpdl.labcam.Model.LocalModel.Configuration;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;

/**
 * Created by Allen on 06.10s.15.
 */
public class SettingsActivity extends AppCompatActivity {


    private Activity activity = this;
    private final String LOG_TAG = SettingsActivity.class.getSimpleName();
    private View rootView;
    Toolbar toolbar;
    String mOption; String networkOption = String.valueOf(DeviceStatus.backupOption.wifi);
    //TODO: replace mOption with networkOption
    String localDevice;
    String remoteServer;

    TextView list_item_backup;
    TextView list_item_local;
    TextView list_item_server;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        LinearLayout setting_backup = (LinearLayout) findViewById(R.id.setting_backup);
        list_item_backup = (TextView) findViewById(R.id.list_item_backup);

        LinearLayout setting_local = (LinearLayout) findViewById(R.id.setting_local);
        list_item_local = (TextView) findViewById(R.id.list_item_local);

        LinearLayout setting_server = (LinearLayout) findViewById(R.id.setting_server);
        list_item_server = (TextView) findViewById(R.id.list_item_server);


        //FlatButton btnDone = (FlatButton) findViewById(R.id.btnDone);


        setting_backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentSetPref = new Intent(getApplicationContext(), PrefActivity.class);
                startActivityForResult(intentSetPref, 0);
            }
        });

        setting_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(activity, LocalAlbumSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });


        setting_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent settingsIntent = new Intent(activity, RemoteCollectionSettingsActivity.class);
                startActivity(settingsIntent);
            }
        });
    }

    /*
        returns after the activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onResume() {
        super.onResume();


        SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        if (myPreferences.contains("status")) {
            String option = myPreferences.getString("status", "");
            if (option.equalsIgnoreCase("wifi")) {
                mOption = getString(R.string.wifi);
            } else if (option.equalsIgnoreCase("both")) {
                mOption = getString(R.string.wifidata);
            } else
                mOption = getString(R.string.manual);
        } else {
            mOption = getString(R.string.wifi);
        }

        //query database to get result


        SharedPreferences preferencesFolders = getSharedPreferences("folder", Context.MODE_PRIVATE);
        HashMap<String, String> folderSyncMap = new HashMap<String, String>();
        folderSyncMap = (HashMap) preferencesFolders.getAll();

        localDevice = "";
        for (Map.Entry<String, String> entry : folderSyncMap.entrySet()) {
            if (String.valueOf(entry.getValue()).equalsIgnoreCase("On")) {
                if(!String.valueOf(entry.getValue()).equalsIgnoreCase("null")) {
                    if(localDevice.equals("")){
                        localDevice = String.valueOf(entry.getKey());
                    }else {
                        localDevice = localDevice + " , " + String.valueOf(entry.getKey());
                    }
                }
            }

        }


        SharedPreferences preferences =  activity.getSharedPreferences("myPref", 0);

        if (preferences.contains("remoteServer")) {
            remoteServer = preferences.getString("remoteServer", "");
        } else {
            remoteServer = "please choose a remote folder";
        }

        Log.v(LOG_TAG, "current collection: "+preferences.getString("collectionID", ""));

        list_item_backup.setText(mOption);
        list_item_local.setText(localDevice);
        list_item_server.setText(remoteServer);
    }

    //save Configuration and create Task
    private void saveSetting(){
        /** new Configuration **/
        Configuration configuration = new Configuration();
        //setActivateTime
        configuration.setActivateTime(System.currentTimeMillis());
        //setBackupOption
        configuration.setBackupOption(networkOption);
    }

    public static Configuration getConfiguration() {
        return new Select()
                .from(Configuration.class)
                .orderBy("RANDOM()")
                .executeSingle();
    }

}






