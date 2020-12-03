package com.tds.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.taptap.sdk.AccessToken;
import com.taptap.sdk.CallBackManager;
import com.taptap.sdk.LoginManager;
import com.taptap.sdk.Profile;
import com.taptap.sdk.RegionType;
import com.taptap.sdk.TapTapSdk;
import com.taptap.sdk.helper.TapLoginHelper;
import com.taptap.sdk.net.Api;
import com.tds.TdsConfig;
import com.tds.TdsInitializer;

public class MainActivity extends Activity implements Button.OnClickListener {
    private CallBackManager callbackManager;
    private String Tag = "TapTapTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化SDK
        initSDK();
        //注册登录回调
        registerLoginCallback();
        //检查是否登录过
        checkLogin();
    }

    private void checkLogin() {
        //未登录用户会返回null
        if (TapLoginHelper.getInstance().getCurrentAccessToken() == null) {
            Log.e(Tag, "checkLogin-onError");
            login();
        } else {
            Log.e(Tag, "checkLogin-onSuccess");
            startGame();
        }

        //已登录用户会实时回调onSuccess，未登录用户会回调onError
//        TapLoginHelper.getInstance().fetchProfileForCurrentAccessToken(new Api.ApiCallback<Profile>() {
//            @Override
//            public void onSuccess(Profile profile) {
//                Log.e(Tag, "checkLogin-onSuccess");
//                //TapDB会用到
//                String openId = Profile.getCurrentProfile().getOpenid();
//                startGame();
//            }
//
//            @Override
//            public void onError(Throwable throwable) {
//                login();
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
        TapLoginHelper.getInstance().setLoginResultCallback(new TapLoginHelper.ITapLoginResultCallback() {
            @Override
            public void onLoginSuccess(AccessToken accessToken) {
                Log.e(Tag, "onLoginSuccess");
                startGame();
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

    private void login() {
        TapLoginHelper.getInstance().startTapLogin(MainActivity.this, TapTapSdk.SCOPE_PUIBLIC_PROFILE);
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btnGame) {
            checkLogin();
        }
    }

    public void startGame() {
        Intent intent = new Intent(MainActivity.this, GameActivity.class);
        startActivity(intent);
    }

}