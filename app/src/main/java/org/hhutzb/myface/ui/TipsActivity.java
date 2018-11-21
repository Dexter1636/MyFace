package org.hhutzb.myface.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import org.hhutzb.myface.R;

public class TipsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tips);

        ImageButton imgbtn = findViewById(R.id.imgbtn_back);
        imgbtn.setOnClickListener(v -> onBackPressed());
    }
}
