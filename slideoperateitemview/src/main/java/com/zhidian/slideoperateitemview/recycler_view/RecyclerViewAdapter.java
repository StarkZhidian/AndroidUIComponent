package com.zhidian.slideoperateitemview.recycler_view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhidian.slideoperateitemview.R;
import com.zhidian.slideoperateitemview.SlideOperateItemView;

import java.util.List;

/**
 * Created by Administrator on 2018/1/6.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    List<String> dataList = null;

    public RecyclerViewAdapter(@NonNull List<String> dataList) {
        if (dataList == null) {
            throw new IllegalArgumentException("param dataList can not be null");
        }
        this.dataList = dataList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.recyclerview_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        if (position < 0 && position >= dataList.size()) {
            return ;
        }
        holder.slideOperateItemView.resetScrollPosition();
        final String data = dataList.get(position);
        holder.textView.setText(data);
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(position);
                notifyDataSetChanged();
            }
        });
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(holder.slideOperateItemView.getContext(),
                        data + "被单击", Toast.LENGTH_SHORT).show();
            }
        });
        holder.slideOperateItemView.setSlideListener(new SlideOperateItemView.SlideListener() {
            @Override
            public void finishHideOperateView() {
                Toast.makeText(holder.slideOperateItemView.getContext(),
                        "OperateView 完成隐藏", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void finishShowOperateView() {
                Toast.makeText(holder.slideOperateItemView.getContext(),
                        "OperateView 完成显示", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        SlideOperateItemView slideOperateItemView = null;
        LinearLayout linearLayout = null;
        TextView textView = null;
        Button button = null;

        public MyViewHolder(View itemView) {
            super(itemView);
            slideOperateItemView = itemView.findViewById(R.id.slideOperateItemView);
            linearLayout = itemView.findViewById(R.id.linearLayout);
            this.textView = itemView.findViewById(R.id.itemViewTextView);
            this.button = itemView.findViewById(R.id.itemViewButton);
        }
    }
}
