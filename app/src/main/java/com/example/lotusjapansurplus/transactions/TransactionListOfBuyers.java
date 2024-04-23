package com.example.lotusjapansurplus.transactions;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.lotusjapansurplus.Method.BuyerItem;
import com.example.lotusjapansurplus.Method.LiveItem;
import com.example.lotusjapansurplus.Method.TransactionItem;
import com.example.lotusjapansurplus.R;
import com.example.lotusjapansurplus.ViewHolder.LiveViewHolder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


public class TransactionListOfBuyers extends AppCompatActivity {

    private List<String> buyersForDate;
    private String selectedDate;

    private static final int STORAGE_PERMISSION_CODE = 101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trasaction_list_of_buyers);
        Button sortButton = findViewById(R.id.sortButton);
        sortButton.setOnClickListener(v -> showSortOptions(selectedDate));

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
        Intent intent = getIntent();
        selectedDate = intent.getStringExtra("date");
        buyersForDate = new ArrayList<>(); // Initialize the list

        // Reference to the 'transactions' node in the Firebase Database
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String buyer = snapshot.child("buyer").getValue(String.class);
                    buyersForDate.add(buyer);
                }
                if (!buyersForDate.isEmpty()) {
                    buyersForDate.add(0, "All"); // Add "All" as the first item in the list
                    displayBuyers();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TransactionListOfBuyers.this, TransactionActivity.class);
                startActivity(intent);
            }
        });

        Button printButton = findViewById(R.id.printButton);
        printButton.setOnClickListener(v -> showConfirmationDialog(selectedDate));

        Button wordButton = findViewById(R.id.wordButton);
        wordButton.setVisibility(View.GONE);
        wordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generateWordFile(selectedDate);
            }
        });
    }
    private void showSortOptions(String selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sort Options");
        String[] sortOptions = {"Delivered", "Not Delivered"};
        builder.setItems(sortOptions, (dialog, which) -> {
            String selectedOption = sortOptions[which];
            if (selectedOption.equals("Delivered")) {
                fetchSortedBuyers(selectedDate, "yes");
            } else if (selectedOption.equals("Not Delivered")) {
                fetchSortedBuyers(selectedDate, "no");
            }
        });

        builder.show();
    }
    private void fetchSortedBuyers(String selectedDate, String status) {
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        Query sortedQuery = transactionsRef.orderByChild("date").equalTo(selectedDate);

        sortedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<String> buyers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String buyer = snapshot.child("buyer").getValue(String.class);
                    String deliveredStatus = snapshot.child("delivered").getValue(String.class);

                    if (deliveredStatus != null && deliveredStatus.equals(status)) {
                        buyers.add(buyer);
                    }
                }

                displaySortedBuyers(buyers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void displaySortedBuyers(List<String> buyers) {
        Set<String> uniqueBuyers = new HashSet<>(buyers);

        ListView buyersListView = findViewById(R.id.buyersListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(uniqueBuyers)
        );
        buyersListView.setAdapter(adapter);

        buyersListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBuyer = adapter.getItem(position);
            navigateToTransactionDetails(selectedBuyer);
        });

        buyersListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String selectedBuyer = adapter.getItem(position);
            navigateToTransactionDetails(selectedBuyer);
            return true;
        });
    }
    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(TransactionListOfBuyers.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(TransactionListOfBuyers.this, new String[]{permission}, requestCode);
        } else {
            Toast.makeText(TransactionListOfBuyers.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void showConfirmationDialog(String selectedDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Export to Excel");
        builder.setMessage("Do you want to save this as an Excel file?");
        builder.setPositiveButton("Yes", (dialog, which) -> exportToExcel(selectedDate));
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(TransactionListOfBuyers.this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TransactionListOfBuyers.this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void exportToExcel(String selectedDate) {
        Toast.makeText(this, selectedDate, Toast.LENGTH_SHORT).show();

        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<TransactionItem> transactions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TransactionItem transaction = snapshot.getValue(TransactionItem.class);
                    transactions.add(transaction);
                }

                StringBuilder excelData = new StringBuilder();


                StringBuilder buyer = new StringBuilder();
                StringBuilder Item = new StringBuilder();
                StringBuilder Price = new StringBuilder();
                excelData.append("Buyer\tItem\tPrice\n"); // Adding headers to the data

                for (TransactionItem transaction : transactions) {
                    String rowData = transaction.getBuyer() + "\t\t\t" + transaction.getName() + "\t\t\t" + transaction.getPrice() + "\n";
                    excelData.append(rowData);
                    String rowPrice = transaction.getPrice()+"\n";
                    String rowItem = transaction.getName()+"\n";
                    String rowBuyer = transaction.getBuyer()+"\n";
                    Price.append(rowPrice);
                    Item.append(rowItem);
                    buyer.append(rowBuyer);
                }

                generateExcel(transactions, selectedDate);
                // Create the intent and pass the Excel data to the next activity
                Intent intent = new Intent(TransactionListOfBuyers.this, TransactionShowExcelData.class);
                intent.putExtra("BUYER_DATA", buyer.toString());
                intent.putExtra("ITEM_DATA", Item.toString());
                intent.putExtra("PRICE_DATA", Price.toString());
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }

    private void generateExcel(List<TransactionItem> transactions, String selectedDate) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transaction Details");

        Row headerRow = sheet.createRow(0);
        String[] headers = {"Buyer", "Code", "Price"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowNum = 1;
        double total = 0.0; // Initialize the total variable

        for (TransactionItem transaction : transactions) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(transaction.getBuyer());
            row.createCell(1).setCellValue(transaction.getName());
            row.createCell(2).setCellValue(transaction.getPrice());

            total += Double.parseDouble(transaction.getPrice()); // Add the price to the total
        }

        // Add the date and total at the end of the Excel file
        Row dateRow = sheet.createRow(rowNum);
        dateRow.createCell(0).setCellValue("Date:");
        dateRow.createCell(1).setCellValue(selectedDate);

        Row totalRow = sheet.createRow(rowNum + 1);
        totalRow.createCell(0).setCellValue("Total:");
        totalRow.createCell(1).setCellValue(total); // Set the total value

        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!path.exists()) {
                path.mkdirs(); // Create the directory if it doesn't exist
            }

            String fileName = "TransactionDetails_" + selectedDate + ".xlsx";

            File file = new File(path, fileName);
            FileOutputStream fileOut = new FileOutputStream(file);
            workbook.write(fileOut);
            fileOut.close();
            workbook.close();

            Toast.makeText(this, "Excel file has been generated successfully at: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Error: Unable to generate Excel file", Toast.LENGTH_SHORT).show());
        }
    }

    private void displayBuyers() {
        ListView buyersListView = findViewById(R.id.buyersListView);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                new ArrayList<>(new HashSet<>(buyersForDate)) // Utilize HashSet to prevent duplicates
        );
        buyersListView.setAdapter(adapter);


        // Retrieve the transaction status for each buyer to display the indicator
        DatabaseReference transactionsRef = FirebaseDatabase.getInstance().getReference("transactions");
        transactionsRef.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (int i = 0; i < buyersForDate.size(); i++) {
                    String buyer = buyersForDate.get(i);
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        String buyerFromSnapshot = snapshot.child("buyer").getValue(String.class);
                        String status = snapshot.child("delivered").getValue(String.class);
                        if (buyer.equals(buyerFromSnapshot)) {
                            if (status != null && status.equals("yes")) {
                                // Check if the current list item already contains "(Delivered)"
                                if (!buyer.contains(" (Delivered)")) {
                                    buyersForDate.set(i, buyer + " (Delivered)"); // Update the list item
                                }
                                break;
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged(); // Refresh the ListView
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if needed
            }
        });

        buyersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedBuyer = adapter.getItem(position); // Retrieve the selected buyer from the adapter
                navigateToTransactionDetails(selectedBuyer);
            }
        });
    }

    private void navigateToTransactionDetails(String buyer) {
        Intent intent = new Intent(TransactionListOfBuyers.this, TransactionDetailsActivity.class);
        String cleanBuyerName = buyer.replace(" (Delivered)", ""); // Remove " (Delivered)" from the buyer's name
        intent.putExtra("buyer", buyer.equals("All") ? "All" : cleanBuyerName); // Set 'All' to null
        intent.putExtra("date", selectedDate);
        startActivity(intent);
    }


    private void generateWordFile(String selectedDate) {
        XWPFDocument document = new XWPFDocument();
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String buyer : buyersForDate) {
            if (!buyer.equals("All")) {
                CompletableFuture<Void> buyerDetailsFuture = new CompletableFuture<>();
                CompletableFuture<Void> transactionFuture = new CompletableFuture<>();

                // Fetch and add buyer details
                DatabaseReference buyerRef = FirebaseDatabase.getInstance().getReference("Buyer");
                buyerRef.orderByChild("fb").equalTo(buyer).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            BuyerItem buyerItem = snapshot.getValue(BuyerItem.class);

                            XWPFParagraph buyerDetails = document.createParagraph();
                            buyerDetails.setAlignment(ParagraphAlignment.LEFT);
                            buyerDetails.setSpacingAfter(200);

                            XWPFRun detailsRun = buyerDetails.createRun();
                            detailsRun.setText("Name: " + buyerItem.getName());
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

                // Fetch and add transaction table
                DatabaseReference transactionRef = FirebaseDatabase.getInstance().getReference("transactions");
                transactionRef.orderByChild("date").equalTo(selectedDate).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        XWPFTable table = document.createTable(1, 3);
                        table.setWidth("100%");
                        XWPFTableRow row = table.getRow(0);
                        row.getCell(0).setText("Item Name");
                        row.getCell(1).setText("Code");
                        row.getCell(2).setText("Price");

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            if (snapshot.child("buyer").getValue(String.class).equals(buyer)) {
                                String itemName = snapshot.child("name").getValue(String.class);
                                String code = snapshot.child("code").getValue(String.class);
                                String price = snapshot.child("price").getValue(String.class);

                                XWPFTableRow dataRow = table.createRow();
                                dataRow.getCell(0).setText(itemName);
                                dataRow.getCell(1).setText(code);
                                dataRow.getCell(2).setText(price);
                            }
                        }
                        transactionFuture.complete(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        transactionFuture.completeExceptionally(databaseError.toException());
                    }
                });

                // Combine both futures
                CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(buyerDetailsFuture, transactionFuture);
                futures.add(combinedFuture);
            }
        }

        // Wait for all combined futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        allFutures.thenAccept((v) -> {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!path.exists()) {
                    path.mkdirs();
                }
                String fileName = "BuyersDetails_" + selectedDate + ".docx";
                File file = new File(path, fileName);
                FileOutputStream out = new FileOutputStream(file);
                document.write(out);
                out.close();
                document.close();
                runOnUiThread(() -> Toast.makeText(this, "Word file has been generated successfully at: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }


}