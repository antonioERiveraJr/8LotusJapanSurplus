package com.example.lotusjapansurplus.transactions;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.Method.BuyerItem;
import com.example.lotusjapansurplus.Method.TransactionItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.ViewHolder.DateViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;


public class TransactionActivity extends AppCompatActivity {

    private RecyclerView dateRecyclerView;
    private DatabaseReference liveDateReference;
    private FirebaseRecyclerAdapter<TransactionItem, DateViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        dateRecyclerView = findViewById(R.id.dateRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TransactionActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        dateRecyclerView.setLayoutManager(new LinearLayoutManager(TransactionActivity.this));

        liveDateReference = FirebaseDatabase.getInstance().getReference("LiveDate");

        EditText searchEditText = findViewById(R.id.searchEditText);

        Button search = findViewById(R.id.searchButton);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchInput = searchEditText.getText().toString();
                searchItems(searchInput);

            }
        });


    }

    private void searchItems(String searchInput) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("LiveDate");

        FirebaseRecyclerOptions<TransactionItem> options;

        if (searchInput != null && !searchInput.isEmpty()) {
            Query query = productsRef.orderByChild("date").startAt(searchInput);

            options = new FirebaseRecyclerOptions.Builder<TransactionItem>()
                    .setQuery(query, TransactionItem.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<TransactionItem, DateViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int i, @NonNull TransactionItem transactionItem) {
                    dateViewHolder.dateTextView.setText(transactionItem.getDate());

                    dateViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TransactionActivity.this, TransactionListOfBuyers.class);
                            intent.putExtra("date", transactionItem.getDate());
                            Toast.makeText(TransactionActivity.this, transactionItem.getDate(), Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }
                    });
                }

                @NonNull
                @Override
                public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
                    DateViewHolder holder = new DateViewHolder(view);
                    return holder;
                }
            };

            dateRecyclerView.setAdapter(adapter);
            adapter.startListening();
        } else {
            // If search input is empty, display all items
            Query query = productsRef.orderByChild("date");

            options = new FirebaseRecyclerOptions.Builder<TransactionItem>()
                    .setQuery(query, TransactionItem.class)
                    .build();
            adapter = new FirebaseRecyclerAdapter<TransactionItem, DateViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int i, @NonNull TransactionItem transactionItem) {
                    dateViewHolder.dateTextView.setText(transactionItem.getDate());

                    dateViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(TransactionActivity.this, TransactionListOfBuyers.class);
                            intent.putExtra("date", transactionItem.getDate());
                            Toast.makeText(TransactionActivity.this, transactionItem.getDate(), Toast.LENGTH_SHORT).show();
                            startActivity(intent);
                        }
                    });
                }

                @NonNull
                @Override
                public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
                    DateViewHolder holder = new DateViewHolder(view);
                    return holder;
                }
            };

            dateRecyclerView.setAdapter(adapter);
            adapter.startListening();
        }

        //adapter.updateOptions(options);
    }
    @Override
    public void onStart() {
        super.onStart();

        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference().child("LiveDate");

        FirebaseRecyclerOptions<TransactionItem> options = new FirebaseRecyclerOptions.Builder<TransactionItem>()
                .setQuery(productsRef.orderByChild("date").startAt(""), TransactionItem.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<TransactionItem, DateViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull DateViewHolder dateViewHolder, int i, @NonNull TransactionItem transactionItem) {
                dateViewHolder.dateTextView.setText(transactionItem.getDate());

                dateViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TransactionActivity.this, TransactionListOfBuyers.class);
                        intent.putExtra("date", transactionItem.getDate());
                        Toast.makeText(TransactionActivity.this, transactionItem.getDate(), Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date, parent, false);
                DateViewHolder holder = new DateViewHolder(view);
                return holder;
            }
        };

        dateRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }



}