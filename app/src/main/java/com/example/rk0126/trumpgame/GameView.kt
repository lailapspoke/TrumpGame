package com.example.rk0126.trumpgame

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback, Runnable {
    //System
    internal var mHolder: SurfaceHolder = holder
    internal var mThread: Thread? = null
    val VIEW_WIDTH = 600
    val VIEW_HEIGHT = 900
    var scale: Float = 0.0f

    //ゲーム変数
    val CARDS1 = 52 //基本カード枚数(13*4)
    val RANKS = 13 //数字
    val SUITS = 4 //マーク
    val CARDS2 = 4 //基本カード枚数(裏およびJOKER)
    val STATE = GameState.TITLE //状態遷移

    val deck = mutableListOf<Trump>() //山札
    val trumpImage1: Array<Bitmap?> = arrayOfNulls(CARDS1) //1～13のイメージ作成
    val trumpImage2: Array<Bitmap?> = arrayOfNulls(CARDS2) //裏面およびJOKERのイメージ作成

    //ゲーム中状態遷移
    enum class GameState(val rawValue: Int)  {
        TITLE(0),
        START(1),
        SELECT(2),
        CHANGE(3),
        FINISH(4),
        GAMEOVER(5)
    }

    //初期化
    init {
        mHolder.addCallback(this)

        for(i in 1..RANKS){
            val rank = i.format(i)
            Log.d("Test", rank)
            deck.add(Trump("c", i, readBitmap(context, "c" + rank)))
            deck.add(Trump("d", i, readBitmap(context, "d" + rank)))
            deck.add(Trump("h", i, readBitmap(context, "h" + rank)))
            deck.add(Trump("s", i, readBitmap(context, "s" + rank)))
        }
        trumpImage2[0] = readBitmap(context, "z01")
        trumpImage2[1] = readBitmap(context, "z02")
        trumpImage2[2] = readBitmap(context, "x01")
        trumpImage2[3] = readBitmap(context, "x02")

    }

    override fun run() {
        while (mThread != null) {

            val cv = holder.lockCanvas()
            /* 描画ここから */
            val p = Paint()
            cv.translate((getWidth() - VIEW_WIDTH)/2 * scale, (getHeight() - VIEW_HEIGHT)/2 * scale);
            cv.drawColor(Color.rgb(0, 127, 0))
            cv.drawBitmap(deck[0].image, 100f, 150f, p)
            /* 描画ここまで */
            holder.unlockCanvasAndPost(cv)

            try {
                Thread.sleep(FRAME_TIME) //フレーム時間だけ停止
            } catch (e: Exception) {
            }

        }

    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        mThread = Thread(this)
        mThread!!.start()
        val scaleX = (width / VIEW_WIDTH).toFloat()
        val scaleY = (height / VIEW_HEIGHT).toFloat()
        scale = if (scaleX > scaleY) scaleY else scaleX
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        mThread = null
    }

    //fps設定
    companion object {
        internal val FPS: Long = 20
        internal val FRAME_TIME = 1000 / FPS
    }



    //ビットマップ読み込み用
    private fun readBitmap(context: Context, name: String): Bitmap {
        val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
        return BitmapFactory.decodeResource(context.resources, resId)
    }
    //ビットマップ用ゼロ埋め
    private fun Int.format(len: Int) = java.lang.String.format("%02d", this)

}