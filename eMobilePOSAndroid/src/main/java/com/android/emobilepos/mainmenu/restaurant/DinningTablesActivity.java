package com.android.emobilepos.mainmenu.restaurant;


import android.app.Fragment;
import android.app.FragmentManager;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.android.database.DBManager;
import com.android.emobilepos.R;
import com.android.support.Global;
import com.android.support.SynchMethods;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;
import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.PageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import org.xml.sax.SAXException;

import java.io.IOException;

public class DinningTablesActivity extends BaseFragmentActivityActionBar {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    public String associateId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        associateId = extras.getString("associateId");
        setContentView(R.layout.activity_dinning_tables);
        new SynchOnHoldOrders().execute();
        refresh(0);
    }

    public void refresh(int page) {
        setmSectionsPagerAdapter(new SectionsPagerAdapter(getFragmentManager()));
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        PageIndicator titlePageIndicator = (TitlePageIndicator) findViewById(R.id.indicator);
        mViewPager.setAdapter(getmSectionsPagerAdapter());
        titlePageIndicator.setViewPager(mViewPager);
        mViewPager.setCurrentItem(page);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_dinning_tables, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    public SectionsPagerAdapter getmSectionsPagerAdapter() {
        return mSectionsPagerAdapter;
    }

    public void setmSectionsPagerAdapter(SectionsPagerAdapter mSectionsPagerAdapter) {
        this.mSectionsPagerAdapter = mSectionsPagerAdapter;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter implements IconPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return new TablesMapFragment();
                default:
                    return new TablesGridFragment();
            }
        }

        @Override
        public int getIconResId(int i) {
            return 0;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.table_map);
                case 1:
                    return getString(R.string.table_list);
            }
            return null;
        }
    }

    @Override
    public void onResume() {
        Global global = (Global) getApplication();
        if (global.isApplicationSentToBackground(this))
            global.loggedIn = false;
        global.stopActivityTransitionTimer();

        if (!global.loggedIn) {
            if (global.getGlobalDlog() != null)
                global.getGlobalDlog().dismiss();
            global.promptForMandatoryLogin(this);
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Global global = (Global) getApplication();
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        boolean isScreenOn = powerManager.isScreenOn();
        if (!isScreenOn)
            global.loggedIn = false;
        global.startActivityTransitionTimer();
    }

    private class SynchOnHoldOrders extends AsyncTask<Void, String, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                updateProgress(getString(R.string.sync_dload_ordersonhold));
                SynchMethods.synchOrdersOnHoldList(DinningTablesActivity.this);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        public void updateProgress(String msg) {
            publishProgress(msg);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            refresh(0);
        }
    }
}
