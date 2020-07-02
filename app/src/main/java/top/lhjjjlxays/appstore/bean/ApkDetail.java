package top.lhjjjlxays.appstore.bean;

import java.util.List;

public class ApkDetail {
    private String apk_developer;       //开发商
    private String evaluate_number;     //评论次数
    private String update_date;         //更新时间
    private String package_name;        //包名must
    private String apk_permission;      //权限
    private List<String> apk_screenshots; //截图
    private String apk_introduce;       //简介
    private String version_feature;     //版本特性

    public ApkDetail() {
    }

    public String getApk_developer() {
        return apk_developer;
    }

    public void setApk_developer(String apk_developer) {
        this.apk_developer = apk_developer;
    }

    public String getEvaluate_number() {
        return evaluate_number;
    }

    public void setEvaluate_number(String evaluate_number) {
        this.evaluate_number = evaluate_number;
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
}
