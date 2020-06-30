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
    private ArrayList<PackageInfo> download;
    private Map<String, DownloadTask> taskMap;

    public MessageEvent() {
    }

    public MessageEvent(String key, ArrayList<PackageInfo> download, Map<String, DownloadTask> taskMap) {
        this.key = (key != null) ? key : "";
        this.download = (download != null) ? download : new ArrayList<PackageInfo>();
        this.taskMap = (taskMap != null) ? taskMap : new HashMap<String, DownloadTask>();
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ArrayList<PackageInfo> getDownload() {
        return download;
    }

    public void setDownload(ArrayList<PackageInfo> download) {
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
