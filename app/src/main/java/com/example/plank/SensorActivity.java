package com.example.plank;

//AndroidX

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.content.Intent;
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


public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView timerText;//タイマーの表示ぶん
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
    private  long timef = 10000;
    private int first = 0;
    private float FirstX,FirstY,FirstZ =0;
    private int frag = 0;
    private int frag_countnum = 0;
    private int timing = 0;
    private int time_count = 0;
    private String now_time;
    private String before_time;
    final Handler handler = new Handler();

    private Runnable delay;
    private Runnable delayStartCountDown;
    // 3分= 3x60x1000 = 180000 msec
    long countNumber = 30000;
    //スタート前
    long countbefore = 10000;
    // インターバル msec
    long interval = 10;
    final CountDown countDown = new CountDown(countNumber, interval);
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




public class SensorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);


        //初心者モード画面に遷移
        Button BiginnerModeButton = findViewById(R.id.biginnermode_button);
        BiginnerModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), BiginnerActivity.class);
                startActivity(intent);
            }
        });


        //中級者モード画面に遷移
        Button IntermediateModeButton = findViewById(R.id.intermediatemode_button);
        IntermediateModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), IntermediateActivity.class);
                startActivity(intent);
            }
        });


        //上級者モード画面に遷移
        Button AdvancedModeButton = findViewById(R.id.advancedmode_button);
        AdvancedModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), AdvancedActivity.class);
                startActivity(intent);
            }
        });

 //ホーム画面に戻る処理
        Button returnButton = findViewById(R.id.return_sub);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();

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
            now_time = (String) timerText.getText();

            if(frag==1) {
                if (FirstZ - nextZ < -1 || FirstZ - nextZ > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                     frag_countnum = 1;
                } else if (FirstX - nextX < -1 || FirstX - nextX > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                    frag_countnum = 1;
                } else if (FirstY - nextY < -1 || FirstY - nextY > 1) {
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1);
                    frag_countnum = 1;
                }else{
                    if(frag_countnum==0 && before_time != now_time){
                        time_count++;
                    }
                    frag_countnum = 0;

                }
            }
            before_time = now_time;
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
            timerText＿trainig.setText(dataFormat.format(30000));
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
        });

    }
}