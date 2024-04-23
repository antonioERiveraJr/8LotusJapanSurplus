package com.example.lotusjapansurplus.Deliver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.transactions.TransactionActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public class DeliverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver);

        ListView buyerListView = findViewById(R.id.buyerListView);
        Spinner methodSpinner = findViewById(R.id.methodSpinner);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(this, R.array.method_array, android.R.layout.simple_spinner_item);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Add "All" option to the adapter
        ArrayList<CharSequence> methodList = new ArrayList<>(methodAdapter.getCount() + 1);
        for (int i = 0; i < methodAdapter.getCount(); i++) {
            methodList.add(methodAdapter.getItem(i));
        }
        methodList.add(0, "All");
        methodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, methodList);
        methodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(methodAdapter);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliverActivity.this, MainActivity.class);
                startActivity(intent);
                Spinner methodSpinner = findViewById(R.id.methodSpinner);
                methodSpinner.setSelection(0);
            }
        });



        methodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedMethod = parent.getItemAtPosition(position).toString();
                if (selectedMethod.equals("All")) {
                    displayAllTransactions(); // Handle displaying all transactions
                } else {
                    filterBySelectedMethod(selectedMethod); // Filter transactions by the selected method
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle empty selection if needed
            }
        });
    }

    private void displayAllTransactions(){

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.orderByChild("delivered").equalTo("no").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> undeliveredBuyers = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String buyerName = snapshot.child("buyer").getValue(String.class);
                    if (buyerName != null) {
                        undeliveredBuyers.add(buyerName);
                    }
                }

                fetchBalanceInfo(undeliveredBuyers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });

    }

    private void fetchBalanceInfo(Set<String> undeliveredBuyers) {
        DatabaseReference buyersRef = FirebaseDatabase.getInstance().getReference("Buyer");
        HashMap<String, Long> buyerBalanceMap = new HashMap<>(); // Change from String to Long

        buyersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String buyerName = snapshot.child("fb").getValue(String.class);
                    Long balance = snapshot.child("balance").getValue(Long.class); // Change to Long
                    String deliveredStatus = snapshot.child("delivered").getValue(String.class);

                    if (buyerName != null && balance != null && "no".equals(deliveredStatus)) {
                        buyerBalanceMap.put(buyerName, balance);
                        undeliveredBuyers.add(buyerName);
                    }
                }

                setupListView(undeliveredBuyers, buyerBalanceMap);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void setupListView(Set<String> undeliveredBuyers, HashMap<String, Long> buyerBalanceMap) {
        ListView buyerListView = findViewById(R.id.buyerListView);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_layout, R.id.textName, new ArrayList<>(undeliveredBuyers)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {


                String buyerName = undeliveredBuyers.toArray(new String[0])[position];

                View view = super.getView(position, convertView, parent);
                TextView textPrice = view.findViewById(R.id.textPrice);
                TextView textName = view.findViewById(R.id.textName);
                TextView textNumber = view.findViewById(R.id.textNumber);
                TextView textBox = view.findViewById(R.id.textBox);
                DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");

                transactionRef.orderByChild("delivered").equalTo("no").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        double total = 0.0;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("buyer").getValue(String.class).equals(buyerName)) {
                                String price = snapshot.child("price").getValue(String.class);

                                total += Double.parseDouble(price);
                                textPrice.setText(String.valueOf((int) total));



                            }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                DatabaseReference getBuyerInfo = FirebaseDatabase.getInstance().getReference("Buyer");
                getBuyerInfo.orderByChild("fb").equalTo(buyerName).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String box = snapshot.child("box").getValue(String.class);
                            textBox.setText(String.valueOf(box));

                            String number = snapshot.child("number").getValue(String.class);
                            textNumber.setText(String.valueOf(number));

                            int buyerBalance = 0; // Initialize with a default value
                            Long buyerBalances = snapshot.child("balance").getValue(Long.class);
                            if (buyerBalances != null) {
                                buyerBalance = Integer.parseInt(String.valueOf(buyerBalances)); // Parse the string to an integer
                            }

                            if (buyerBalance <   1) {
                                textName.setTextColor(Color.GREEN);
                            } else {
                                textName.setTextColor(Color.RED);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });




                Long balance = buyerBalanceMap.get(buyerName);

                textName.setText(buyerName);
                // Set your price and box information here
                // For example:


                return view;
            }
        };

        buyerListView.setAdapter(adapter);

        buyerListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBuyer = ((ArrayAdapter<String>) parent.getAdapter()).getItem(position);

            Intent intent = new Intent(DeliverActivity.this, DeliverBuyerDetailsActivity.class);
            intent.putExtra("SELECTED_BUYER", selectedBuyer);
            startActivity(intent);
        });
    }
    private void filterBySelectedMethod(String selectedMethod) {
        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
        Query query;

        if (selectedMethod.equals(getString(R.string.select_method))) {
            query = buyerRef;
        } else {
            query = buyerRef.orderByChild("method").equalTo(selectedMethod);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Set<String> undeliveredBuyers = new HashSet<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String buyerName = snapshot.child("fb").getValue(String.class);
                    if (buyerName != null) {
                        checkDeliveryStatus(buyerName, undeliveredBuyers);
                    }
                }

                fetchBalanceInfo(undeliveredBuyers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void checkDeliveryStatus(String buyerName, Set<String> undeliveredBuyers) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        Query deliveryQuery = transactionsRef.orderByChild("buyer").equalTo(buyerName);

        deliveryQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String deliveredStatus = snapshot.child("delivered").getValue(String.class);
                    if (deliveredStatus != null && deliveredStatus.equals("no")) {
                        undeliveredBuyers.add(buyerName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

}