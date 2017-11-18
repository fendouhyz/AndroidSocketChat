package com.wtz.androidsocketchat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.wtz.androidsocketchat.utils.IPHelper;
import com.wtz.androidsocketchat.view.FragmentClient;
import com.wtz.androidsocketchat.view.FragmentServer;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity {
    private final String TAG = MainActivity.class.getName();

    private TextView tvLocalIp;
    private Button btnLeft;
    private Button btnRight;

    private FragmentManager fragmentManager;
    private Fragment mFragmentLeft;
    private Fragment mFragmentRight;

    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
    };
    private List<String> mPermissionList = new ArrayList<>();
    private final static int REQUEST_PERMISSIONS_CODE = 1;

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

        fragmentManager = getSupportFragmentManager();
        
        updateLocalIP();
        setTabSelection(0);
        btnLeft.setBackgroundResource(R.drawable.bg_yellow);
        btnRight.setBackgroundResource(R.drawable.bg_gray);

        if (Build.VERSION.SDK_INT >= 23) {
            judgePermission();
        }
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
                btnLeft.setBackgroundResource(R.drawable.bg_yellow);
                btnRight.setBackgroundResource(R.drawable.bg_gray);
                setTabSelection(0);
                break;
            case R.id.btn_right:
                btnRight.setBackgroundResource(R.drawable.bg_yellow);
                btnLeft.setBackgroundResource(R.drawable.bg_gray);
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

    @TargetApi(Build.VERSION_CODES.M)
    private void judgePermission() {
        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            // TODO: 2017/10/9
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            this.requestPermissions(permissions, REQUEST_PERMISSIONS_CODE);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS_CODE:
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        //判断是否勾选禁止后不再询问
                        boolean showRequestPermission = this.shouldShowRequestPermissionRationale(permissions[i]);
                        if (showRequestPermission) {//
//                            judgePermission();//重新申请权限
//                            return;
                        } else {
                            //已经禁止
                        }
                    }
                }
                // TODO: 2017/9/29 Do something
                break;
            default:
                break;
        }
    }
}
