package com.example.upc.flexiblelayout;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

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
        final LinearLayout midLy = (LinearLayout) findViewById(R.id.toggle_mid);
        final LinearLayout bottomLy = (LinearLayout) findViewById(R.id.toggle_bottom);

        flexibleLayout = (FlexibleLayout) findViewById(R.id.toggle);

        flexibleLayout.post(new Runnable() {
            @Override
            public void run() {
                flexibleLayout.setToClose();
                flexibleLayout.setAllowOpen(true);
                flexibleLayout.setAllowExtend(true);
                flexibleLayout.setCloseOffset(topLy.getHeight());
                flexibleLayout.setOpenOffset(topLy.getHeight() + midLy.getHeight());
                flexibleLayout.setExtendOffset(topLy.getHeight() + midLy.getHeight() + bottomLy.getHeight());
                flexibleLayout.getBackground().setAlpha(0);// 设置为透明色
                flexibleLayout.setOnScrollListener(mOnScrollListener);
            }
        });
    }

    private FlexibleLayout.OnScrollListener mOnScrollListener = new FlexibleLayout.OnScrollListener() {
        @Override
        public void onScrollProgressChanged(float currProgress) {

        }

        @Override
        public void onScrollFinished(FlexibleLayout.Status currStatus) {

        }

        @Override
        public void onChildScroll(int top) {

        }
    };
}
