package top.lhjjjlxays.appstore;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.db.DownloadManager;
import com.lzy.okserver.OkDownload;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import top.lhjjjlxays.appstore.base.BaseFragment;
import top.lhjjjlxays.appstore.base.storefragment.HomeFragment;
import top.lhjjjlxays.appstore.base.storefragment.SearchFragment;
import top.lhjjjlxays.appstore.bean.MessageEvent;
import top.lhjjjlxays.appstore.util.PermissionUtils;

public class AppStoreActivity extends FragmentActivity implements
        View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    private String TAG = AppStoreActivity.class.getSimpleName();

    private Context context;
    private boolean isExit = false;
    private BaseFragment oldFragment;
    private PopupWindow popupWindow;
    private List<BaseFragment> baseFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_store);

        context = getApplicationContext();
        OkGo.getInstance().init(getApplication());
        OkDownload.getInstance().setFolder(Environment.getExternalStorageDirectory().getAbsolutePath());
        OkDownload.restore(DownloadManager.getInstance().getAll()); //这里是将数据库的数据恢复

        findViewById(R.id.ib_menu).setOnClickListener(this);
        ((TextView) findViewById(R.id.page_name)).setText("应用超市");
        ((RadioGroup) findViewById(R.id.rg_store_group)).setOnCheckedChangeListener(this);

        //初始化按钮
        initRadioButton();

        //解决Fragment重叠问题
        if (savedInstanceState == null) {
            initFragment();
        }
    }

    private void initRadioButton() {
        Drawable home = ResourcesCompat.getDrawable(getResources(), R.drawable.fragment_store_home_selector, null);
        Drawable search = ResourcesCompat.getDrawable(getResources(), R.drawable.fragment_store_selector, null);

        assert home != null;
        home.setBounds(0, 0, 64, 64);

        assert search != null;
        search.setBounds(0, 0, 64, 64);

        ((RadioButton) findViewById(R.id.rb_store_home)).setCompoundDrawables(null, home, null, null);
        ((RadioButton) findViewById(R.id.rb_store_search)).setCompoundDrawables(null, search, null, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
        if (!PermissionUtils.checkPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            finish();
        }
    }

    private void initFragment() {
        baseFragments = new ArrayList<>();
        BaseFragment home = new HomeFragment();
        baseFragments.add(home);
        baseFragments.add(new SearchFragment());

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        oldFragment = home;
        ft.add(R.id.fl_store_body, home).commit();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int position = 0;
        switch (checkedId) {
            case R.id.rb_store_home:
                position = 0;
                break;
            case R.id.rb_store_search:
                position = 1;
                break;
            default:
                break;
        }

        BaseFragment fragment = baseFragments.get(position);
        switchFragmentTo(oldFragment, fragment);
    }

    private void switchFragmentTo(BaseFragment from, BaseFragment to) {
        if (from != to) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

            oldFragment = to;

            if (!to.isAdded()) {

                if (from != null) {
                    ft.hide(from);
                }

                ft.add(R.id.fl_store_body, to).commit();
            } else {
                if (from != null) {
                    ft.hide(from);
                }

                ft.show(to).commit();
            }
        }
    }

    private void showPopupWindow(View view) {
        @SuppressLint("InflateParams") View contentView = LayoutInflater.from(context).inflate(R.layout.popup_window, null);
        popupWindow = new PopupWindow(contentView, 500, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        //设置各个控件的点击响应
        LinearLayout ll_first = contentView.findViewById(R.id.ll_first);
        ll_first.setVisibility(View.VISIBLE);
        Drawable drawable1 = ContextCompat.getDrawable(context, R.drawable.icon_pop_window_download);
        ((ImageView) ll_first.getChildAt(0)).setImageDrawable(drawable1);
        ((TextView) ll_first.getChildAt(1)).setText("下载管理");
        ll_first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HomeFragment homeFragment = (HomeFragment) baseFragments.get(0);
                SearchFragment searchFragment = (SearchFragment) baseFragments.get(1);
                MessageEvent event = homeFragment.getMessageEvent();
                event.addEvent(searchFragment.getMessageEvent());
                EventBus.getDefault().postSticky(event);

                Intent intent = new Intent(context, AppDownloadActivity.class);
                startActivity(intent);

                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
            }
        });

        LinearLayout ll_second = contentView.findViewById(R.id.ll_second);
        ll_second.setVisibility(View.VISIBLE);
        Drawable drawable2 = ContextCompat.getDrawable(context, R.drawable.icon_pop_window_exit);
        ((ImageView) ll_second.getChildAt(0)).setImageDrawable(drawable2);
        ((TextView) ll_second.getChildAt(1)).setText("退出");
        ll_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //显示PopupWindow
        popupWindow.showAsDropDown(view, 0, 50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OkDownload.getInstance().pauseAll();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ib_menu) {
            showPopupWindow(v);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isExit) {
                this.finish();
            } else {
                Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                isExit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isExit = false;
                    }
                }, 2000);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
