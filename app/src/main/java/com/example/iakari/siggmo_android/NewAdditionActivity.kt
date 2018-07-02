package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import java.util.*

class NewAdditionActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_addition)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Log.d("TAG", "Realmセットアップ開始(NewAdditionActivity)")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了(NewAdditionActivity)")

        /* 保存ボタンがクリックされたらレコードを追加する */
        // createテスト(テスト用レコードの追加)
        // ※ここではテスト用データを事前に宣言してレコードを作成

        Log.d("TAG", "createメソッド開始")
        create(MName[0], MPhonetic[0], SName[0], SPhonetic[0], FLine[0], PKey[0],
                MLink[0], Score[0], FMemo[0])
        create(MName[1], MPhonetic[1], SName[1], SPhonetic[1], FLine[1], PKey[1],
                MLink[1], Score[1], FMemo[1])
        Log.d("TAG", "createメソッド終了")
    }


    // データベースにレコードを追加する
    fun create(mName:String, mPhonetic:String, sName:String, sPhonetic:String,
               fLine:String, pKey:Int, mLink:String, Score:Float, fMemo:String){
        mRealm.executeTransaction{
            // ランダムなidを設定
            var siggmoDB = mRealm.createObject(SiggmoDB::class.java, UUID.randomUUID().toString())

            // 各項目を設定
            siggmoDB.music_name      = mName
            siggmoDB.music_phonetic  = mPhonetic
            siggmoDB.singer_name     = sName
            siggmoDB.singer_phonetic = sPhonetic
            siggmoDB.first_line      = fLine
            siggmoDB.proper_key      = pKey
            siggmoDB.movie_link      = mLink
            siggmoDB.score           = Score
            siggmoDB.free_memo       = fMemo
            mRealm.copyToRealm(siggmoDB)
        }
    }
}
