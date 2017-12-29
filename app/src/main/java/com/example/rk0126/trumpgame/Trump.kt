package com.example.rk0126.trumpgame

/*
 * トランプ
 * 親クラス: GameObject
 * suit: 記号
 * rank: 数字
 */

class Trump(var suit: String, var rank: Int)  : GameObject() {
    var visible: Boolean = true //オモテウラ：trueのときに表
    var change: Boolean = false //交換フラグ：trueのときに交換

    init {
        this.visible = true
        this.change = false
        this.width = 100
        this.height = 150
    }

}