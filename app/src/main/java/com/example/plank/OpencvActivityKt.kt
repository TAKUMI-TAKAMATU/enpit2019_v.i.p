package com.example.plank


import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_compare.*
import org.opencv.android.OpenCVLoader
import org.opencv.video.BackgroundSubtractorMOG2
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit
import kotlin.math.abs


// パーミッションを要求するときのリクエストコード番号です
// 複数のContextからパーミッションが要求された時にどこから要求されたかを区別するために使います
private const val REQUEST_CODE_PERMISSIONS = 10

// 要求する全パーミッションの配列
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class OpencvActivityKt : AppCompatActivity() {
    private lateinit var viewFinder: TextureView    //動画用
    lateinit var file:File                  //保存先
    var capture_done=0                      //キャプチャーボタンを押したかどうか
    var luma  :Double = 0.0
    var luma2 : Double =0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_opencv)

        if(!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "error_openCV")
        }
        viewFinder = findViewById(R.id.view_finder)

        // カメラパーミッションの要求
        if (allPermissionsGranted()) {
            viewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // texture viewが変化した時にLayoutの再計算を行う
        viewFinder.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            updateTransform()
        }

        //11/11ポッキーの日
        timer_button.setOnClickListener { /**画像班タイマー*/

            object : CountDownTimer(5000,100){
                override fun onFinish() {
                    //終了時の処理
                    count.text = "　　　　終了！！！"

                }
                override fun onTick(p0: Long) {
                    // 区切り（0.1秒毎）
                    count.text = "　　　　後 ${p0 /1000} 秒(デモ用)"
                }
            }.start()
        }
    }
    /**カメラを起動*/
    private fun startCamera() {
        // viewfinder use caseのコンフィグレーションオブジェクトを生成
        val previewConfig = PreviewConfig.Builder().apply {
            setTargetAspectRatio(Rational(1, 1))
            setTargetResolution(Size(640, 640))
        }.build()

        // viewfinder use caseの生成
        val preview = Preview(previewConfig)

        // viewfinderが更新されたらLayoutを再計算
        preview.setOnPreviewOutputUpdateListener {

            // SurfaceTextureの更新して再度親Viewに追加する
            val parent = viewFinder.parent as ViewGroup
            parent.removeView(viewFinder)
            parent.addView(viewFinder, 0)

            viewFinder.surfaceTexture = it.surfaceTexture
            updateTransform()
        }

        // image capture use caseのコンフィグレーションオブジェクトを生成
        // Create configuration object for the image capture use case
        val imageCaptureConfig = ImageCaptureConfig.Builder()
                .apply {
                    setTargetAspectRatio(Rational(1, 1))
                    // イメージキャプチャの解像度は設定しない。
                    // 代わりに、アスペクト比と要求されたモードに基づいて
                    // 適切な解像度を推測するキャプチャモードを選択します。
                    setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                }.build()


        // image capture use caseの生成とボタンのClickリスナーの登録
        val imageCapture = ImageCapture(imageCaptureConfig)
        /**キャプチャーボタン*/
        findViewById<ImageButton>(R.id.capture_button).setOnClickListener {
        /**キャプチャーボタンを押した時の処理*/
        capture_done=1      //キャプチャーボタンが押されたことを意味する
        luma2=luma
        val msg = "計測を開始します\n　\n"
        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        }

        var lastAnalyzedTimestamp = 0L
        //https://proandroiddev.com/android-camerax-preview-analyze-capture-1b3f403a9395
        /**フレームごとに画像処理する関数*/
        fun buildImageAnalysisUseCase(): UseCase? {
            /**画像処理するための前準備*/
            val analysisConfig = ImageAnalysisConfig.Builder().apply {
                // 不具合を防ぐためにワーカースレッドを使う
                val analyzerThread = HandlerThread(
                        "LuminosityAnalysis").apply { start() }
                setCallbackHandler(Handler(analyzerThread.looper))
                // ここではすべての画像を分析するよりも、最新の画像を重視する
                setImageReaderMode(
                        ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            }.build()

            val analysis = ImageAnalysis(analysisConfig)
            analysis.setAnalyzer { image , rotationDegrees ->

                //image plane bufferからbyte配列を抽出するHelper
                fun ByteBuffer.toByteArray(): ByteArray {
                    rewind()    // バッファを０にする
                    val data = ByteArray(remaining())
                    get(data)   // Byte配列にバッファをコピー
                    return data // Byte配列を返却
                }

                val currentTimestamp = System.currentTimeMillis()
                if (currentTimestamp - lastAnalyzedTimestamp >=
                        TimeUnit.SECONDS.toMillis(1)) {
                    /**ここから画像処理が始まる．毎秒ごとに平均輝度を計算する------------------------------*/
                    val Threshold=10  //しきい値
                    // ImageAnalysisはYUV形式なのでimage.planes[0]にはY (輝度) planeが格納されている
                    val buffer = image.planes[0].buffer
                    // callback objectからimage dataの抽出
                    val data = buffer.toByteArray()
                    // pixel値の配列にデータを変換
                    val pixels = data.map { it.toInt() and 0xFF }
                    // imageの平均輝度の計算
                    luma = pixels.average()
                    if(abs(luma-luma2)>Threshold &&capture_done==1) {

                        val msg = "画像内の異常を検知しました.\nt秒維持することができました\n"
                        Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                        Log.d("CameraXApp" , "Average luminosity: $luma")
                        capture_done=0

                    }
                    /**ここまでーーーーーーーーーーーーーーーーーーーー----------------------------------*/
                    lastAnalyzedTimestamp = currentTimestamp
                }
             }
            return analysis
         }
        // use caseをlifecycleにバインドする
        CameraX.bindToLifecycle(this , preview , imageCapture , buildImageAnalysisUseCase())
    }

    private fun updateTransform() {
        val matrix = Matrix()

        // view finderの中心の計算
        val centerX = viewFinder.width / 2f
        val centerY = viewFinder.height / 2f

        // 表示回転を考慮したプレビュー出力
        val rotationDegrees = when(viewFinder.display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> return
        }
        matrix.postRotate(-rotationDegrees.toFloat(), centerX, centerY)

        // TextureViewへのセット
        viewFinder.setTransform(matrix)
    }

    /**
     * カメラのパーミッションリクエストダイアログ処理の結果を確認します
     * パーミッションが許可された場合はCameraを開始します
     * そうでない場合はToastを表示して終了します
     */
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                viewFinder.post { startCamera() }
            } else {
                Toast.makeText(this,
                        "パーミッションが許可されませんでした",
                        Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    /**
     * 定義されたパーミッションをチェックします
     */
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
                this, it) == PackageManager.PERMISSION_GRANTED
    }

    /**この関数で動体検知をしたい*/
    fun motion(image: ImageProxy, rotationDegrees: Int){
        val moG2: BackgroundSubtractorMOG2
    }

}
