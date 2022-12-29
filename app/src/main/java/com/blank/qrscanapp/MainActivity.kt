package com.blank.qrscanapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blank.qrscanapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val cameraScanner = CameraScanner.Builder(this).addOnSuccessListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Log.e("Error", it.message.toString())
        }.addOnPermissionFailureListener {

        }.showCamera(binding.preview)

        binding.btnFlash.setOnClickListener {
            cameraScanner.setFlash(!cameraScanner.isFlashOn)
        }
    }

    private fun showCamera() {
//        binding.preview.showCamera(this)
    }
}