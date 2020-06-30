package top.lhjjjlxays.appstore.base.storefragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
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

public class SearchFragment extends BaseFragment implements
        SearchView.OnQueryTextListener, ApplicationMessageAdapter.OnItemClickListener {
    private String TAG = SearchFragment.class.getSimpleName();

    private int page = 1;
    private String query;

    private Context mContext;
    private ProgressBar pb_store_progress;
    private SearchView sv_store_search;
    private LRecyclerView lrv_store_search_package;
    private ApplicationMessageAdapter mAdapter;
    private ArrayList<PackageInfo> mPackageList = new ArrayList<>(); // 已安装应用的包信息队列
    private Map<String, PackageInfo> mPackageMap = new HashMap<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();

        page = 1;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPackageList != null && mPackageList.size() > 0) {
            mAdapter.notifyDataSetChanged();
            lrv_store_search_package.setOnLoadMoreListener(new OnLoadMoreListener() {
                @Override
                public void onLoadMore() {
                    searchByOkGo();
                }
            });
        }

        if (sv_store_search != null)
            sv_store_search.clearFocus();
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
        View root = LayoutInflater.from(mContext).inflate(R.layout.fragment_store_search, null);
        sv_store_search = root.findViewById(R.id.sv_store_search);
        pb_store_progress = root.findViewById(R.id.pb_store_progress);
        lrv_store_search_package = root.findViewById(R.id.rv_store_search_package);

        sv_store_search.setSubmitButtonEnabled(true);
        sv_store_search.setOnQueryTextListener(this);

        setLRecyclerView();

        return root;
    }

    private void setLRecyclerView() {
        mAdapter = new ApplicationMessageAdapter(mContext, mPackageList);
        mAdapter.setOnItemClickListener(this);

        LRecyclerViewAdapter adapter = new LRecyclerViewAdapter(mAdapter);

        lrv_store_search_package.setAdapter(adapter);
        lrv_store_search_package.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        lrv_store_search_package.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        lrv_store_search_package.setPullRefreshEnabled(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        lrv_store_search_package.setVisibility(View.GONE);
        pb_store_progress.setVisibility(View.VISIBLE);


        initLRV(query);

        searchByOkGo();

        mAdapter.notifyDataSetChanged();
        return false;
    }

    private void initLRV(String query) {
        page = 1;
        this.query = query;

        lrv_store_search_package.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                searchByOkGo();
            }
        });

        lrv_store_search_package.removeAllViews();
        mPackageList.clear();
        mPackageMap.clear();
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    private void searchByOkGo() {
        OkGo.<String>get(NetworkUtils.SEARCH_URL)                            // 请求方式和请求url
                .tag(this)                       // 请求的 tag, 主要用于取消对应的请求
                .cacheKey("cacheKey")            // 设置当前请求的缓存key,建议每个不同功能的请求设置一个
                .params("param", query)      //参数
                .params("page", page)
                .cacheMode(CacheMode.NO_CACHE)    // 缓存模式，详细请看缓存介绍
                .execute(new StringCallback() {
                    @Override
                    public void onSuccess(Response<String> response) {

                        String result = response.body();

                        if (result.length() < 20) {
                            lrv_store_search_package.setNoMore(true);
                            lrv_store_search_package.setOnLoadMoreListener(new OnLoadMoreListener() {
                                @Override
                                public void onLoadMore() {
                                    lrv_store_search_package.setNoMore(true);
                                }
                            });
                        } else {    // 把json串转换为PackageResp类型的数据对象packageResp
                            PackageResp packageResp = new Gson().fromJson(result, PackageResp.class);
                            if (packageResp.package_list == null) {
                                Toast.makeText(mContext, "数据错误", Toast.LENGTH_LONG).show();
                            } else {
                                mPackageList.addAll(packageResp.package_list);
                                lrv_store_search_package.refreshComplete(packageResp.package_list.size());

                                for (PackageInfo info : packageResp.package_list) {
                                    mPackageMap.put(info.getDownload_url(), info);
                                }
                            }

                            page++;
                        }

                        pb_store_progress.setVisibility(View.GONE);
                        lrv_store_search_package.setVisibility(View.VISIBLE);
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
        if (position < mPackageList.size()) {
            EventBus.getDefault().postSticky(mPackageList.get(position));
            Intent intent = new Intent(getActivity(), AppInfoActivity.class);
            startActivity(intent);
        }
    }
}
