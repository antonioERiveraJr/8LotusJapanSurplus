package com.example.lotusjapansurplus.Method;
public class BuyerItem {
    private String fb;
    private String name;
    private String method;
    private String address;
    private double balance;
    private String status;

    private String id;

    private  String contact;
    private String note;

    public BuyerItem() {
        // Default constructor required for Firebase
    }

    public BuyerItem(String fb, String name, String method, String address,Double balance,String status,String id,String contact,String note) {
        this.fb = fb;
        this.name = name;
        this.method = method;
        this.address = address;
        this.balance = balance;
        this.status = status;
        this.id = id;
        this.note = note;
        this.contact = contact;
    }

    public String getFb() {
        return fb;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getName() {
        return name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public String getAddress() {
        return address;
    }

    public double getBalance() {
        return balance;
    }

    public String getStatus() {
        return status;
    }
}