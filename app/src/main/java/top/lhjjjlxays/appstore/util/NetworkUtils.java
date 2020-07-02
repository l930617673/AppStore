package top.lhjjjlxays.appstore.util;

import com.lzy.okgo.model.HttpHeaders;

/**
 * @author lhj
 * @version 1.0
 * @date 2020/5/8 13:40
 * @description
 */
public class NetworkUtils {
    private static HttpHeaders HEADERS = new HttpHeaders();
    private static String BASE_URL = "https://lhjjjlxays.top/NetServer";

    public static String SHOW_URL = BASE_URL + "/show";
    public static String UPLOAD_URL = BASE_URL + "/upload";
    public static String SEARCH_URL = BASE_URL + "/search";
    public static String SEARCH_DETAIL = BASE_URL + "/detail";
    public static String DOWNLOAD_URL = BASE_URL + "/download";
    public static String INITIALIZE_URL = BASE_URL + "/initialize";
    public static String VERIFY_CODE_URL = BASE_URL + "/verifyCode";
    public static String LOCATION_URL = "http://api.map.baidu.com/reverse_geocoding/v3/?ak=ZhqOIBcYVUgCjUYaRIHHBUwIkVhFSbz4&output=json&coordtype=wgs84ll&location=%f,%f";

    public static HttpHeaders getHeaders() {
        HEADERS.put("Accept", "text/json,text/html,application/json;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        HEADERS.put("Accept-Encoding", "gzip, deflate, br");
        HEADERS.put("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7");
        HEADERS.put("Cache-Control", "no-cache");
        HEADERS.put("Connection", "keep-alive");
        HEADERS.put("Host", "www.lhjjjlxays.com");
        HEADERS.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.138 Safari/537.36");
        return HEADERS;
    }
}
