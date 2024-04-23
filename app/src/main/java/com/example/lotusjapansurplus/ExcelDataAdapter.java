package com.example.lotusjapansurplus;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExcelDataAdapter extends RecyclerView.Adapter<ExcelDataAdapter.ExcelDataViewHolder> {
    private List<String> data;

    public ExcelDataAdapter(List<String> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ExcelDataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_excel_data, parent, false);
        return new ExcelDataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExcelDataViewHolder holder, int position) {
        String rowData = data.get(position);
        holder.bind(rowData);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ExcelDataViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ExcelDataViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
        }

        public void bind(String rowData) {
            textView.setText(rowData);
        }
    }
}
