package com.example.lotusjapansurplus.live;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.Method.LiveItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.ViewHolder.LiveViewHolder;
import com.example.lotusjapansurplus.transactions.TransactionActivity;
import com.example.lotusjapansurplus.transactions.TransactionDetailsActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.formula.functions.Replace;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiveActivity extends AppCompatActivity {

    private EditText priceEditText;
    private Button confirmButton;
    private AutoCompleteTextView itemNameEditText, searchEditText;

    private DatabaseReference transactionReference;
    private DatabaseReference databaseReference; // Reference to the "Buyer" node
    private ArrayAdapter<String> itemAdapter;
    private ArrayAdapter<String> buyerAdapter;
    private List<String> allItems = new ArrayList<>();
    private List<String> buyerNames = new ArrayList<>();
    private FirebaseRecyclerAdapter<LiveItem, LiveViewHolder> adapter;
    private EditText noteEditText;
    private RecyclerView liveRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        //codeEditText = findViewById(R.id.codeEditText);
        priceEditText = findViewById(R.id.priceEditText);
        confirmButton = findViewById(R.id.confirmButton);
        searchEditText = findViewById(R.id.searchEditText);
        itemNameEditText = findViewById(R.id.itemNameEditText);


        noteEditText = findViewById(R.id.noteEditText);
        liveRecyclerView = findViewById(R.id.transactionRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LiveActivity.this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        liveRecyclerView.setLayoutManager(linearLayoutManager);

        itemAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, allItems);
        itemNameEditText.setAdapter(itemAdapter);
        fetchItemsFromFirebase();

        buyerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, buyerNames);
        searchEditText.setAdapter(buyerAdapter);
        fetchBuyersFromFirebase();


        // Set a listener for changes in the search query
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter buyers based on the search query
                filterBuyers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });


        itemNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterItems(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to MainActivity
                Intent intent = new Intent(LiveActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String price = priceEditText.getText().toString();

                String buyer = searchEditText.getText().toString();
                String name = itemNameEditText.getText().toString();
                String note = noteEditText.getText() != null ? noteEditText.getText().toString() : "null";

                if (!buyer.isEmpty()) {
                    DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
                    String searchQuery = buyer.toLowerCase();

                    buyerRef.orderByChild("fb").equalTo(buyer)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        // Searched item found in the "Buyer" reference
                                        showConfirmationDialog(note,price, buyer, name);
                                    } else {
                                        // Searched item not found, save as a new buyer
                                        showCreateNewBuyerDialog(note, price, buyer, name);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Handle the error
                                }
                            });
                }

            }
        });

        transactionReference = FirebaseDatabase.getInstance().getReference("transactions");
        databaseReference = FirebaseDatabase.getInstance().getReference("Buyer"); // Initialize the "Buyer" reference

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
                //populateBuyersInDropdown();
                buyerAdapter.notifyDataSetChanged();
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
        List<String> filteredBuyers = new ArrayList<>();
        for (String buyer : buyerNames) {
            if (buyer.toLowerCase().contains(searchQuery.toLowerCase())) {
                filteredBuyers.add(buyer);
            }
        }
        buyerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filteredBuyers);
       searchEditText.setAdapter(buyerAdapter);
    }






    private void showConfirmationDialog(final String note, final String price, final String buyer, final String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("\nPrice: " + price + "\nBuyer: " + buyer + "\nItem Name: " + name + "\nNote: " + note);
        builder.setPositiveButton("Confirm Transaction", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
                String dateTime = dateFormat.format(calendar.getTime());
                String productRandomKey = dateTime;
                DatabaseReference itemsReference = FirebaseDatabase.getInstance().getReference("Items");

                itemsReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                            String itemName = itemSnapshot.getValue(String.class);
                            if (itemName != null && itemName.equals(name)) {
                                Toast.makeText(LiveActivity.this, "Item " + name + " successfully added", Toast.LENGTH_SHORT).show();

                            } else {
                                // If the item does not exist, update the specific child with productRandomKey
                                HashMap<String, Object> hashMap = new HashMap<>();
                                hashMap.put(productRandomKey, name);
                                itemsReference.child(productRandomKey).updateChildren(hashMap);
                                Intent intent = new Intent(LiveActivity.this, LiveActivity.class);
                                startActivity(intent);
                                Toast.makeText(LiveActivity.this, "Item " + name + " successfully created", Toast.LENGTH_SHORT).show();
                            }
                        }

                        itemAdapter.notifyDataSetChanged(); // Notify adapter of the data change
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Handle the error
                    }
                });

                DatabaseReference buyerReference = FirebaseDatabase.getInstance().getReference("Buyer");


                        // Update the buyer's balance
                        buyerReference.orderByChild("fb").equalTo(buyer).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    DataSnapshot balanceNode = childSnapshot.child("balance");
                                    int currentBalance = balanceNode.getValue(Integer.class);
                                    int priceValue = Integer.valueOf(price);
                                    int newBalance = currentBalance + priceValue;

                                    balanceNode.getRef().setValue(newBalance);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle the error
                            }
                        });

                        // Your existing code...

                // Clear fields and save the transaction to Firebase
                priceEditText.getText().clear();
                searchEditText.getText().clear();
                itemNameEditText.getText().clear();
                noteEditText.getText().clear();
                saveTransactionToFirebase(note, price, buyer, name); // Save without the note
                // showSecondConfirmationDialog(price, buyer, name);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void showSecondConfirmationDialog(final String price, final String buyer,String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to push another transaction?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                priceEditText.getText().clear();
                searchEditText.getText().clear();
                itemNameEditText.getText().clear();
                noteEditText.getText().clear();
                // Show a message or navigate to another screen if needed
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // If the user selects "No," navigate to MainActivity.class
                Intent intent = new Intent(LiveActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String dateTime = dateFormat.format(calendar.getTime());
        String productRandomKey = dateTime;
        Toast.makeText(this, getCurrentDate(), Toast.LENGTH_SHORT).show();
        DatabaseReference ProductsRef = FirebaseDatabase.getInstance().getReference("transactions");

        FirebaseRecyclerOptions<LiveItem> options = new FirebaseRecyclerOptions.Builder<LiveItem>()
                .setQuery(ProductsRef.orderByChild("date").equalTo(getCurrentDate()), LiveItem.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<LiveItem, LiveViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull LiveViewHolder liveViewHolder, int i, @NonNull LiveItem liveItem) {





                liveViewHolder.textViewBuyers.setText(liveItem.getBuyer());
                liveViewHolder.textViewPrices.setText(liveItem.getPrice());
                liveViewHolder.textViewCodes.setText(liveItem.getName());
                liveViewHolder.itemView.setOnClickListener(v -> {
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(LiveActivity.this);
                    builder.setTitle("Choose Action");
                    builder.setMessage("Do you want to Edit?");

                    // Option for View
                    builder.setPositiveButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Dismiss the dialog after making a selection
                    });

                    // Option for Edit
                    builder.setNegativeButton("Edit", (dialog, which) -> {
                        showEditDialog(liveItem);
                        dialog.dismiss(); // Dismiss the dialog after making a selection
                    });

                    builder.show();
                });

            }

            @NonNull
            @Override
            public LiveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_item_layout, parent, false);
                return new LiveViewHolder(view);
            }
        };
        liveRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    private void showEditDialog(LiveItem liveItem) {
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
    private void updateBuyerBalance(LiveItem liveItem, String updatedBuyer, String updatedPrice) {
        // Update the previous buyer's balance by subtracting the old price
        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
        Query oldBuyerQuery = buyerRef.orderByChild("fb").equalTo(liveItem.getBuyer());

        oldBuyerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double oldBalance = snapshot.child("balance").getValue(Double.class);
                    if (oldBalance != null) {
                        double newBalance = oldBalance - Double.parseDouble(liveItem.getPrice());
                        snapshot.getRef().child("balance").setValue(newBalance);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error
            }
        });

        // Update the updated buyer's balance by adding the updated price
        Query updatedBuyerQuery = buyerRef.orderByChild("fb").equalTo(updatedBuyer);

        updatedBuyerQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double updatedBalance = snapshot.child("balance").getValue(Double.class);
                    if (updatedBalance != null) {
                        double newBalance = updatedBalance + Double.parseDouble(updatedPrice);
                        snapshot.getRef().child("balance").setValue(newBalance);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle the error
            }
        });
    }

    private void showConfirmationDialog(LiveItem liveItem, String updatedBuyer, String updatedName, String updatedPrice, String ids, String updatedNote) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmation");
        builder.setMessage("Do you want to save the changes?\n\nBuyer: " + updatedBuyer + "\nCode: " + updatedName + "\nPrice: " + updatedPrice + "\nNote: " + updatedNote);
        builder.setPositiveButton("Save", (dialog, which) -> {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Saving...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(ids);

            // Prepare the data to be updated
            Map<String, Object> updates = new HashMap<>();
            updates.put("note", updatedNote);
            updates.put("buyer", updatedBuyer);
            updates.put("code", updatedName);
            updates.put("price", updatedPrice);
            updateBuyerBalance(liveItem, updatedBuyer, updatedPrice);

            transactionsRef.updateChildren(updates)
                    .addOnSuccessListener(aVoid -> {
                        progressDialog.dismiss();
                        dialog.dismiss();
                        showUpdatedDetailsDialog(updatedBuyer, updatedName, updatedPrice);
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to save changes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }


    private void showUpdatedDetailsDialog(String updatedCode, String updatedName, String updatedPrice) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Updated Details");
        builder.setMessage("Updated Code: " + updatedCode + "\nUpdated Name: " + updatedName + "\nUpdated Price: " + updatedPrice);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showFinalConfirmationDialog(String updatedCode, String updatedName, String updatedPrice, String ids) {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
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
    private void saveTransactionToFirebase(String note, String price, String buyer, String name) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMddyyyy_HHmmss");
        String dateTime = dateFormat.format(calendar.getTime());
        String productRandomKey = dateTime;
        DatabaseReference newTransactionRef = transactionReference.child(dateTime);
        double priceValue = Double.parseDouble(price);
        HashMap<String, Object> transactionData = new HashMap<>();
        transactionData.put("price", priceValue);
        transactionData.put("buyer", buyer);
        transactionData.put("date", getCurrentDate());
        transactionData.put("name", name);
        transactionData.put("note", note);
        transactionData.put("delivered", "no");
        transactionData.put("idItem", productRandomKey);
        transactionData.put("id", getCurrentDate() + buyer);

        String daters = getCurrentDate();

        newTransactionRef.setValue(transactionData);

        DatabaseReference liveDate = FirebaseDatabase.getInstance().getReference("LiveDate").child(daters);

        HashMap<String, Object> dateMap = new HashMap<>();
        dateMap.put("date", daters);
        liveDate.setValue(dateMap);

        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
        Query query = buyerRef.orderByChild("fb").startAt(buyer.toLowerCase()).endAt(buyer.toLowerCase() + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot buyerSnapshot : dataSnapshot.getChildren()) {
                    String dbBuyer = buyerSnapshot.child("fb").getValue(String.class);
                    if (dbBuyer != null && dbBuyer.equalsIgnoreCase(buyer)) {
                        Double currentBalance = buyerSnapshot.child("balance").getValue(Double.class);
                        if (currentBalance != null) {
                            double newBalance = currentBalance + Double.parseDouble(price);
                            buyerSnapshot.getRef().child("balance").setValue(newBalance);
                        } else {
                            // Handle the case when the balance is null
                            // For example, set a default balance or log an error
                        }
                        return;
                    }
                }

                // If the buyer is not found, you can proceed with saving a new buyer or other handling
                // showCreateNewBuyerDialog(price, buyer, name);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

// Other parts of the LiveActivity class remain the same



    private String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        return dateFormat.format(calendar.getTime());
    }



    private void showCreateNewBuyerDialog(String note, final String price,String searched,String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create a New Buyer");
        builder.setMessage("Do you want to create a new buyer?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // You can save the new buyer directly to Firebase here
                saveNewBuyerToFirebase(note,price,searched,name);

                // Optionally, you can also clear the code and price fields
                searchEditText.getText().clear();
                itemNameEditText.getText().clear();
                priceEditText.getText().clear();
                noteEditText.getText().clear();
            }
        });
        builder.setNegativeButton("No", null);
        builder.create().show();
    }

    private void saveNewBuyerToFirebase(String note, final String price,String searched,String name) {
        // Generate a unique key for the new buyer
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Buyer");
        double numericPrice = Double.parseDouble(price);
        // Get the name of the new buyer from the buyerSpinner
        String buyerName = searched;
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        String saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss a");
        String saveCurrentTime = currentTime.format(calendar.getTime());
        String productRandomKey = saveCurrentDate + saveCurrentTime;
        // Create a HashMap for the new buyer
        HashMap<String, Object> newBuyerMap = new HashMap<>();
        newBuyerMap.put("fb", buyerName);
        // Set other fields to null
        newBuyerMap.put("name", "null");
        newBuyerMap.put("method", "null");
        newBuyerMap.put("address", "null");
        newBuyerMap.put("balance", 0);
        newBuyerMap.put("status", "unidentified");
        newBuyerMap.put("id", productRandomKey);
        newBuyerMap.put("contact", "null");

        String buyer = buyerName;
        // Save the new buyer to Firebase
        databaseReference.child(productRandomKey).updateChildren(newBuyerMap);

        showConfirmationDialog(note,price, buyer,name);

    }


    // ... Existing methods for Firebase fetch, confirmation dialog, save to Firebase, create new buyer dialog, etc.
}