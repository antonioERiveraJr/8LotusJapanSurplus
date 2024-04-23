package com.example.lotusjapansurplus.Deliver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.lotusjapansurplus.MainActivity;
import com.example.lotusjapansurplus.Method.BuyerItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.transactions.TransactionActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class DeliverBuyerDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deliver_buyer_details);



        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeliverBuyerDetailsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        ListView transactionsListView = findViewById(R.id.transactionsListView); // Assuming you have a ListView in the layout XML

        String selectedBuyer = getIntent().getStringExtra("SELECTED_BUYER");

        List<String> transactionsList = new ArrayList<>();
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.orderByChild("buyer").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if ("no".equals(snapshot.child("delivered").getValue(String.class))) {
                        String itemName = snapshot.child("name").getValue(String.class);
                        String code = snapshot.child("code").getValue(String.class);
                        String price = snapshot.child("price").getValue(String.class);
                        transactionsList.add("Name: " + itemName + "\nCode: " + code + "\nPrice: " + price);
                    }
                }

                // Display the list of undelivered transactions for the selected buyer
                ArrayAdapter<String> adapter = new ArrayAdapter<>(DeliverBuyerDetailsActivity.this, android.R.layout.simple_list_item_1, transactionsList);
                transactionsListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
        Button wordButton = findViewById(R.id.wordButton);
        wordButton.setOnClickListener(v -> showConfirmationDialog(selectedBuyer));

        Button completeButton = findViewById(R.id.completeButton);
        completeButton.setOnClickListener(v -> {
            // Show confirmation dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(DeliverBuyerDetailsActivity.this);
            builder.setMessage("Mark transaction as delivered?");
            builder.setPositiveButton("Yes", (dialogInterface, i) -> markTransactionAsDelivered(selectedBuyer));
            builder.setNegativeButton("No", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        });
        Button updateButton = findViewById(R.id.updateButton);
        updateButton.setOnClickListener(view -> showEditDialog(selectedBuyer));


    }
    private void showEditDialog(String selectedBuyer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Box and Number");

        View dialogView = getLayoutInflater().inflate(R.layout.update_dialog, null);
        EditText boxEditText = dialogView.findViewById(R.id.boxEditText);
        EditText numberEditText = dialogView.findViewById(R.id.numberEditText);

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedBoxValue = boxEditText.getText().toString();
            String updatedNumberValue = numberEditText.getText().toString();

            // Update the Firebase Realtime Database here
            DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
            buyerRef.orderByChild("fb").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        snapshot.getRef().child("box").setValue(updatedBoxValue);
                        snapshot.getRef().child("number").setValue(updatedNumberValue);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showConfirmationDialog(String selectedBuyer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save as Word File");
        builder.setMessage("Do you want to save this as a Word file?");
        builder.setPositiveButton("Yes", (dialog, which) -> generateWordFile(selectedBuyer));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
        private void markTransactionAsDelivered(String selectedBuyer) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Updating...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
            transactionRef.orderByChild("buyer").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if ("no".equals(snapshot.child("delivered").getValue(String.class))) {
                            snapshot.getRef().child("delivered").setValue("yes");


                        }
                    }          progressDialog.dismiss();
                    // Navigate to DeliverActivity
                    Intent intent = new Intent(DeliverBuyerDetailsActivity.this, DeliverActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Handle errors if needed
                }
            });
            DatabaseReference transactionRefs = FirebaseDatabase.getInstance().getReference("Buyer");

            transactionRefs.orderByChild("fb").equalTo(selectedBuyer).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (dataSnapshot.exists()) {

                            snapshot.getRef().child("box").setValue(null);
                            snapshot.getRef().child("number").setValue(null);
                        }
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

    private void generateWordFile(String selectedBuyer) {
        AlertDialog.Builder shippingFeeDialog = new AlertDialog.Builder(this);
        shippingFeeDialog.setTitle("Shipping Fee");
        shippingFeeDialog.setMessage("Do you want to add a shipping fee before exporting to Word?");
        shippingFeeDialog.setPositiveButton("Yes", (dialog, which) -> {
            // Prompt for shipping fee and number of boxes
            AlertDialog.Builder feeAndBoxesDialog = new AlertDialog.Builder(this);
            feeAndBoxesDialog.setTitle("Shipping Fee Details");
            View feeAndBoxesView = getLayoutInflater().inflate(R.layout.shipping_fee_dialog, null);
            feeAndBoxesDialog.setView(feeAndBoxesView);

            feeAndBoxesDialog.setPositiveButton("Confirm", (dialog1, which1) -> {
                String shippingFee = ((EditText) feeAndBoxesView.findViewById(R.id.shippingFeeInput)).getText().toString();
                String numOfBoxes = ((EditText) feeAndBoxesView.findViewById(R.id.numOfBoxesInput)).getText().toString();

                XWPFDocument document = new XWPFDocument();
                List<CompletableFuture<?>> futures = new ArrayList<>();

                if (!selectedBuyer.equals("All")) {
                    CompletableFuture<Void> buyerDetailsFuture = new CompletableFuture<>();
                    CompletableFuture<Void> transactionFuture = new CompletableFuture<>();

                    DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
                    buyerRef.orderByChild("fb").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                BuyerItem buyerItem = snapshot.getValue(BuyerItem.class);

                                XWPFParagraph buyerDetails = document.createParagraph();
                                buyerDetails.setAlignment(ParagraphAlignment.LEFT);
                                buyerDetails.setSpacingAfter(200);

                                XWPFRun detailsRun = buyerDetails.createRun();
                                detailsRun.setText("Name: " + buyerItem.getName());
                                detailsRun.setFontSize(30);
                                detailsRun.addBreak();
                                detailsRun.setText("Method: " + buyerItem.getMethod());
                                detailsRun.addBreak();
                                detailsRun.setText("Address: " + buyerItem.getAddress());
                                detailsRun.addBreak();
                                detailsRun.setText("Contact: " + buyerItem.getContact());
                                detailsRun.addBreak();

                                buyerDetailsFuture.complete(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            buyerDetailsFuture.completeExceptionally(databaseError.toException());
                        }
                    });

                    DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
                    transactionRef.orderByChild("delivered").equalTo("no").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            XWPFTable table = document.createTable(1, 4);
                            table.setWidth("100%");
                            XWPFTableRow row = table.getRow(0);
                            row.getCell(0).setText("Item Name");
                            row.getCell(1).setText("Date");
                            row.getCell(2).setText("Price");

                            double total = 0.0;

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                if (snapshot.child("buyer").getValue(String.class).equals(selectedBuyer)) {
                                    String itemName = snapshot.child("name").getValue(String.class);
                                    String dates = snapshot.child("date").getValue(String.class);
                                    String price = snapshot.child("price").getValue(String.class);

                                    XWPFTableRow dataRow = table.createRow();
                                    dataRow.getCell(0).setText(itemName);
                                    dataRow.getCell(1).setText(dates);
                                    dataRow.getCell(2).setText(price);

                                    total += Double.parseDouble(price);
                                }
                            }

                            if (!shippingFee.isEmpty() && !numOfBoxes.isEmpty()) {
                                double fee = Double.parseDouble(shippingFee);
                                int boxes = Integer.parseInt(numOfBoxes);

                                XWPFParagraph shippingDetails = document.createParagraph();
                                shippingDetails.setAlignment(ParagraphAlignment.LEFT);
                                shippingDetails.setSpacingAfter(200);

                                XWPFRun shippingRun = shippingDetails.createRun();
                                shippingRun.addBreak();
                                shippingRun.addBreak();
                                shippingRun.setText("Shipping Fee: " + shippingFee + " per BOX (" + numOfBoxes + " BOX)");
                                shippingRun.setFontSize(10);


                                total += (fee * boxes);
                            }

                            XWPFParagraph totalParagraph = document.createParagraph();
                            XWPFRun totalRun = totalParagraph.createRun();
                            totalRun.setText("Total: " + total);
                            totalRun.setFontSize(15);
                            totalRun.addBreak();

                            transactionFuture.complete(null);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            transactionFuture.completeExceptionally(databaseError.toException());
                        }
                    });

                    CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(buyerDetailsFuture, transactionFuture);
                    futures.add(combinedFuture);
                }

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                allFutures.thenAccept((v) -> {
                    try {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        if (!path.exists()) {
                            path.mkdirs();
                        }
                        String fileName = "BuyersDetails_" + selectedBuyer + ".docx";
                        File file = new File(path, fileName);
                        FileOutputStream out = new FileOutputStream(file);
                        document.write(out);
                        out.close();
                        document.close();
                        runOnUiThread(() -> Toast.makeText(this, "Word file has been generated successfully at: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(this, "Error: Unable to generate Word file", Toast.LENGTH_SHORT).show());
                    }
                }).exceptionally(e -> {
                    e.printStackTrace();
                    return null;
                });
            });

            feeAndBoxesDialog.setNegativeButton("Cancel", (dialog12, which12) -> dialog12.dismiss());
            feeAndBoxesDialog.show();
        });
        shippingFeeDialog.setNegativeButton("No", (dialog, which) -> generateWordFileWithoutShippingFee(selectedBuyer));
        shippingFeeDialog.show();
    }

    private void generateWordFileWithoutShippingFee(String selectedBuyer) {
        XWPFDocument document = new XWPFDocument();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        if (!selectedBuyer.equals("All")) {
            CompletableFuture<Void> buyerDetailsFuture = new CompletableFuture<>();
            CompletableFuture<Void> transactionFuture = new CompletableFuture<>();

            DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
            buyerRef.orderByChild("fb").equalTo(selectedBuyer).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        BuyerItem buyerItem = snapshot.getValue(BuyerItem.class);

                        XWPFParagraph buyerDetails = document.createParagraph();
                        buyerDetails.setAlignment(ParagraphAlignment.LEFT);
                        buyerDetails.setSpacingAfter(200);

                        XWPFRun detailsRun = buyerDetails.createRun();
                        detailsRun.setText("Name: " + buyerItem.getName());
                        detailsRun.setFontSize(30);
                        detailsRun.addBreak();
                        detailsRun.setText("Method: " + buyerItem.getMethod());
                        detailsRun.addBreak();
                        detailsRun.setText("Address: " + buyerItem.getAddress());
                        detailsRun.addBreak();
                        detailsRun.setText("Contact: " + buyerItem.getContact());
                        detailsRun.addBreak();

                        buyerDetailsFuture.complete(null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    buyerDetailsFuture.completeExceptionally(databaseError.toException());
                }
            });

            DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
            transactionRef.orderByChild("delivered").equalTo("no").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    XWPFTable table = document.createTable(1, 3);
                    table.setWidth("100%");
                    XWPFTableRow row = table.getRow(0);
                    row.getCell(0).setText("Item Name");
                    row.getCell(1).setText("Date");
                    row.getCell(2).setText("Price");

                    double total = 0.0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        if (snapshot.child("buyer").getValue(String.class).equals(selectedBuyer)) {
                            String itemName = snapshot.child("name").getValue(String.class);
                            String dates = snapshot.child("date").getValue(String.class);
                            String price = snapshot.child("price").getValue(String.class);

                            XWPFTableRow dataRow = table.createRow();
                            dataRow.getCell(0).setText(itemName);
                            dataRow.getCell(1).setText(dates);
                            dataRow.getCell(2).setText(price);

                            total += Double.parseDouble(price);
                        }
                    }

                    XWPFParagraph totalParagraph = document.createParagraph();
                    XWPFRun totalRun = totalParagraph.createRun();
                    totalRun.addBreak();
                    totalRun.setText("Total: " + total);
                    totalRun.setFontSize(15);
                    totalRun.addBreak();

                    transactionFuture.complete(null);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    transactionFuture.completeExceptionally(databaseError.toException());
                }
            });

            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(buyerDetailsFuture, transactionFuture);
            futures.add(combinedFuture);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenAccept((v) -> {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists()) {
                    path.mkdirs();
                }
                String fileName = "BuyersDetails_" + selectedBuyer + ".docx";
                File file = new File(path, fileName);
                FileOutputStream out = new FileOutputStream(file);
                document.write(out);
                out.close();
                document.close();
                runOnUiThread(() -> Toast.makeText(this, "Word file has been generated successfully at: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error: Unable to generate Word file", Toast.LENGTH_SHORT).show());
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });



    }


}