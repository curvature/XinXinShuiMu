package net.newsmth.dirac.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import net.newsmth.dirac.R;
import net.newsmth.dirac.adapter.MessagePagerAdapter;

public class MessagePagerFragment extends Fragment {

    private ViewPager pager;
    private TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_message, container, false);
        pager = rootView.findViewById(R.id.pager);
        tabLayout = rootView.findViewById(R.id.tab);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pager.setAdapter(new MessagePagerAdapter(getActivity()));
        tabLayout.setupWithViewPager(pager);
        tabLayout.setSelectedTabIndicatorColor(
                ContextCompat.getColor(getActivity(), R.color.primary));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.message, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.create) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}