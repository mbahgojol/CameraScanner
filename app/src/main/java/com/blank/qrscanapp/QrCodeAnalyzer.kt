package com.blank.qrscanapp

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QrCodeAnalyzer(
    private val successListener: (String) -> Unit, private val failure: (Exception) -> Unit
) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        val img = image.image
        if (img != null) {
            val inputImage = InputImage.fromMediaImage(img, image.imageInfo.rotationDegrees)
            scanBarcodes(inputImage) {
                image.close()
            }
        }
    }

    private fun scanBarcodes(image: InputImage, completeListener: () -> Unit) {
        val options = BarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC
        ).build()

        val scanner = BarcodeScanning.getClient(options)
        scanner.process(image).addOnSuccessListener { barcodes ->
            for (barcode in barcodes) {
                val rawValue = barcode.rawValue
                val valueType = barcode.valueType
                Log.d("Raw", rawValue.toString())
                successListener.invoke(rawValue.toString())

                when (valueType) {
                    Barcode.TYPE_URL -> {
                        val title = barcode.url!!.title
                        val url = barcode.url!!.url
                    }
                }
            }
        }.addOnFailureListener {
            failure.invoke(it)
        }.addOnCompleteListener {
            completeListener.invoke()
        }
    }
}