package com.example.rk0126.trumpgame

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.MotionEvent
import com.example.rk0126.trumpgame.GameView.HandPrize.*
import java.util.*
import android.view.Display
import android.content.Context.WINDOW_SERVICE
import android.view.WindowManager
import android.content.Context.WINDOW_SERVICE





class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable, TrumpGame {

    //System
    private var mHolder: SurfaceHolder = holder
    private var mThread: Thread? = null
    private val VIEW_WIDTH = 600 //ゲーム横幅
    private val VIEW_HEIGHT = 900
    private val wm = context.getSystemService(WINDOW_SERVICE) as WindowManager
    private val disp = wm.defaultDisplay
    private val p = Point()

    //ゲーム定数
    private val RANKS = 13 //数字
    private val SUITS = 4 //マーク
    private val HANDS = 5 //手札枚数
    private val MAX_BET = 5 //最大ベット
    private val HAND_POS_X = 30.0f //手札1枚目X座標
    private val HAND_POS_Y = 400.0f //手札Y座標
    private val HAND_SPACE = 110.0f //手札間隔
    private val BUTTON_POS_X = 30.0f //交換ボタンX座標
    private val BUTTON_POS_Y = 600.0f //交換ボタンY座標
    private val CHANGE_TIMER = 300 //演出用タイマー

    //ゲーム変数
    private var mState = GameState.TITLE //enumによる状態遷移
    private var mPrize = HandPrize.INIT //enumによる役判定管理
    private var mDeck = mutableListOf<Trump>() //山札
    private var mHand = mutableListOf<Trump>() //手札
    private var mMoney = 0
    private var mChange = ChangeButton() //交換ボタン
    private var mResult = mutableListOf<GameObject>() //結果表示
    private var mWinLose = GameObject() //勝敗表示
    private var mPrizeSheet = GameObject() //配当表

    //ゲーム中状態遷移
    enum class GameState(val rawValue: Int)  {
        TITLE(0),
        START(1),
        SELECT(2),
        PRIZE_CHECK(3),
        CHANGE(4),
        FINISH(5),
        GAME_OVER(6)
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
        JACKS_OR_BETTER(9),
        ONE_PAIR(10),
        NO_PAIR(11),
    }

    //初期化
    init {
        disp.getSize(p)
        mHolder.addCallback(this)
        mHolder.setFixedSize(VIEW_WIDTH, VIEW_WIDTH * p.y / p.x)
        Log.d("Size", "$VIEW_WIDTH, ${VIEW_WIDTH * p.y / p.x}")
        mChange.setPos(BUTTON_POS_X, BUTTON_POS_Y)
        mPrizeSheet.setPos(60.0f, 100.0f)
        mPrizeSheet.setSize(480, 270)//画面サイズ取得の準備
    }

    //定数設定
    companion object {
        //FPS設定
        private val FPS: Long = 30
        private val FRAME_TIME = 1000 / FPS
        private var mTimer = 0.0f
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
                    drawBitmap(mPrizeSheet, readBitmap(context, "haitou"), cv)
                }
                GameState.PRIZE_CHECK -> {
                    mChange.change = false
                    (0 until HANDS)
                            .forEach {
                                drawBitmap(mHand[it], cv)
                                if(mHand[it].change) mChange.change = true
                            }
                    drawBitmap(mChange, cv)
                }
                GameState.CHANGE -> {
                    mTimer += FPS
                    Log.d("Timer", mTimer.toString())
                    if(mTimer >= CHANGE_TIMER){
                        mTimer = 0.0f
                        mState = GameState.FINISH
                    }
                    (0 until HANDS)
                            .forEach{ drawBitmap(mHand[it], cv) }
                }
                GameState.FINISH -> {
                    (0 until HANDS)
                            .forEach{ drawBitmap(mHand[it], cv) }
                    drawResult(cv, mPrize)

                }
                GameState.GAME_OVER -> {

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

        val posX: Float = ev.x * VIEW_WIDTH / width
        val posY: Float = ev.y * VIEW_WIDTH * p.y / p.x / height

        if (ev.action == MotionEvent.ACTION_DOWN) {
            Log.d("onClick", "touch=${ev.x} ${ev.y} pos=$posX $posY")

            //タイトル画面
            when (mState) {
                GameState.TITLE -> {
                    makeDeck()
                    makeHand(mDeck)
                    mState = GameState.SELECT
                }
                GameState.START -> {

                }
                GameState.SELECT -> {
                    //カードタッチ時: 交換する/しないの切り替え
                    (0 until HANDS)
                            .filter { posX >= mHand[it].px && posX <= mHand[it].px + mHand[it].width
                                    && posY >= mHand[it].py && posY <= mHand[it].py + mHand[it].height }
                            .forEach {
                                cardSelect(mHand[it]) //選択したカードの交換フラグ切り替え
                                Log.d("onClick", "カード選択：${mHand[it].suit}${mHand[it].rank} ${mHand[it].change}")
                            }
                    //交換ボタンタッチ時: 交換処理へ移行
                    if(posX >= mChange.px && posX <= mChange.px + mChange.width
                            && posY >= mChange.py && posY <= mChange.py + mChange.height){
                        changeHand(mHand)
                        mState = GameState.CHANGE
                    }

                }
                GameState.PRIZE_CHECK -> {
                    mState = GameState.SELECT
                }
                GameState.CHANGE -> {

                }
                GameState.FINISH -> {
                    mPrize = INIT
                }
                GameState.GAME_OVER -> {

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
        prizeCheck(hand)
        Log.d("PRIZE", mPrize.toString())
    }

    //交換手札選択
    override fun cardSelect(card: Trump) {
        card.change = !card.change //交換フラグON/OFF
    }

    //役判定
    override fun prizeCheck(hand: MutableList<Trump>){
        val suits = mutableMapOf("c" to 0, "d" to 0, "h" to 0, "s" to 0)
        val ranks = mutableMapOf<Int, Int>()
        for(i in 1..13){ranks.put(i, 0)}
        var cnt = 0 //ストレート判定用
        var jacks = false //ジャックスオアベター判定
        //手札集計
        for(i in hand){
            suits.put(i.suit, suits[i.suit]!!+1)
            ranks.put(i.rank, ranks[i.rank]!!+1)
        }
        /*
         * 今回の役判定手順
         * ・まずは記号をチェックしフラッシュかどうか判定する．
         * ・次に数字をチェックし，特定の数字が1枚の場合のみストレートになるかどうかを判定．
         * 　　・5回のループで連番になることを確認できたらストレートのフラグを付与．
         * ・以降，もしフラッシュならストレートフラッシュorロイヤルの判定のみ行う
         * 　　・もしストレートが10JQKAの組み合わせならロイヤルを付与
         * 　　・そうでない場合はストレートフラッシュ
         * ・そうでない場合，スリーカードもしくはフォーカードの判定を行い，さらにフルハウスかどうか判定．
         * 　　・3枚以上でスリーカードorフォーカードorフルハウス判定
         * 　　・スリーカード時にペア判定もしくはペア時にスリーカード判定でフルハウス
         * 　　・ワンペア時にさらにペアでツーペア
         * 　　・ワンペア時1or11以上ならジャックスオアベター
         * ・どれにも当てはまらなかったら残念！
         */
        for(i in suits){
            if(i.value == 5){
                mPrize = FLUSH
            }
        }
        for(i in ranks){
            if(i.value == 1) {
                cnt++
            }
            else cnt = 0
            if(mPrize == FLUSH){
                if(i.key == RANKS && cnt == 4){
                    if(ranks[1] == 1) mPrize = ROYAL
                }else if(cnt == 5){
                    mPrize = STRAIGHT_FLUSH
                }
            }else{
                //スリーカードorフォーカードの判定
                if(i.value >= 3){
                    mPrize = if(i.value >= 4){
                        FOUR_CARD
                    } else {
                        //フルハウスの判定
                        if(mPrize == ONE_PAIR) FULLHOUSE else THREE_CARD
                    }
                }else if(i.value == 2){
                    //ワンペアorツーペアorフルハウスの判定
                    if(i.key == 1 || i.key >= 11) jacks = true
                    mPrize = if(mPrize == ONE_PAIR) TWO_PAIR
                    else if(mPrize == THREE_CARD) FULLHOUSE
                    else ONE_PAIR
                }
                if(cnt == 5 || (i.key == RANKS && cnt == 4)) mPrize = STRAIGHT
            }
        }
        if(mPrize == ONE_PAIR && jacks) mPrize = JACKS_OR_BETTER
        if(mPrize == INIT) mPrize = NO_PAIR
        setResult(mPrize)
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

    //結果描画
    private fun setResult(prize: HandPrize){
        mResult.clear()
        mWinLose.setPos(100.0f, 350.0f)
        mWinLose.setSize(400, 400)
        mResult.add(GameObject(0.0f, 100.0f, 600, 150))
        when(prize){
            ROYAL -> {
                mResult[0].setPos(0.0f, 0.0f)
                mResult.add(GameObject(0.0f, mResult[0].py + 150, 600, 150))
                mResult.add(GameObject(0.0f, mResult[1].py + 150, 600, 150))
            }
            STRAIGHT_FLUSH -> {
                mResult.add(GameObject(0.0f, mResult[0].py + 150, 600, 150))
            }
            FOUR_CARD -> {
            }
            FULLHOUSE -> {
            }
            FLUSH -> {
            }
            STRAIGHT -> {
            }
            THREE_CARD -> {
            }
            TWO_PAIR -> {
            }
            JACKS_OR_BETTER -> {
                mResult.add(GameObject(0.0f, mResult[0].py + 150, 600, 150))
            }
            else -> {
            }
        }
    }
    //結果描画
    private fun drawResult(cv: Canvas, prize: HandPrize){
        when(prize){
            INIT -> {
                mState = GameState.TITLE
            }
            ROYAL -> {
                drawBitmap(mResult[0], readBitmap(context,"royal"), cv)
                drawBitmap(mResult[1], readBitmap(context,"straight"), cv)
                drawBitmap(mResult[2], readBitmap(context,"flush"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            STRAIGHT_FLUSH -> {
                drawBitmap(mResult[0], readBitmap(context,"straight"), cv)
                drawBitmap(mResult[1], readBitmap(context,"flush"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            FOUR_CARD -> {
                drawBitmap(mResult[0], readBitmap(context,"fourcard"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            FULLHOUSE -> {
                drawBitmap(mResult[0], readBitmap(context,"fullhouse"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            FLUSH -> {
                drawBitmap(mResult[0], readBitmap(context,"flush"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            STRAIGHT -> {
                drawBitmap(mResult[0], readBitmap(context,"straight"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            THREE_CARD -> {
                drawBitmap(mResult[0], readBitmap(context,"threecard"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            TWO_PAIR -> {
                drawBitmap(mResult[0], readBitmap(context,"twopair"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            JACKS_OR_BETTER -> {
                drawBitmap(mResult[0], readBitmap(context,"jacks1"), cv)
                drawBitmap(mResult[1], readBitmap(context,"jacks2"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_win_boy"), cv)
            }
            else -> {
                drawBitmap(mResult[0], readBitmap(context,"nopair"), cv)
                drawBitmap(mWinLose, readBitmap(context,"pose_lose_boy"), cv)
            }
        }
    }

}