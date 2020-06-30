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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
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

import top.lhjjjlxays.appstore.bean.PackageInfo;
import top.lhjjjlxays.appstore.util.ApkUtils;
import top.lhjjjlxays.appstore.util.GlideSizeTransformUtil;

public class AppInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = AppInfoActivity.class.getSimpleName();

    private Context mContext;
    private PackageInfo packageInfo;
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

    private Button btn_apk_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        mContext = this;
        OkGo.getInstance().init(getApplication());
        EventBus.getDefault().register(this);

        initFindView();
        setInfo();
        init();
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

        btn_apk_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });

        findViewById(R.id.tv_apk_permission).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
    }

    public void setInfo() {
        Glide.with(mContext)
                .asBitmap()
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(25)))
                .load(packageInfo.getApk_icon())
                .into(iv_apk_icon);
        tv_apk_name.setText(packageInfo.getApk_name());
        tv_apk_grade.setText(stringFormat(packageInfo.getApk_grade()));
        tv_apk_size.setText(packageInfo.getApk_size());
        tv_evaluate_number.setText(packageInfo.getEvaluate_number());

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mContext, packageInfo.getApk_screenshots());
        rv_apk_screenshots.setAdapter(adapter);
        rv_apk_screenshots.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));

        tv_update_date.setText(packageInfo.getData_update() + " 更新");
        tv_apk_version.setText(packageInfo.getApk_version() + " 版本");
        et_apk_introduce.setText(packageInfo.getApk_introduce());
        et_version_feature.setText(packageInfo.getVersion_feature());
        tv_apk_developer.setText(packageInfo.getApk_developer());

        tag = packageInfo.getDownload_url();
        page_name.setText(packageInfo.getApk_name());
    }

    public String stringFormat(String string) {
        double grade = Integer.parseInt(string) / 2.0;
        return String.format("%.1f 分", grade);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetStickyEvent(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
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
                EventBus.getDefault().postSticky(packageInfo.getApk_permission());
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
        int judge = packageInfo.versionController();
        if (!TextUtils.isEmpty(packageInfo.getOld_version())) {
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
                        ApkUtils.uninstall(mContext, packageInfo.getPackage_name());
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
