package com.example.plank;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("debug","onCreate()");//10/2追加
        setContentView(R.layout.activity_main);

        if (isFirstTime()) {
            // show dialog
            FragmentManager fragmentManager = getFragmentManager();

            // DialogFragment を継承したAlertDialogFragmentのインスタンス
           AlertDialogFragment dialogFragment = new MainActivity.AlertDialogFragment();
            // DialogFragmentの表示
            dialogFragment.show(fragmentManager, "test alert dialog");
        }

        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageResource(R.drawable.plank01);

        //カメラモードに移動
        Button PhotoButton = findViewById(R.id.ViewImg);
        PhotoButton.setOnClickListener(new View.OnClickListener() {

                public void onClick (View v) {
                   Intent intent = new Intent(getApplication(), ImageActivity.class);
                   startActivity(intent);
                }

        });

        //センサーようの移動ボタン
        Button sendButton_sensor = findViewById(R.id.sensor_button);
        sendButton_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), SensorActivity.class);
                startActivity(intent);
            }
        });

        Button helpButton = findViewById(R.id.helpButton);

        // ボタンタップでAlertを表示させる
        helpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();

                // DialogFragment を継承したAlertDialogFragmentのインスタンス
                MainActivity.AlertDialogFragment dialogFragment = new MainActivity.AlertDialogFragment();
                // DialogFragmentの表示
                dialogFragment.show(fragmentManager, "test alert dialog");
            }
        });

        Button QuestionnaireButton = findViewById(R.id.QuestionnaireButton);
        QuestionnaireButton.setOnClickListener(new View.OnClickListener() {
            public void onClick (View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://forms.gle/Zaq9huVe8jLWLCKG7"));
                startActivity(intent);
            }
        });


    }
    public void setTextView(String message){

    }

    private boolean isFirstTime()
    {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean ranBefore = preferences.getBoolean("RanBefore", false);
        if (!ranBefore) {
            // first time
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("RanBefore", true);
            editor.commit();
        }
        return !ranBefore;
    }

    // DialogFragment を継承したクラス
    public static class AlertDialogFragment extends DialogFragment {
        // 選択肢のリスト
        private String[] menulist = {"選択(1)","選択(2)","選択(3)"};

        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        @NonNull
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            // タイトル
            alert.setTitle("このアプリの使い方");
            alert.setView(R.layout.activity_help_main);
            alert.setPositiveButton( "OK", null );

            return alert.create();
        }
        private void setMassage(String message) {
            MainActivity mainActivity = (MainActivity) getActivity();
            // mainActivity.setTextView(message);
        }
    }

}

