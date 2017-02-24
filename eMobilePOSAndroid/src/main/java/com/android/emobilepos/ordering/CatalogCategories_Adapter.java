package com.android.emobilepos.ordering;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.database.CategoriesHandler;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.util.List;

/**
 * Created by anieves on 2/23/17.
 */

public class CatalogCategories_Adapter extends RecyclerView.Adapter<CatalogCategories_Adapter.CategoriesViewHolder> {
    private List<CategoriesHandler.EMSCategory> categories;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isPortrait;

    public class CategoriesViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;

        public CategoriesViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.categoryItemTextView);
            imageView = (ImageView) view.findViewById(R.id.categoryItemImage);
        }
    }

    public CatalogCategories_Adapter(Context context, List<CategoriesHandler.EMSCategory> categories, ImageLoader imageLoader) {
        this.categories = categories;
        this.imageLoader = imageLoader;
        isPortrait = Global.isPortrait(context);

        if (isPortrait)
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                    imageScaleType(ImageScaleType.IN_SAMPLE_INT).build();
        else
            options = new DisplayImageOptions.Builder().resetViewBeforeLoading(true).displayer(new FadeInBitmapDisplayer(800)).cacheOnDisc(true).
                    imageScaleType(ImageScaleType.IN_SAMPLE_INT).showImageOnLoading(R.drawable.loading_image)
                    .showImageForEmptyUri(R.drawable.no_image).build();
    }

    @Override
    public CategoriesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.catalog_category_item_view, parent, false);

        return new CategoriesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(CategoriesViewHolder holder, int position) {
        CategoriesHandler.EMSCategory cat = categories.get(position);

        String categoryName = cat.getCategoryName();
        if (categoryName == null) {
            categoryName = "";
        }
        holder.textView.setText(categoryName);

        String iconUrl = cat.getIconUrl();
        if (iconUrl != null) {
            if (!iconUrl.equals(holder.imageView.getTag())) {
                holder.imageView.setTag(iconUrl);
                imageLoader.displayImage(iconUrl, holder.imageView, options);
            }
        } else {
            holder.imageView.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
