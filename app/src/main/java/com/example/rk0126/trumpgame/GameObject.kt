package com.example.rk0126.trumpgame

/*
 * ゲームオブジェクト
 * ゲーム内オブジェクトの親クラスとして，座標やサイズを管理．
 */

open class GameObject {
    var px: Float //X座標
    var py: Float //Y座標
    var width: Int  //幅
    var height: Int //高さ

    constructor(){
        this.px = 0.0f
        this.py = 0.0f
        this.width = 0
        this.height = 0
    }

    constructor(px: Float, py: Float, width: Int, height: Int){
        this.px = px
        this.py = py
        this.width = width
        this.height = height
    }

    fun setPos(px: Float, py: Float){
        this.px = px
        this.py = py
    }

    fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
    }

}