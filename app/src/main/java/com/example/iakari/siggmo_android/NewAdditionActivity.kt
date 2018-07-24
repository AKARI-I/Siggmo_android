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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_addition)

        Log.d("activity", "start NewAdditionActivity")

        // debug
        debugText.text = "musicInfo_s → ${musicInfo_s["mn"]}"
        musicInfo_s["mn"] = "hoge"
        debugText2.text = "update musicInfo_s → ${musicInfo_s["mn"]}"

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
            finish()    // メイン画面に戻る
        }

        Log.d("activity", "finish NewAdditionActivity")

    }

    // 保存ボタンが押されたらinsert処理をしてメイン画面に戻る
    fun save(){
        // 入力があれば
        if(!isEmpty(edit_music_name.text)) {
            musicInfo_s["mn"] = edit_music_name.text.toString()
            // 他の項目の取得
            /*if(!isEmpty(edit_music_phonetic.text)) {
                Log.d("TAG", "music phonetic -> ${musicInfo_s["mp"]}")
                musicInfo_s["mp"] = edit_music_phonetic.text.toString() }*/
            /*if(!isEmpty(edit_singer_name.text)){
                Log.d("TAG", "singer name -> ${musicInfo_s["sn"]}")
                musicInfo_s["sn"] = edit_singer_name.text.toString() }*/
            /*if(!isEmpty(edit_singer_phonetic.text)) {
                Log.d("TAG", "singer_phonetic -> ${musicInfo_s["sp"]}")
                musicInfo_s["sp"] = edit_singer_phonetic.text.toString() }*/
            /*if(!isEmpty(edit_first_line.text)) {
                Log.d("TAG", "first line -> ${musicInfo_s["fl"]}")
                musicInfo_s["fl"] = edit_first_line.text.toString() }*/
            /*if(!isEmpty(edit_proper_key.text)) {
                Log.d("TAG", "proper key -> ${musicInfo_i["pk"]}")
                musicInfo_i["pk"] = edit_proper_key as Int }*/
            /*if(!isEmpty(edit_movie_link.text)) {
                Log.d("TAG", "movie link -> ${musicInfo_s["ml"]}")
                musicInfo_s["ml"] = edit_movie_link.text.toString() }*/
            /*if(!isEmpty(edit_score.text)) {
                Log.d("TAG", "score -> ${musicInfo_f["sc"]}")
                musicInfo_f["sc"] = edit_score as Float }*/
            /*if(!isEmpty(edit_free_memo.text)) {
                Log.d("TAG", "free memo -> ${musicInfo_s["fm"]}")
                musicInfo_s["fm"] = edit_free_memo.text.toString() }*/
            Log.d("TAG", "insert if check clear")
        } else {
            Log.d("TAG", "not insert")
            Toast.makeText(this, "曲名の入力がありませんでした", Toast.LENGTH_LONG).show()
        }
        Log.d("TAG", "finish save method")

        // 新規登録処理
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
