package com.android.emobilepos.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anieves on 2/24/17.
 */

public class EMSCategory implements Parcelable {
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

    private EMSCategory(Parcel in) {
        categoryId = in.readString();
        categoryName = in.readString();
        iconUrl = in.readString();
        numberOfSubCategories = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(categoryId);
        dest.writeString(categoryName);
        dest.writeString(iconUrl);
        dest.writeInt(numberOfSubCategories);
    }

    public static final Parcelable.Creator<EMSCategory> CREATOR = new Creator<EMSCategory>() {
        @Override
        public EMSCategory createFromParcel(Parcel source) {
            return new EMSCategory(source);
        }

        @Override
        public EMSCategory[] newArray(int size) {
            return new EMSCategory[size];
        }
    };
}
