package top.lhjjjlxays.appstore;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadListener;
import com.lzy.okserver.download.DownloadTask;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import top.lhjjjlxays.appstore.bean.ApkDetail;
import top.lhjjjlxays.appstore.bean.ApkGeneral;
import top.lhjjjlxays.appstore.util.ApkUtils;
import top.lhjjjlxays.appstore.util.GlideSizeTransformUtil;
import top.lhjjjlxays.appstore.util.NetworkUtils;

public class AppInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = AppInfoActivity.class.getSimpleName();

    private Context mContext;
    private ApkGeneral apkGeneral;
    private ApkDetail apkDetail;
    private DownloadTask task;
    private String tag;

    private TextView page_name;
    private ImageView iv_apk_icon;
    private TextView tv_apk_name;
    private TextView tv_apk_grade;
    private TextView tv_apk_size;
    private TextView tv_evaluate_number;
    private RecyclerView rv_apk_screenshots;
    private TextView tv_update_date;
    private TextView tv_apk_version;
    private ExpandableTextView et_apk_introduce;
    private ExpandableTextView et_version_feature;
    private TextView tv_apk_developer;
    private LinearLayout ll_apk_hide;
    private ProgressBar pb_apk_loading;

    private Button btn_apk_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        mContext = this;
        OkGo.getInstance().init(getApplication());
        EventBus.getDefault().register(this);

        initFindView();
        initializeByOkGo();
    }

    public void initFindView() {
        page_name = findViewById(R.id.page_name);

        iv_apk_icon = findViewById(R.id.iv_apk_icon);
        tv_apk_name = findViewById(R.id.tv_apk_name);
        tv_apk_grade = findViewById(R.id.tv_apk_grade);
        tv_apk_size = findViewById(R.id.tv_apk_size);
        tv_evaluate_number = findViewById(R.id.tv_evaluate_number);
        rv_apk_screenshots = findViewById(R.id.rv_apk_screenshots);
        tv_update_date = findViewById(R.id.tv_update_date);
        tv_apk_version = findViewById(R.id.tv_apk_version);
        et_apk_introduce = findViewById(R.id.et_apk_introduce);
        et_version_feature = findViewById(R.id.et_version_feature);
        tv_apk_developer = findViewById(R.id.tv_apk_developer);
        btn_apk_download = findViewById(R.id.btn_apk_download);
        ll_apk_hide = findViewById(R.id.ll_apk_hide);
        pb_apk_loading = findViewById(R.id.pb_apk_loading);

        btn_apk_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        findViewById(R.id.tv_apk_permission).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        //设置已知标签
        Glide.with(mContext)
                .asBitmap()
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(25)))
                .load(apkGeneral.getApk_icon())
                .into(iv_apk_icon);

        tv_apk_name.setText(apkGeneral.getApk_name());
        tv_apk_grade.setText(stringFormat(apkGeneral.getApk_grade()));
        tv_apk_size.setText(apkGeneral.getApk_size());

        pb_apk_loading.setVisibility(View.VISIBLE);
        btn_apk_download.setVisibility(View.GONE);
        ll_apk_hide.setVisibility(View.GONE);
    }

    public String stringFormat(String string) {
        double grade = Integer.parseInt(string) / 2.0;
        return String.format("%.1f 分", grade);
    }

    private void initializeByOkGo() {
        OkGo.<String>get(NetworkUtils.SEARCH_DETAIL)   // 请求方式和请求url
                .tag(this)                              // 请求的 tag, 主要用于取消对应的请求
                .params("apk", apkGeneral.getPackage_name())
                .cacheMode(CacheMode.NO_CACHE)          // 缓存模式，详细请看缓存介绍
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String result = response.body();

                        if (result.length() < 20) {
                            Toast.makeText(mContext, "数据错误", Toast.LENGTH_LONG).show();
                        } else {    // 把json串转换为PackageResp类型的数据对象packageResp
                            ApkDetail apkDetail1 = new Gson().fromJson(result, ApkDetail.class);
                            if (apkDetail1 == null) {
                                Toast.makeText(mContext, "数据错误", Toast.LENGTH_LONG).show();
                            } else {
                                apkDetail = apkDetail1;
                                setInfo();
                                init();
                            }
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Toast.makeText(mContext, "数据错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setInfo() {
        tv_evaluate_number.setText(apkDetail.getEvaluate_number());

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mContext, apkDetail.getApk_screenshots());
        rv_apk_screenshots.setAdapter(adapter);
        rv_apk_screenshots.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        tv_update_date.setText(apkDetail.getUpdate_date() + " 更新");
        tv_apk_version.setText(apkGeneral.getApk_version() + " 版本");
        et_apk_introduce.setText(apkDetail.getApk_introduce());
        et_version_feature.setText(apkDetail.getVersion_feature());
        tv_apk_developer.setText(apkDetail.getApk_developer());

        tag = apkGeneral.getDownload_url();
        page_name.setText(apkGeneral.getApk_name());

        pb_apk_loading.setVisibility(View.GONE);
        btn_apk_download.setVisibility(View.VISIBLE);
        ll_apk_hide.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetStickyEvent(ApkGeneral apkGeneral) {
        this.apkGeneral = apkGeneral;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_apk_permission:
                EventBus.getDefault().postSticky(apkDetail.getApk_permission());
                Intent intent = new Intent(this, AppPermissionActivity.class);
                startActivity(intent);
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
        private Context mContext; // 声明一个上下文对象
        private List<String> screenshots; // 应用信息队列

        RecyclerViewAdapter(Context context, List<String> list) {
            mContext = context;
            screenshots = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_image, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            String image = screenshots.get(position);

            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(100)))
                    .into(new GlideSizeTransformUtil(holder.iv_apk_screenshot, mContext));
        }

        @Override
        public int getItemCount() {
            return screenshots.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView iv_apk_screenshot;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iv_apk_screenshot = itemView.findViewById(R.id.iv_apk_screenshot);
            }
        }
    }

    void init() {
        int judge = apkGeneral.versionController();
        if (!TextUtils.isEmpty(apkGeneral.getOld_version())) {
            if (judge == 0) {
                btn_apk_download.setText("最新");
                btn_apk_download.setEnabled(false);
            } else if (judge > 0) {
                btn_apk_download.setText("待更");
                btn_apk_download.setEnabled(false);
            } else {
                btn_apk_download.setText("更新");
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 卸载指定包名的应用
                        ApkUtils.uninstall(mContext, apkGeneral.getPackage_name());
                        btn_apk_download.setText("下载");
                        btn_apk_download.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                start();
                            }
                        });
                    }
                });
            }
        }

        Progress progress = DownloadManager.getInstance().get(tag);
        if (progress != null) {
            DownloadListener downloadListener = new StoreDownloadListener(tag);
            this.task = OkDownload.restore(progress).register(downloadListener);
            switch (progress.status) {
                case Progress.NONE:
                    break;
                case Progress.PAUSE:
                    btn_apk_download.setText("继续");
                    btn_apk_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            start();
                        }
                    });
                    break;
                case Progress.ERROR:
                    btn_apk_download.setText("重下");
                    btn_apk_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restart();
                        }
                    });
                    break;
                case Progress.WAITING:
                    btn_apk_download.setText("暂停");
                    btn_apk_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pause();
                        }
                    });
                    break;
                case Progress.FINISH:
                    setDownloaded(new File(progress.filePath));
                    break;
                case Progress.LOADING:
                    btn_apk_download.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pause();
                        }
                    });
                    break;
            }
        }
    }

    void refresh(final Progress progress) {
        btn_apk_download.setText((int) (progress.fraction * 100) + "%");

        switch (progress.status) {
            case Progress.NONE:
                btn_apk_download.setText("下载");
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start();
                    }
                });
                break;
            case Progress.PAUSE:
                btn_apk_download.setText("继续");
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        start();
                    }
                });
                break;
            case Progress.ERROR:
                btn_apk_download.setText("重下");
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        restart();
                    }
                });
                break;
            case Progress.WAITING:
                btn_apk_download.setText("暂停");
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pause();
                    }
                });
                break;
            case Progress.FINISH:
                setDownloaded(new File(progress.filePath));
                break;
            case Progress.LOADING:
                btn_apk_download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pause();
                    }
                });
                break;
        }
    }

    public void start() {
        DownloadListener downloadListener = new StoreDownloadListener(tag);
        Progress progress = DownloadManager.getInstance().get(tag);
        if (progress == null) {
            GetRequest<File> request = OkGo.get(tag);
            this.task = OkDownload.request(tag, request)
                    .register(downloadListener)
                    .save();
        } else {
            this.task = OkDownload.restore(progress).register(downloadListener);
        }

        progress = task.progress;

        switch (progress.status) {
            case Progress.PAUSE:
            case Progress.NONE:
            case Progress.ERROR:
                task.start();
                break;
            case Progress.FINISH:
                setDownloaded(new File(progress.filePath));
                break;
        }
        refresh(progress);
    }

    void pause() {
        task.pause();
    }

    void remove() {
        task.remove(true);
    }

    void restart() {
        task.restart();
    }

    void setDownloaded(final File file) {
        btn_apk_download.setText("安装");
        btn_apk_download.setTextColor(Color.GREEN);
        btn_apk_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApkUtils.install(mContext, file);
            }
        });
    }

    private class StoreDownloadListener extends DownloadListener {

        StoreDownloadListener(Object tag) {
            super(tag);
        }

        @Override
        public void onStart(Progress progress) {

        }

        @Override
        public void onProgress(Progress progress) {
            refresh(progress);
        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
            setDownloaded(file);
        }

        @Override
        public void onRemove(Progress progress) {
            remove();
            btn_apk_download.setText("安装");
            btn_apk_download.setTextColor(Color.GREEN);
            btn_apk_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    start();
                }
            });
        }
    }
}
