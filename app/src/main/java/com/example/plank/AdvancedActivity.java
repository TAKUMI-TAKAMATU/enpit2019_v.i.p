package com.example.plank;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.CountDownTimer;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.View.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.List;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;
import android.view.animation.RotateAnimation;
import android.os.Handler;
import android.graphics.Color;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import java.util.*;
import java.lang.*;
import java.io.*;

public class AdvancedActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView timerText;//タイマーの表示文
    private TextView timerText＿trainig;
    private SimpleDateFormat dataFormat =
            new SimpleDateFormat("mm:ss", Locale.US);//https://akira-watson.com/android/countdowntimer.html
    //"mm:ss.SSS", Locale.US
    private TextView textView, textInfo;
    private SoundPool soundPool;
    private int soundOne, soundTwo, soundThree,soundFour;
    float nextX =0;
    float nextY =0;
    float nextZ =0;
    private  long time = 10000;
    private int first = 0;
    private float FirstX,FirstY,FirstZ =0;
    private int frag = 0;
    private int timing = 0;
    final Handler handler = new Handler();


    private Runnable delay;
    private Runnable delayStartCountDown;
    // 3分= 3x60x1000 = 180000 msec
    long countNumber = 40000;
    //スタート前
    long countbefore = 10000;
    // インターバル msec
    long interval = 10;
    final AdvancedActivity.CountDown countDown = new AdvancedActivity.CountDown(countNumber, interval);
    Button startButton;
    Button stopButton;
    //private Runnable;

    private Sensor accel;
    private TextView textGraph;
    private LineChart mChart;
    private String[] labels = new String[]{
            "揺れ",
            "Y軸の揺れ",
            "Z軸の揺れ"};
    private int[] colors = new int[]{
            Color.BLUE,
            Color.GRAY,
            Color.MAGENTA};
    private boolean lineardata = true;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);


        startButton = findViewById(R.id.start_button);//タイマーのボタン
        stopButton = findViewById(R.id.stop_button);//タイマーのボタン
        timerText = findViewById(R.id.timer);
        timerText＿trainig = findViewById(R.id.timer_training);
        timerText.setText(dataFormat.format(10000));
        timerText＿trainig.setText(dataFormat.format(40000));


        // CountDownTimer(long millisInFuture, long countDownInterval)

        final AdvancedActivity.CountDown countDown_before = new AdvancedActivity.CountDown(countbefore, interval);


        // 縦画面
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get an instance of the TextView
        textGraph = findViewById(R.id.text_view);

        mChart = findViewById(R.id.chart);
        // インスタンス生成
        mChart.setData(new LineData());
        // no description text
        mChart.getDescription().setEnabled(false);
        // Grid背景色
        mChart.setDrawGridBackground(true);
        // 右側の目盛り
        mChart.getAxisRight().setEnabled(false);

        // Grid縦軸を破線 x軸に関する処理

        XAxis xAxis = mChart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setEnabled(false);//x軸のラベル消す


        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisMaxValue(4.0f);
        leftAxis.setAxisMinValue(-4.0f);
        leftAxis.setDrawGridLines(true);

        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // textInfo = findViewById(R.id.text_info);

        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        //スタートボタンの処理
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startButton.setEnabled(false);
                countDown_before.start();
                //wait_time();
                //countDown_beforeで終わるときにスタートボタンが押せるの防ぐ
                timing =1;
                delayStartCountDown =  new Runnable(){//遅延定義 10/31
                    @Override
                    public void run() {
                        if(timing==1) {
                            soundPool.play(soundFour, 1.0f, 1.0f, 0, 0, 1);
                        }
                    }
                };

                delay =  new Runnable(){//遅延定義 10/31
                    @Override
                    public void run() {
                        mChart.setData(new LineData());
                        soundPool.play(soundFour, 1.0f, 1.0f, 0, 0, 1);

                        // 開始
                        timing =1;
                        first =1;
                        frag=1;
                        countDown.start();
                        startButton.setEnabled(false);
                    }
                    // }
                };
                handler.postDelayed(delayStartCountDown, 7000);//遅延実行
                handler.postDelayed(delayStartCountDown, 8000);//遅延実行
                handler.postDelayed(delayStartCountDown, 9000);//遅延実行
                handler.postDelayed(delay, 10200);//遅延実行
            }
        });
        //ストップボタンの処理
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 中止
                startButton.setEnabled(true);
                if(frag==1){
                    countDown.cancel();}
                if(frag ==0){
                    countDown_before.cancel();}
                handler.removeCallbacks(delay);
                frag=0;
                timing =0;
                handler.removeCallbacks(delayStartCountDown);
                handler.removeCallbacks(delay);
                timerText.setText(dataFormat.format(10000));
                timerText＿trainig.setText(dataFormat.format(40000));

                //Intent intent = new Intent(getApplication(), ImageActivity.class);
                //startActivity(intent);
            }
        });


        Button returnButton_sensor = findViewById(R.id.return_button_sensor);
        returnButton_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(frag==1){
                    countDown.cancel();}
                if(frag ==0){
                    countDown_before.cancel();}
                handler.removeCallbacks(delay);
                frag=0;
                timing =0;
                handler.removeCallbacks(delayStartCountDown);
                handler.removeCallbacks(delay);
                finish();
            }
        });
        //以下追加
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(2)
                .build();

        // one.wav をロードしておく
        soundOne = soundPool.load(this, R.raw.one, 1);
        soundTwo = soundPool.load(this, R.raw.two, 1);
        soundThree = soundPool.load(this, R.raw.three, 1);
        soundFour = soundPool.load(this, R.raw.four, 1);

        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                Log.d("debug","sampleId="+sampleId);
                Log.d("debug","status="+status);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listenerの登録
        Sensor accel = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_GAME);
        //sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_UI);
    }


    // 解除するコードも入れる!
    @Override
    protected void onPause() {
        super.onPause();
        // Listenerを解除
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float sensorX, sensorY, sensorZ;
        if(first==1){
            FirstX = event.values[0];
            FirstY = event.values[1];
            FirstZ = event.values[2];
            first=0;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];


            if(frag==1) {
                if (FirstZ - nextZ < -1 || FirstZ - nextZ > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                } else if (FirstX - nextX < -1 || FirstX - nextX > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                } else if (FirstY - nextY < -1 || FirstY - nextY > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                }
            }
            nextX = sensorX;
            nextY = sensorY;
            nextZ = sensorZ;

        }

        float gravity[] = new float[3];
        float linear_acceleration[] = new float[1];

        final float alpha = 0.5f;

        if(frag==1) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {

                // alpha is calculated as t / (t + dT)
                // with t, the low-pass filter's time-constant
                // and dT, the event delivery rate

                gravity[0] = (FirstX - nextX)*alpha;
                gravity[1] = (FirstY - nextY)*alpha;
                gravity[2] = (FirstZ - nextZ)*alpha;

                float x = Math.max(gravity[0], gravity[1]);
                float y = Math.max(x, gravity[2]);

                float x2 = Math.min(gravity[0], gravity[1]);
                float y2 = Math.min(gravity[0], gravity[1]);
                if(Math.abs(y)>Math.abs(y2)){
                    linear_acceleration[0] =y;
                }else{
                    linear_acceleration[0] =y2;
                }


                //linear_acceleration[1] = gravity[1];
                //linear_acceleration[2] = gravity[2];

                String accelero;


                LineData data = mChart.getLineData();

                if (data != null) {
                    for (int i = 0; i < 1; i++) {
                        ILineDataSet set3 = data.getDataSetByIndex(i);
                        if (set3 == null) {
                            LineDataSet set = new LineDataSet(null, labels[i]);
                            set.setLineWidth(2.0f);
                            set.setColor(colors[i]);
                            // liner line
                            set.setDrawCircles(false);
                            // no values on the chart
                            set.setDrawValues(false);
                            set3 = set;
                            data.addDataSet(set3);
                        }

                        // data update
                        if (!lineardata) {
                            data.addEntry(new Entry(set3.getEntryCount(), event.values[i]), i);
                        } else {
                            data.addEntry(new Entry(set3.getEntryCount(), linear_acceleration[i]), i);
                        }

                        data.notifyDataChanged();
                    }


                    mChart.notifyDataSetChanged(); // 表示の更新のために変更を通知する
                    mChart.setVisibleXRangeMaximum(180); // 表示の幅を決定する
                    mChart.moveViewToX(data.getEntryCount()); // 最新のデータまで表示を移動させる
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    class CountDown extends CountDownTimer {

        CountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            // 完了

            timerText.setText(dataFormat.format(10000));
            timerText＿trainig.setText(dataFormat.format(40000));
            frag =0;
            if(timing ==1){
                startButton.setEnabled(true);}
            soundPool.play(soundFour, 1.0f, 1.0f, 0, 0, 1);
        }


        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            // 残り時間を分、秒、ミリ秒に分割
            //long mm = millisUntilFinished / 1000 / 60;
            //long ss = millisUntilFinished / 1000 % 60;
            //long ms = millisUntilFinished - ss * 1000 - mm * 1000 * 60;
            //timerText.setText(String.format("%1$02d:%2$02d.%3$03d", mm, ss, ms));

            if(frag==0){
                timerText.setText(dataFormat.format(millisUntilFinished));
            }
            if(frag ==1){
                timerText＿trainig.setText(dataFormat.format(millisUntilFinished));
            }
        }

    }



}
