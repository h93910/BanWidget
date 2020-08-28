package com.example.banwidget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Created by Ban on 2016/7/26.
 */
public class DateRecyclerAdapter extends CommonBaseRecyclerAdapter<String> {
    private View.OnCreateContextMenuListener listener;

    public DateRecyclerAdapter(Context context, List<String> datas, View.OnCreateContextMenuListener listener) {
        super(context, datas, R.layout.item_date);
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolderAbs(ViewGroup parent) {
        return new ViewHold(LayoutInflater.from(context).inflate(resourceId, parent, false));
    }

    @Override
    public void onBindViewHolderAbs(RecyclerView.ViewHolder holder, int position) {
        ViewHold hold = (ViewHold) holder;
        hold.name.setText(datas.get(position));
    }

    class ViewHold extends CommonRecyclerViewHolder {
        private TextView name;

        public ViewHold(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.item);
            itemView.setOnCreateContextMenuListener(listener);
        }
    }
}