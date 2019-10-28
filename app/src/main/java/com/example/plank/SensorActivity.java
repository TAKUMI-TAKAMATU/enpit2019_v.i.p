package com.example.plank;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.CountDownTimer;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class SensorActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private TextView timerText;//タイマーの表示ぶん
    private SimpleDateFormat dataFormat =
            new SimpleDateFormat("mm:ss.SSS", Locale.US);//https://akira-watson.com/android/countdowntimer.html
    private TextView textView, textInfo;
    private SoundPool soundPool;
    private int soundOne, soundTwo, soundThree;
    float nextX =0;
    float nextY =0;
    float nextZ =0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        // 3分= 3x60x1000 = 180000 msec
        long countNumber = 180000;
        // インターバル msec
        long interval = 10;
        Button startButton = findViewById(R.id.start_button);//タイマーのボタン
        Button stopButton = findViewById(R.id.stop_button);//タイマーのボタン
        timerText = findViewById(R.id.timer);
        timerText.setText(dataFormat.format(0));
        // CountDownTimer(long millisInFuture, long countDownInterval)
        final CountDown countDown = new CountDown(countNumber, interval);


        // Get an instance of the SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        textInfo = findViewById(R.id.text_info);

        // Get an instance of the TextView
        textView = findViewById(R.id.text_view);
        //スタートボタンの処理
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 開始
                countDown.start();
            }
        });
        //ストップボタンの処理
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 中止
                countDown.cancel();
                timerText.setText(dataFormat.format(0));
            }
        });


        Button returnButton_sensor = findViewById(R.id.return_button_sensor);
        returnButton_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //soundPool.play(soundOne, 1.0f, 1.0f, 0, 1, 1);//音声ならす
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


        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values[0];
            sensorY = event.values[1];
            sensorZ = event.values[2];

            String strTmp = "加速度センサー\n"
                    + " X: " + sensorX + "\n"
                    + " Y: " + sensorY + "\n"
                    + " Z: " + sensorZ;
            textView.setText(strTmp);

            //showInfo(event);
            //音センサー追加
            //サウンド追加
            if (sensorZ - nextZ < -0.5 || sensorZ - nextZ > 0.5 ) {
                soundPool.play(soundOne, 1.0f, 1.0f, 0, 1, 1);
            } else if (sensorX - nextX < -0.5 || sensorX - nextX > 0.5) {
                soundPool.play(soundTwo, 1.0f, 1.0f, 0, 1, 1);
            } else if (sensorY - nextY < -0.5 || sensorY - nextY > 0.5) {
                soundPool.play(soundThree, 1.0f, 1.0f, 0, 1, 1);
            }
            nextX = sensorX;
            nextY = sensorY;
            nextZ = sensorZ;

        }



    }

    // （お好みで）加速度センサーの各種情報を表示
    /*
    private void showInfo(SensorEvent event){
        // センサー名
        StringBuffer info = new StringBuffer("Name: ");
        info.append(event.sensor.getName());
        info.append("\n");

        // ベンダー名
        info.append("Vendor: ");
        info.append(event.sensor.getVendor());
        info.append("\n");

        // 型番
        info.append("Type: ");
        info.append(event.sensor.getType());
        info.append("\n");

        // 最小遅れ
        int data = event.sensor.getMinDelay();
        info.append("Mindelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // 最大遅れ
        //data = event.sensor.getMaxDelay();
        info.append("Maxdelay: ");
        info.append(String.valueOf(data));
        info.append(" usec\n");

        // レポートモード
        //data = event.sensor.getReportingMode();
        String stinfo = "unknown";
        if(data == 0){
            stinfo = "REPORTING_MODE_CONTINUOUS";
        }else if(data == 1){
            stinfo = "REPORTING_MODE_ON_CHANGE";
        }else if(data == 2){
            stinfo = "REPORTING_MODE_ONE_SHOT";
        }
        info.append("ReportingMode: ");
        info.append(stinfo);
        info.append("\n");

        // 最大レンジ
        info.append("MaxRange: ");
        float fData = event.sensor.getMaximumRange();
        info.append(String.valueOf(fData));
        info.append("\n");

        // 分解能
        info.append("Resolution: ");
        fData = event.sensor.getResolution();
        info.append(String.valueOf(fData));
        info.append(" m/s^2\n");

        // 消費電流
        info.append("Power: ");
        fData = event.sensor.getPower();
        info.append(String.valueOf(fData));
        info.append(" mA\n");
        textInfo.setText(info);
    }*/


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
            timerText.setText(dataFormat.format(0));
        }

        // インターバルで呼ばれる
        @Override
        public void onTick(long millisUntilFinished) {
            // 残り時間を分、秒、ミリ秒に分割
            //long mm = millisUntilFinished / 1000 / 60;
            //long ss = millisUntilFinished / 1000 % 60;
            //long ms = millisUntilFinished - ss * 1000 - mm * 1000 * 60;
            //timerText.setText(String.format("%1$02d:%2$02d.%3$03d", mm, ss, ms));

            timerText.setText(dataFormat.format(millisUntilFinished));

        }
    }

}