package com.example.lotusjapansurplus.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.Method.LiveItem;
import com.example.lotusjapansurplus.Method.TransactionItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.ViewHolder.LiveViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TransactionDetailsActivity extends AppCompatActivity {

    private RecyclerView liveRecyclerView;
    private DatabaseReference liveDateReference;
    private FirebaseRecyclerAdapter<LiveItem, LiveViewHolder> adapter;
    private ArrayList<Double> prices = new ArrayList<>();
    private ArrayAdapter<String> itemAdapter;
    private ArrayAdapter<String> buyerAdapter;

    private List<String> allItems = new ArrayList<>();
    private List<String> buyerNames = new ArrayList<>();
    private AutoCompleteTextView itemNameEditText, searchEditText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);



        liveRecyclerView = findViewById(R.id.liveDateRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        liveRecyclerView.setLayoutManager(linearLayoutManager);

        liveDateReference = FirebaseDatabase.getInstance().getReference("transactions");
        Button deliveredButton = findViewById(R.id.DeliveredButton);
        deliveredButton.setVisibility(View.GONE);
        deliveredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToDelivered();
                Intent intent = new Intent(TransactionDetailsActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });
        Button totalButton = findViewById(R.id.totalButton);
        totalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateTotal();
            }
        });
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionDetailsActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });




    }

    @Override
    public void onStart() {
        super.onStart();


        String dates = getIntent().getStringExtra("date");

        String selectedBuyer = getIntent().getStringExtra("buyer");

        Toast.makeText(this, selectedBuyer, Toast.LENGTH_SHORT).show();
        if(selectedBuyer.equals("All")){
            generateAll();
        }else {

            DatabaseReference ProductsRef = FirebaseDatabase.getInstance().getReference("transactions");

            FirebaseRecyclerOptions<LiveItem> options = new FirebaseRecyclerOptions.Builder<LiveItem>()
                    .setQuery(ProductsRef.orderByChild("id").equalTo(dates + selectedBuyer), LiveItem.class)
                    .build();

            adapter = new FirebaseRecyclerAdapter<LiveItem, LiveViewHolder>(options) {
                @Override
                protected void onBindViewHolder(@NonNull LiveViewHolder liveViewHolder, int i, @NonNull LiveItem liveItem) {

                    liveViewHolder.itemView.setOnClickListener(v -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(TransactionDetailsActivity.this);
                        builder.setTitle("Choose Action");
                        builder.setMessage("Do you want to View or Edit?");

                        // Option for View
                        builder.setPositiveButton("View", (dialog, which) -> {
                            showViewDialog(liveItem);
                            dialog.dismiss(); // Dismiss the dialog after making a selection
                        });

                        // Option for Edit
                        builder.setNegativeButton("Edit", (dialog, which) -> {
                            showEditDialog(liveItem);
                            dialog.dismiss(); // Dismiss the dialog after making a selection
                        });

                        builder.setNeutralButton("Delete", (dialog, which) -> {
                            String ids = liveItem.getIdItem();
                            String buyers = liveItem.getBuyer();
                            showDeleteConfirmationDialog(ids,buyers);
                            dialog.dismiss(); // Dismiss the dialog after making a selection
                        });
                        builder.show();
                    });
                    liveViewHolder.textViewBuyer.setText("Buyer: "+liveItem.getBuyer());
                    liveViewHolder.textViewPrice.setText("Price: "+liveItem.getPrice());
                    liveViewHolder.textViewCode.setText("Code: "+liveItem.getName());

                    double price = Double.parseDouble(liveItem.getPrice());
                    prices.add(price);
                }

                @NonNull
                @Override
                public LiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_date_layout, parent, false);
                    return new LiveViewHolder(view);
                }
            };
            liveRecyclerView.setAdapter(adapter);
            adapter.startListening();
        }
    }
    private void showDeleteConfirmationDialog(String ids,String buyers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to delete this item?");
        builder.setPositiveButton("Delete", (dialog, which) -> {
            deleteTransaction(ids,buyers);
            dialog.dismiss();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
    private void deleteTransaction(String ids,String buyers) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(ids);
        transactionsRef.removeValue()
                .addOnSuccessListener(aVoid ->{ Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT).show();

                    // Assuming transactionsRef is a DatabaseReference pointing to the correct location in your database
                    transactionsRef.child("price").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // dataSnapshot contains the data at the "price" location
                                if (dataSnapshot.exists()) {
                                    // Get the value as a double (assuming it's stored as a double in the database)
                                    double price = dataSnapshot.getValue(Double.class);
                                    DatabaseReference updateBuyerBalance = FirebaseDatabase.getInstance().getReference( "Buyer");
                                    updateBuyerBalance.orderByChild("fb").equalTo(buyers).addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot buyerSnapshot : snapshot.getChildren()) {
                                                // Assuming "balance" is a field in your Buyer node
                                                double currentBalance = buyerSnapshot.child("balance").getValue(Double.class);

                                                // Update the balance by subtracting the price
                                                double newBalance = currentBalance - price;

                                                // Set the new balance in the database
                                                buyerSnapshot.getRef().child("balance").setValue(newBalance);

                                                // Now you can use the 'newBalance' variable as needed
                                                // For example, you can display it in a TextView or use it in calculations
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                    // Now you can use the 'price' variable as needed
                                    // For example, you can display it in a TextView or use it in calculations
                                } else {
                                // Handle the case where the "price" node does not exist
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle errors, if any
                        }
                    });


                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void updateStatusToDelivered() {
        String selectedBuyer = getIntent().getStringExtra("buyer");
        String dates = getIntent().getStringExtra("date");

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");

        transactionsRef.orderByChild("id").equalTo(dates + selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // Update the status to "delivered" for each matching record
                    snapshot.getRef().child("status").setValue("delivered");

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors
            }
        });
    }
    private void generateAll() {
        String dates = getIntent().getStringExtra("date");



        String selectedBuyer = getIntent().getStringExtra("buyer");
        DatabaseReference ProductsRef = FirebaseDatabase.getInstance().getReference("transactions");

        FirebaseRecyclerOptions<LiveItem> options = new FirebaseRecyclerOptions.Builder<LiveItem>()
                .setQuery(ProductsRef.orderByChild("date").equalTo(dates), LiveItem.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<LiveItem, LiveViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LiveViewHolder liveViewHolder, int i, @NonNull LiveItem liveItem) {

                liveViewHolder.itemView.setOnClickListener(v -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(TransactionDetailsActivity.this);
                    builder.setTitle("Choose Action");
                    builder.setMessage("Do you want to View or Edit?");

                    // Option for View
                    builder.setPositiveButton("View", (dialog, which) -> {
                        showViewDialog(liveItem);
                        dialog.dismiss(); // Dismiss the dialog after making a selection
                    });

                    // Option for Edit
                    builder.setNegativeButton("Edit", (dialog, which) -> {
                        showEditDialog(liveItem);
                        dialog.dismiss(); // Dismiss the dialog after making a selection
                    });

                    builder.show();
                });
                liveViewHolder.textViewBuyer.setText("Buyer: "+liveItem.getBuyer());
                liveViewHolder.textViewPrice.setText("Price: "+liveItem.getPrice());
                liveViewHolder.textViewCode.setText("Code: "+liveItem.getName());

                double price = Double.parseDouble(liveItem.getPrice());
                prices.add(price);
            }

            @NonNull
            @Override
            public LiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_date_layout, parent, false);
                return new LiveViewHolder(view);
            }
        };
        liveRecyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    private void calculateTotal() {
        double total = 0.0;
        for (Double price : prices) {
            total += price;
        }
        displayTotal(total);
    }

    private void displayTotal(double total) {
        TextView totalTextView = new TextView(this);
        totalTextView.setText("Total: " + total);
        setContentView(totalTextView);
    }
    private void showViewDialog(LiveItem liveItem) {
        View dialogView = getLayoutInflater().inflate(R.layout.view_dialog, null);

        TextView textId = dialogView.findViewById(R.id.textViewId);
        TextView textCode = dialogView.findViewById(R.id.textViewCode);
        TextView textName = dialogView.findViewById(R.id.textViewName);
        TextView textPrice = dialogView.findViewById(R.id.textViewPrice);
        TextView textBuyer = dialogView.findViewById(R.id.textViewBuyer);

        textId.setText(liveItem.getId());
        textCode.setText(liveItem.getCode());
        textName.setText(liveItem.getName());
        textPrice.setText(liveItem.getPrice());
        textBuyer.setText(liveItem.getBuyer());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("View Details");
        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showEditDialog(LiveItem liveItem) {
        //

        //

        View dialogView = getLayoutInflater().inflate(R.layout.edit_dialog, null);
        AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.textViewBuyer);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, buyerNames);
        autoCompleteTextView.setAdapter(adapter);
        autoCompleteTextView.setThreshold(1);

        TextView textNote = dialogView.findViewById(R.id.textViewNote);
        TextView textId = dialogView.findViewById(R.id.textViewId);
        TextView textName = dialogView.findViewById(R.id.textViewName);
        TextView textPrice = dialogView.findViewById(R.id.textViewPrice);

        textId.setText(liveItem.getId());
        textName.setText(liveItem.getName());
        textPrice.setText(liveItem.getPrice());
        autoCompleteTextView.setText(liveItem.getBuyer());
        textNote.setText(liveItem.getNote());

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Edit Details");
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String updatedBuyer = autoCompleteTextView.getText().toString();
                String updatedName = textName.getText().toString();
                String updatedPrice = textPrice.getText().toString();
                String updatedNote = textNote.getText().toString();

                String ids = liveItem.getIdItem();
                showConfirmationDialog(liveItem, updatedBuyer, updatedName, updatedPrice, ids,updatedNote);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void fetchBuyersFromFirebase() {
        DatabaseReference buyersRef = FirebaseDatabase.getInstance().getReference("Buyer");

        buyersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot buyerSnapshot : dataSnapshot.getChildren()) {
                    String buyerName = buyerSnapshot.child("fb").getValue(String.class);
                    if (buyerName != null) {
                        buyerNames.add(buyerName);
                    }
                }
                populateBuyersInDropdown();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error
            }
        });
    }

    private void populateBuyersInDropdown() {
        buyerAdapter.addAll(buyerNames);
        buyerAdapter.notifyDataSetChanged();
    }


    private void fetchItemsFromFirebase() {
        DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference("Items");
        itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    String itemName = itemSnapshot.getValue(String.class);
                    if (itemName != null) {
                        allItems.add(itemName);
                    }
                }

                itemAdapter.notifyDataSetChanged(); // Notify adapter of the data change
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error
            }
        });
    }


    private void filterItems(String searchQuery) {
        List<String> filteredItems = new ArrayList<>();
        for (String item : allItems) {
            if (item.toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredItems.add(item);
            }
        }
        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filteredItems);
        itemNameEditText.setAdapter(itemAdapter);
    }

    private void filterBuyers(String searchQuery) {
        if (searchQuery.isEmpty()) {
            buyerAdapter.clear();
            buyerAdapter.addAll(buyerNames);
            buyerAdapter.notifyDataSetChanged();
        } else {
            DatabaseReference buyersRef = FirebaseDatabase.getInstance().getReference("Buyer");
            buyersRef.orderByChild("fb")
                    .startAt(searchQuery.toLowerCase())
                    .endAt(searchQuery.toLowerCase() + "\uf8ff")
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<String> filteredBuyers = new ArrayList<>();
                            for (DataSnapshot buyerSnapshot : dataSnapshot.getChildren()) {
                                String buyerName = buyerSnapshot.child("fb").getValue(String.class);
                                if (buyerName != null && buyerName.toLowerCase().contains(searchQuery.toLowerCase())) {
                                    filteredBuyers.add(buyerName);
                                }
                            }
                            buyerAdapter.clear();
                            buyerAdapter.addAll(filteredBuyers);
                            buyerAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // Handle the error
                        }
                    });
        }
    }
    private void showConfirmationDialog(LiveItem liveItem,String updatedCode, String updatedName, String updatedPrice, String ids,String updatedNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to save the changes?\n\nCode: " + updatedCode + "\nName: " + updatedName + "\nPrice: " + updatedPrice+ "\nNote: "+updatedNote);
        builder.setPositiveButton("Save", (dialog, which) -> {
            DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(ids);

            // Prepare the data to be updated
            Map<String, Object> updates = new HashMap<>();
            updates.put("buyer", updatedCode);
            updates.put("code", updatedName);
            updates.put("price", updatedPrice);
            updates.put("note",updatedNote);

            // Update the values in the database
            transactionsRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        dialog.dismiss();
                        showUpdatedDetailsDialog(updatedCode, updatedName, updatedPrice,updatedNote);
                     //   showFinalConfirmationDialog(updatedCode, updatedName, updatedPrice, ids);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showUpdatedDetailsDialog(String updatedCode, String updatedName, String updatedPrice,String note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Updated Details");
        builder.setMessage("Updated Code: " + updatedCode + "\nUpdated Name: " + updatedName + "\nUpdated Price: " + updatedPrice+"\nUpdated Note: "+note);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showFinalConfirmationDialog(String updatedCode, String updatedName, String updatedPrice, String ids) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Final Confirmation");
        builder.setMessage("Do you confirm the changes?\n\nBuyer: " + updatedCode + "\nCode: " + updatedName + "\nPrice: " + updatedPrice);
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // Perform database update operations with the updated values
            // e.g., updateValuesInDatabase(updatedCode, updatedName, updatedPrice);

            Toast.makeText(this, "Final changes saved!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


}
