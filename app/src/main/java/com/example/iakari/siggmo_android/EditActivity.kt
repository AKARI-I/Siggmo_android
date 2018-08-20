package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.activity_new_addition.*

class EditActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        var eLevel = 1


        //       val editText = findViewById<EditText>(android.R.id.music_name_edit)
 //       val edit = m_name_edit.text.toString()
 //       m_name_edit.setText("==========")

        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        /*-------------------- 歌えるレベルのボタン --------------------*/
        downButtonE.setOnClickListener {
            if(eLevel-1 < 1){
                eLevel = 1
            } else {
                eLevel -= 1
            }

            s_level.text = eLevel.toString()
            Log.d("TAG", "level = ${eLevel}(press down)")
        }
        upButtonE.setOnClickListener {
            if(4 < eLevel+1){
                eLevel = 4
            } else {
                eLevel += 1
            }

            s_level.text = eLevel.toString()
            Log.d("TAG", "level = ${eLevel}(press up)")
        }

        val tapid = intent.getStringExtra("TapID")
        val record = quaryById(tapid)
        val s_record = quaryByScore(record!!.id)

        // 保存済みのデータを表示
        if (s_record != null) {
            Log.d("TAG", "${record}")
            m_name_edit.setText(record.music_name)
            m_phone.setText(record.music_phonetic)
            s_name.setText(record.singer_name)
            s_phone.setText(record.singer_phonetic)
            f_line.setText(record.first_line)
            s_level.setText(record.singing_level.toString())
            p_key.setText(record.proper_key)
            m_link.setText(record.movie_link)
            s_edit.setText(s_record.score.toString())
            f_memo.setText(record.free_memo)
        }

        // update処理にまわす
        val button: Button = findViewById(R.id.editbutton)
        button.setOnClickListener{
            //editにとりあえず今は曲名だけを入れてupdateに渡す
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
            update(record, s_record, sgm)
            //DetailActivityにもどる
            val intent: Intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID",tapid)
            startActivity(intent)
        }
    }

        //id(tapid),record,曲名を渡す
    fun update(record: SiggmoDB?, s_record: ScoreResultDB?, sgm: Array<String>){
        mRealm.executeTransaction{
            // スコアの範囲チェック
            // ToDo:範囲チェックがfalse->落ちる
            if(checkScore(sgm[8].toFloat())){
                Log.d("TAG", "スコアの範囲チェック：true")
                //sgm配列に項目を入れて曲名から順番にDB(record)の中身と一緒かどうかを調べる
                //今は項目一つしか入れてないのでループとかはせず曲名だけ見てる
                val list = listOf(record)
                if (record != null && s_record != null) {
                    for (item in list) {
                        //ループと条件分岐が難しそうなので一気に全部更新
                        record.music_name       = sgm[0]
                        record.music_phonetic   = sgm[1]
                        record.singer_name      = sgm[2]
                        record.singer_phonetic  = sgm[3]
                        record.first_line       = sgm[4]
                        record.singing_level    = sgm[5].toInt()
                        record.proper_key       = sgm[6]
                        record.movie_link       = sgm[7]
                        s_record.score          = sgm[8].toFloat()
                        record.free_memo        = sgm[9]
                    }
                }
            } else {
                Log.d("TAG", "スコアの範囲チェック：false")
                edit_score.error = "0~100の点数を入力してください"
            }
        }
    }
    fun checkScore(score:Float): Boolean {
        if(0 <= score && score <= 100){
            return true
        } else {
            return false
        }
    }

    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    // scoreを参照する
    fun quaryByScore(id: String): ScoreResultDB? {
        Log.d("TAG", "quaryByScore(DetailActivity)")
        return mRealm.where(ScoreResultDB::class.java)
                .equalTo("music_id", id)
                .findFirst()
    }
}