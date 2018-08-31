package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_new_addition.*
import java.util.*

class NewAdditionActivity : AppCompatActivity() {

    lateinit var mRealm: Realm

    // mutableMapOf：書き込み可能なコレクションを生成する(mapOfは読み取り専用)
    private val musicInfoS: MutableMap<String, String> = mutableMapOf(
            "mn" to "",  // 曲名
            "mp" to "",  // よみがな(曲名)
            "sn" to "",  // 歌手名
            "sp" to "",  // よみがな(歌手名)
            "fl" to "",  // 歌いだし
            "pk" to "",  // 適正キー
            "ml" to "",  // 動画のリンク
            "fm" to "")  // 自由記入欄

    private val musicInfoF: MutableMap<String, Float?> = mutableMapOf("sc" to null)
    private val musicInfoI: MutableMap<String, Int> = mutableMapOf("sl" to 1)
    var insertFlg = false
    var singing_level = 1   // 歌えるレベル(1~4)
    var proper_key_level = 0 // 適正キー(-7~7)
    var score = ""           // スコア

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_addition)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        /*-------------------- 歌えるレベルのボタン --------------------*/
        singing_level_downButton.setOnClickListener {
            if (singing_level - 1 < 1) {
                singing_level = 1
            } else {
                singing_level -= 1
            }
            edit_singing_level.text = singing_level.toString()
        }

        singing_level_upButton.setOnClickListener{
            if(4 < singing_level+1){
                singing_level = 4
            } else {
                singing_level += 1
            }
            edit_singing_level.text = singing_level.toString()
        }

        /*-------------------- 適正キーのボタン --------------------*/
        proper_key_downButton.setOnClickListener{
            if(proper_key_level-1 < -7){
                proper_key_level = -7
            } else {
                proper_key_level -= 1
            }
            edit_proper_key.text = proper_key_level.toString()
        }

        proper_key_upButton.setOnClickListener{
            if(7 < proper_key_level+1){
                proper_key_level = 7
            } else {
                proper_key_level += 1
            }

            edit_proper_key.text = proper_key_level.toString()
        }

        /* 保存ボタンがクリックされたらレコードを追加する */
        saveButon.setOnClickListener {
            save()      // 新規登録処理
            if(insertFlg) {
                finish()    // メイン画面に戻る
            }
        }
    }

    // 保存ボタンが押されたらinsert処理をしてメイン画面に戻る
    // 数値は一度String型に変換してから元の型に戻す必要があるみたい(参考：https://appcoding.net/string-to-int-kotlin/)
    private fun save(){
        // 入力値を取得
        if(!isEmpty(edit_music_name.text))     { musicInfoS["mn"] = edit_music_name.text.toString() }
        if(!isEmpty(edit_music_phonetic.text)) { musicInfoS["mp"] = edit_music_phonetic.text.toString() }
        if(!isEmpty(edit_singer_name.text))    { musicInfoS["sn"] = edit_singer_name.text.toString() }
        if(!isEmpty(edit_singer_phonetic.text)){ musicInfoS["sp"] = edit_singer_phonetic.text.toString() }
        if(!isEmpty(edit_first_line.text))     { musicInfoS["fl"] = edit_first_line.text.toString() }
        if(!isEmpty(edit_proper_key.text))     { musicInfoS["pk"] = edit_proper_key.text.toString()}
        if(!isEmpty(edit_movie_link.text))     { musicInfoS["ml"] = edit_movie_link.text.toString() }
        if(!isEmpty(edit_score.text))          { musicInfoF["sc"] = edit_score.text.toString().toFloat() }
        if(!isEmpty(edit_free_memo.text))      { musicInfoS["fm"] = edit_free_memo.text.toString() }
        musicInfoI["sl"] = singing_level   // 表示している数字をそのまま代入するためifはいらない

        // 入力値のチェックはここでする
        if(isEmpty(edit_music_name.text)){
            edit_music_name.error = "曲名を入力してください"
        }else if( !isEmpty(edit_score.text) ){
            if( scoreCheck(edit_score.text.toString().toFloat())) {
                edit_score.error = "1~100の数字を入力してください"
            }else {
                // 曲名の入力があった場合
                create( musicInfoS["mn"].toString(),
                        musicInfoS["mp"].toString(),
                        musicInfoS["sn"].toString(),
                        musicInfoS["sp"].toString(),
                        musicInfoS["fl"].toString(),
                        musicInfoI["sl"] as Int,
                        musicInfoS["pk"].toString(),
                        musicInfoS["ml"].toString(),
                        musicInfoF["sc"],
                        musicInfoS["fm"].toString())
                finish()    // メイン画面に戻る
            }
        } else {
            // 曲名の入力があった場合
            create( musicInfoS["mn"].toString(),
                    musicInfoS["mp"].toString(),
                    musicInfoS["sn"].toString(),
                    musicInfoS["sp"].toString(),
                    musicInfoS["fl"].toString(),
                    musicInfoI["sl"] as Int,
                    musicInfoS["pk"].toString(),
                    musicInfoS["ml"].toString(),
                    musicInfoF["sc"],
                    musicInfoS["fm"].toString())
            finish()    // メイン画面に戻る
        }
    }

    // データベースにレコードを追加する
    private fun create(mName:String, mPhonetic:String, sName:String, sPhonetic:String, fLine:String,
                       sLevel: Int, pKey: String, mLink:String, Score:Float?, fMemo:String){

        mRealm.executeTransaction{
            // ランダムなidを設定
            val siggmoDB = mRealm.createObject(SiggmoDB::class.java, UUID.randomUUID().toString())
            val scoreResultDB = mRealm.createObject(ScoreResultDB::class.java, UUID.randomUUID().toString())

            /*-------------------- 時間の取得 --------------------*/
            var calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)          // 年
            val month = calendar.get(Calendar.MONTH)+1      // 月
            val day = calendar.get(Calendar.DAY_OF_MONTH)   // 日
            val hour = calendar.get(Calendar.HOUR_OF_DAY)   // 時
            val minute = calendar.get(Calendar.MINUTE)      // 分
            val second = calendar.get(Calendar.SECOND)      // 秒

            val date = "$year/$month/$day/$hour:$minute:$second"    // 年/月/日/時:分:秒

            // 各項目を設定
            siggmoDB.music_name      = mName
            siggmoDB.music_phonetic  = mPhonetic
            siggmoDB.singer_name     = sName
            siggmoDB.singer_phonetic = sPhonetic
            siggmoDB.first_line      = fLine
            siggmoDB.singing_level   = sLevel
            siggmoDB.proper_key      = pKey
            siggmoDB.movie_link      = mLink
            siggmoDB.free_memo       = fMemo
            scoreResultDB.music_id   = siggmoDB.id
            scoreResultDB.score      = Score
            scoreResultDB.reg_data   = date
            if( Score != null )
                siggmoDB!!.music_count = siggmoDB.music_count + 1

            // データベースに追加
            mRealm.copyToRealm(siggmoDB)
            mRealm.copyToRealm(scoreResultDB)
        }
    }

    private fun scoreCheck(score: Float): Boolean {
        return score < 0 || 100 < score     // 1~100の範囲外ならtrueを返す
    }
}