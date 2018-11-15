package com.zhidian.itemoperateview.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zhidian.itemoperateview.custom_view.MyConstraintLayout;
import com.zhidian.itemoperateview.R;

import java.util.List;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewHolder> {
    public interface ItemLongClickListener {
        boolean onItemClick(View view, MotionEvent event, String data, int index);
    }

    private static final String TAG = MyRecyclerViewAdapter.class.getSimpleName();
    private static final int[] COLORS = new int[]{
            Color.WHITE, Color.RED, Color.BLUE, Color.YELLOW, Color.CYAN, Color.MAGENTA};
    private List<String> data;
    private ItemLongClickListener itemLongClickListener;

    public MyRecyclerViewAdapter(@NonNull List<String> data) {
        this.data = data;
    }

    public void setItemLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.itemLongClickListener = itemLongClickListener;
    }

    private static int getRandomColor(int index) {
        if (index < 0) {
            return Color.WHITE;
        }
        return COLORS[index % COLORS.length];
    }

    @NonNull
    @Override
    public MyRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i < 0 || i >= data.size()) {
            return null;
        }
        return new MyRecyclerViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.recycler_view_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyRecyclerViewHolder viewHolder, final int i) {
        if (i < 0 || i >= data.size() || viewHolder == null) {
            return;
        }
        Log.d(TAG, "onBindViewHolder, position: " + i);
        final MyConstraintLayout mainLayout = (MyConstraintLayout) viewHolder.itemView;
        mainLayout.setTouchEventListener(new MyConstraintLayout.TouchEventListener() {
            @Override
            public boolean onTouchEvent(View view,
                                        MyConstraintLayout.TouchEventType type, MotionEvent event) {
                if (itemLongClickListener != null && type == MyConstraintLayout.TouchEventType.ON_LONG_PRESS) {
                    return itemLongClickListener.onItemClick(view, event, data.get(i), i);
                }
                return false;
            }
        });
        TextView textView = mainLayout.findViewById(R.id.right_text_view);
        textView.setBackgroundColor(getRandomColor(i));
        textView.setText(data.get(i));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
