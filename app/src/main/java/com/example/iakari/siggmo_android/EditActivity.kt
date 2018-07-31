package com.example.iakari.siggmo_android

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_edit.*


class EditActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

 //       val editText = findViewById<EditText>(android.R.id.music_name_edit)
 //       val edit = m_name_edit.text.toString()
 //       m_name_edit.setText("==========")

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
            m_name_edit.setText(record.music_name)
            m_phone.setText(record.music_phonetic)
            s_name.setText(record.singer_name)
            s_phone.setText(record.singer_phonetic)
            f_line.setText(record.first_line)
            p_key.setText(record.proper_key.toString())
            m_link.setText(record.movie_link)
            s_edit.setText(record.score.toString())
            f_memo.setText(record.free_memo)

        }
        val button: Button = findViewById(R.id.editbutton)
        button.setOnClickListener{
            //editにとりあえず今は曲名だけを入れてupdateに渡す
            val sgm = arrayOf(m_name_edit.text.toString(),
                    m_phone.text.toString(),
                    s_name.text.toString(),
                    s_phone.text.toString(),
                    f_line.text.toString(),
                    p_key.text.toString(),
                    m_link.text.toString(),
                    s_edit.text.toString(),
                    f_memo.text.toString())
            update(record, sgm)
            //DetailActivityにもどる
            val intent: Intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID",tapid)
            startActivity(intent)
        }


    }

        //id(tapid),record,曲名を渡す
    fun update(record: SiggmoDB?, sgm: Array<String>){
        mRealm.executeTransaction{
            //sgm配列に項目を入れて曲名から順番にDB(record)の中身と一緒かどうかを調べる
            //今は項目一つしか入れてないのでループとかはせず曲名だけ見てる
            val list = listOf(record)
            if (record != null) {
                for (item in list) {

                    //ループと条件分岐が難しそうなので一気に全部更新
                    record.music_name = sgm[0]
                    record.music_phonetic = sgm[1]
                    record.singer_name = sgm[2]
                    record.singer_phonetic = sgm[3]
                    record.first_line = sgm[4]
                    record.proper_key = sgm[5].toInt()
                    record.movie_link = sgm[6]
                    record.score = sgm[7].toFloat()
                    record.free_memo = sgm[8]


                }
            }
        }

    }



    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }


}