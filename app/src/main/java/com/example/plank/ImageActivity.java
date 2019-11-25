package com.example.plank;

//AndroidX

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);


        //opencvの処理をする画面に遷移
        Button OpenCvButton = findViewById(R.id.opencv_button);
        OpenCvButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), OpencvActivity.class);
                startActivity(intent);
            }
        });


        //画像を比較する画面に遷移
        Button ImageCompareButton = findViewById(R.id.image_compare);
        ImageCompareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), CompareActivity.class);
                startActivity(intent);
            }
        });


        //posenetの処理をする画面に遷移
        Button PoseNetButton = findViewById(R.id.posene_button);
        PoseNetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplication(), PosenetActivity.class);
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