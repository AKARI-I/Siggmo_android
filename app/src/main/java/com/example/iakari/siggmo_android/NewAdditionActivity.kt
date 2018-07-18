package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.util.Log
import android.widget.Toast
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_new_addition.*
import java.util.*

class NewAdditionActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    // String型データ用
    val musicInfo_s: MutableMap<String, String> = mutableMapOf(
            "mn" to "曲名",           // 曲名
            "mp" to "よみがな(曲名)",  // よみがな(曲名)
            "sn" to "歌手名",         // 歌手名
            "sp" to "よみがな(歌手名)",// よみがな(歌手名)
            "fl" to "歌いだし",       // 歌い出し
            "ml" to "動画のリンク",    // 動画のリンク
            "fm" to "自由記入欄")     // 自由記入欄
    // Int型データ用(適正キー)
    val musicInfo_i: MutableMap<String, Int> = mutableMapOf("pk" to 10)
    // Float型データ用(採点結果)
    val musicInfo_f: MutableMap<String, Float> = mutableMapOf("sc" to 999F)

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
        // ※ここではテスト用データを事前に宣言してレコードを作成
        saveButon.setOnClickListener {
            // 新規登録処理
            save()

            // メイン画面に戻る
            finish()
        }
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

    // 保存ボタンが押されたらinsert処理をしてメイン画面に戻る
    fun save(){
        Log.d("TAG", "start save method")
        // 入力があれば
        Log.d("TAG", "isEmpty is ${isEmpty(edit_music_name.text)}")
        if(!isEmpty(edit_music_name.text)){
            musicInfo_s["mn"] = edit_music_name.text.toString()
            Log.d("TAG", "曲名：${musicInfo_s["mn"]}")

            // 新規登録処理
            Log.d("TAG", "")
            create( musicInfo_s["mn"].toString(),
                    musicInfo_s["mp"].toString(),
                    musicInfo_s["sn"].toString(),
                    musicInfo_s["sp"].toString(),
                    musicInfo_s["fl"].toString(),
                    musicInfo_i["pk"] as Int,
                    musicInfo_s["ml"].toString(),
                    musicInfo_f["sc"] as Float,
                    musicInfo_s["fm"].toString()
            )
            Toast.makeText(this, "保存しました", Toast.LENGTH_LONG).show()

        } else {
            Toast.makeText(this, "曲名の入力がありませんでした", Toast.LENGTH_LONG).show()
        }
        Log.d("TAG", "finish save method")
    }
}
