package com.example.rk0126.trumpgame

import android.util.Log

interface TrumpGame {
    fun makeDeck() //デッキ生成
    fun makeHand(deck: MutableList<Trump>) //手札生成
    fun changeHand(hand: MutableList<Trump>) //手札交換
    fun cardSelect(card: Trump) //交換手札選択
    fun prizeCheck(hand: MutableList<Trump>) //役判定
}