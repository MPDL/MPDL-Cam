package de.mpg.mpdl.labcam.ItemDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import de.mpg.mpdl.labcam.Gallery.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.MicrophoneDialogFragment;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.NoteDialogFragment;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.activity.LocalImageActivity;
import de.mpg.mpdl.labcam.code.rxbus.EventSubscriber;
import de.mpg.mpdl.labcam.code.rxbus.RxBus;
import de.mpg.mpdl.labcam.code.rxbus.event.NoteRefreshEvent;
import de.mpg.mpdl.labcam.code.rxbus.event.VoiceRefreshEvent;

import uk.co.senab.photoview.PhotoViewAttacher;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rx.Subscription;

import static de.mpg.mpdl.labcam.Utils.BatchOperationUtils.addImages;
import static de.mpg.mpdl.labcam.Utils.BatchOperationUtils.noteDialogNewInstance;
import static de.mpg.mpdl.labcam.Utils.BatchOperationUtils.voiceDialogNewInstance;


public class DetailActivity extends AppCompatActivity implements android.support.v7.view.ActionMode.Callback{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private Activity activity = this;
    private View rootView;
    private List<String> itemPathList;
    private PhotoViewAttacher mAttacher;

    private String serverName;

    // viewPager
    private ViewPager viewPager;
    private  ViewPagerAdapter viewPagerAdapter;
    boolean isLocalImage;
    int positionInList;

    // positionSet
    public Set<Integer> positionSet = new HashSet<>();

    private android.support.v7.view.ActionMode actionMode;
    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;

    //user info
    private SharedPreferences mPrefs;
    private String userName;
    private String userId;

    private Subscription mNoteRefreshEventSub;
    private Subscription mVoiceRefreshEventSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        observeNoteRefresh();
        observeVoiceRefresh();

        mPrefs = this.getSharedPreferences("myPref", 0);
        userName = mPrefs.getString("username", "");
        userId = mPrefs.getString("userId","");
        serverName = mPrefs.getString("serverName","");

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemPathList = extras.getStringArrayList("itemPathList");
            isLocalImage = extras.getBoolean("isLocalImage");
            positionInList = extras.getInt("positionInList");

            forceRefresh();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        if (actionMode == null) {
            actionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu_local, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_upload_local:
                Log.i(LOG_TAG, "upload");
                batchOperation(R.id.item_upload_local);
                mode.finish();
                return true;
            case R.id.item_microphone_local:
                Log.i(LOG_TAG, "microphone");
                batchOperation(R.id.item_microphone_local);
                mode.finish();
                return true;
            case R.id.item_notes_local:
                Log.i(LOG_TAG, "notes");
                batchOperation(R.id.item_notes_local);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
        actionMode = null;
        positionSet.clear();
        viewPagerAdapter.notifyDataSetChanged();
    }



    /**upload methods**/
     /*
            upload the selected files
        */
    private void uploadList(String[] imagePathArray) {

        newInstance(imagePathArray).show(this.getFragmentManager(), "remoteListDialog");
    }

    public static RemoteListDialogFragment newInstance(String[] imagePathArray)
    {
        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray("imagePathArray", imagePathArray);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
    }



    private void addOrRemove(int position) {

        if (positionSet.contains(position)) {

            positionSet.remove(position);
        } else {

            positionSet.add(position);
        }
        if (positionSet.size() == 0) {

            actionMode.finish();
        } else {

            actionMode.setTitle(positionSet.size() + " selected");

            viewPagerAdapter.notifyDataSetChanged();

        }
    }

    private void batchOperation(int operationType){
        if(positionSet.size()!=0) {
            Log.v(LOG_TAG, " "+positionSet.size());
            List imagePathList = new ArrayList();
            //TODO: Question why convert itemPathList to imagePathList
            for (Integer i : positionSet) {
                imagePathList.add(itemPathList.get(i));
            }
            if (imagePathList != null) {
                String[] imagePathArray = (String[]) imagePathList.toArray(new String[imagePathList.size()]);
                switch (operationType){
                    case R.id.item_upload_local:
                        uploadList(imagePathArray);
                        break;
                    case R.id.item_microphone_local:
                        showVoiceDialog(imagePathArray);
                        break;
                    case R.id.item_notes_local:
                        showNoteDialog(imagePathArray);
                        break;
                }
            }
            imagePathList.clear();
        }
    }
    public void showVoiceDialog(String[] imagePathArray){
        voiceDialogNewInstance(imagePathArray).show(this.getFragmentManager(), "voiceDialogFragment");
    }

    public void showNoteDialog(String[] imagePathArray){
        noteDialogNewInstance(imagePathArray).show(this.getFragmentManager(),"noteDialogFragment");
    }

    private void observeNoteRefresh() {
        mNoteRefreshEventSub = RxBus.getDefault()
                .observe(NoteRefreshEvent.class)
                .subscribe(new EventSubscriber<NoteRefreshEvent>() {
                    @Override
                    public void onEvent(NoteRefreshEvent event) {
                        String imgPath = event.getImgPath();
                        if(itemPathList.contains(imgPath)){
                            positionInList = itemPathList.indexOf(imgPath);
                        }
                        forceRefresh();
                    }
                });
    }

    private void observeVoiceRefresh() {
        mVoiceRefreshEventSub = RxBus.getDefault()
                .observe(VoiceRefreshEvent.class)
                .subscribe(new EventSubscriber<VoiceRefreshEvent>() {
                    @Override
                    public void onEvent(VoiceRefreshEvent event) {
                        String imgPath = event.getImgPath();
                        if(itemPathList.contains(imgPath)){
                            positionInList = itemPathList.indexOf(imgPath);
                        }
                       forceRefresh();
                    }
                });
    }

    private void forceRefresh(){
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        viewPager = (ViewPager) rootView.findViewById(R.id.view_pager_detail_image);
        viewPagerAdapter = new ViewPagerAdapter(this,size,isLocalImage,itemPathList, userId, serverName);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setCurrentItem(positionInList);
        if(isLocalImage) {
            viewPagerAdapter.setOnItemClickListener(new ViewPagerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if(actionMode != null) {
                        addOrRemove(position);
                        viewPagerAdapter.setPositionSet(positionSet);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    if (actionMode == null) {
                        actionMode = ((AppCompatActivity) activity).startSupportActionMode(ActionModeCallback);
                    }
                }
            });
        }else {
            viewPagerAdapter.setOnItemClickListener(null);
        }
    }
}