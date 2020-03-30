package net.newsmth.dirac.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentPagerAdapter;

import net.newsmth.dirac.R;
import net.newsmth.dirac.fragment.MessageFragment;

public class MessagePagerAdapter extends FragmentPagerAdapter {

    private String[] mTitleArray;

    public MessagePagerAdapter(FragmentActivity activity) {
        super(activity.getSupportFragmentManager());
        mTitleArray = activity.getResources().getStringArray(R.array.message_pager_title_array);
    }

    @Override
    public Fragment getItem(int position) {
        return new MessageFragment();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleArray[position];
    }
}
