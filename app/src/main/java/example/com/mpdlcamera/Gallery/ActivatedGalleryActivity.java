package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Auth.LoginActivity;
import example.com.mpdlcamera.Folder.UploadService;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.ImageFileFilter;

/**
 * Created by kiran on 29.10.15.
 */
public class ActivatedGalleryActivity extends AppCompatActivity implements UploadResultReceiver.Receiver {


    private final String LOG_TAG = ActivatedGalleryActivity.class.getSimpleName();
    private List<String> toBeDeleteImagePathList = new ArrayList<String>();
    private SharedPreferences mPrefs;




    private View rootView;
    private Toolbar toolbar;
    private Activity activity = this;
    private String galleryName;
    private String galleryPath;
    private List<String> imagePathList = new ArrayList<String>();
    public  GalleryGridAdapter adapter;
    private GridView gridView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.active_gallery_gridview);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = activity.getIntent();
        if (intent != null) {
            galleryName = intent.getStringExtra("galleryName");
            galleryPath = intent.getStringExtra("galleryPath");
            if(galleryName != null) {
                ///storage/emulated/0/DCIM/Screenshots
                titleView.setText(galleryName);
            }
        }

        File galleryFolder = new File(galleryPath);
        File[] galleryFiles = galleryFolder.listFiles();

        for (File imageFile : galleryFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                imagePathList.add(imageFile.getAbsolutePath());
            }
        }

        adapter = new GalleryGridAdapter(activity, imagePathList, this);

        gridView = (GridView) findViewById(R.id.image_gridView);

        gridView.setAdapter(adapter);



        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int nr = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.clearSelection();
                toolbar.setVisibility(View.VISIBLE);

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                nr = 0;

                toolbar.setVisibility(View.GONE);

                //adapter.getCheckBox().setVisibility(View.VISIBLE);

                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu_delete, menu);

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {

                    case R.id.item_delete:
                        nr = 0;
                        Integer count = 0;

                        //adapter.clearSelection();
                        if(toBeDeleteImagePathList != null) {
                            delete(toBeDeleteImagePathList);
                            count = toBeDeleteImagePathList.size();
                            Toast.makeText(activity, "" + count + " files deleted", Toast.LENGTH_SHORT).show();
                            Intent newIntent = new Intent(activity, ActivatedGalleryActivity.class);
                            newIntent.putExtra("galleryName", galleryName);
                            newIntent.putExtra("galleryPath", galleryPath);
                            startActivity(newIntent);
                        }

                        mode.finish();
                }

                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position,
                                                  long id,
                                                  boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    nr++;
                    adapter.setNewSelection(position, checked);
                    toBeDeleteImagePathList.add(imagePathList.get(position));

                    // toBeUploadDataPathList.add(dataPathList.get(position));
                    //adapter.getCheckBox().setChecked(true);

                } else {
                    nr--;
                    adapter.removeSelection(position);
                    //adapter.getCheckBox().setChecked(false);

                }
                mode.setTitle(nr + " selected");

            }
        });

        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intentNew = new Intent(this, UploadService.class);
        intentNew.putExtra("receiver", mReceiver);
        this.startService(intentNew);

        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                // TODO Auto-generated method stub

                gridView.setItemChecked(position, !adapter.isPositionChecked(position));
                return false;
            }
        });




    }

    private void delete(List<String> toBeDeleteImagePathList) {

        for(String imagePath : toBeDeleteImagePathList) {

            File file = new File(imagePath);
            Boolean deleted = file.delete();
            Log.v(LOG_TAG, "deleted:" +deleted);
          //  adapter.notifyDataSetChanged();

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_select) {
            Log.v(LOG_TAG, "slected");

            return true;
        }
        if (id == R.id.item_delete) {
            Log.v(LOG_TAG,"delete");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Are you sure to delete?");
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.add(1, cmi.position, 0, "Delete");
        menu.add(2, cmi.position, 0, "Cancel");
    }



    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete")){

            Log.v("", String.valueOf(item.getItemId()));
        }
        else if(item.getTitle().equals("Cancel")){
            Log.v("", String.valueOf(item.getItemId()));
        }
        else {
            return false;

        }
        // Return false to allow normal context menu processing to proceed,
        //        true to consume it here.
        return true;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 0:

                setProgressBarIndeterminateVisibility(true);
                break;
            case 1:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

                //  String[] results = resultData.getStringArray("result");

                /* Update ListView with result */
                //ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, results);
                //listView.setAdapter(arrayAdapter);
              //  Toast.makeText(this, "Files are synced", Toast.LENGTH_LONG).show();




                mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor e = mPrefs.edit();
                e.putString("UStatus","true");
                e.commit();
                adapter.notifyDataSetChanged();
//                Intent showLocalImageIntent = new Intent(activity, LocalGalleryActivity.class);
//                startActivity(showLocalImageIntent);



/*                if(mPrefs.contains("L_A_U")) {

                    if(mPrefs.getBoolean("L_A_U", true)) {

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        View popupView = inflater.inflate(R.layout.logout_confirm, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                550,
                                300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        popupWindow.setAnimationStyle(R.style.AnimationPopup);

                        Button yes = (Button) popupView.findViewById(R.id.buttonYes);
                        Button no = (Button) popupView.findViewById(R.id.buttonNo);
                        popupWindow.showAtLocation(findViewById(R.id.navigation), Gravity.CENTER, 0, 0);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                                Intent loginIntent = new Intent(activity, LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                    }
                }*/

                break;
            case 2:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

}
