package example.com.mpdlcamera.TaskManager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by yingli on 1/22/16.
 */
public class TaskManagerAdapter extends BaseAdapter {

    private static String TAG = TaskManagerAdapter.class.getSimpleName();
    private LayoutInflater inflater;

    private Activity activity;
    private List<Task> taskList;

    private RemoveTaskInterface removeTaskInterface;

    static final int  AU_WAITING = 1001;
    static final int AU_UPLOADING = 1002;
    static final int AU_FINISH = 1003;

    static final int MU_UPLOADING = 2002;
    static final int MU_FINISH = 2003;

    public TaskManagerAdapter() {
    }

    public TaskManagerAdapter(Activity activity, List<Task> taskList, RemoveTaskInterface removeTaskInterface) {
        this.activity = activity;
        this.taskList = taskList;
        this.removeTaskInterface = removeTaskInterface;
    }

    @Override
    public int getCount() {
        return taskList.size();
    }

    @Override
    public Object getItem(int i) {
        return taskList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {


        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null)
            view = inflater.inflate(R.layout.task_list_cell, null);

        // set task info according to mode
        TextView taskNameTextView = (TextView) view.findViewById(R.id.tv_task_name);
        if(taskList.get(position).getUploadMode().equalsIgnoreCase("AU")){
            // AU
            taskNameTextView.setText("Captured Fotos upload to");
        }else {
            // MU
            taskNameTextView.setText(taskList.get(position).getTotalItems()+" selected Fotos upload to");
        }
        // set collection name
        TextView taskCollectionTextView = (TextView) view.findViewById(R.id.tv_task_collection);
        taskCollectionTextView.setText(taskList.get(position).getCollectionName());

        //ProgressBar
        ProgressBar firstBar = (ProgressBar) view.findViewById(R.id.firstBar);

        //get task
        final Task task = taskList.get(position);
        int currentNum = task.getFinishedItems();
        int maxNum = task.getTotalItems();

        firstBar.setMax(maxNum);
        firstBar.setProgress(currentNum);

        //percent
        TextView percentTextView = (TextView) view.findViewById(R.id.tv_percent);
        int percent;
        if(maxNum!=0){
            percent = (currentNum * 100) / maxNum;}
        else {
            percent = 0;
        }
        percentTextView.setText(percent + "");


        /**
         * layout changes in different phrases
         */
        int phrase = -1;

        if(task.getUploadMode().equalsIgnoreCase("AU")){
            if(maxNum == 0){
                phrase = AU_WAITING;
            }else if(currentNum == maxNum){
                phrase = AU_FINISH;
            }else {
                phrase = AU_UPLOADING;
            }

        }else {
            if(currentNum == maxNum){
                phrase = MU_FINISH;
            }else {
                phrase = MU_UPLOADING;
            }
        }

        RelativeLayout progressLayout = (RelativeLayout) view.findViewById(R.id.layout_progress);
        RelativeLayout toolButtonLayout = (RelativeLayout) view.findViewById(R.id.layout_stop_delete);
        Button clearButton = (Button) view.findViewById(R.id.btn_clear);


        switch (phrase){
            case AU_WAITING:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);

                break;
            case AU_UPLOADING:
                progressLayout.setVisibility(View.VISIBLE);
                toolButtonLayout.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.GONE);

                break;
            case AU_FINISH:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
                clearButton.setVisibility(View.GONE);

                break;
            case MU_UPLOADING:
                progressLayout.setVisibility(View.VISIBLE);
                toolButtonLayout.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.GONE);

                break;
            case MU_FINISH:
                progressLayout.setVisibility(View.GONE);
                toolButtonLayout.setVisibility(View.GONE);
                clearButton.setVisibility(View.VISIBLE);
                clearButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteTask(task, position);
                    }
                });

                break;
        }

        if(maxNum == 0||currentNum == maxNum){
            //hide both

            //show clear for MU
            if(task.getUploadMode().equalsIgnoreCase("AU")){
                //AU

            } else {
                //MU
                //show clear hide stop/delete

            }

        }

        //DeleteTask
        ImageView deleteTaskImageView = (ImageView) view.findViewById(R.id.task_delete);


            deleteTaskImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(activity)
                            .setTitle("abort uploading")
                            .setMessage("Are you sure you want to give up this uploading process?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    if(task.getUploadMode().equalsIgnoreCase("AU")){
                                        //abort uploading, but keep the task
                                        //stop the service
                                        Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                        activity.stopService(uploadIntent);

                                        //delete all image with this taskId
                                        new Delete().from(Image.class).where("taskId = ?", task.getTaskId()).execute();

                                        //resume task
                                        resumeTask(task);
                                    }else {
                                        // continue with delete
                                        deleteTask(task, position);
                                    }

                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();

                }
            });



        //pause/play Task
        CheckBox isPausedCheckBox = (CheckBox) view.findViewById(R.id.checkBox_is_paused);
        if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))){
            isPausedCheckBox.setChecked(false);
        }else {
            //true is play button, means state now is paused
            isPausedCheckBox.setChecked(true);
        }

        isPausedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                String currentTaskId = task.getTaskId();
                if(!compoundButton.isChecked()){
                    // pause clicked
                    try {
                        Task currentTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
                        currentTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                        currentTask.save();
                        Log.v(TAG, "setState: WAITING" );
                        if(currentTask.getUploadMode().equalsIgnoreCase("AU")){
                            // start AU TaskUploadService
                            Log.v(TAG,"start TaskUploadService");
                            Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                            activity.startService(uploadIntent);
                        }else{
                            // start ManualUploadService
                            Log.v(TAG,"start manualUploadService");
                            Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                            manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                            activity.startService(manualUploadServiceIntent);
                        }
                    }catch (Exception e){}

                }else if(compoundButton.isChecked()){
                    // pause button
                    try {
                        Task currentTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
                        currentTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                        currentTask.save();
                        Log.v(TAG, "setState: STOPPED");
                        if(currentTask.getUploadMode().equalsIgnoreCase("AU")){
                            // start AU TaskUploadService
                            Intent uploadIntent = new Intent(activity, TaskUploadService.class);
//                            uploadIntent.putExtra("isBusy",false);
                            activity.startService(uploadIntent);
                        }else{
                            // start ManualUploadService
                            Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                            manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                            activity.startService(manualUploadServiceIntent);
                        }
                    }catch (Exception e){}

                }
            }
        });

        return view;
    }

    private void deleteTask(Task task,int position){
        String currentTaskId = task.getTaskId();
        new Delete().from(Task.class).where("taskId = ?", currentTaskId).execute();
        Log.v(TAG, "task delete clicked");
        //TODO: delete from data list
        removeTaskInterface.remove(position);
        taskList.remove(task);
        notifyDataSetChanged();
        /** if it is a Au, what to do? */
    }

    private void resumeTask(Task task){
        task.setFinishedItems(0);
        task.setTotalItems(0);
        task.setLogs("");
        task.save();
    }
}

