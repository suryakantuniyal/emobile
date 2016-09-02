package com.android.emobilepos.settings;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.android.emobilepos.R;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;


/**
 * An activity representing a single Setting detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link SettingListActivity}.
 */
public class SettingDetailActivity extends BaseFragmentActivityActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_detail);


        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            SettingsActivity.PrefsFragment fragment = new SettingsActivity.PrefsFragment();
            Bundle extras = getIntent().getExtras();
            int section = extras.getInt("section");
            arguments.putInt("section", section);
            fragment.setArguments(arguments);

//            arguments.putString(SettingDetailFragment.ARG_ITEM_ID,
//                    getIntent().getStringExtra(SettingDetailFragment.ARG_ITEM_ID));
//            SettingDetailFragment fragment = new SettingDetailFragment();
//            fragment.setArguments(arguments);
            getFragmentManager().beginTransaction()
                    .add(R.id.setting_detail_container, fragment)
                    .commit();
        }
    }


}
