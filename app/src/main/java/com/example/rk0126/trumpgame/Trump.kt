package com.example.rk0126.trumpgame

import android.graphics.Bitmap

class Trump(var suit: String, var rank: Int, var image: Bitmap) {
    var px: Int
    var py: Int
    var width: Int
    var height: Int
    var visible: Boolean

    init {
        this.px = 0
        this.py = 0
        this.width = 100
        this.height = 150
        this.visible = true
    }

}