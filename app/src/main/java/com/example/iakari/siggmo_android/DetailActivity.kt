package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Log.d("TAG", "Realmセットアップ開始(DetailActivity)")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了(DetailActivity)")

        // 受け取ったIDをTextViewで表示
        val tapid = intent.getStringExtra("TapID")
        Log.d("TAG", "clear intent")
        val record = quaryById(tapid)
        Log.d("TAG", "clear quaryById method")
        testTextView.text = record.toString()
        Log.d("TAG", "clear textview.text set")
    }

    // 渡されたidからデータベースを検索してレコードを返す
    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }
}
