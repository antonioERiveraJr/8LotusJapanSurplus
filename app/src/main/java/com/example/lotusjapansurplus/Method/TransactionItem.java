package com.example.lotusjapansurplus.Method;


public class TransactionItem {
    private String id;
    private String code;
    private String price;
    private String buyer;
    private String date;
    private String name;

    public TransactionItem() {
        // Default constructor required for calls to DataSnapshot.getValue(TransactionItem.class)
    }

    public TransactionItem(String id, String code, String price, String buyer, String date, String name) {
        this.id = id;
        this.code = code;
        this.price = price;
        this.buyer = buyer;
        this.date = date;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getDate() {
        return date;
    }

    // Other setters and getters if needed
}