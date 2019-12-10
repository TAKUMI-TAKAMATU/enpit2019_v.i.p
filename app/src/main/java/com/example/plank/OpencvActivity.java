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
import org.opencv.core.Core;
import org.opencv.core.Point;
import org.opencv.video.BackgroundSubtractorMOG2;
import org.opencv.videoio.VideoCapture;
import org.opencv.video.Video;


public class OpencvActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener {

    // CameraBridgeViewBase は JavaCameraView/NativeCameraView のスーパークラス
    private CameraBridgeViewBase mCameraView;
    private Mat mOutputFrame;
    private VideoCapture   m_videoCapture;

    static {
        System.loadLibrary("opencv_java4");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posenet);
        getPermissionCamera(this);
        Log.d("camera", "get permission");
        // カメラビューのインスタンスを変数にバインド
        mCameraView = findViewById(R.id.camera_view);
        mCameraView.setCameraPermissionGranted();
        // リスナーの設定 (後述)
        mCameraView.setCvCameraViewListener(this);
    }

// ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected)
// public abstract class BaseLoaderCallback implements LoaderCallbackInterface

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            Log.d("tag", "onManagerConnected・status=" + status);
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    Log.d("tag", "読み込みが成功したらカメラプレビューを開始");
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    public static boolean getPermissionCamera(Activity activity){
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
        mCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }


    //カメラビュー開始時の処理
    @Override
    public void onCameraViewStarted(int width, int height) {
        // カメラプレビュー開始時に呼ばれる
    }

    //カメラビュー終了時の処理
    @Override
    public void onCameraViewStopped() {
        // カメラプレビュー終了時に呼ばれる
    }

    //画像処理するメソッド
    @Override
    public Mat onCameraFrame(Mat inputFrame) {


        //フレーム
        Mat dest = new Mat();

        //output
        Mat output = new Mat();

        Imgproc.cvtColor(inputFrame, dest, Imgproc.COLOR_BGR2GRAY);

        BackgroundSubtractorMOG2 mog2 = Video.createBackgroundSubtractorMOG2();

        mog2.apply(dest,output,-1);

        //mog2.apply(dest,output,-1);

        return output;


    }

}