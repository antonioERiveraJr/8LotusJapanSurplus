package com.example.lotusjapansurplus.payment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import androidx.annotation.NonNull;
import java.util.ArrayList;

public class paymentActivity extends AppCompatActivity {

    private ListView paymentListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        TextView paymentTextView = findViewById(R.id.paymentTextView);
        paymentListView = findViewById(R.id.paymentListView);
        Button backButton = findViewById(R.id.backButton);

        ArrayList<String> paymentDetails = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, paymentDetails);
        paymentListView.setAdapter(adapter);

        DatabaseReference buyersRef = FirebaseDatabase.getInstance().getReference("Buyer");
        buyersRef.orderByChild("balance").startAt(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String fb = snapshot.child("fb").getValue(String.class);
                    long balance = snapshot.child("balance").getValue(Long.class);
                    String paymentInfo = "Name: " + fb + "    Balance: " + balance;
                    paymentDetails.add(paymentInfo);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(paymentActivity.this, MainActivity.class));
            finish();
        });

        paymentListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedItem = ((ArrayAdapter<String>) parent.getAdapter()).getItem(position);
            String[] parts = selectedItem.split("    "); // Assuming "    " is the separator between Name and Balance

            if (parts.length == 2) {
                String selectedBuyer = parts[0].substring(6); // Assuming "Name: " is 6 characters
                showPaymentDialog(selectedBuyer);
            }
        });
    }

    private void showPaymentDialog(String selectedBuyer) {
        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
        buyerRef.orderByChild("fb").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    long balance = snapshot.child("balance").getValue(Long.class);
                    handleBuyerClick(selectedBuyer, balance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    // Inside the method where you handle the buyer click event
    private void handleBuyerClick(String buyerName, double balance) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.payment_dialog, null);
        dialogBuilder.setView(dialogView);

        TextView balanceTextView = dialogView.findViewById(R.id.balanceTextView);
        EditText amountToPayEditText = dialogView.findViewById(R.id.amountToPayEditText);
        Button payButton = dialogView.findViewById(R.id.payButton);

        // Set the balance in the TextView
        balanceTextView.setText("Balance: " + balance);

        // Handle the pay button click
        payButton.setOnClickListener(v -> {
            String amountToPayStr = amountToPayEditText.getText().toString();
            if (!amountToPayStr.isEmpty()) {
                double amountToPay = Double.parseDouble(amountToPayStr);

                AlertDialog.Builder confirmationDialogBuilder = new AlertDialog.Builder(this);
                confirmationDialogBuilder.setMessage("Are you sure you want to pay " + amountToPay + "?");
                confirmationDialogBuilder.setPositiveButton("Yes", (dialog, which) -> {
                    ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Saving...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    double updatedBalance = balance - amountToPay;
                    // Call your method to update the balance in the database
                    updateBuyerBalance(buyerName, updatedBalance);

                    // Simulating a delay with a Handler, in real scenarios, this delay is due to the database operation
                    new Handler().postDelayed(() -> {
                        progressDialog.dismiss();
                        Intent intent = new Intent(paymentActivity.this, paymentActivity.class);
                        startActivity(intent);
                    }, 2000); // A 2-second delay to simulate the database update process
                    dialog.dismiss();
                });
                confirmationDialogBuilder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

                confirmationDialogBuilder.create().show();
            }
            else {
                // Show a message or toast indicating that the amount to pay is empty
            }
        });

        dialogBuilder.create().show();
    }

    private void updateBuyerBalance(String buyerName, double newBalance) {
        DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
        buyerRef.orderByChild("fb").equalTo(buyerName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().child("balance").setValue(newBalance);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

}