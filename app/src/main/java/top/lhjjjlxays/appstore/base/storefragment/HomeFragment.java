package top.lhjjjlxays.appstore.base.storefragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jdsjlzx.interfaces.OnLoadMoreListener;
import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import top.lhjjjlxays.appstore.AppInfoActivity;
import top.lhjjjlxays.appstore.R;
import top.lhjjjlxays.appstore.adapter.ApplicationMessageAdapter;
import top.lhjjjlxays.appstore.base.BaseFragment;
import top.lhjjjlxays.appstore.bean.MessageEvent;
import top.lhjjjlxays.appstore.bean.PackageInfo;
import top.lhjjjlxays.appstore.bean.PackageResp;
import top.lhjjjlxays.appstore.util.NetworkUtils;

public class HomeFragment extends BaseFragment implements
        ApplicationMessageAdapter.OnItemClickListener {
    private String TAG = HomeFragment.class.getSimpleName();
    private int page = 1;

    private Context mContext;
    private LRecyclerView lrv_package;
    private ProgressBar pb_store_home_loading;
    private ApplicationMessageAdapter mAdapter;
    private ArrayList<PackageInfo> mPackageList = new ArrayList<>(); // 已安装应用的包信息队列
    private Map<String, PackageInfo> mPackageMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        // 启动初始化商店加载应用
        page = 1;
        new Handler().postDelayed(initialize, 100);
    }

    @Override
    public void onResume() {
        super.onResume();
        //重新开始，刷新列表，否则不能显示下载任务
        mAdapter.notifyDataSetChanged();
    }

    public MessageEvent getMessageEvent() {
        ArrayList<PackageInfo> package1 = new ArrayList<>();
        ArrayList<PackageInfo> package2 = new ArrayList<>();

        Map<String, DownloadTask> map = OkDownload.getInstance().getTaskMap();

        for (String key : map.keySet()) {
            Progress progress = DownloadManager.getInstance().get(key);
            if (progress != null) {
                PackageInfo info = mPackageMap.get(key);
                if (info != null) {
                    if (progress.status == Progress.FINISH) {
                        package2.add(info);
                    } else {
                        package1.add(info);
                    }
                }
            }
        }

        package1.addAll(package2);

        return new MessageEvent("tasks", package1, map);
    }


    @Override
    protected View initView() {
        View root = LayoutInflater.from(mContext).inflate(R.layout.fragment_store_home, null);
        lrv_package = root.findViewById(R.id.rv_store_home_package);
        pb_store_home_loading = root.findViewById(R.id.pb_store_home_loading);

        // 下面利用最新的包信息队列，刷新应用列表的展示
        setLRecyclerView();
        lrv_package.setVisibility(View.GONE);

        return root;
    }

    private void setLRecyclerView() {
        mAdapter = new ApplicationMessageAdapter(mContext, mPackageList);
        mAdapter.setOnItemClickListener(this);

        LRecyclerViewAdapter adapter = new LRecyclerViewAdapter(mAdapter);
        lrv_package.setAdapter(adapter);

        lrv_package.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        lrv_package.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        lrv_package.setPullRefreshEnabled(false);

        lrv_package.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                initializeByOkGo();
            }
        });
    }

    // 定义一个已安装应用的版本检查任务
    private Runnable initialize = new Runnable() {
        @Override
        public void run() {
            // 弹出默认的圆圈进度对话框
            pb_store_home_loading.setVisibility(View.VISIBLE);
            initializeByOkGo();
        }
    };

    private void initializeByOkGo() {
        OkGo.<String>get(NetworkUtils.INITIALIZE_URL)   // 请求方式和请求url
                .tag(this)                              // 请求的 tag, 主要用于取消对应的请求
                .params("page", page)
                .cacheMode(CacheMode.NO_CACHE)          // 缓存模式，详细请看缓存介绍
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {
                        String result = response.body();

                        if (result.length() < 20) {
                            lrv_package.setNoMore(true);
                            lrv_package.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    lrv_package.setNoMore(true);
                                }
                            });
                        } else {    // 把json串转换为PackageResp类型的数据对象packageResp
                            PackageResp packageResp = new Gson().fromJson(result, PackageResp.class);
                            if (packageResp.package_list == null) {
                                Toast.makeText(mContext, "数据错误", Toast.LENGTH_LONG).show();
                            } else {
                                mPackageList.addAll(packageResp.package_list);
                                lrv_package.refreshComplete(packageResp.package_list.size());

                                for (PackageInfo info : packageResp.package_list) {
                                    mPackageMap.put(info.getDownload_url(), info);
                                }
                            }

                            page++;
                        }

                        lrv_package.setVisibility(View.VISIBLE);
                        if (pb_store_home_loading != null &&
                                pb_store_home_loading.getVisibility() == View.VISIBLE) {
                            pb_store_home_loading.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onError(Response<String> response) {
                        super.onError(response);
                        Toast.makeText(mContext, "数据错误", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position < mPackageList.size() && position >= 0) {
            EventBus.getDefault().postSticky(mPackageList.get(position));
            Intent intent = new Intent(getActivity(), AppInfoActivity.class);
            startActivity(intent);
        }
    }
}
