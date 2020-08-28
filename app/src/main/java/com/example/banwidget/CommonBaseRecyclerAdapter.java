package com.example.banwidget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * 通用的RecyclerView适配器
 *
 * @param <T>
 * @author Ban
 */
public abstract class CommonBaseRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected List<T> datas;
    protected Context context;
    protected int resourceId;
    protected OnItemClickListener mItemClickListener;
    protected OnItemLongClickListener mItemLongClickListener;

    public CommonBaseRecyclerAdapter(Context context, List<T> datas, int resourceId) {
        this.datas = datas;
        this.context = context;
        this.resourceId = resourceId;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder hold = onCreateViewHolderAbs(parent);
        return hold;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        onBindViewHolderAbs(holder, position);
    }


    //多写的抽象方法，怕忘记重写重要方法
    public abstract RecyclerView.ViewHolder onCreateViewHolderAbs(ViewGroup parent);

    //多写的抽象方法，怕忘记重写重要方法
    public abstract void onBindViewHolderAbs(RecyclerView.ViewHolder holder, int position);

    /**
     * 单击侦听器
     */
    public interface OnItemClickListener {
        void OnItemClick(View v, int position);
    }

    /**
     * 长按侦听器
     */
    public interface OnItemLongClickListener {
        boolean OnItemLongClick(View v, int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        this.mItemLongClickListener = itemLongClickListener;
    }

    public abstract class CommonRecyclerViewHolder extends RecyclerView.ViewHolder {
        public CommonRecyclerViewHolder(View itemView) {
            super(itemView);
            if (mItemClickListener != null) {
                itemView.setOnClickListener(v -> mItemClickListener.OnItemClick(v, getLayoutPosition()));
            }

            if (mItemLongClickListener != null) {
                itemView.setOnLongClickListener(v -> mItemLongClickListener.OnItemLongClick(v, getLayoutPosition())
                );
            }
        }
    }
}
