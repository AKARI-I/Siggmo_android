package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
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

        Log.d("activity", "start DetailActivity")

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

        // レコードが返されたら曲名を表示
        if (record != null) {
            music_name.text = record.music_name
            music_phonetic.text = record.music_phonetic
            singer_name.text = record.singer_name
            singer_phonetic.text = record.singer_phonetic
            first_line.text = record.first_line
            proper_key.text = record.proper_key.toString()
            movie_link.text = record.movie_link
            score.text = record.score.toString()
            free_memo.text = record.free_memo
            debugText.text = record.score_id
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
        Log.d("activity", "finish DetailActivity")
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
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }
}
