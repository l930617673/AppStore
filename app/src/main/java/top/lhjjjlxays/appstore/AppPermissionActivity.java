package top.lhjjjlxays.appstore;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppPermissionActivity extends AppCompatActivity implements View.OnClickListener {
    private ListView lv_store_permission;
    private String[] permissions;
    private String permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_permission);

        EventBus.getDefault().register(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        lv_store_permission = findViewById(R.id.lv_store_permission);

        permissions = permission.replaceAll("▪", "").split("\n");
        lv_store_permission.setDivider(null);
        lv_store_permission.setAdapter(new PermissionDisplay(this, permissions));
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

    /**
     * 获得item视图
     */
    private class PermissionDisplay extends ArrayAdapter<String> {
        private Context context;
        private String[] displayContent;

        private PermissionDisplay(Context context, String[] displayContent) {
            super(context, R.layout.item_permission, displayContent);
            this.displayContent = displayContent;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            @SuppressLint("ViewHolder")
            View root = LayoutInflater.from(context).inflate(R.layout.item_permission, parent, false);

            TextView per = root.findViewById(R.id.tv_permission);
            per.setText(displayContent[position].trim());
            return root;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return displayContent.length;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }
    }
}
