package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*

class EditActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm

    // mutableMapOf：書き込み可能なコレクションを生成する


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var sLevel = 1  // 歌えるレベル
        var pKey = 0 // 適正キー

        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        /*-------------------- 歌えるレベルのボタン --------------------*/
        s_level_downButton.setOnClickListener {
            if(sLevel-1 < 1){
                sLevel = 1
            } else {
                sLevel -= 1
            }

            s_level.text = sLevel.toString()
        }
        s_level_upButton.setOnClickListener {
            if(4 < sLevel+1){
                sLevel = 4
            } else {
                sLevel += 1
            }

            s_level.text = sLevel.toString()
        }

        /*-------------------- 適正キーのボタン --------------------*/
        p_key_downButton.setOnClickListener{
            if(pKey-1 < -7){
                pKey = -7
            } else {
                pKey -= 1
            }

            p_key.text = pKey.toString()
        }
        p_key_upButton.setOnClickListener{
            if(7 < pKey+1){
                pKey = 7
            } else {
                pKey += 1
            }

            p_key.text = pKey.toString()
        }

        val tapid = intent.getStringExtra("TapID")
        val record = quaryById(tapid)
        val sRecord = quaryByScore(record!!.id)

        // 保存済みのデータを表示
        if (sRecord != null) {
            m_name_edit.setText(record.music_name)
            m_phone.setText(record.music_phonetic)
            s_name.setText(record.singer_name)
            s_phone.setText(record.singer_phonetic)
            f_line.setText(record.first_line)
            s_level.text = record.singing_level.toString()
            p_key.text = record.proper_key
            m_link.setText(record.movie_link)
            s_edit.setText(sRecord.score.toString())
            f_memo.setText(record.free_memo)
        }

        // update処理にまわす
        editbutton.setOnClickListener{
            val musicInfoS: MutableMap<String, String> = mutableMapOf(
                    "mn" to m_name_edit.text.toString(),  // 曲名
                    "mp" to m_phone.text.toString(),      // よみがな(曲名)
                    "sn" to s_name.text.toString(),       // 歌手名
                    "sp" to s_phone.text.toString(),      // よみがな(歌手名)
                    "fl" to f_line.text.toString(),       // 歌いだし
                    "pk" to p_key.text.toString(),        // 適正キー
                    "ml" to m_link.text.toString(),       // 動画のリンク
                    "fm" to f_memo.text.toString())       // 自由記入欄
            val musicInfoF: MutableMap<String, Float?> = mutableMapOf(
                    "sc" to s_edit.text.toString().toFloat())   // 採点結果
            val musicInfoI: MutableMap<String, Int> = mutableMapOf(
                    "sl" to s_level.text.toString().toInt())    // 歌えるレベル

            if(update(record, sRecord, musicInfoS, musicInfoF, musicInfoI)) {
                finish()    // EditActivityを終了する
            }
        }
    }

    //id(tapid),record,曲名を渡す
    private fun update(
            record: SiggmoDB?,
            s_record: ScoreResultDB?,
            dataS: MutableMap<String, String>,
            dataF: MutableMap<String, Float?>,
            dataI: MutableMap<String, Int>
    ) :Boolean{
        // 曲名のエラーチェック
        if(isEmpty(dataS["mn"])){
            m_name_edit.error = "曲名を入力してください"
            return false
        }
        // スコアの範囲チェック
        if(!checkScore(dataF["sc"])) {
            s_edit.error = "0~100の点数を入力してください"
            return false
        }

        mRealm.executeTransaction {
            if (record != null && s_record != null) {

                // 時間の取得
                var calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)          // 年
                val month = calendar.get(Calendar.MONTH) + 1    // 月
                val day = calendar.get(Calendar.DAY_OF_MONTH)   // 日
                val hour = calendar.get(Calendar.HOUR_OF_DAY)   // 時
                val minute = calendar.get(Calendar.MINUTE)      // 分
                val second = calendar.get(Calendar.SECOND)      // 秒

                val date = "$year/$month/$day/$hour:$minute:$second"    // 年/月/日/時:分:秒

                record.music_name       = dataS["mn"].toString()
                record.music_phonetic   = dataS["mp"].toString()
                record.singer_name      = dataS["sn"].toString()
                record.singer_phonetic  = dataS["sp"].toString()
                record.first_line       = dataS["fl"].toString()
                record.singing_level    = dataI["sl"] as Int
                record.proper_key       = dataS["pk"].toString()
                record.movie_link       = dataS["ml"].toString()
                s_record.score          = dataF["sc"] as Float
                record.free_memo        = dataS["fm"].toString()
                s_record.reg_data       = date
            }
        }
        return true
    }
    private fun checkScore(score: Float?): Boolean {
        return 0.0 <= score!! && score <= 100.0    // 範囲内ならtrueを返す
    }

    private fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    // scoreを参照する
    private fun quaryByScore(id: String): ScoreResultDB? {
        val records = mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id", id)
                .findAll().sort("reg_data", Sort.DESCENDING)
        return records[0]
    }
}