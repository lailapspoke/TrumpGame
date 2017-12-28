package com.example.rk0126.trumpgame

import android.graphics.Bitmap
import android.graphics.BitmapFactory

/*
 * トランプ
 * 親クラス: GameObject
 * suit: 記号
 * rank: 数字
 */

class Trump(var suit: String, var rank: Int)  : GameObject() {
    var visible: Boolean //オモテウラ：trueのときに表
    var change: Boolean  //交換フラグ：trueのときに交換

    init {
        this.visible = true
        this.change = false
        this.width = 100
        this.height = 150
    }

}