package com.oneminutebefore.workout;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.android.volley.Request;
import com.oneminutebefore.workout.helpers.Keys;
import com.oneminutebefore.workout.helpers.SharedPrefsUtil;
import com.oneminutebefore.workout.helpers.UrlBuilder;
import com.oneminutebefore.workout.helpers.VolleyHelper;
import com.oneminutebefore.workout.models.WorkoutExercise;

import org.json.JSONException;

import java.util.HashMap;

public class SplashActivity extends BaseRequestActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        boolean areLinksDownloaded = SharedPrefsUtil.getBooleanPreference(this, Keys.KEY_LINKS_DOWNLOADED, false);

        if(!areLinksDownloaded){
            VolleyHelper volleyHelper = new VolleyHelper(this, false);
            String url = new UrlBuilder(UrlBuilder.API_ALL_VIDEOS).build();
            volleyHelper.callApi(Request.Method.GET, url, null, new VolleyHelper.VolleyCallback() {
                @Override
                public void onSuccess(String result) throws JSONException {
                    saveLinks(result);
                }

                @Override
                public void onError(String error) {
                    saveLinks(null);
                }
            });
        }else{
            checkLoginAndRedirect();
        }
    }

    private void checkLoginAndRedirect(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String userId = SharedPrefsUtil.getStringPreference(SplashActivity.this, Keys.KEY_USER_ID, "-1");
                WorkoutApplication.getmInstance().setUserId(userId);
                if(!userId.equals("-1")){
                    WorkoutApplication.getmInstance().setUserId(userId);
                    startActivity(new Intent(SplashActivity.this, HomeNewActivity.class));
                }else{
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }
//                startActivity(new Intent(SplashActivity.this, HomeNewActivity.class));
            }
        },2000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
               finish();
            }
        }, 3000);

    }

    private void saveLinks(String linksData){

        HashMap<String, WorkoutExercise> map = WorkoutExercise.createMapFromJson(linksData);
        if(map != null && !map.isEmpty()){
            WorkoutApplication.getmInstance().setWorkouts(map);
            SharedPrefsUtil.setStringPreference(SplashActivity.this,Keys.KEY_VIDEOS_INFO, linksData);
            SharedPrefsUtil.setBooleanPreference(SplashActivity.this,Keys.KEY_LINKS_DOWNLOADED, true);
        }
        checkLoginAndRedirect();
    }
}