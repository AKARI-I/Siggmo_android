package com.example.iakari.siggmo_android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.activity_edit.*


class EditActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
/*
        // Realmのセットアップ
        Log.d("TAG", "Realmセットアップ開始")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了")

        val tapid = intent.getStringExtra("TapID")
        val record = quaryById(tapid)

        if (record != null) {
            music_name.text = record.music_name
            music_phonetic.text = record.music_phonetic
            singer_name.text = record.singer_name
            singer_phonetic.text = record.singer_phonetic
            first_line.text = record.first_line
            //proper_key.text = record.proper_key
            movie_link.text = record.movie_link
            //score. = record.score
            free_memo.text = record.free_memo

        }
        */
    }


    /*
    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }
    */

}