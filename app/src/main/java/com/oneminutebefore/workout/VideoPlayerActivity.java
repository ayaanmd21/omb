package com.oneminutebefore.workout;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.oneminutebefore.workout.models.WorkoutExercise;

import java.util.HashMap;
import java.util.Map;

public class VideoPlayerActivity extends AppCompatActivity {

    public static final String KEY_URL = "url";
    public static final String KEY_WORKOUT = "workout";

    private YouTubePlayerFragment youTubePlayerFragment;

    private WorkoutExercise workoutExercise;

    private boolean isDemo;

    private ProgressBar progressBar;

    private ImageView ivPlayPause;

    private TextView txtMsgTimerAction;

    private boolean timerRunning;

    private int timerSecond;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent mIntent = getIntent();
        workoutExercise = (WorkoutExercise) mIntent.getSerializableExtra(KEY_WORKOUT);



        isDemo = workoutExercise == null;
        if(isDemo){
            HashMap<String, WorkoutExercise> workouts = WorkoutApplication.getmInstance().getWorkouts();
            if(workouts != null && !workouts.isEmpty()){
                for(Map.Entry entry : workouts.entrySet()){
                    if(((WorkoutExercise)entry.getValue()).getName().equals("Desk Push ups")){
                        workoutExercise = (WorkoutExercise)entry.getValue();
                        break;
                    }
                }
            }
        }

        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            if(isDemo){
                mActionBar.setTitle("Demo");
            }else{
                mActionBar.setTitle(workoutExercise.getName());
            }
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.player_layout);

        if (!TextUtils.isEmpty(workoutExercise.getVideoLink())) {
            findViewById(R.id.youtube_content).setVisibility(View.VISIBLE);
            findViewById(R.id.layout_no_content).setVisibility(View.GONE);
            youTubePlayerFragment.initialize(Constants.DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {
                @Override
                public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                    youTubePlayer.setShowFullscreenButton(true);
                    String url = workoutExercise.getVideoLink();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.MINIMAL);
                    youTubePlayer.cueVideo(url.substring(url.lastIndexOf("/") + 1));
                }

                @Override
                public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

                }
            });

            timerSecond = 0;
            ivPlayPause = (ImageView)findViewById(R.id.iv_play_pause);
            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            txtMsgTimerAction = (TextView)findViewById(R.id.txt_timer_action_msg);

            progressBar.setMax(60);
            progressBar.setProgress(0);
            progressBar.setMax(60);
            ((CircleProgressBar)progressBar).setProgressBackgroundColor(getResources().getColor(R.color.divider_color));
            ivPlayPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleTimer();
                }
            });


        } else {
            findViewById(R.id.youtube_content).setVisibility(View.GONE);
            findViewById(R.id.layout_no_content).setVisibility(View.VISIBLE);
        }
    }

    private void toggleTimer(){

        if(timerSecond >= 60){
            return;
        }
        timerRunning = !timerRunning;

        if(timerRunning){
            ivPlayPause.setImageResource(R.drawable.ic_pause_48dp);
            timerTask = new TimerTask();
            timerTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            txtMsgTimerAction.setText(getString(R.string.pause));
        }else{
            txtMsgTimerAction.setText(getString(R.string.start));
            ivPlayPause.setImageResource(R.drawable.ic_play_arrow_48dp);
            if(timerTask != null){
                timerTask.cancel(true);
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onDestroy() {
        if(timerTask !=null){
            timerTask.cancel(true);
        }
//        youTubePlayerFragment.onDestroy();
        super.onDestroy();
    }

    private class TimerTask extends AsyncTask<Void, Void, Void> {

        MediaPlayer mp;

        @Override
        protected Void doInBackground(Void... params) {

            boolean flag = true;
            while (flag && timerSecond < 60) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    flag = false;
                }
                timerSecond++;
                publishProgress();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(timerSecond);
            if(timerSecond == 30 || timerSecond == 50 || timerSecond > 55){
                mp = MediaPlayer.create(VideoPlayerActivity.this,R.raw.beep);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.stop();
                        mp.release();
                    }
                });
//                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                    @Override
//                    public void onCompletion(MediaPlayer mp) {
//                        mp.prepareAsync();
//                    }
//                });
//                mp.reset();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(!isCancelled()){
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoPlayerActivity.this);
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_reps_input,null);
                final EditText etRepsCount = (EditText)dialogView.findViewById(R.id.et_reps_count);
                builder.setView(dialogView);
                builder.setNegativeButton(getString(R.string.cancel), null);
                builder.setPositiveButton(getString(R.string.ok), null);
                final AlertDialog alertDialog = builder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String repsCount = etRepsCount.getText().toString().trim();
                                if(TextUtils.isEmpty(repsCount)){
                                    ((TextInputLayout)dialogView.findViewById(R.id.til_reps_count)).setError("Please enter reps");
                                }else{
                                    Toast.makeText(VideoPlayerActivity.this, "Thank you for entering", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    finish();
                                }
                            }
                        });
                    }
                });
                alertDialog.show();
                ivPlayPause.setVisibility(View.GONE);
            }
        }
    }
}
