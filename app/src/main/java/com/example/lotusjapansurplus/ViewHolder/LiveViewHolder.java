package com.example.lotusjapansurplus.ViewHolder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.INTERFACE.ItemClickListener;
import com.example.lotusjapansurplus.R;

public class LiveViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView textViewBuyer,textViewBuyers;
    public TextView textViewCode, textViewCodes;
    public TextView textViewPrice, textViewPrices;

    public ItemClickListener listener;
    public LiveViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewBuyer = itemView.findViewById(R.id.buyerTextView);
        textViewCode = itemView.findViewById(R.id.codeTextView);
        textViewPrice = itemView.findViewById(R.id.priceTextView);
        textViewBuyers = itemView.findViewById(R.id.buyerTextViews);
        textViewCodes = itemView.findViewById(R.id.codeTextViews);
        textViewPrices = itemView.findViewById(R.id.priceTextViews);
    }

    @Override
    public void onClick(View view) {

        listener.onClick(view, getAdapterPosition(), false);
    }

    public void setItemClickListener(ItemClickListener listener){
        this.listener = listener;
    }
}
