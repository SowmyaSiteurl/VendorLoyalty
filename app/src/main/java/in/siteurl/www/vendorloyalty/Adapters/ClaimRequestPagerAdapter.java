package in.siteurl.www.vendorloyalty.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by siteurl on 5/1/18.
 */

public class ClaimRequestPagerAdapter extends FragmentPagerAdapter {


    private ArrayList<Fragment> mClaimRequestArrayList = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();

    public ClaimRequestPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void addFragments(Fragment fragment,String title)
    {
        mClaimRequestArrayList.add(fragment);
        mTitles.add(title);
    }


    @Override
    public Fragment getItem(int position) {
        return mClaimRequestArrayList.get(position);
    }

    @Override
    public int getCount() {
        return mClaimRequestArrayList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }
}
