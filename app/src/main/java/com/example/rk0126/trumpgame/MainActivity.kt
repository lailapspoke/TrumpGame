package com.example.rk0126.trumpgame

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        newGameStart.setOnClickListener { startGameActivity(100) }
        loadGameStart.setOnClickListener { startGameActivity(255)  }
    }

    private fun startGameActivity(money: Int){
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra(PLAYER_MONEY, money)
        startActivity(intent)
    }
    companion object Factory {
        val PLAYER_MONEY = "player_money"
    }
}
