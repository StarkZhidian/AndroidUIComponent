package com.zhidian.itemoperateview;

import android.support.constraint.ConstraintLayout;
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
//                    itemOperateView.show(view);
                }
                return false;
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        itemOperateView = new ItemOperateView(this, R.layout.item_operate_layout);
        ConstraintLayout upView = (ConstraintLayout) LayoutInflater.from(this).inflate(
                R.layout.item_operate_layout, null);
        upView.findViewById(R.id.triangle).setVisibility(View.GONE);
        ConstraintLayout downView = (ConstraintLayout) LayoutInflater.from(this).inflate(
                R.layout.item_operate_layout, null);
        downView.findViewById(R.id.triangle_handstand).setVisibility(View.GONE);
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
        // 防止窗体泄漏
        if (itemOperateView.isShowing()) {
            itemOperateView.dismiss();
        }
        super.onDestroy();
    }
}

