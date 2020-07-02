package top.lhjjjlxays.appstore.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import top.lhjjjlxays.appstore.R;
import top.lhjjjlxays.appstore.bean.ApkGeneral;
import top.lhjjjlxays.appstore.bean.ApkInfo;
import top.lhjjjlxays.appstore.util.ApkUtils;
import top.lhjjjlxays.appstore.util.DataUtil;

// 展示应用包信息列表
@SuppressLint("UseSparseArrays")
public class ApplicationMessageAdapter extends RecyclerView.Adapter<ApplicationMessageAdapter.ViewHolder> {
    private final static String TAG = ApplicationMessageAdapter.class.getSimpleName();

    private String activityTag;

    private Context mContext; // 声明一个上下文对象
    private ArrayList<ApkGeneral> mApkGeneralList; // 应用信息队列
    private ArrayList<ApkInfo> mDownloadedApkList; // 已下载的APK文件队列
    private HashMap<Integer, ViewHolder> mViewMap = new HashMap<>(); // 视图持有者的映射

    public ApplicationMessageAdapter(Context context, ArrayList<ApkGeneral> mApkGeneralList) {
        this.mContext = context;
        this.mApkGeneralList = mApkGeneralList;
        // 获取设备中所有已下载的APK文件
        this.mDownloadedApkList = ApkUtils.getAllApkFile(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_package, parent, false);
        return new ViewHolder(itemView);
    }

    /**
     * 由于RecyclerView的onBindViewHolder()方法，
     * 只有在getItemViewType()返回类型不同时才会调用，
     * 这点是跟ListView的getView()方法不同的地方，
     * 所以如果想要每次都调用onBindViewHolder()刷新item数据，
     * 就要重写getItemViewType()，让其返回position，否则很容易产生数据错乱的现象。
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final ApkGeneral item = mApkGeneralList.get(position);
        Progress progress = DownloadManager.getInstance().get(item.getDownload_url());
        if (progress == null) {
            holder.ll_store_info.setVisibility(View.VISIBLE);
            holder.ll_store_download.setVisibility(View.GONE);
        } else {
            holder.ll_store_info.setVisibility(View.GONE);
            holder.ll_store_download.setVisibility(View.VISIBLE);
        }

        holder.tv_store_apk_name.setText(item.getApk_name());
        String temp = item.getApk_grade();
        float tt = 0;
        if (temp != null && temp.length() > 0) {
            temp = temp.replaceAll(" ", "").trim();
            tt = Float.parseFloat(temp) / 2;
        }
        holder.rb_store_apk_grade.setRating(tt);
        holder.tv_store_apk_size.setText(item.getApk_size());
        holder.tag = item.getDownload_url();
        holder.pb_store_download.setMax(1000);
        Glide.with(mContext)
                .asBitmap()
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(25)))
                .load(item.getApk_icon())
                .into(holder.iv_store_img);

        String version = ApkUtils.getInstallVersion(mContext, item.getPackage_name());
        if (!TextUtils.isEmpty(version)) {
            item.setOld_version(version);
            holder.tv_store_old_version.setText(version);
        }

        holder.tv_store_new_version.setText(item.getApk_version());
        holder.tv_store_new_version.setTextColor(holder.tv_store_old_version.getTextColors());

        int judge = item.versionController();
        if (judge == 0) {
            holder.tv_store_old_version.setVisibility(View.VISIBLE);
        } else if (item.versionController() > 0) {
            holder.tv_store_old_version.setVisibility(View.VISIBLE);
            holder.tv_store_new_version.setTextColor(Color.BLUE);
        } else if (judge < 0 && judge != Integer.MIN_VALUE) {
            holder.tv_store_old_version.setVisibility(View.VISIBLE);
            holder.tv_store_new_version.setTextColor(Color.RED);
        } else {
            holder.tv_store_old_version.setVisibility(View.GONE);
        }

        // 暂不支持应用升级。替代做法是：先卸载旧版本，再下载装新版本
        if (!TextUtils.isEmpty(item.getOld_version())) {
            if (judge == 0) {
                holder.btn_store_operation.setText("最新");
                holder.btn_store_operation.setEnabled(false);
            } else if (judge > 0) {
                holder.btn_store_operation.setText("待更");
                holder.btn_store_operation.setEnabled(false);
            } else {
                holder.btn_store_operation.setText("更新");
                holder.btn_store_operation.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 卸载指定包名的应用
                        ApkUtils.uninstall(mContext, item.getPackage_name());
                        holder.btn_store_operation.setText("下载");
                        holder.btn_store_operation.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                download(holder);
                            }
                        });
                    }
                });
            }
        } else {    // 检查本地是否已有该版本下载完的安装包
            final String apkPath = getLocalPath(item.getPackage_name(), item.getApk_version());
            if (TextUtils.isEmpty(apkPath)) { // 本地未找到最新安装包，则需联网下载
                holder.btn_store_operation.setText("下载");
                if (TextUtils.isEmpty(item.getDownload_url())) { // 没有下载地址
                    holder.btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(mContext, "无下载地址", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else { // 有下载地址
                    holder.btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 从指定地址下载该应用的安装包
                            download(holder);
                        }
                    });
                    holder.init();
                }
            } else { // 本地已有最新安装包，则设置应用状态为已下载
                // 设置应用状态为已下载
                setDownloaded(holder, new File(apkPath));
            }
        }

        if (!mViewMap.containsKey(position)) {
            mViewMap.put(position, holder);
        }
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public int getItemCount() {
        return mApkGeneralList.size();
    }

    // 下载完成，准备进行安装
    private void setDownloaded(ViewHolder holder, final File file) {
        holder.btn_store_operation.setText("安装");
        holder.tv_store_download_speed.setText("下载完成");
        holder.btn_store_operation.setTextColor(Color.GREEN);
        holder.btn_store_operation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ApkUtils.install(mContext, file);
            }
        });
    }

    private void download(ViewHolder holder) {
        holder.ll_store_info.setVisibility(View.GONE);
        holder.ll_store_download.setVisibility(View.VISIBLE);
        holder.pb_store_download.setProgress(0);
        holder.tv_store_download_size.setText("0");
        holder.tv_store_download_speed.setText("0 K/s");
        holder.start();
    }

    // 获取该App指定版本号的安装包路径，返回非空则表示有找到
    private String getLocalPath(String packageName, String versionName) {
        String local_path = "";
        for (ApkInfo info : mDownloadedApkList) {
            if (packageName.equals(info.package_name) && versionName.equals(info.version_name)) {
                local_path = info.file_path;
            }
        }
        return local_path;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //默认显示
        LinearLayout ll_store_info;
        ImageView iv_store_img;
        TextView tv_store_apk_name;
        RatingBar rb_store_apk_grade;
        TextView tv_store_apk_size;
        TextView tv_store_old_version;
        TextView tv_store_new_version;

        //下载时显示
        LinearLayout ll_store_download;
        ProgressBar pb_store_download;
        TextView tv_store_download_size;
        TextView tv_store_download_speed;

        Button btn_store_download_cancel;
        Button btn_store_operation;

        //下载任务
        private DownloadTask task;
        private String tag;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ll_store_info = itemView.findViewById(R.id.ll_store_info);
            this.iv_store_img = itemView.findViewById(R.id.iv_store_img);
            this.tv_store_apk_size = itemView.findViewById(R.id.tv_store_apk_size);
            this.tv_store_apk_name = itemView.findViewById(R.id.tv_store_apk_name);
            this.tv_store_old_version = itemView.findViewById(R.id.tv_store_old_version);
            this.tv_store_new_version = itemView.findViewById(R.id.tv_store_new_version);
            this.rb_store_apk_grade = itemView.findViewById(R.id.rb_store_apk_grade);

            this.ll_store_download = itemView.findViewById(R.id.ll_store_download);
            this.pb_store_download = itemView.findViewById(R.id.pb_store_download);
            this.tv_store_download_size = itemView.findViewById(R.id.tv_store_download_size);
            this.tv_store_download_speed = itemView.findViewById(R.id.tv_store_download_speed);

            this.btn_store_operation = itemView.findViewById(R.id.btn_store_operation);

            btn_store_download_cancel = itemView.findViewById(R.id.btn_store_download_cancel);

            //设置取消键点击事件
            btn_store_download_cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ll_store_download.setVisibility(View.GONE);
                    ll_store_info.setVisibility(View.VISIBLE);

                    if (onCancelClickListener != null) {
                        onCancelClickListener.onCancelClick(v, getLayoutPosition() - 1);
                    }

                    cancel(true);
                    btn_store_operation.setText("下载");
                    btn_store_operation.setTextColor(Color.BLACK);

                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 从指定地址下载该应用的安装包
                            ll_store_info.setVisibility(View.GONE);
                            ll_store_download.setVisibility(View.VISIBLE);
                            pb_store_download.setProgress(0);
                            tv_store_download_size.setText("0");
                            tv_store_download_speed.setText("0 K/s");
                            start();
                        }
                    });
                }
            });

            //设置点击视图回调函数
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(v, getLayoutPosition() - 1);
                    }
                }
            });

            //设置长按视图回调函数
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(v, getLayoutPosition() - 1);
                    }

                    return false;
                }
            });
        }

        void init() {
            DownloadListener downloadListener = new StoreDownloadListener(tag, this);
            Progress progress = DownloadManager.getInstance().get(tag);
            if (progress != null) {
                this.task = OkDownload.restore(progress).register(downloadListener);
                ll_store_info.setVisibility(View.GONE);
                ll_store_download.setVisibility(View.VISIBLE);

                String currentSize = DataUtil.formatData(progress.currentSize);
                String totalSize = DataUtil.formatData(progress.totalSize);

                btn_store_operation.setText("继续");
                tv_store_download_speed.setText("已暂停");
                pb_store_download.setProgress((int) (progress.fraction * 1000));
                tv_store_download_size.setText(String.format("%s/%s", currentSize, totalSize));

                switch (progress.status) {
                    case Progress.NONE:
                        break;
                    case Progress.PAUSE:
                        tv_store_download_speed.setText("已暂停");
                        btn_store_operation.setText("继续");
                        btn_store_operation.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                start();
                            }
                        });
                        break;
                    case Progress.ERROR:
                        tv_store_download_speed.setText("下载出错");
                        btn_store_operation.setText("重下");
                        btn_store_operation.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                restart();
                            }
                        });
                        break;
                    case Progress.WAITING:
                        tv_store_download_speed.setText("等待中");
                        btn_store_operation.setText("暂停");
                        btn_store_operation.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pause();
                            }
                        });
                        break;
                    case Progress.FINISH:
                        tv_store_download_speed.setText("已完成");
                        setDownloaded(this, new File(progress.filePath));
                        break;
                    case Progress.LOADING:
                        String speed = DataUtil.formatData(progress.speed);
                        tv_store_download_speed.setText(String.format("%s/s", speed));
                        btn_store_operation.setText("暂停");
                        btn_store_operation.setOnClickListener(new OnClickListener() {
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
            String currentSize = DataUtil.formatData(progress.currentSize);
            String totalSize = DataUtil.formatData(progress.totalSize);

            pb_store_download.setProgress((int) (progress.fraction * 1000));
            tv_store_download_size.setText(String.format("%s/%s", currentSize, totalSize));

            switch (progress.status) {
                case Progress.NONE:
                    tv_store_download_speed.setText("无状态");
                    btn_store_operation.setText("下载");
                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            start();
                        }
                    });
                    break;
                case Progress.PAUSE:
                    tv_store_download_speed.setText("已暂停");
                    btn_store_operation.setText("继续");
                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            start();
                        }
                    });
                    break;
                case Progress.ERROR:
                    tv_store_download_speed.setText("下载出错");
                    btn_store_operation.setText("重下");
                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            restart();
                        }
                    });
                    break;
                case Progress.WAITING:
                    tv_store_download_speed.setText("等待中");
                    btn_store_operation.setText("暂停");
                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pause();
                        }
                    });
                    break;
                case Progress.FINISH:
                    setDownloaded(this, new File(progress.filePath));
                    break;
                case Progress.LOADING:
                    String speed = DataUtil.formatData(progress.speed);
                    tv_store_download_speed.setText(String.format("%s/s", speed));
                    btn_store_operation.setText("暂停");
                    btn_store_operation.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pause();
                        }
                    });
                    break;
            }
        }

        public void start() {
            DownloadListener downloadListener = new StoreDownloadListener(tag, this);
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
                case Progress.LOADING:
                    task.pause();
                    break;
                case Progress.FINISH:
                    setDownloaded(this, new File(progress.filePath));
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

        void cancel(boolean isDeleteFile) {
            task.unRegister(tag);
            task.remove(isDeleteFile);
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

    private class StoreDownloadListener extends DownloadListener {

        private ViewHolder holder;

        StoreDownloadListener(Object tag, ViewHolder holder) {
            super(tag);
            this.holder = holder;
        }

        @Override
        public void onStart(Progress progress) {
        }

        @Override
        public void onProgress(Progress progress) {
            if (tag == holder.getTag()) {
                holder.refresh(progress);
            }
        }

        @Override
        public void onError(Progress progress) {
            Throwable throwable = progress.exception;
            if (throwable != null) throwable.printStackTrace();
        }

        @Override
        public void onFinish(File file, Progress progress) {
            setDownloaded(holder, new File(progress.filePath));
        }

        @Override
        public void onRemove(Progress progress) {
            holder.remove();
            holder.btn_store_operation.setText("重下");
            holder.btn_store_operation.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.start();
                }
            });
        }
    }

    //回调函数实现监听器
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    //回调函数实现监听器
    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }

    //回调函数实现监听器
    private OnCancelClickListener onCancelClickListener;

    public void setOnCancelClickListener(OnCancelClickListener onCancelClickListener) {
        this.onCancelClickListener = onCancelClickListener;
    }

    public interface OnCancelClickListener {
        void onCancelClick(View view, int position);
    }
}
