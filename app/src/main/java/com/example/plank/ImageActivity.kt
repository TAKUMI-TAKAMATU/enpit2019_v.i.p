package com.example.plank

//AndroidX

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class ImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)


        //opencvの処理をする画面に遷移
        val OpenCvButton = findViewById<Button>(R.id.opencv_button)
        OpenCvButton.setOnClickListener {
            val intent = Intent(application , OpencvActivityKt::class.java)
            startActivity(intent)
        }


        //画像を比較する画面に遷移
        val ImageCompareButton = findViewById<Button>(R.id.image_compare)
        ImageCompareButton.setOnClickListener {
            val intent = Intent(application , CompareActivity::class.java)
            startActivity(intent)
        }


        //posenetの処理をする画面に遷移
        val PoseNetButton = findViewById<Button>(R.id.posene_button)
        PoseNetButton.setOnClickListener {
            val intent = Intent(application , PosenetActivity::class.java)
            startActivity(intent)
        }

        //ホーム画面に戻る処理
        val returnButton = findViewById<Button>(R.id.return_sub)
        returnButton.setOnClickListener { finish() }

    }
}