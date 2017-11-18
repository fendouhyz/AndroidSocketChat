package com.wtz.androidsocketchat;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    private final String TAG = MainActivity.class.getName();

    private TextView tvLocalIp;
    private Button btnLeft;
    private Button btnRight;

    private FragmentManager fragmentManager;
    private Fragment mFragmentLeft;
    private Fragment mFragmentRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate...");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        tvLocalIp = (TextView) findViewById(R.id.tv_local_ip);
        btnLeft = (Button) findViewById(R.id.btn_left);
        btnRight = (Button) findViewById(R.id.btn_right);
        btnLeft.setOnClickListener(btnClick);
        btnRight.setOnClickListener(btnClick);

        fragmentManager = getFragmentManager();
        
        updateLocalIP();
        setTabSelection(0);
    }

    private void updateLocalIP() {
        String ip = IPHelper.getLocalIPAddress();
        if (!TextUtils.isEmpty(ip)) {
            String prefix = getResources().getString(R.string.local_ip);
            tvLocalIp.setText(prefix + ip);
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart...");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume...");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause...");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop...");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy...");
        super.onDestroy();
    }

    private View.OnClickListener btnClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_left:
                setTabSelection(0);
                break;
            case R.id.btn_right:
                setTabSelection(1);
                break;

            default:
                break;
            }
        }
    };

    private void setTabSelection(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        hideAllFragments(transaction);

        switch (index) {
        case 0:
            if (mFragmentLeft == null) {
                mFragmentLeft = new FragmentClient();
                transaction.add(R.id.fl_fragment_container, mFragmentLeft);
            } else {
                transaction.show(mFragmentLeft);
            }
            break;
        case 1:
            if (mFragmentRight == null) {
                mFragmentRight = new FragmentServer();
                transaction.add(R.id.fl_fragment_container, mFragmentRight);
            } else {
                transaction.show(mFragmentRight);
            }
            break;
        }
        
        transaction.commitAllowingStateLoss();
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        if (mFragmentLeft != null) {
            transaction.hide(mFragmentLeft);
        }
        if (mFragmentRight != null) {
            transaction.hide(mFragmentRight);
        }
    }
}
