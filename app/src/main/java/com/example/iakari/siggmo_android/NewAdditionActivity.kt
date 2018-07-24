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

    // String型データ用　入力値がない場合ここに設定したデフォルト値が入る
    // mutableMapOf：書き込み可能なコレクションを生成する(mapOfは読み取り専用)
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
    var insertFlg = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_addition)

        Log.d("activity", "start NewAdditionActivity")

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
            save()      // 新規登録処理
            if(insertFlg) {
                finish()    // メイン画面に戻る
            }
        }

        Log.d("activity", "finish NewAdditionActivity")

    }

    // 保存ボタンが押されたらinsert処理をしてメイン画面に戻る
    // 数値は一度String型に変換してから元の型に戻す必要があるみたい(参考：https://appcoding.net/string-to-int-kotlin/)
    fun save(){
        // 入力値を取得
        musicInfo_s["mn"] = edit_music_name.text.toString()
        musicInfo_s["mp"] = edit_music_phonetic.text.toString()
        musicInfo_s["sn"] = edit_singer_name.text.toString()
        musicInfo_s["sp"] = edit_singer_phonetic.text.toString()
        musicInfo_s["fl"] = edit_first_line.text.toString()
        musicInfo_i["pk"] = edit_proper_key.text.toString().toInt()
        musicInfo_s["ml"] = edit_movie_link.text.toString()
        musicInfo_f["sc"] = edit_score.text.toString().toFloat()
        musicInfo_s["fm"] = edit_free_memo.text.toString()

        if(isEmpty(edit_music_name.text)){
            // 曲名の入力がなかった場合
            Log.d("TAG", "edit_music_name is empty")
            edit_music_name.error = "曲名を入力してください"
        } else {
            // 曲名の入力があった場合
            // 新規登録処理
            save()
            // メイン画面に戻る
            finish()

        }
    }

    // データベースにレコードを追加する
    fun create(mName:String, mPhonetic:String, sName:String, sPhonetic:String,
               fLine:String, pKey:Int, mLink:String, Score:Float, fMemo:String){
        Log.d("TAG", "start create method")
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
        Log.d("TAG", "finish create method")

    }
}
