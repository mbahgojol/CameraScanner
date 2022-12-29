package com.blank.qrscanapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.blank.qrscanapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestPermission()
    }

    private fun showCamera() {
        CameraScanner.Builder(this).addOnSuccessListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Log.e("Error", it.message.toString())
        }.showCamera(binding.preview)
//        binding.preview.showCamera(this)
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showCamera()
        } else {
            Toast.makeText(
                this, "Camera Cannot be open because you don't allow permission", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun requestPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA
            ) -> {
                showCamera()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }
}