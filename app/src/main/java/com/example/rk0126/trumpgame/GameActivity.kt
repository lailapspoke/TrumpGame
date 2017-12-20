package com.example.rk0126.trumpgame

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        var money = intent.getIntExtra(MainActivity.PLAYER_MONEY, 0)
        playerMoney.text = Integer.toString(money)
    }
}