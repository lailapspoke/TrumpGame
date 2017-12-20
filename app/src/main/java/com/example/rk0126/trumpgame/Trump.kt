package com.example.rk0126.trumpgame

class Trump{
    var suit: String
    var rank: Int
    var visible: Boolean

    constructor(suit: String, rank: Int, visible: Boolean){
        this.suit = suit
        this.rank = rank
        this.visible = visible
    }
}