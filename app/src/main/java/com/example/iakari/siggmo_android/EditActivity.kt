package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils.isEmpty
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_edit.*
import java.util.*

class EditActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var s_Level = 1  // 歌えるレベル
        var p_Key = 0 // 適正キー

        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        /*-------------------- 歌えるレベルのボタン --------------------*/
        s_level_downButton.setOnClickListener {
            if(s_Level-1 < 1){
                s_Level = 1
            } else {
                s_Level -= 1
            }

            s_level.text = s_Level.toString()
        }
        s_level_upButton.setOnClickListener {
            if(4 < s_Level+1){
                s_Level = 4
            } else {
                s_Level += 1
            }

            s_level.text = s_Level.toString()
        }

        /*-------------------- 適正キーのボタン --------------------*/
        p_key_downButton.setOnClickListener{
            if(p_Key-1 < -7){
                p_Key = -7
            } else {
                p_Key -= 1
            }

            p_key.text = p_Key.toString()
        }
        p_key_upButton.setOnClickListener{
            if(7 < p_Key+1){
                p_Key = 7
            } else {
                p_Key += 1
            }

            p_key.text = p_Key.toString()
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
            val sgm = arrayOf(
                    m_name_edit.text.toString(),
                    m_phone.text.toString(),
                    s_name.text.toString(),
                    s_phone.text.toString(),
                    f_line.text.toString(),
                    s_level.text.toString(),
                    p_key.text.toString(),
                    m_link.text.toString(),
                    s_edit.text.toString(),
                    f_memo.text.toString())
            if(update(record, sRecord, sgm)) {
                finish()    // EditActivityを終了する
            }
        }
    }

    //id(tapid),record,曲名を渡す
    private fun update(record: SiggmoDB?, s_record: ScoreResultDB?, sgm: Array<String>) :Boolean{
        Log.d("TAG", "record:$record")
        Log.d("TAG", "s_record:$s_record")

        // 曲名のエラーチェック
        if(isEmpty(sgm[0])){
            m_name_edit.error = "曲名を入力してください"
            return false
        }
        // スコアの範囲チェック
        if(!checkScore(sgm[8].toFloat())) {
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
                Log.d("TAG", "Edit：日付の取得->$date")

                // ToDo:mutablelistにしたい
                record.music_name = sgm[0]
                record.music_phonetic = sgm[1]
                record.singer_name = sgm[2]
                record.singer_phonetic = sgm[3]
                record.first_line = sgm[4]
                record.singing_level = sgm[5].toInt()
                record.proper_key = sgm[6]
                record.movie_link = sgm[7]
                s_record.score = sgm[8].toFloat()
                record.free_memo = sgm[9]
                s_record.reg_data = date
            }
        }
        return true
    }
    private fun checkScore(score:Float): Boolean {
        return score in 0.0..100.0
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