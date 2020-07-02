package top.lhjjjlxays.appstore.bean;

import com.lzy.okserver.download.DownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lhj
 * @version 1.0
 * @date 2020/5/17 10:25
 * @description
 */
public class MessageEvent {
    private String key;
    private ArrayList<ApkGeneral> download;
    private Map<String, DownloadTask> taskMap;

    public MessageEvent() {
    }

    public MessageEvent(String key, ArrayList<ApkGeneral> download, Map<String, DownloadTask> taskMap) {
        this.key = (key != null) ? key : "";
        this.download = (download != null) ? download : new ArrayList<ApkGeneral>();
        this.taskMap = (taskMap != null) ? taskMap : new HashMap<String, DownloadTask>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<ApkGeneral> getDownload() {
        return download;
    }

    public void setDownload(ArrayList<ApkGeneral> download) {
        this.download = download;
    }

    public Map<String, DownloadTask> getTaskMap() {
        return taskMap;
    }

    public void setTaskMap(Map<String, DownloadTask> taskMap) {
        this.taskMap = taskMap;
    }

    public void addEvent(MessageEvent message) {
        if (this.key.equals(message.getKey())) {
            this.download.addAll(message.getDownload());
            this.taskMap.putAll(message.getTaskMap());
        }
    }
}
