package com.tds.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.taptap.sdk.AccessToken;
import com.taptap.sdk.AccountGlobalError;
import com.taptap.sdk.Profile;
import com.taptap.sdk.RegionType;

import com.taptap.sdk.TapLoginHelper;
import com.taptap.sdk.net.Api;
import com.tds.TdsConfig;
import com.tds.TdsInitializer;
import com.tds.moment.TapTapMomentSdk;
import com.tds.tapdb.sdk.LoginType;
import com.tds.tapdb.sdk.TapDB;

public class MainActivity extends Activity implements Button.OnClickListener {
    private String Tag = "TapTapTest";
    private String openID = null;
    private TapTapMomentSdk.Config momentConfig = new TapTapMomentSdk.Config();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //登录：1。初始化SDK
        initSDK();
        //登录：2。注册登录回调
        registerLoginCallback();

        //DB：1。开启TapDB
        TdsInitializer.enableTapDB(MainActivity.this, "1.0", "taptap");
        //动态：1。开启动态
        TdsInitializer.enableMoment(MainActivity.this);
        //动态：2。注册动态回调
        registerMomentCallback();
    }

    private void login() {
        //登录：3。登录
        TapLoginHelper.startTapLogin(MainActivity.this, TapLoginHelper.SCOPE_PUBLIC_PROFILE);
    }
    private boolean checkLogin() {
        //登录：获取登录信息
        // 未登录用户会返回null
        if (TapLoginHelper.getCurrentAccessToken() == null) {
            Log.e(Tag, "checkLogin-onError");
            return false;
        } else {
            Log.e(Tag, "checkLogin-onSuccess");
            return true;
        }

    }

    private void initSDK() {
        TdsConfig tdsConfig = new TdsConfig.Builder()
                .appContext(MainActivity.this)
                .clientId(getResources().getString(R.string.tap_client_id))//开发者中心获取到的client Id
                .build();


        TdsInitializer.init(tdsConfig);

    }

    private void registerLoginCallback() {
        TapLoginHelper.registerLoginCallback(new TapLoginHelper.TapLoginResultCallback() {
            @Override
            public void onLoginSuccess(AccessToken accessToken) {
                Log.e(Tag,"accessToken: "+accessToken.access_token);
                TapLoginHelper.fetchProfileForCurrentAccessToken(new Api.ApiCallback<Profile>() {

                    @Override
                    public void onSuccess(Profile profile) {
                        openID = profile.getOpenid();
                        Log.e(Tag, openID);
                        //TapDB.setUser("zwtest", openID, LoginType.TapTap);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(Tag, "fetchProfileForCurrentAccessToken: onError");
                    }
                });
            }

            @Override
            public void onLoginCancel() {
                Log.e(Tag, "Login: onLoginCancel");
            }

            @Override
            public void onLoginError(AccountGlobalError accountGlobalError) {
                Log.e(Tag, "Login: onLoginError");
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btnGame) {
            if (checkLogin()) {
                Toast.makeText(this, "请勿重复登录", Toast.LENGTH_LONG).show();
            } else {
                login();
            }

        } else if (viewId == R.id.btnStart) {
            if (checkLogin()) {
                startGame();
                Log.e(Tag,openID);
            } else {
                login();
            }
        } else if (viewId == R.id.btnMoment_open) {
            //动态：4。打开动态页面
            TapTapMomentSdk.openTapMoment(momentConfig);
        } else if (viewId == R.id.btnMoment_video) {
            if (checkLogin()) {
                momentConfig.orientation = TapTapMomentSdk.ORIENTATION_DEFAULT;
                String content = "普通动态描述";
                String[] imagePaths = new String[]{"/sdcard/DCIM/Camera/IMG_20201201_110006.jpg"};
                TapTapMomentSdk.publishMoment(momentConfig, imagePaths, content);
            } else {
                Toast.makeText(this, "请先登录", Toast.LENGTH_LONG).show();
            }
        } else if (viewId == R.id.btnMoment_exit) {
            TapLoginHelper.logout();
        }
    }

    public void startGame() {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
    }

    private void registerMomentCallback() {
        TapTapMomentSdk.setCallback(new TapTapMomentSdk.TapMomentCallback() {
            @Override
            public void onCallback(int code, String msg) {
                //ignore code 500 and 600
                Log.e(Tag, "TapTapMomentSdk-callback: code: " + code + ", msg: " + msg);
                    Toast.makeText(MainActivity.this, "获取通知数量:" + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
}