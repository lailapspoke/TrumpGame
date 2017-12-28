package com.example.rk0126.trumpgame

/*
 * 交換ボタン
 * 親クラス: GameObject
 * change: 交換フラグの有無チェック，ONなら交換する，OFFなら交換しないを表示する．
 */

class ChangeButton : GameObject() {

    var change: Boolean = false //交換するorしない表示の切り替え
    init {
        this.width = 540
        this.height = 135
    }

}