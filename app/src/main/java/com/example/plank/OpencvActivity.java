package com.example.plank;

//androidのモジュール
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;

//opencvのモジュール
import org.opencv.android.OpenCVLoader;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;



public class OpencvActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    private CameraBridgeViewBase m_cameraView;
    private Mat mOutputFrame;

    static {
        System.loadLibrary("opencv_java4");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opencv);

        //OpenCVLoader.initDebug();
        getPermissionCamera(this);
        Log.d("camera","get permission");

        // カメラビューのインスタンスを変数にバインド
        m_cameraView = findViewById(R.id.camera_view);

        m_cameraView.setCameraPermissionGranted();

        // リスナーの設定 (後述)
        m_cameraView.setCvCameraViewListener(this);


        //m_cameraView.enableView();
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            Log.d("tag","onManagerConnected・status="+status);
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    Log.d("tag","読み込みが成功したらカメラプレビューを開始");
                    m_cameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public static boolean getPermissionCamera(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{Manifest.permission.CAMERA};
            ActivityCompat.requestPermissions(activity, permissions, 0);
            return false;
        } else {
            return true;
        }
    }

    //必須
    @Override
    protected void onResume() {
        super.onResume();
        // 非同期でライブラリの読み込み/初期化を行う
        if (!OpenCVLoader.initDebug()) {
            Log.d("onResume", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("onResume", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        m_cameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_cameraView != null) {
            m_cameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {}

    @Override
    public void onCameraViewStopped() {}

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        Mat dest1 = new Mat();
        Mat dest2 = new Mat();
        Imgproc.cvtColor(inputFrame, dest1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(inputFrame, dest2, Imgproc.COLOR_BGR2GRAY);

        return dest1;
    }


}



