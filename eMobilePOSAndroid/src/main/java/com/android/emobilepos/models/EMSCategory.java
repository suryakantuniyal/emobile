package com.android.emobilepos.models;

/**
 * Created by anieves on 2/24/17.
 */

public class EMSCategory {
    private String categoryId;
    private String categoryName;
    private String iconUrl;
    private int numberOfSubCategories;

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public int getNumberOfSubCategories() {
        return numberOfSubCategories;
    }

    public void setNumberOfSubCategories(int numberOfSubCategories) {
        this.numberOfSubCategories = numberOfSubCategories;
    }

    public EMSCategory(String categoryId, String categoryName, String iconUrl, int numberOfSubCategories) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.iconUrl = iconUrl;
        this.numberOfSubCategories = numberOfSubCategories;
    }
}
