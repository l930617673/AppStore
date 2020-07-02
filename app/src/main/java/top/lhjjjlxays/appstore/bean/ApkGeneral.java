package top.lhjjjlxays.appstore.bean;

public class ApkGeneral {
    private String apk_name;        //名称
    private String apk_icon;        //icon
    private String apk_grade;       //评分
    private String apk_size;        //大小
    private String apk_version;     //版本
    private String download_url;    //下载地址
    private String package_name;    //包名must
    private String old_version;     //旧版本

    public ApkGeneral() {
    }

    public String getOld_version() {
        return old_version;
    }

    public void setOld_version(String old_version) {
        this.old_version = old_version;
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

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public int versionController() {
        if (old_version != null) {
            return old_version.compareTo(apk_version);
        }

        return Integer.MIN_VALUE;
    }
}
