package com.blank.qrscanapp

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData


class CameraScanner(private val builder: Builder) {
    private val flashLive = MutableLiveData(false)
    fun setFlash(isFlashOn: Boolean) {
        flashLive.value = isFlashOn
    }

    val isFlashOn get() = flashLive.value ?: false

    class Builder(activity: AppCompatActivity) {
        var myCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            private set
        var myActivity: AppCompatActivity? = null
            private set
        var mySuccessListener: (String) -> Unit = {}
            private set
        var myErrorListener: (Exception) -> Unit = {}
            private set
        var myErrorPermissionListener: (Exception) -> Unit = {}
            private set

        init {
            myActivity = activity
        }

        fun addOnSuccessListener(listener: (String) -> Unit) = apply {
            mySuccessListener = listener
        }

        fun addOnFailureListener(listener: (Exception) -> Unit) = apply {
            myErrorListener = listener
        }

        fun addOnPermissionFailureListener(listener: (Exception) -> Unit) = apply {
            myErrorPermissionListener = listener
        }

        fun setCameraSelector(cameraSelector: CameraSelector) = apply {
            myCameraSelector = cameraSelector
        }

        fun showCamera(previewView: PreviewView) = CameraScanner(this).apply {
            showCamera(previewView)
        }
    }

    private fun havePermission(successAccessPermission: () -> Unit) {
        builder.myActivity?.let {
            when (PackageManager.PERMISSION_GRANTED) {
                ContextCompat.checkSelfPermission(
                    it, Manifest.permission.CAMERA
                ) -> {
                    successAccessPermission()
                }
                else -> {
                    builder.myActivity?.registerForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            successAccessPermission()
                        } else {
                            Toast.makeText(
                                builder.myActivity,
                                "Camera Cannot be open because you don't allow permission",
                                Toast.LENGTH_SHORT
                            ).show()
                            builder.myErrorPermissionListener.invoke(Exception("Camera Cannot be open because you don't allow permission"))
                        }
                    }?.launch(
                        Manifest.permission.CAMERA
                    )
                }
            }
        }
    }

    private fun showCamera(previewView: PreviewView) {
        builder.myActivity?.let { activity ->
            havePermission {
                val preview = Preview.Builder().build()
                    .apply { setSurfaceProvider(previewView.surfaceProvider) }

                val cameraExecutor = ContextCompat.getMainExecutor(activity)

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build().also {
                        it.setAnalyzer(
                            cameraExecutor,
                            QrCodeAnalyzer(builder.mySuccessListener, builder.myErrorListener)
                        )
                    }

                try {
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            activity, builder.myCameraSelector, preview, imageAnalyzer
                        )
                        if (camera.cameraInfo.hasFlashUnit()) {
                            flashLive.observe(activity) {
                                camera.cameraControl.enableTorch(it)
                            }
                        } else {
                            Toast.makeText(
                                activity,
                                "Terjadi kesalahan pada flash anda",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }, ContextCompat.getMainExecutor(activity))
                } catch (exc: Exception) {
                    builder.myErrorListener.invoke(exc)
                    Toast.makeText(
                        activity, "Failed open camera", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}

fun PreviewView.showCamera(
    activity: AppCompatActivity,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    successListener: (String) -> Unit = {},
    errorListener: (Exception) -> Unit = {},
) {
    val preview =
        Preview.Builder().build().apply { setSurfaceProvider(this@showCamera.surfaceProvider) }

    val cameraExecutor = ContextCompat.getMainExecutor(activity)

    val imageAnalyzer =
        ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build().also {
                it.setAnalyzer(
                    cameraExecutor, QrCodeAnalyzer(successListener, errorListener)
                )
            }

    try {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                activity, cameraSelector, preview, imageAnalyzer
            )
        }, ContextCompat.getMainExecutor(activity))
    } catch (exc: Exception) {
        Toast.makeText(
            activity, "Gagal memunculkan kamera.", Toast.LENGTH_SHORT
        ).show()
    }
}