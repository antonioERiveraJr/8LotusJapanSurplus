    package com.example.lotusjapansurplus.ViewHolder;

    import android.view.View;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.example.lotusjapansurplus.INTERFACE.ItemClickListener;
    import com.example.lotusjapansurplus.Method.BuyerItem;
    import com.example.lotusjapansurplus.R;
    public class BuyerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView fbTextView;
        private TextView nameTextView;
        private TextView addressTextView;
        private TextView methodTextView;
        private BuyerItem buyerItem;
        private ItemClickListener listener;

        public BuyerViewHolder(@NonNull View itemView) {
            super(itemView);
            fbTextView = itemView.findViewById(R.id.fbTextView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            addressTextView = itemView.findViewById(R.id.addressTextView);
            methodTextView = itemView.findViewById(R.id.methodTextView);

            // Set the click listener for the item view
            itemView.setOnClickListener(this);
        }

        public void setBuyerData(BuyerItem buyerItem) {
            this.buyerItem = buyerItem;

            fbTextView.setText(buyerItem.getFb());
            nameTextView.setText(buyerItem.getName());
          //  addressTextView.setText(buyerItem.getAddress());
            methodTextView.setText(buyerItem.getMethod());
        }

        public BuyerItem getBuyerItem() {
            return buyerItem;
        }

        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onClick(view, getAdapterPosition(), false);
            }
        }

        public void setItemClickListener(ItemClickListener listener) {
            this.listener = listener;
        }
    }