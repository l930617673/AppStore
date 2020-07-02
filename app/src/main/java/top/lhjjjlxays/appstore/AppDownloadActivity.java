package top.lhjjjlxays.appstore;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.jdsjlzx.recyclerview.LRecyclerView;
import com.github.jdsjlzx.recyclerview.LRecyclerViewAdapter;
import com.lzy.okgo.OkGo;
import com.lzy.okserver.OkDownload;
import com.lzy.okserver.download.DownloadTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Map;

import top.lhjjjlxays.appstore.adapter.ApplicationMessageAdapter;
import top.lhjjjlxays.appstore.adapter.ApplicationMessageAdapter.*;
import top.lhjjjlxays.appstore.bean.ApkGeneral;
import top.lhjjjlxays.appstore.bean.MessageEvent;

public class AppDownloadActivity extends AppCompatActivity implements View.OnClickListener,
        OnItemClickListener, OnCancelClickListener {
    private static final String TAG = AppDownloadActivity.class.getSimpleName();

    private Context mContext;
    private boolean isPause = false;
    private ArrayList<ApkGeneral> download;
    private Map<String, DownloadTask> taskMap;
    private ApplicationMessageAdapter mAdapter;

    private TextView tv_app_download;
    private Button btn_app_download;
    private LRecyclerView lrv_app_download;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_download);

        mContext = this;
        OkGo.getInstance().init(getApplication());
        EventBus.getDefault().register(this);

        initFindView();
        initRecyclerView();
    }

    public void initFindView() {
        tv_app_download = findViewById(R.id.tv_app_download);
        btn_app_download = findViewById(R.id.btn_app_download);
        lrv_app_download = findViewById(R.id.lrv_app_download);

        tv_app_download.setText(String.format("下载（%s）", download.size()));

        btn_app_download.setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        ((TextView) findViewById(R.id.page_name)).setText("下载管理");
    }

    public void initRecyclerView() {
        mAdapter = new ApplicationMessageAdapter(mContext, download);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setOnCancelClickListener(this);

        LRecyclerViewAdapter adapter = new LRecyclerViewAdapter(mAdapter);
        lrv_app_download.setAdapter(adapter);

        lrv_app_download.setLayoutManager(new LinearLayoutManager(mContext, RecyclerView.VERTICAL, false));
        lrv_app_download.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        lrv_app_download.setPullRefreshEnabled(false);
        lrv_app_download.setLoadMoreEnabled(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetStickyEvent(MessageEvent message) {
        download = message.getDownload();
        taskMap = message.getTaskMap();
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
            case R.id.btn_app_download:
                if (isPause) {
                    OkDownload.getInstance().startAll();
                    btn_app_download.setText("全部暂停");
                } else {
                    OkDownload.getInstance().pauseAll();
                    btn_app_download.setText("全部开始");
                }

                isPause = !isPause;
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position < download.size()) {
            EventBus.getDefault().postSticky(download.get(position));
            Intent intent = new Intent(this, AppInfoActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onCancelClick(View view, int position) {
        if (position < download.size()) {
            download.remove(position);
            mAdapter.notifyDataSetChanged();
            tv_app_download.setText(String.format("下载（%s）", download.size()));
        }
    }

}
