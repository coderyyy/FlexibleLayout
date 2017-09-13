package com.example.upc.flexiblelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.flexible.FlexibleLayout;

public class MainActivity extends AppCompatActivity {

    private FlexibleLayout flexibleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    private void initView() {
        final LinearLayout topLy = (LinearLayout) findViewById(R.id.toggle_top);
        final RelativeLayout midLy = (RelativeLayout) findViewById(R.id.toggle_mid);
        final RelativeLayout bottomLy = (RelativeLayout) findViewById(R.id.toggle_bottom);
        final TextView topTv = (TextView) findViewById(R.id.toggle_top_tv);
        final TextView midTv = (TextView) findViewById(R.id.toggle_mid_tv);
        final TextView bottomTv = (TextView) findViewById(R.id.toggle_bottom_tv);

        flexibleLayout = (FlexibleLayout) findViewById(R.id.toggle);

        flexibleLayout.post(new Runnable() {
            @Override
            public void run() {
                flexibleLayout.setAllowOpen(true);
                flexibleLayout.setAllowExtend(true);
                flexibleLayout.setCloseOffset(topLy.getHeight());
                flexibleLayout.setOpenOffset(topLy.getHeight() + midLy.getHeight());
                flexibleLayout.setExtendOffset(topLy.getHeight() + midLy.getHeight() + bottomLy.getHeight());
                flexibleLayout.setToClose();
                flexibleLayout.getBackground().setAlpha(0);// 设置为透明色
                flexibleLayout.setOnScrollListener(mOnScrollListener);
            }
        });

        topTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, topTv.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        midTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, midTv.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });

        bottomTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, bottomTv.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private FlexibleLayout.OnScrollListener mOnScrollListener = new FlexibleLayout.OnScrollListener() {
        @Override
        public void onScrollProgressChanged(float currProgress) {
            System.out.println("progress : " + currProgress);
        }

        @Override
        public void onScrollFinished(FlexibleLayout.Status currStatus) {

        }

        @Override
        public void onChildScroll(int top) {

        }
    };
}
