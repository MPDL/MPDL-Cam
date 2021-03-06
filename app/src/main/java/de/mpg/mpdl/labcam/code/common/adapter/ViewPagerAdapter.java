package de.mpg.mpdl.labcam.code.common.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.module.glide.ImageLoader;
import de.mpg.mpdl.labcam.code.rxbus.RxBus;
import de.mpg.mpdl.labcam.code.rxbus.event.VoiceRefreshEvent;
import de.mpg.mpdl.labcam.code.utils.KeyboardUtils;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;

/**
 * Created by yingli on 3/16/16.
 */
public class ViewPagerAdapter extends PagerAdapter {

    private static final String TAG = ViewPagerAdapter.class.getSimpleName();
    Context context;
    LayoutInflater inflater;
    Point size;
    List<String> imagePathList;
    private String userId;
    private String serverName;
    boolean isLocalImage;
    private boolean isLoading;
    private ViewPagerAdapter adapter;

    private OnItemClickListener onItemClickListener;
    private OnLoadMoreDetailListener onLoadMoreDetailListener;

    private Set<Integer> positionSet = new HashSet<>();

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void setPositionSet(Set<Integer> positionSet){
        this.positionSet = positionSet;
        Log.e("setPositionSet",positionSet.toString());
        notifyDataSetChanged();
    }

    public ViewPagerAdapter(Context context, Point size, boolean isLocalImage, List<String> imagePathList,
                            OnLoadMoreDetailListener onLoadMoreDetailListener, String userId, String serverName) {
        this.context = context;
        this.size = size;
        this.isLocalImage = isLocalImage;
        this.imagePathList = imagePathList;
        this.userId = userId;
        this.serverName = serverName;
        this.onLoadMoreDetailListener = onLoadMoreDetailListener;
        this.adapter = this;
    }

    @Override
    public int getCount() {
        return imagePathList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);

        ImageView imageView = (ImageView) itemView.findViewById(R.id.detail_image);
        TextView imageNameTextView = (TextView) itemView.findViewById(R.id.tv_image_detail_name);
        String fileName = "";
        if(imagePathList.get(position)!=null){
            if(!isLocalImage){
                fileName = imagePathList.get(position);
            } else {
                String[] imgPathSplit = imagePathList.get(position).split("/");
                fileName = imgPathSplit[imgPathSplit.length-1];
            }
        }
        imageNameTextView.setText(fileName);

        // voice panel

        //check mark
        ImageView checkMark = (ImageView) itemView.findViewById(R.id.viewpager_check_mark);
        if(positionSet.contains(position)){
            checkMark.setVisibility(View.VISIBLE);
        }else {
            checkMark.setVisibility(View.GONE);
        }

        if(isLocalImage){
            Uri uri = Uri.fromFile(new File(imagePathList.get(position)));
            Picasso.with(context)
                    .load(uri)
                    .resize(size.x, size.y)
                    .centerInside()
                    .into(imageView);
        }else {
            ImageLoader.loadStringRes(imageView, imagePathList.get(position), ImageLoader.defConfig, null);
        }

        if (onItemClickListener!=null){

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onItemClickListener.onItemLongClick(v, position);
                    return false;
                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
        }

        initImageInfoLayout(itemView, position);  // init notes and voice


        ((ViewPager) container).setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if(!isLoading) {
                        int lastVisibleItem;
                        int visibleThreshold = 1;
                        int totalItemCount = adapter.getCount();
                        lastVisibleItem = position;
                        if (totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                            if (onLoadMoreDetailListener != null) {
                                onLoadMoreDetailListener.onLoadMore(position);
                            }
                            isLoading = true;
                        }
                    }
            }

            @Override
            public void onPageSelected(int position) {
                // onPageSelected
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // onPageScrollStateChanged
            }
        });

        ((ViewPager) container).addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

    // view pager need to remove all views and reload them all
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    private void initImageInfoLayout(View itemView, int position){
        // get Image object
        Image image = DBConnector.getImageByPath(imagePathList.get(position), userId, serverName);
        if(image != null){
            RelativeLayout notePanelLayout = (RelativeLayout) itemView.findViewById(R.id.layout_note_panel);
            RelativeLayout voicePanelLayout = (RelativeLayout) itemView.findViewById(R.id.layout_voice_panel);

            if(image.getNoteId()!=null){      // show notes
                initNotePanel(itemView, image);
                notePanelLayout.setVisibility(View.VISIBLE);
            }else notePanelLayout.setVisibility(View.GONE);

            if(image.getVoiceId()!=null){     // show voice
                initVoicePanel(itemView, image, voicePanelLayout, position);  // init player
                voicePanelLayout.setVisibility(View.VISIBLE);
            }else voicePanelLayout.setVisibility(View.GONE);
        }
    }

    private void initNotePanel(View itemView, final Image image){
        final LinearLayout editNoteButtonLayout = (LinearLayout) itemView.findViewById(R.id.layout_edit_note_button);
        final TextView noteTextView = (TextView) itemView.findViewById(R.id.tv_notes_detail);
        final EditText noteEditText = (EditText) itemView.findViewById(R.id.et_notes_detail);
        final TextView cancelTextView = (TextView) itemView.findViewById(R.id.tv_cancel_edit_note);
        final TextView saveTextView = (TextView) itemView.findViewById(R.id.tv_save_edit_note);

        noteTextView.setVisibility(View.VISIBLE);
        if(image.getNoteId()==null){
            noteTextView.setVisibility(View.GONE);
        }else noteTextView.setText(DBConnector.getNoteById(image.getNoteId(), userId, serverName).getNoteContent());

        cancelTextView.setOnClickListener(new View.OnClickListener() {  //
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideKeyboard(v);
                editNoteButtonLayout.setVisibility(View.GONE);
                noteEditText.setVisibility(View.GONE);
                noteTextView.setVisibility(View.VISIBLE);
            }
        });

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Image> selectedImageList = new ArrayList<>(); // selected ImageList
                selectedImageList.add(image);

                DBConnector.batchEditNote(selectedImageList, String.valueOf(noteEditText.getText()), userId, serverName);
                KeyboardUtils.hideKeyboard(v);
                editNoteButtonLayout.setVisibility(View.GONE);
                noteEditText.setVisibility(View.GONE);

                noteTextView.setText(noteEditText.getText());
                noteTextView.setVisibility(View.VISIBLE);
            }
        });

        noteTextView.setOnClickListener(new View.OnClickListener() {  // click to edit note
            @Override
            public void onClick(View v) {
                noteTextView.setVisibility(View.GONE);

                noteEditText.setText(DBConnector.getNoteById(image.getNoteId(),userId,serverName).getNoteContent());
                noteEditText.setVisibility(View.VISIBLE);

                editNoteButtonLayout.setVisibility(View.VISIBLE);
            }
        });

    }

    /** voice panel **/
    private void initVoicePanel(View itemView, final Image image, final View voicePanelLayout, final int position){

        final MediaPlayer mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(DBConnector.getVoiceById(image.getVoiceId(), userId, serverName).getVoicePath());
            mediaPlayer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
            return;
        }

        final ImageButton pauseButton = (ImageButton) itemView.findViewById(R.id.btn_pause_voice);
        final ImageButton rewindButton = (ImageButton) itemView.findViewById(R.id.btn_rewind_voice);
        final ImageButton deleteButton = (ImageButton) itemView.findViewById(R.id.btn_delete_voice);
        final ImageButton resetButton = (ImageButton) itemView.findViewById(R.id.btn_reset_voice);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                pauseButton.setEnabled(true);
                pauseButton.setVisibility(View.GONE);
                rewindButton.setEnabled(true);
                rewindButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.GONE);
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();

                pauseButton.setEnabled(false);
                pauseButton.setVisibility(View.GONE);
                rewindButton.setEnabled(true);
                rewindButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();

                pauseButton.setEnabled(true);
                pauseButton.setVisibility(View.VISIBLE);
                rewindButton.setEnabled(false);
                rewindButton.setVisibility(View.GONE);
                resetButton.setVisibility(View.VISIBLE);
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShortMessage(context, "Reseting sound");
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(DBConnector.getVoiceById(image.getVoiceId(), userId, serverName).getVoicePath());
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    Log.e(TAG, "IOException", e);
                    return;
                }

                pauseButton.setEnabled(true);
                pauseButton.setVisibility(View.GONE);
                rewindButton.setEnabled(true);
                rewindButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.GONE);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Do you want to delete this voice note?")
                        .setPositiveButton("Yes", (dialogInterface, i) -> {
                            ToastUtils.showLongMessage(context, "Voice note deleted");
                            voicePanelLayout.setVisibility(View.GONE);
                            deleteVoice(DBConnector.getVoiceById(image.getVoiceId(), userId, serverName), position, image.getId());
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> {
                            // do nothing
                        })
                        .show();
            }
        });
    }

    private void deleteVoice(Voice voice, int position, Long imageId){
        Image image = DBConnector.getImageByImgId(imageId);
        image.setVoiceId(null);
        if(image.getNoteId()==null && image.getVoiceId()== null &&
                DBConnector.isNeedUpload(image.getImagePath(), userId, serverName))
            image.delete();
        else
            image.save();

        List<String> imageIds = voice.getImageIds();
        if(imageIds.contains(imageId.toString())){
            imageIds.remove(imageId.toString());
        }
        voice.setImageIds(imageIds);
        voice.save();

        if(voice.getImageIds().isEmpty()){
            voice.delete();
        }

        VoiceRefreshEvent voiceRefreshEvent = new VoiceRefreshEvent(imagePathList.get(position));
        RxBus.getDefault().post(voiceRefreshEvent);
    }

    public void setDataSet(List<String> newDataSet){
        this.imagePathList = newDataSet;
        notifyDataSetChanged();
    }

    public interface OnLoadMoreDetailListener {
        void onLoadMore(int position);
    }

    public void setLoaded(){
        isLoading = false;
    }



}
