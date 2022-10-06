package com.kendev.abscan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.GridLayout
import android.widget.TextView
import com.kendev.abscan.others.SharedVariables

class presentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_present)
        supportActionBar?.hide()

        val text_class_name = findViewById<TextView>(R.id.text_class_name)
//        text_class_name.setText(SharedVariables().class_name)

        var extras = intent.getStringExtra("className")
        text_class_name.setText(extras)
        val button_back_to_main = findViewById<GridLayout>(R.id.button_back_to_main)
            button_back_to_main.setOnClickListener(){
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

    }
}