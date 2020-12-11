package com.tds.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.taptap.sdk.AccessToken;
import com.taptap.sdk.Profile;
import com.taptap.sdk.RegionType;
import com.taptap.sdk.TapTapSdk;
import com.taptap.sdk.helper.TapLoginHelper;
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

        //DB：1。开启DB
        TdsInitializer.enableTapDB(MainActivity.this,"1.0", "taptap");
        //动态：1。开启动态
        TdsInitializer.enableMoment(MainActivity.this);
        //动态：2。注册动态回调
        registerMomentCallback();
    }

    private void login() {
        //登录：3。登录
        TapLoginHelper.getInstance().startTapLogin(MainActivity.this, TapTapSdk.SCOPE_PUIBLIC_PROFILE);
    }
    private boolean checkLogin() {
        //登录：获取登录信息
        // 未登录用户会返回null
        if (TapLoginHelper.getInstance().getCurrentAccessToken() == null) {
            Log.e(Tag, "checkLogin-onError");
            return false;
        } else {
            Log.e(Tag, "checkLogin-onSuccess");
            return true;
        }

        //已登录用户会实时回调onSuccess，未登录用户会回调onError
//        TapLoginHelper.getInstance().fetchProfileForCurrentAccessToken(new Api.ApiCallback<Profile>() {
//            @Override
//            public void onSuccess(Profile profile) {
//                Log.e(Tag, "checkLogin-onSuccess");
//                //TapDB会用到
//                openID = Profile.getCurrentProfile().getOpenid();
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                Log.e(Tag, "checkLogin-onError: "+throwable.getMessage());
//            }
//        });
    }

    private void initSDK() {
        TdsConfig tdsConfig = new TdsConfig.Builder()
                .appContext(MainActivity.this)
                .clientId(getResources().getString(R.string.tap_client_id))//开发者中心获取到的client Id
                .build();


        TdsInitializer.init(tdsConfig);

        //登录配置
        TapTapSdk.LoginSdkConfig loginSdkConfig = new TapTapSdk.LoginSdkConfig();
        loginSdkConfig.roundCorner = false;//false：登录页面是直角，true：登录页面是圆角
        loginSdkConfig.regionType = RegionType.CN;//标识为国际版，从2.5版本才开始支持
        TapTapSdk.changeTapLoginConfig(loginSdkConfig);
    }

    private void registerLoginCallback() {
        TapLoginHelper.getInstance().addLoginResultCallback(new TapLoginHelper.TapLoginResultCallback() {
            @Override
            public void onLoginSuccess(AccessToken accessToken) {
                Log.e(Tag, "onLoginSuccess");
                TapLoginHelper.getInstance().fetchProfileForCurrentAccessToken(new Api.ApiCallback<Profile>() {

                    @Override
                    public void onSuccess(Profile profile) {
                        openID = profile.getOpenid();
                        Log.e(Tag, openID);
                        TapDB.setUser("zwtest", openID, LoginType.TapTap);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e(Tag, "Login: onError");
                    }

                });

            }

            @Override
            public void onLoginCancel() {

            }

            @Override
            public void onLoginError(Throwable throwable) {
                Log.e(Tag, "onLoginError: " + throwable.getMessage());
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
                if (code == TapTapMomentSdk.CALLBACK_CODE_GET_NOTICE_SUCCESS) {
                    Toast.makeText(MainActivity.this, "获取通知数量:" + msg, Toast.LENGTH_SHORT).show();
                } else if (code == TapTapMomentSdk.CALLBACK_CODE_LOGIN_SUCCESS) {
                    //动态：3。设置动态登录状态
                    TapTapMomentSdk.setHandleLoginResult(true);
                }
            }
        });
    }
}