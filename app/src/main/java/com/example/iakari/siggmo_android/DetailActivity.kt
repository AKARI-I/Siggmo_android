package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
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

        /*-------------------- Realm --------------------*/
        Log.d("TAG", "Realmセットアップ開始(DetailActivity)")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了(DetailActivity)")

        // 受け取ったIDをTextViewで表示
        val tapid = intent.getStringExtra("TapID")
        // idで検索をかけて、その曲の情報がrecordに入る(はず)
        val record = quaryById(tapid)

        // レコードが返されたら曲名を表示
        // ToDo:多分、他の項目もrecord.music_phoneticって感じに参照できるはずなので他の項目も表示できるようにしてみて
        if (record != null) {
            music_name.text = record.music_name
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

    }

    // 渡されたidからデータベースを検索して曲の情報を返す
    // select * from SiggmoDB where id = idと同じ意味
    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }
}
