package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.Button
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_detail.*

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
        val s_record = quaryByScore(record!!.score_id)

        // レコードが返されたら曲名を表示
        if (s_record != null) {
            music_name.text      = record.music_name
            music_phonetic.text  = record.music_phonetic
            singer_name.text     = record.singer_name
            singer_phonetic.text = record.singer_phonetic
            first_line.text      = record.first_line
            singing_level.text   = record.singing_level.toString()
            proper_key.text      = record.proper_key
            movie_link.text      = record.movie_link
            score.text           = s_record.score.toString()
            free_memo.text       = record.free_memo
            last_update.text     = s_record.reg_data
        }
        /*------------------- dialog --------------------*/
        // 選択した曲IDと一致する採点結果を取得
        val getData = mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id",tapid )
                .findAll()
        var maxScore = 0F            // 最高得点
        var sum = 0F                 // 合計値
        // 点数レコードを回す
        getData.forEach{
            if(maxScore < it.score)
            sum += it.score
            maxScore = it.score
        }
        var count = getData.count() // 歌った回数
        var averageScore = sum / count  // 平均点

        score_detail.setOnClickListener {
            val detail = AlertDialog.Builder(this@DetailActivity)
            detail.setTitle("点数詳細")
            detail.setMessage("・最高得点\n" +  maxScore  +   "点\n" +
                    "・平均点\n" +  averageScore  +  "点\n" +
                    "・歌った回数\n" +  count  +  "回\n"
            )
            detail.setPositiveButton("OK" , null).show()
        }


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

    // 渡されたidからデータベースを検索して曲の情報を返す
    // select * from SiggmoDB where id = idと同じ意味
    fun quaryById(id: String): SiggmoDB? {
        Log.d("TAG", "quaryById(DetailActivity)")
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    // scoreを参照する
    fun quaryByScore(s_id: String): ScoreResultDB? {
        Log.d("TAG", "quaryByScore(DetailActivity)")
        return mRealm.where(ScoreResultDB::class.java)
                .equalTo("score_id", s_id)
                .findFirst()
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }
}
