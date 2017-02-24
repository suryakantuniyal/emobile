package com.android.emobilepos.ordering;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.emobilepos.R;
import com.android.emobilepos.models.EMSCategory;
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
    private List<EMSCategory> categories;
    private ImageLoader imageLoader;
    private DisplayImageOptions options;
    private boolean isPortrait;
    private int selectedPosition = -1;
    private CategoriesCallback callback;

    public class CategoriesViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public ImageView imageView;
        public TextView subCategoriesTextView;

        public CategoriesViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.categoryItemTextView);
            imageView = (ImageView) view.findViewById(R.id.categoryItemImage);
            subCategoriesTextView = (TextView) view.findViewById(R.id.categoryItemSubCategoriesTextView);
        }
    }

    public interface CategoriesCallback {
        void categorySelected(EMSCategory category);
    }

    public CatalogCategories_Adapter(Context context, CategoriesCallback callback, List<EMSCategory> categories, ImageLoader imageLoader) {
        this.callback = callback;
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
    public void onBindViewHolder(CategoriesViewHolder holder, final int position) {
        final EMSCategory cat = categories.get(position);

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

        holder.subCategoriesTextView.setVisibility((cat.getNumberOfSubCategories() > 0) ? View.VISIBLE : View.INVISIBLE);
        holder.subCategoriesTextView.setText(Integer.toString(cat.getNumberOfSubCategories()));

        if (selectedPosition == position) {
            holder.textView.setTextColor(Color.WHITE);
            holder.itemView.setBackgroundColor(Color.BLUE);
            holder.textView.setBackgroundColor(Color.BLUE);
        } else {
            holder.textView.setTextColor(Color.BLACK);
            holder.textView.setBackgroundColor(Color.LTGRAY);
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                notifyItemChanged(selectedPosition);
                selectedPosition = position;
                notifyItemChanged(selectedPosition);

                if (callback != null) {
                    callback.categorySelected(cat);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}
