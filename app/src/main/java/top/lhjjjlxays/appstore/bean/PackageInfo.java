package top.lhjjjlxays.appstore.bean;

import java.util.List;

public class PackageInfo {
    private String apk_developer;
    private String apk_name;
    private String apk_icon;
    private String apk_grade;
    private String evaluate_number;
    private String download_url;
    private String apk_size;
    private String apk_version;
    private String update_date;
    private String package_name;
    private String apk_permission;
    private List<String> apk_screenshots;
    private String apk_introduce;
    private String version_feature;
    private String data_update;
    private String old_version;

    public String getApk_developer() {
        return apk_developer;
    }

    public void setApk_developer(String apk_developer) {
        this.apk_developer = apk_developer;
    }

    public String getApk_name() {
        return apk_name;
    }

    public void setApk_name(String apk_name) {
        this.apk_name = apk_name;
    }

    public String getApk_icon() {
        return apk_icon;
    }

    public void setApk_icon(String apk_icon) {
        this.apk_icon = apk_icon;
    }

    public String getApk_grade() {
        return apk_grade;
    }

    public void setApk_grade(String apk_grade) {
        this.apk_grade = apk_grade;
    }

    public String getEvaluate_number() {
        return evaluate_number;
    }

    public void setEvaluate_number(String evaluate_number) {
        this.evaluate_number = evaluate_number;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getApk_size() {
        return apk_size;
    }

    public void setApk_size(String apk_size) {
        this.apk_size = apk_size;
    }

    public String getApk_version() {
        return apk_version;
    }

    public void setApk_version(String apk_version) {
        this.apk_version = apk_version;
    }

    public String getUpdate_date() {
        return update_date;
    }

    public void setUpdate_date(String update_date) {
        this.update_date = update_date;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getApk_permission() {
        return apk_permission;
    }

    public void setApk_permission(String apk_permission) {
        this.apk_permission = apk_permission;
    }

    public List<String> getApk_screenshots() {
        return apk_screenshots;
    }

    public void setApk_screenshots(List<String> apk_screenshots) {
        this.apk_screenshots = apk_screenshots;
    }

    public String getApk_introduce() {
        return apk_introduce;
    }

    public void setApk_introduce(String apk_introduce) {
        this.apk_introduce = apk_introduce;
    }

    public String getVersion_feature() {
        return version_feature;
    }

    public void setVersion_feature(String version_feature) {
        this.version_feature = version_feature;
    }

    public String getData_update() {
        return data_update;
    }

    public void setData_update(String data_update) {
        this.data_update = data_update;
    }

    public String getOld_version() {
        return old_version;
    }

    public void setOld_version(String old_version) {
        this.old_version = old_version;
    }

    public int versionController() {
        if (old_version != null) {
            return old_version.compareTo(apk_version);
        }

        return Integer.MIN_VALUE;
    }

    @Override
    public String toString() {
        return "PackageInfo{" +
                "apk_developer='" + apk_developer + '\'' +
                ", apk_name='" + apk_name + '\'' +
                ", apk_icon='" + apk_icon + '\'' +
                ", download_url='" + download_url + '\'' +
                ", apk_size='" + apk_size + '\'' +
                ", apk_version='" + apk_version + '\'' +
                ", update_date='" + update_date + '\'' +
                ", package_name='" + package_name + '\'' +
                ", apk_permission='" + apk_permission + '\'' +
                ", apk_screenshots=" + apk_screenshots +
                ", apk_introduce='" + apk_introduce + '\'' +
                ", version_feature='" + version_feature + '\'' +
                ", data_update='" + data_update + '\'' +
                ", old_version='" + old_version + '\'' +
                '}';
    }
}
