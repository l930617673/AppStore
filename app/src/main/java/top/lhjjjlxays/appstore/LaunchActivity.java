package top.lhjjjlxays.appstore;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startANewActivity();
            }
        }, 2000);
    }

    private void startANewActivity() {
        Intent intent = new Intent(this, AppStoreActivity.class);
        startActivity(intent);
        finish();
    }
}
