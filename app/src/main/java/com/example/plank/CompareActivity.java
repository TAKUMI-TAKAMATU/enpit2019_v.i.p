package com.example.plank;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
//追加
import android.app.Activity;

import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
//ここまで
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.ParcelFileDescriptor;

import java.io.FileDescriptor;
import java.io.IOException;
import android.provider.MediaStore;
import android.util.Log;


public class CompareActivity extends AppCompatActivity {

    private final static int RESULT_CAMERA = 1001;
    private final static int REQUEST_PERMISSION = 1002;//10/2追加
    private ImageView imageView;
    private ImageView imageView2;
    private Uri cameraUri;//10/2追加
    private File cameraFile;//10/2追加


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        imageView = findViewById(R.id.image_view);
        imageView2 = findViewById(R.id.image_view2);

        //カメラボタン
        Button cameraButton = findViewById(R.id.camera_button);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //追加
                // Android 6, API 23以上でパーミッシンの確認
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermission();
                }
                else {
                    cameraIntent();
                }

            }
        });

        //写真を表示する処理
        Button PhotoButton = findViewById(R.id.select_phote);
        PhotoButton.setOnClickListener(new View.OnClickListener() {

            public void onClick (View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, RESULT_CAMERA);
            }

        });

        Button returnButton = findViewById(R.id.return_sub_com);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    //10/2作成　カメラ保存
    private void cameraIntent(){
        // 保存先のフォルダー
        File cFolder = getExternalFilesDir(Environment.DIRECTORY_DCIM);

        String fileDate = new SimpleDateFormat(
                "ddHHmmss", Locale.US).format(new Date());
        // ファイル名
        String fileName = String.format("CameraIntent_%s.jpg", fileDate);

        cameraFile = new File(cFolder, fileName);

        cameraUri = FileProvider.getUriForFile(
                CompareActivity.this,
                getApplicationContext().getPackageName() + ".fileprovider",
                cameraFile);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
        startActivityForResult(intent, RESULT_OK);

        Log.d("debug","startActivityForResult()");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //画像表示(撮影してすぐ)
        /**
         if (requestCode == RESULT_CAMERA) {
         Bitmap bitmap;
         // cancelしたケースも含む
         if( data.getExtras() == null){
         Log.d("debug","cancel ?");
         return;
         }
         else{
         bitmap = (Bitmap) data.getExtras().get("data");
         if(bitmap != null){
         // 画像サイズを計測
         int bmpWidth = bitmap.getWidth();
         int bmpHeight = bitmap.getHeight();
         Log.d("debug",String.format("w= %d",bmpWidth));
         Log.d("debug",String.format("h= %d",bmpHeight));
         }
         }
         */


        //カメラの処理

        if (requestCode == RESULT_CAMERA) {

            if(cameraUri != null){
                imageView2.setImageURI(cameraUri);

                registerDatabase(cameraFile);
            }
            else{
                Log.d("debug","cameraUri == null");
            }
        }


        if (requestCode == RESULT_CAMERA && resultCode == Activity.RESULT_OK) {

            if (data.getData() != null) {

                ParcelFileDescriptor pfDescriptor = null;
                try {
                    Uri uri = data.getData();

                    pfDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                    if (pfDescriptor != null) {
                        FileDescriptor fileDescriptor = pfDescriptor.getFileDescriptor();
                        Bitmap bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        pfDescriptor.close();
                        imageView.setImageBitmap(bmp);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pfDescriptor != null) {
                            pfDescriptor.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }


    //以下の関数10/2追加
    // アンドロイドのデータベースへ登録する
    private void registerDatabase(File file) {
        ContentValues contentValues = new ContentValues();
        ContentResolver contentResolver = CompareActivity.this.getContentResolver();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put("_data", file.getAbsolutePath());
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    }



    // Runtime Permission check
    private void checkPermission(){
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                PackageManager.PERMISSION_GRANTED){
            cameraIntent();
        }
        // 拒否していた場合
        else{
            requestPermission();
        }
    }

    // 許可を求める
    private void requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(CompareActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION);

        } else {
            Toast toast = Toast.makeText(this,
                    "許可されないとアプリが実行できません",
                    Toast.LENGTH_SHORT);
            toast.show();

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,},
                    REQUEST_PERMISSION);
        }
    }
/*

    // 結果の受け取り
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        Log.d("debug","onRequestPermissionsResult()");

        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraIntent();

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
 */

}