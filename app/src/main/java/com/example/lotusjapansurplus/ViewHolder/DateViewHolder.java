package com.example.lotusjapansurplus.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.INTERFACE.ItemClickListener;
import com.example.lotusjapansurplus.R;

public class DateViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    public TextView dateTextView;
    public ItemClickListener listener;
    public DateViewHolder(@NonNull View itemView) {
        super(itemView);
        dateTextView = itemView.findViewById(R.id.dateTextView); // Replace with the ID of your TextView
    }

    @Override
    public void onClick(View view) {

        listener.onClick(view, getAdapterPosition(), false);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
}