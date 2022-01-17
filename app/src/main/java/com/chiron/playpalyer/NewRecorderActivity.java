package com.chiron.playpalyer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.astuetz.PagerSlidingTabStrip;
import com.chiron.playpalyer.fragment.FileViewerFragment;
import com.chiron.playpalyer.fragment.RecordFragment;
import com.chiron.playpalyer.fragment.TTSFragment;

public class NewRecorderActivity extends AppCompatActivity {

    private ViewPager pager;
    private PagerSlidingTabStrip tabs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_record);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
    }

    public class MyAdapter extends FragmentPagerAdapter{

        private String[] titles ={"Text to sound","Record","Recorded files"};

        public MyAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:{
                    return TTSFragment.newInstance();
                }
                case 1:{
                    return RecordFragment.newInstance(position);
                }
                case 2:{
                    return FileViewerFragment.newInstance(position);
                }

            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
}