package com.zhidian.slideoperateitemview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.zhidian.slideoperateitemview.recycler_view.RecyclerViewAdapter;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        ArrayList<String> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataList.add("项目" + (i+1));
        }
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(dataList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
    }
}
