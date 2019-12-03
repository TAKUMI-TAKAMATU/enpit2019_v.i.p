package com.example.plank


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_compare.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.TimeUnit

// パーミッションを要求するときのリクエストコード番号です
// 複数のContextからパーミッションが要求された時にどこから要求されたかを区別するために使います
private const val REQUEST_CODE_PERMISSIONS = 10

// 要求する全パーミッションの配列
private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class CompareActivity : AppCompatActivity() {
    private var imageView: ImageView? = null    //静止画用
    private lateinit var viewFinder: TextureView    //動画用
    lateinit var file:File                  //保存先
    var capture_done=0                      //キャプチャーボタンを押したかどうか

    //11/17　SoundPool実装
    private lateinit var soundPool: SoundPool
    private var soundOne = 0

    private class LuminosityAnalyzer : ImageAnalysis.Analyzer {
        private var lastAnalyzedTimestamp = 0L

        /*
        * image plane bufferからbyte配列を抽出するHelper
        */
        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // バッファを０にする
            val data = ByteArray(remaining())
            get(data)   // Byte配列にバッファをコピー
            return data // Byte配列を返却
        }

        override fun analyze(image: ImageProxy, rotationDegrees: Int) {
            val currentTimestamp = System.currentTimeMillis()
            //11/23　SoundPool実装
            lateinit var soundPool: SoundPool

            var soundOne = 0
            // 毎秒ごとに平均輝度を計算する
            if (currentTimestamp - lastAnalyzedTimestamp >=
                    TimeUnit.SECONDS.toMillis(1)) {
                // ImageAnalysisはYUV形式なのでimage.planes[0]にはY (輝度) planeが格納されている
                val buffer = image.planes[0].buffer
                // callback objectからimage dataの抽出
                val data = buffer.toByteArray()
                // pixel値の配列にデータを変換
                val pixels = data.map { it.toInt() and 0xFF }
                // imageの平均輝度の計算
                val luma = pixels.average()
                // 輝度のログ表示
                Log.d("CameraXApp", "Average luminosity: $luma")
                // 最後に分析したフレームのタイムスタンプに更新
                lastAnalyzedTimestamp = currentTimestamp

                //メンバ変数を取れない

                //11/23追加
//                if (luma <= 100){
//                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1.0f)
//                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)

        //11/23追加　スペースの関係でアクションバー非表示にしました(´･ω･`)
        val actionBar = supportActionBar
//        actionBar!!.hide()
        //11/23image_viewをドラッグして動かせるようにしたやつ
        var listener = View.OnTouchListener(function = { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                view.y = motionEvent.rawY - view.height/2
                view.x = motionEvent.rawX - view.width/2
            }
            true
        })
        image_view.setOnTouchListener(listener)
        //11/23ここまで

        //11/23　image_viewを半透明にする処理
        image_button.setOnClickListener {
            val image = image_view
            image.setImageAlpha(128)

        }

        imageView = findViewById(R.id.image_view)

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

        //11/17soundpool実装
        val audioAttributes = AudioAttributes.Builder()
                // USAGE_MEDIA
                // USAGE_GAME
                .setUsage(AudioAttributes.USAGE_GAME)
                // CONTENT_TYPE_MUSIC
                // CONTENT_TYPE_SPEECH, etc.
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

        soundPool = SoundPool.Builder()
                .setAudioAttributes(audioAttributes)
                // ストリーム数に応じて
                .setMaxStreams(2)
                .build()

        // one.wav をロードしておく
        soundOne = soundPool.load(this, R.raw.alarm, 1)


        // load が終わったか確認する場合
        soundPool.setOnLoadCompleteListener{ soundPool, sampleId, status ->
            Log.d("debug", "sampleId=$sampleId")
            Log.d("debug", "status=$status")
        }




        //11/11ポッキーの日
        timer_button.setOnClickListener { /**画像班タイマー*/

            object : CountDownTimer(5000,100){
                override fun onFinish() {
                    //終了時の処理
                    count.text = "終了！！！"
                    soundPool.play(soundOne, 1.0f, 1.0f, 0, 0, 1.0f)

                }

                override fun onTick(p0: Long) {
                    // 区切り（0.1秒毎）
                    count.text = "後 ${p0 /1000} 秒"
                }

            }.start()

        }

        /**ギャラリーから写真を表示する処理*/
        val PhotoButton = findViewById<Button>(R.id.select_phote)
        PhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            startActivityForResult(intent, RESULT_CAMERA)
        }



    }
    /**画面下にカメラを起動*/
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
                try {
                    if(capture_done==1){
                        val bmp: Bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                        /**---回転バグ修正のための処理------------*/
                        // 画像の横、縦サイズを取得
                        val imageWidth = bmp.getWidth();
                        val imageHeight = bmp.getHeight();

                        // Matrix インスタンス生成
                        val matrix =Matrix()
                        // 画像中心を基点に90度回転
                        matrix.postRotate(90.toFloat()) // 回転値

                        // 90度回転したBitmap画像を生成
                        val bitmap2 = Bitmap.createBitmap(bmp, 0, 0,
                        imageWidth, imageHeight, matrix, true);

                        imageView!!.setImageBitmap(bitmap2)
                        /**-----------------------------------*/
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            /**キャプチャーボタンを押した時の処理*/
            file = File(externalMediaDirs.first(),
                    "${System.currentTimeMillis()}.jpg")
            imageCapture.takePicture(file,
                    object : ImageCapture.OnImageSavedListener {
                        //失敗した時
                        override fun onError(error: ImageCapture.UseCaseError,
                                             message: String, exc: Throwable?) {
                            val msg = "Photo capture failed: $message"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.e("CameraXApp", msg)
                            exc?.printStackTrace()
                        }
                        //成功した時
                        override fun onImageSaved(file: File) {
                            val msg = "画像が保存されました.\nもう一度押すと\n保存された画像を表示できます"
                            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                            Log.d("CameraXApp", msg)
                        }
                    })

            //registerDatabase(file!!)
            capture_done=1      //キャプチャーボタンが押されたことを意味する
        }

        // 平均輝度を計算するimage analysis pipelineのセットアップ
        val analyzerConfig = ImageAnalysisConfig.Builder().apply {
            // 不具合を防ぐためにワーカースレッドを使う
            val analyzerThread = HandlerThread(
                    "LuminosityAnalysis").apply { start() }
            setCallbackHandler(Handler(analyzerThread.looper))
            // ここではすべての画像を分析するよりも、最新の画像を重視する
            setImageReaderMode(
                    ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
        }.build()

        // image analysis use caseの生成とanalyzerのインスタンス生成
        val analyzerUseCase = ImageAnalysis(analyzerConfig).apply {
            analyzer = LuminosityAnalyzer()
        }

        // use caseをlifecycleにバインドする
        CameraX.bindToLifecycle(this, preview, imageCapture, analyzerUseCase)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        /**ギャラリー*/
        if (requestCode == RESULT_CAMERA) {

            if (data!!.data != null) {

                var pfDescriptor: ParcelFileDescriptor? = null
                try {
                    val uri = data.data

                    pfDescriptor = contentResolver.openFileDescriptor(uri!!, "r")
                    if (pfDescriptor != null) {
                        val fileDescriptor = pfDescriptor.fileDescriptor
                        val bmp = BitmapFactory.decodeFileDescriptor(fileDescriptor)
                        pfDescriptor.close()
                        imageView!!.setImageBitmap(bmp)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                } finally {
                    try {
                        pfDescriptor?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }

            }
        }
    }


    //以下の関数10/2追加
    // アンドロイドのデータベースへ登録する
    private fun registerDatabase(file: File) {
        val contentValues = ContentValues()
        val contentResolver = this@CompareActivity.contentResolver
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        contentValues.put("_data", file.absolutePath)
        contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }

    companion object {

        private val RESULT_CAMERA = 1001
        //private val REQUEST_PERMISSION = 1002//10/2追加
    }

}
