package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by yingli on 12/14/15.
 */

@Table(name = "Images")
public class Image extends Model {

    @Expose
    @Column(name = "taskId")
    public Long taskId;

    @Expose
    @Column(name = "imageName")
    private String imageName;

    @Expose
    @Column(name = "state")
    private String state;

    @Expose
    @Column(name = "errorLevel")
    private String errorLevel;

    @Expose
    @Column(name = "imagePath")
    private String imagePath;

    @Column(name = "size")
    private String size;

    @Expose
    @Column(name = "createTime")
    private String createTime;

    @Expose
    @Column(name = "latitude")
    private String latitude;

    @Expose
    @Column(name = "longitude")
    private String longitude;

    @Expose
    @Column(name = "logs")
    private String log;

    @Expose
    @Column(name = "userId")
    private String userId;

    @Expose
    @Column(name = "serverName")
    private String serverName;

    @Expose
    @Column(name = "noteId")
    private Long noteId;

    @Expose
    @Column(name = "voiceId")
    private Long voiceId;

    public Image() {
        super();
    }

    public Image(Long taskId, String imageName, String state, String errorLevel, String imagePath, String size, String createTime, String latitude, String longitude, String log, String userId, String serverName, Long noteId, Long voiceId) {
        this.taskId = taskId;
        this.imageName = imageName;
        this.state = state;
        this.errorLevel = errorLevel;
        this.imagePath = imagePath;
        this.size = size;
        this.createTime = createTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.log = log;
        this.userId = userId;
        this.serverName = serverName;
        this.noteId = noteId;
        this.voiceId = voiceId;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorLeverl() {
        return errorLevel;
    }

    public void setErrorLeverl(String errorLeverl) {
        this.errorLevel = errorLeverl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String severName) {
        this.serverName = severName;
    }

    public Long getNoteId() {
        return noteId;
    }

    public void setNoteId(Long noteId) {
        this.noteId = noteId;
    }

    public Long getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(Long voiceId) {
        this.voiceId = voiceId;
    }
}
