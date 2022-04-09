package com.neo.request

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neo.request.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = "https://dog.ceo/api/breeds/image/random"

        val builder = Request.Builder(Request.Method.GET, url)

        val request = builder.build()

        request.then {
            when (it) {
                is Response.Success -> {
                    binding.helloWord.text = it.body ?: it.errorBody
                }
                is Response.Failure -> {
                    binding.helloWord.text = it.exception.message
                }
            }
        }
    }
}