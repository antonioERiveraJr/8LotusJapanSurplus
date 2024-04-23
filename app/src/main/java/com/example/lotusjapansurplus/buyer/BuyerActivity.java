package com.example.lotusjapansurplus.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.Method.BuyerItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.ViewHolder.BuyerViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class BuyerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private FirebaseRecyclerAdapter<BuyerItem, BuyerViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buyer");

        // Initialize adapter with the 'all' query
        all();
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the "Back" button is clicked, navigate back to the previous activity (e.g., MainActivity)
                Intent intent = new Intent(BuyerActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Button addBuyerButton = findViewById(R.id.addBuyerButton);
        addBuyerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // When the "Add Buyer" button is clicked, navigate to AddBuyerActivity
                Intent intent = new Intent(BuyerActivity.this, AddBuyerActivity.class);
                startActivity(intent);
            }
        });

        Button sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSortMenu(v); // Show the sorting options menu
            }
        });
    }

    private void showSortMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.sort_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.menu_sort_all) {
                    // Handle sorting by "All"
                    all();
                } else if (item.getItemId() == R.id.menu_sort_balance) {
                    // Handle sorting by "Balance"
                    retrieveBuyersWithPositiveBalance();
                } else if (item.getItemId() == R.id.menu_sort_unidentified) {
                    // Handle sorting by "Unidentified"
                    retrieveUnidentifiedBuyers();
                }else if (item.getItemId() == R.id.menu_sort_newBuyer){
                    retrieveNewBuyer();
                }
                return true;
            }


        });
        popup.show();
    }

    // Query all buyers
    private void all() {
        Query query = databaseReference;
        setAdapterWithQuery(query);
    }

    private void retrieveUnidentifiedBuyers() {
        Query query = databaseReference.orderByChild("status").equalTo("unidentified");
        //   Query query = databaseReference;
        FirebaseRecyclerOptions<BuyerItem> options =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(query, BuyerItem.class)
                        .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<BuyerItem, BuyerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BuyerViewHolder holder, int position, @NonNull BuyerItem model) {
                holder.setBuyerData(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BuyerItem clickedBuyer = model;
                        if (clickedBuyer != null) {
                            Intent intent = new Intent(view.getContext(), BuyerDetailsActivity.class);
                            intent.putExtra("fb", clickedBuyer.getFb());
                            intent.putExtra("name", clickedBuyer.getName());
                            intent.putExtra("method", clickedBuyer.getMethod());
                            intent.putExtra("address", clickedBuyer.getAddress());
                            intent.putExtra("balance", clickedBuyer.getBalance());
                            intent.putExtra("status", clickedBuyer.getStatus());
                            intent.putExtra("id", clickedBuyer.getId());
                            intent.putExtra("contact", clickedBuyer.getContact());
                            intent.putExtra("note", clickedBuyer.getNote());
                            view.getContext().startActivity(intent);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public BuyerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buyer, parent, false);
                return new BuyerViewHolder(view);
            }

        };

        // Sorting the adapter items alphabetically by name
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        // Sorting items alphabetically by name
        Query sortedQuery = query; // No need for another orderByChild
        FirebaseRecyclerOptions<BuyerItem> sortedOptions =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(sortedQuery, BuyerItem.class)
                        .build();

        adapter.updateOptions(sortedOptions);
    }
    private void retrieveNewBuyer() {
        Query query = databaseReference.orderByChild("name").equalTo("null");
        //   Query query = databaseReference;
        FirebaseRecyclerOptions<BuyerItem> options =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(query, BuyerItem.class)
                        .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<BuyerItem, BuyerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BuyerViewHolder holder, int position, @NonNull BuyerItem model) {
                holder.setBuyerData(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BuyerItem clickedBuyer = model;
                        if (clickedBuyer != null) {
                            Intent intent = new Intent(view.getContext(), BuyerDetailsActivity.class);
                            intent.putExtra("fb", clickedBuyer.getFb());
                            intent.putExtra("name", clickedBuyer.getName());
                            intent.putExtra("method", clickedBuyer.getMethod());
                            intent.putExtra("address", clickedBuyer.getAddress());
                            intent.putExtra("balance", clickedBuyer.getBalance());
                            intent.putExtra("status", clickedBuyer.getStatus());
                            intent.putExtra("id", clickedBuyer.getId());
                            intent.putExtra("contact", clickedBuyer.getContact());
                            intent.putExtra("note", clickedBuyer.getNote());
                            view.getContext().startActivity(intent);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public BuyerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buyer, parent, false);
                return new BuyerViewHolder(view);
            }

        };

        // Sorting the adapter items alphabetically by name
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        // Sorting items alphabetically by name
        Query sortedQuery = query; // No need for another orderByChild
        FirebaseRecyclerOptions<BuyerItem> sortedOptions =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(sortedQuery, BuyerItem.class)
                        .build();

        adapter.updateOptions(sortedOptions);
    }
    // Retrieve buyers with balance > 0
    private void retrieveBuyersWithPositiveBalance() {
        Query query = databaseReference.orderByChild("balance").startAt(1);
     //   Query query = databaseReference;
        FirebaseRecyclerOptions<BuyerItem> options =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(query, BuyerItem.class)
                        .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<BuyerItem, BuyerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BuyerViewHolder holder, int position, @NonNull BuyerItem model) {
                holder.setBuyerData(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BuyerItem clickedBuyer = model;
                        if (clickedBuyer != null) {
                            Intent intent = new Intent(view.getContext(), BuyerDetailsActivity.class);
                            intent.putExtra("fb", clickedBuyer.getFb());
                            intent.putExtra("name", clickedBuyer.getName());
                            intent.putExtra("method", clickedBuyer.getMethod());
                            intent.putExtra("address", clickedBuyer.getAddress());
                            intent.putExtra("balance", clickedBuyer.getBalance());
                            intent.putExtra("status", clickedBuyer.getStatus());
                            intent.putExtra("id", clickedBuyer.getId());
                            intent.putExtra("contact", clickedBuyer.getContact());
                            intent.putExtra("note", clickedBuyer.getNote());
                            view.getContext().startActivity(intent);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public BuyerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buyer, parent, false);
                return new BuyerViewHolder(view);
            }

        };

        // Sorting the adapter items alphabetically by name
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        // Sorting items alphabetically by name
        Query sortedQuery = query; // No need for another orderByChild
        FirebaseRecyclerOptions<BuyerItem> sortedOptions =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(sortedQuery, BuyerItem.class)
                        .build();

        adapter.updateOptions(sortedOptions);
    }

    private void setAdapterWithQuery(Query query) {
        FirebaseRecyclerOptions<BuyerItem> options =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(query, BuyerItem.class)
                        .build();

        if (adapter != null) {
            adapter.stopListening();
        }

        adapter = new FirebaseRecyclerAdapter<BuyerItem, BuyerViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull BuyerViewHolder holder, int position, @NonNull BuyerItem model) {
                holder.setBuyerData(model);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BuyerItem clickedBuyer = model;
                        if (clickedBuyer != null) {
                            Intent intent = new Intent(view.getContext(), BuyerDetailsActivity.class);
                            intent.putExtra("fb", clickedBuyer.getFb());
                            intent.putExtra("name", clickedBuyer.getName());
                            intent.putExtra("method", clickedBuyer.getMethod());
                            intent.putExtra("address", clickedBuyer.getAddress());
                            intent.putExtra("balance", clickedBuyer.getBalance());
                            intent.putExtra("status", clickedBuyer.getStatus());
                            intent.putExtra("id", clickedBuyer.getId());
                            intent.putExtra("contact", clickedBuyer.getContact());
                            intent.putExtra("note", clickedBuyer.getNote());
                            view.getContext().startActivity(intent);
                        }
                    }
                });
            }

            @NonNull
            @Override
            public BuyerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_buyer, parent, false);
                return new BuyerViewHolder(view);
            }

        };

        // Sorting the adapter items alphabetically by name
        adapter.startListening();
        recyclerView.setAdapter(adapter);

        // Sorting items alphabetically by name
        Query sortedQuery = query.orderByChild("fb");
        FirebaseRecyclerOptions<BuyerItem> sortedOptions =
                new FirebaseRecyclerOptions.Builder<BuyerItem>()
                        .setQuery(sortedQuery, BuyerItem.class)
                        .build();

        adapter.updateOptions(sortedOptions);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}
