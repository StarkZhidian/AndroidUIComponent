package com.zhidian.itemoperateview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.zhidian.itemoperateview.adapter.MyRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ItemOperateView itemOperateView;
    private RecyclerView recyclerView;
    private List<String> textList = new ArrayList<>();
    private MyRecyclerViewAdapter adapter;

    private void initData() {
        for (int i = 10; i < 100; i++) {
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                builder.append(i);
            }
            textList.add(builder.toString());
        }
        adapter.notifyDataSetChanged();
    }

    private void init() {
        recyclerView = findViewById(R.id.recycler_view);
        adapter = new MyRecyclerViewAdapter(textList);
        adapter.setItemLongClickListener(new MyRecyclerViewAdapter.ItemLongClickListener() {
            @Override
            public boolean onItemClick(View view, MotionEvent event, String data, int index) {
                if (itemOperateView != null) {
                    itemOperateView.show(view, (int) event.getY());
                }
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        itemOperateView = new ItemOperateView(this, R.layout.item_operate_layout);
        LinearLayout upView = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.item_operate_layout, null);
        upView.getChildAt(0).setVisibility(View.GONE);
        LinearLayout downView = (LinearLayout) LayoutInflater.from(this).inflate(
                R.layout.item_operate_layout, null);
        downView.getChildAt(downView.getChildCount() - 1).setVisibility(View.GONE);
        itemOperateView.setContentViewAsUp(upView);
        itemOperateView.setContentViewAsDown(downView);
        initData();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    protected void onDestroy() {
        if (itemOperateView.isShowing()) {
            itemOperateView.dismiss();
        }
        super.onDestroy();
    }
}

