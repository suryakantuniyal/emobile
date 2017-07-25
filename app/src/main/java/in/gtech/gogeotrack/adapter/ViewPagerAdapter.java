package in.gtech.gogeotrack.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import in.gtech.gogeotrack.fragments.AddFragment;

/**
 * Created by silence12 on 4/7/17.
 */

public class   ViewPagerAdapter extends FragmentPagerAdapter {

    final int PAGE_COUNT = 3;
    Context context;
    private AddFragment addFragment1, addFragment2, addFragment3, addFragment4;

    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Log.d("Position", String.valueOf(position));
        switch (position) {
            case 0:
                if (addFragment1 == null) {
                    addFragment1 = AddFragment.newInstance(position);
                }
                return addFragment1;
            case 1:
                if (addFragment2 == null) {
                    addFragment2 = AddFragment.newInstance(position);
                }
                return addFragment2;
            case 2:
                if (addFragment3 == null) {
                    addFragment3 = AddFragment.newInstance(position);
                }
                return addFragment3;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
    }

    @Override
    public Parcelable saveState() {
        return null;
    }
}
