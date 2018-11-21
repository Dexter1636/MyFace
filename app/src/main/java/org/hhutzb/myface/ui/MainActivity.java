package org.hhutzb.myface.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.hhutzb.myface.R;
import org.hhutzb.myface.utilities.WindowUtils;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // 沉浸式状态栏
        WindowUtils.setStatusBarTranslucent(this);

        Button btnSettings = findViewById(R.id.btn_welcome);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
        });
    }

}
