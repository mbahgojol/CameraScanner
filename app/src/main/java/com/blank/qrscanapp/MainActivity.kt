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

        showCamera()
    }

    private fun showCamera() {
        CameraScanner.Builder(this).addOnSuccessListener {
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Log.e("Error", it.message.toString())
        }.showCamera(binding.preview)
//        binding.preview.showCamera(this)
    }
}