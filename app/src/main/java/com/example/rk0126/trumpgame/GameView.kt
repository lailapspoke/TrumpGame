package com.example.rk0126.trumpgame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.MotionEvent
import java.util.*


class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable, TrumpGame {

    //System
    private var mHolder: SurfaceHolder = holder
    private var mThread: Thread? = null
    val VIEW_WIDTH = 900 //ゲーム横幅
    val VIEW_HEIGHT = 1600 //ゲーム高さ


    //ゲーム変数
    private var mState = GameState.TITLE //enumによる状態遷移
    private var mPrize = HandPrize.INIT //enumによる役判定管理
    private var mDeck = mutableListOf<Trump>() //山札
    private var mHand = mutableListOf<Trump>() //手札
    private var mChange = ChangeButton()

    //ゲーム中状態遷移
    enum class GameState(val rawValue: Int)  {
        TITLE(0),
        START(1),
        SELECT(2),
        CHANGE(3),
        FINISH(4),
        GAMEOVER(5)
    }

    //役
    enum class HandPrize(val rawValue: Int)  {
        INIT(0),
        ROYAL(1),
        STRAIGHT_FLUSH(2),
        FOUR_CARD(3),
        FULLHOUSE(4),
        FLUSH(5),
        STRAIGHT(6),
        THREE_CARD(7),
        TWO_PAIR(8),
        ONE_PAIR(9),
        NO_PAIR(10),
    }

    //初期化
    init {
        mHolder.addCallback(this)
        //mHolder.setFixedSize(VIEW_WIDTH, VIEW_HEIGHT)
        mChange.setPos(BUTTON_POS_X, BUTTON_POS_Y)
    }

    //定数設定
    companion object {
        //FPS設定
        private val FPS: Long = 30
        private val FRAME_TIME = 1000 / FPS

        //ゲーム定数
        private val RANKS = 13 //数字
        private val SUITS = 4 //マーク
        private val HANDS = 5 //手札枚数
        private val HAND_POS_X = 30.0f //手札1枚目X座標
        private val HAND_POS_Y = 400.0f //手札Y座標
        private val HAND_SPACE = 110.0f //手札間隔
        private val BUTTON_POS_X = 30.0f //交換ボタンX座標
        private val BUTTON_POS_Y = 600.0f //交換ボタンY座標
    }

    //surfaceCreated
    override fun surfaceCreated(holder: SurfaceHolder) {
        mThread = Thread(this)
        mThread!!.start()
    }

    //surfaceChanged
    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    //surfaceDestroyed
    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread = null
    }

    //ゲームメイン処理
    override fun run() {
        while (mThread != null) {

            val cv = mHolder.lockCanvas()
            /* 描画ここから */
            cv.drawColor(Color.rgb(0, 127, 0)) //背景
            //以下、ゲーム状態に合わせて描画処理の実装
            when (mState) {
                GameState.TITLE -> {
                    val title  = Bitmap.createScaledBitmap(readBitmap(context, "poker"), 600, 150, false)
                    cv.drawBitmap(title, 0.0f, 0.0f, null)
                }
                GameState.START -> {
                }
                GameState.SELECT -> {
                    mChange.change = false
                    (0 until HANDS)
                            .forEach {
                                drawBitmap(mHand[it], cv)
                                if(mHand[it].change) mChange.change = true
                            }
                    drawBitmap(mChange, cv)
                }
                GameState.CHANGE -> {

                }
                GameState.FINISH -> {
                    (0 until HANDS)
                            .forEach{ drawBitmap(mHand[it], cv) }

                }
                GameState.GAMEOVER -> {

                }
            }
            /* 描画ここまで */
            mHolder.unlockCanvasAndPost(cv)

            try {
                Thread.sleep(FRAME_TIME) //フレーム時間だけ停止
            } catch (e: Exception) {
            }
        }
    }

    //タッチイベント処理
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val dm = Resources.getSystem().displayMetrics //画面サイズ
        val posX: Float = ev.x
        val posY: Float = ev.y

        if (ev.action == MotionEvent.ACTION_DOWN) {
            Log.d("onClick", "touch=${ev.x} ${ev.y} pos=$posX $posY")

            //タイトル画面
            when (mState) {
                GameState.TITLE -> {
                    Log.d("onClick", "ゲーム開始")
                    makeDeck()
                    makeHand(mDeck)
                    mState = GameState.SELECT
                }
                GameState.START -> {

                }
                GameState.SELECT -> {
                    (0 until HANDS)
                            .filter { posX >= mHand[it].px && posX <= mHand[it].px + mHand[it].width
                                    && posY >= mHand[it].py && posY <= mHand[it].py + mHand[it].height }
                            .forEach {
                                cardSelect(mHand[it]) //選択したカードの交換フラグ切り替え
                                Log.d("onClick", "カード選択：${mHand[it].suit}${mHand[it].rank} ${mHand[it].change}")
                            }
                    if(posX >= mChange.px && posX <= mChange.px + mChange.width
                            && posY >= mChange.py && posY <= mChange.py + mChange.height){
                        changeHand(mHand)
                        mState = GameState.FINISH
                    }

                }
                GameState.CHANGE -> {

                }
                GameState.FINISH -> {
                    mPrize = HandPrize.INIT
                    mState = GameState.TITLE
                }
                GameState.GAMEOVER -> {

                }
            }
        }
        return true
    }

    //デッキ生成
    override fun makeDeck(){
        mDeck.clear() //デッキ初期化
        for(i in 1..RANKS){
            //Log.d("Test", rank)
            //クラブ，ダイヤ，ハート，スペードの各数字をセット
            mDeck.add(Trump("c", i))
            mDeck.add(Trump("d", i))
            mDeck.add(Trump("h", i))
            mDeck.add(Trump("s", i))
        }
        Collections.shuffle(mDeck) //作ったデッキをシャッフル
    }

    //手札生成
    override fun makeHand(deck: MutableList<Trump>) {
        mHand.clear() //手札初期化
        for(i in 0 until HANDS) {
            mHand.add(mDeck[0]) //山札からカードをドロー
            mDeck.removeAt(0) //ドローしたカードをデッキから消去
        }
        Log.d("Test", "${mDeck.size}")
        setCardPos()
    }

    //手札位置調整
     private fun setCardPos(){
         for(i in 0 until HANDS) mHand[i].setPos(HAND_POS_X + i * HAND_SPACE, HAND_POS_Y)
     }

    //手札交換
    override fun changeHand(hand: MutableList<Trump>) {
        for(i in 0 until HANDS){
            if(hand[i].change) {
                hand[i] = mDeck[0]
                mDeck.removeAt(0) //ドローしたカードをデッキから消去
            }
        }
        setCardPos()
    }

    //交換手札選択
    override fun cardSelect(card: Trump) {
        card.change = !card.change //交換フラグON/OFF
    }

    //役判定
    override fun prizeCheck(hand: MutableList<Trump>){

    }

    //ビットマップ読み込み用：変数名からリソースIDを取得
    private fun readBitmap(context: Context, name: String): Bitmap {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return BitmapFactory.decodeResource(context.resources, resId)
    }

    //ビットマップ描画
    private fun drawBitmap(obj: GameObject, bmp: Bitmap, cv: Canvas){
        val src = Rect(0, 0, obj.width, obj.height)
        val pos = Rect(obj.px.toInt(), obj.py.toInt(), (obj.px + obj.width).toInt(), (obj.py + obj.height).toInt())
        val drawBmp = Bitmap.createScaledBitmap(bmp, obj.width, obj.height, false)
        cv.drawBitmap(drawBmp, src, pos, null)
    }

    //ビットマップ描画
    @SuppressLint("DefaultLocale")
    private fun drawBitmap(card: Trump, cv: Canvas){
        val bmp =  readBitmap(context, card.suit + java.lang.String.format("%02d", card.rank))
        drawBitmap(card, bmp, cv)
        if(card.change) {
            val p = Paint()
            p.setARGB(127, 80, 80,255)
            cv.drawRect(card.px, card.py, card.px + card.width, card.py + card.height, p)
        }
    }

    //ビットマップ描画
    private fun drawBitmap(button: ChangeButton, cv: Canvas){
        val bmp = if(button.change){
            readBitmap(context, "button1")
        }else{
            readBitmap(context, "button2")
        }
        drawBitmap(button, bmp, cv)
    }


}