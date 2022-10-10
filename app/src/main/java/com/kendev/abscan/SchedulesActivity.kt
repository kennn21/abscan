package com.kendev.abscan

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SchedulesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedules)
        supportActionBar?.hide()
    }
}