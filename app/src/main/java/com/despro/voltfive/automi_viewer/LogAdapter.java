package com.despro.voltfive.automi_viewer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder>{

    private List<String> logList;

    public LogAdapter(List<String> logList) {
        this.logList = logList;
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_view, parent, false);
        return new LogViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
        holder.log.setText(logList.get(position));
    }

    @Override
    public int getItemCount() {
        return logList.size();
    }

    public class LogViewHolder extends RecyclerView.ViewHolder {
        public TextView log;

        public LogViewHolder(View view) {
            super(view);
            log = (TextView) view.findViewById((R.id.logText));
        }
    }
}
