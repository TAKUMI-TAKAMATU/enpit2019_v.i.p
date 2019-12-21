package com.example.plank

//AndroidX


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.view.View
import androidx.appcompat.app.AlertDialog


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

/*
        //posenetの処理をする画面に遷移
        val PoseNetButton = findViewById<Button>(R.id.posene_button)
        PoseNetButton.setOnClickListener {
            val intent = Intent(application , PosenetActivity::class.java)
            startActivity(intent)
        }
*/
        //ホーム画面に戻る処理
        val returnButton = findViewById<Button>(R.id.return_sub)
        returnButton.setOnClickListener { finish() }

        val helpButton = findViewById<Button>(R.id.helpButton2)
        helpButton.setOnClickListener {
            AlertDialog.Builder(this)
                    .setTitle("2人用の使い方！")
                    .setView(R.layout.activity_help2)
                    .setPositiveButton( "OK", null )
                    .show()
        }

    }

}