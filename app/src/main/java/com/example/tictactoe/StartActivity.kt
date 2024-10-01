package com.example.tictactoe

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val humanButton: Button = findViewById(R.id.button_human)
        val machineButton: Button = findViewById(R.id.button_machine)

        // Set up click listeners
        humanButton.setOnClickListener {
            startGame(true) // Human starts
        }

        machineButton.setOnClickListener {
            startGame(false) // Machine starts
        }
    }

    private fun startGame(isHumanStarting: Boolean) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("isHumanStarting", isHumanStarting)
        startActivity(intent)
    }
}
