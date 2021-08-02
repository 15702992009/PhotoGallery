package com.example.criminalintent;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public abstract class SingleFragmentActivity extends AppCompatActivity {
    private static final String TAG = "SingleFragmentActivity";

    protected abstract Fragment createLeftFragment();

    protected abstract Fragment createRightFragment();


    @LayoutRes
    protected abstract int getLayoutResId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_fragment);
        setContentView(getLayoutResId());
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        // 手机和平板绑定的为什么相同?2131427359 2131427359
        Log.d(TAG, String.valueOf(getLayoutResId()));

        FragmentManager fm = getSupportFragmentManager();
        Fragment leftFragment = createLeftFragment();
        Fragment rightFragment = createRightFragment();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.fragment_container, leftFragment);
        if (findViewById(R.id.detail_fragment_container)!=null&&rightFragment!=null) {
            transaction.add(R.id.detail_fragment_container, rightFragment);
        }
        transaction.commit();

    }
}
