package com.example.lotusjapansurplus.Method;

public class LiveItem {
    private String buyer;
    private String code;
    private String price;
    private String id;

    private String name;
    private String note;
    private String idItem;
    public LiveItem() {
        // Default constructor required for calls to DataSnapshot.getValue(LiveItem.class)
    }

    public LiveItem(String buyer, String code, String price,String id,String name,String idItem,String note) {
        this.buyer = buyer;
        this.code = code;
        this.price = price;
        this.id = id;
        this.name = name;
        this.idItem = idItem;
        this.note= note;
    }

    public String getBuyer() {
        return buyer;
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

    public String getIdItem() {
        return idItem;
    }

    public void setIdItem(String idItem) {
        this.idItem = idItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public String getPrice() {
        return price;
    }
}
