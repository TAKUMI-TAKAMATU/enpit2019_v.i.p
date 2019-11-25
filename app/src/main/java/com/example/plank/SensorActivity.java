package com.example.plank;

//AndroidX

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;


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
        });

    }
}