package com.example.rk0126.trumpgame

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_game.*

class GameActivity : AppCompatActivity() {

    var money: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        money = intent.getIntExtra(MainActivity.PLAYER_MONEY, 0)

        //requestWindowFeature(Window.FEATURE_NO_TITLE)
        //window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(GameView(this))
    }

    fun moneyOverWrite(money: Int){
        this.money = money
    }

}