package top.lhjjjlxays.appstore;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppPermissionActivity extends AppCompatActivity implements View.OnClickListener {
    private String permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permission);

        EventBus.getDefault().register(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        ((TextView) findViewById(R.id.page_name)).setText("应用权限");
        ((TextView) findViewById(R.id.tv_apk_permission)).setText(permission);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onGetStickyEvent(String permission) {
        this.permission = permission;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_back) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
