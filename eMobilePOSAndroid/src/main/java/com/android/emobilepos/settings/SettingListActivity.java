package com.android.emobilepos.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.android.emobilepos.R;
import com.android.emobilepos.settings.dummy.DummyContent;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An activity representing a list of Settings. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link SettingDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class SettingListActivity extends BaseFragmentActivityActionBar {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    private enum SettingSection {
        GENERAL(0), RESTAURANT(1), GIFTCARD(2), PAYMENTS(3), PRINTING(4), PRODUCTS(5), OTHERS(6);
        int code;

        SettingSection(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static SettingSection getInstance(int code) {
            switch (code) {
                case 0:
                    return GENERAL;
                case 1:
                    return RESTAURANT;
                case 2:
                    return GIFTCARD;
                case 3:
                    return PAYMENTS;
                case 4:
                    return PRINTING;
                case 5:
                    return PRODUCTS;
                case 6:
                    return OTHERS;
                default:
                    return GENERAL;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_list);

        View recyclerView = findViewById(R.id.setting_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.setting_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(Arrays.asList(getResources().getStringArray(R.array.settingsSectionsArray))));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<String> mValues;

        public SimpleItemRecyclerViewAdapter(List<String> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.setting_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.mItem = mValues.get(position);
            holder.mIdView.setText(mValues.get(position));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        SettingsActivity.PrefsFragment fragment = new SettingsActivity.PrefsFragment();
                        Bundle args = new Bundle();
                        args.putInt("section", SettingSection.getInstance(position).getCode());
                        fragment.setArguments(args);

//                        Bundle arguments = new Bundle();
//                        arguments.putString(SettingDetailFragment.ARG_ITEM_ID, holder.mItem);
//                        SettingDetailFragment fragment = new SettingDetailFragment();
//                        fragment.setArguments(arguments);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.setting_detail_container, fragment)
                                .commit();
                    } else {
//                        SettingsActivity.PrefsFragment fragment = new SettingsActivity.PrefsFragment();
//                        Bundle args = new Bundle();
//                        args.putInt("section", SettingSection.getInstance(position).getCode());
//                        fragment.setArguments(args);

                        Context context = v.getContext();
                        Intent intent = new Intent(context, SettingDetailActivity.class);
                        intent.putExtra("section", SettingSection.getInstance(position).getCode());

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public String mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
            }

            @Override
            public String toString() {
                return super.toString();
            }
        }
    }
}
