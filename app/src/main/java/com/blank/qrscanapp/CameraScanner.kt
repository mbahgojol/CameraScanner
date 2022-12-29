package com.blank.qrscanapp

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat


class CameraScanner(private val builder: Builder) {
    class Builder(activity: AppCompatActivity) {
        var myCameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            private set
        var myActivity: AppCompatActivity? = null
            private set
        var mySuccessListener: (String) -> Unit = {}
            private set
        var myErrorListener: (Exception) -> Unit = {}
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

        fun setCameraSelector(cameraSelector: CameraSelector) = apply {
            myCameraSelector = cameraSelector
        }

        fun showCamera(previewView: PreviewView) = CameraScanner(this).apply {
            showCamera(previewView)
        }
    }

    private fun showCamera(previewView: PreviewView) {
        builder.myActivity?.let { activity ->
            val preview =
                Preview.Builder().build().apply { setSurfaceProvider(previewView.surfaceProvider) }

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
                    cameraProvider.bindToLifecycle(
                        activity, builder.myCameraSelector, preview, imageAnalyzer
                    )
                }, ContextCompat.getMainExecutor(activity))
            } catch (exc: Exception) {
                Toast.makeText(
                    activity, "Failed open camera", Toast.LENGTH_SHORT
                ).show()
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