package com.quan.administrator.myapp1;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

/**
 * Created by Administrator on 2016/9/1 0001 上午 7:17.
 */
public class SecondActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        Bundle bundle=getIntent().getBundleExtra("var");
        int result=bundle.getInt("sum");
        TextView tv= (TextView) findViewById(R.id.tv_data);
        tv.setText("result="+result);
    }
}
