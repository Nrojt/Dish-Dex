package com.nrojt.dishdex.backend;

public class Category{
    private String categoryName;
    private int categoryID;

    public Category(int categoryID, String categoryName) {
        this.categoryName = categoryName;
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public int getCategoryID() {
        return categoryID;
    }
}
