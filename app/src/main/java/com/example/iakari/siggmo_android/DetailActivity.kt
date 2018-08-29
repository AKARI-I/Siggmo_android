package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import android.widget.EditText
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_detail.*
import java.lang.String.format
import java.util.*

class DetailActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        Log.d("TAG", "start DetailActivity")

        /*-------------------- Realm --------------------*/
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        // 受け取ったIDをTextViewで表示
        val tapid = intent.getStringExtra("TapID")
        // idから曲の情報を取得
        val record = quaryById(tapid)
        val s_record = readScore(record!!.id)

        /*s_record.forEach {
            Log.d("s_record", "\nscore = ${it.score}\nreq_date${it.reg_data}")
        }
        Log.d("s_record", "\n===============================\n")*/

        // レコードが返されたら曲名を表示
        //if (s_record != null) {
            music_name.text      = record.music_name
            music_phonetic.text  = record.music_phonetic
            singer_name.text     = record.singer_name
            singer_phonetic.text = record.singer_phonetic
            first_line.text      = record.first_line
            singing_level.text   = record.singing_level.toString()
            proper_key.text      = record.proper_key
            movie_link.text      = record.movie_link
            score.text           = checkScore(s_record.max("score") as Float?).toString()
            free_memo.text       = record.free_memo
            last_update.text     = s_record.last()!!.reg_data
        //}
        Log.d("music_count", "read ${record.music_count}")
        score_detail.setOnClickListener {dialogRun(tapid, record.music_count)}

        /*------------------- Button --------------------*/
        val button: Button = findViewById(R.id.send_button)
        button.setOnClickListener {
            //新しく開くアクティビティに渡す値
            val intent: Intent = Intent(this, EditActivity::class.java)
            intent.putExtra("TapID",tapid)

            //新しくアクティビティを開く
            startActivity(intent)
        }
        Log.d("TAG", "finish DetailActivity")
    }

    // 標準Backkeyの遷移先変更
    override fun onKeyDown(keyCode: Int,event: KeyEvent?): Boolean{
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            val intent: Intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            return true
        }
        return false
    }

    fun dialogRun(tapid: String, count: Int){
        // 選択した曲IDと一致する採点結果を取得
        val getData = readScore(tapid)

        val detail = AlertDialog.Builder(this@DetailActivity)
        detail.setTitle("点数詳細")
        detail.setMessage("・最高得点\n" +  checkScore(getData.max("score") as Float?)  +   "点\n" +
                "・平均点\n" +  format("%.1f",(getData.average("score")))  +  "点\n" +
                "・歌った回数\n" +  count  +  "回\n" +
                "\n\nスコアを追加"
        )

        val editView = EditText(this@DetailActivity)
        // editViewの小数入力の強制
        editView.inputType = InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_FLAG_DECIMAL
        detail.setView(editView)
        detail.setPositiveButton("OK"){ _, _ ->
            if (editView.text != null && !editView.text.toString().isEmpty()){
                val score = editView.text.toString().toFloat()
                if (score in 0.0..100.0) { // scoreの範囲チェック
                    if (count < 100) {
                        saveScore(tapid, score)
                    } else {
                        // ScoreResultDBの数が100を超えると古いものから削除
                        val results: RealmResults<ScoreResultDB> = mRealm.where(ScoreResultDB::class.java)
                                .equalTo("score_id", getData[0]!!.score_id)
                                .findAll()
                        mRealm.executeTransaction(Realm.Transaction {
                            Log.d("TAG", "in realm delete process")
                            results.deleteFromRealm(0)
                            results.deleteLastFromRealm()
                        })
                        saveScore(tapid, score)
                    }
                }else{
                    editView.error = "1~100の数字を入力してください"
                }
            }
        }
        detail.setNegativeButton("キャンセル"){ _, _ ->

        }

        detail.show()
    }

    // 渡されたidからデータベースを検索して曲の情報を返す
    // select * from SiggmoDB where id = idと同じ意味
    fun quaryById(id: String): SiggmoDB? {
        Log.d("TAG", "quaryById(DetailActivity)")
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    fun checkScore(score: Float?): Float{
        return score as? Float ?: 0.0F
    }

    // scoreを参照する
    fun quaryByScore(m_id: String): ScoreResultDB? {
        Log.d("TAG", "quaryByScore(DetailActivity)")
        return mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id", m_id)
                .findFirst()
    }

    // Scoreを日付の古い順に取ってくる
    fun readScore(id: String) : RealmResults<ScoreResultDB> {
        return mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id", id)
                .findAll().sort("reg_data")
    }
    fun saveScore(musicId:String, score:Float){
        mRealm.executeTransaction {
            val siggmoDB = quaryById(musicId)
            val scoreResultDB = mRealm.createObject(ScoreResultDB::class.java, UUID.randomUUID().toString())
            /*-------------------- 時間の取得 --------------------*/
            var calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)          // 年
            val month = calendar.get(Calendar.MONTH) + 1      // 月
            val day = calendar.get(Calendar.DAY_OF_MONTH)   // 日
            val hour = calendar.get(Calendar.HOUR_OF_DAY)   // 時
            val minute = calendar.get(Calendar.MINUTE)      // 分
            val second = calendar.get(Calendar.SECOND)      // 秒

            val date = "$year/$month/$day/$hour:$minute:$second"    // 年/月/日/時:分:秒

            Log.d("music_count", "${siggmoDB!!.music_count}")
            siggmoDB!!.music_count = siggmoDB!!.music_count + 1
            scoreResultDB.music_id = musicId
            scoreResultDB.score = score
            scoreResultDB.reg_data = date
            mRealm.copyToRealm(scoreResultDB)
        }
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }
}
