package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var btnQuery: Button
    private val gson = Gson()  // Move Gson instance to a field for reusability

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Apply insets handling using system-defined attributes
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Bind the button and set the click listener
        btnQuery = findViewById(R.id.btnQuery)
        btnQuery.setOnClickListener {
            btnQuery.isEnabled = false  // Disable button to prevent multiple clicks
            setRequest()  // Make the API request
        }
    }

    // Send the API request
    private fun setRequest() {
        val url = "https://api.italkutalk.com/api/air"
        val req = Request.Builder()
            .url(url)
            .build()

        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val myObject = gson.fromJson(json, MyObject::class.java)  // Using the field-level Gson instance
                showDialog(myObject)
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    btnQuery.isEnabled = true  // Re-enable button on failure
                    Toast.makeText(this@MainActivity, "查詢失敗: $e", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Show result in a dialog
    private fun showDialog(myObject: MyObject) {
        val items = mutableListOf<String>()
        myObject.result.records.forEach { data ->
            items.add("地區：${data.SiteName}, 狀態：${data.Status}")
        }

        runOnUiThread {
            btnQuery.isEnabled = true  // Re-enable button after displaying results
            AlertDialog.Builder(this@MainActivity)
                .setTitle("臺北市空氣品質")
                .setItems(items.toTypedArray(), null)
                .show()
        }
    }
}
