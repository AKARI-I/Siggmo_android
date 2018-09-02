package com.example.iakari.siggmo_android

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_detail.*
import java.lang.String.format
import java.util.*

class DetailActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm

    // ここでActivityが初めて生成される, 初期化は全てここに書く
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        /*-------------------- Realm --------------------*/
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    override fun onResume() {
        super.onResume()

        // idの受け取り
        val tapid = intent.getStringExtra("TapID")

        setDetail(tapid)     // データを表示

        /*------------------- Button --------------------*/
        val button: Button = findViewById(R.id.send_button)
        button.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            intent.putExtra("TapID",tapid)

            startActivity(intent)
        }
    }

    // 標準Backkeyの遷移先変更
    override fun onKeyDown(keyCode: Int,event: KeyEvent?): Boolean{
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            finish()    // DetailActivityの終了
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    private fun setDetail(tapid:String){
        // idから曲の情報を取得
        val record = quaryById(tapid)
        val sRecord = readScore(record!!.id)

        // レコードが返されたら曲名を表示
        music_name.text      = record.music_name
        music_phonetic.text  = record.music_phonetic
        singer_name.text     = record.singer_name
        singer_phonetic.text = record.singer_phonetic
        first_line.text      = record.first_line
        singing_level.text   = record.singing_level.toString()
        proper_key.text      = record.proper_key
        movie_link.text      = record.movie_link
        score.text           = checkScore(sRecord.max("score") as Float?).toString()
        free_memo.text       = record.free_memo
        last_update.text     = sRecord.last()!!.reg_data

        // スコアダイアログ
        score_detail.setOnClickListener {dialogRun(tapid, record.music_count)}
    }

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    @SuppressLint("DefaultLocale")
    private fun dialogRun(tapid: String, count: Int) {
        // 選択した曲IDと一致する採点結果を取得
        val getData = readScore(tapid)
        val detail = AlertDialog.Builder(this@DetailActivity)
        //val m_dig = detail.show()
        //val buttonOK: Button = m_dig.getButton(DialogInterface.BUTTON_POSITIVE)

        detail.setTitle("点数詳細")
        detail.setMessage("・最高得点\n" + checkScore(getData.max("score") as Float?) + "点\n" +
                "・平均点\n" + format("%.1f", (getData.average("score"))) + "点\n" +
                "・歌った回数\n" + count + "回\n" +
                "\n\nスコアを追加"
        )
        // editViewで点数の追加
        val editView = EditText(this@DetailActivity)
        // editViewの小数入力の強制
        editView.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
        detail.setView(editView)

        //detail.setPositiveButton("OK",null)

        // setPositiveButton() でリスナー指定するとダイアログが閉じる
        // setOnClickListener() でリスナーを指定する
        //buttonOK.setOnClickListener{
        detail.setPositiveButton("OK"){ _, _ ->
            // 入力があった場合
            if (editView.text != null && !editView.text.toString().isEmpty()) {
                val score = editView.text.toString().toFloat()

                // scoreの範囲チェック
                if (score in 0.0..100.0) {
                    if (count < 100) {
                        saveScore(tapid, score)
                    } else {
                        // ScoreResultDBの数が100を超えると古いものから削除
                        val results: RealmResults<ScoreResultDB> = mRealm.where(ScoreResultDB::class.java)
                                .equalTo("score_id", getData[0]!!.score_id)
                                .findAll()
                        mRealm.executeTransaction({
                            results.deleteFromRealm(0)
                            results.deleteLastFromRealm()
                        })
                        saveScore(tapid, score)
                    }
                } else {
                    Toast.makeText(this, "1~100の数字を入力してください", Toast.LENGTH_SHORT).show()
                }
            }
        }
        detail.show()
    }

    // 渡されたidからデータベースを検索して曲の情報を返す
    private fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    private fun checkScore(score: Float?): Float{
        return score ?: 0.0F
    }

    // Scoreを日付の古い順に取ってくる
    private fun readScore(id: String) : RealmResults<ScoreResultDB> {
        return mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id", id)
                .findAll().sort("reg_data")
    }

    // ダイアログからのスコアの追加処理
    private fun saveScore(musicId:String, score:Float){
        mRealm.executeTransaction {
            val siggmoDB = quaryById(musicId)
            val scoreResultDB = mRealm.createObject(ScoreResultDB::class.java, UUID.randomUUID().toString())
            /*-------------------- 時間の取得 --------------------*/
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)          // 年
            val month = calendar.get(Calendar.MONTH) + 1    // 月
            val day = calendar.get(Calendar.DAY_OF_MONTH)   // 日
            val hour = calendar.get(Calendar.HOUR_OF_DAY)   // 時
            val minute = calendar.get(Calendar.MINUTE)      // 分
            val second = calendar.get(Calendar.SECOND)      // 秒

            val date = "$year/$month/$day/$hour:$minute:$second"    // 年/月/日/時:分:秒

            siggmoDB!!.music_count = siggmoDB.music_count + 1
            scoreResultDB.music_id = musicId
            scoreResultDB.score = score
            scoreResultDB.reg_data = date
            mRealm.copyToRealm(scoreResultDB)
        }
    }
}