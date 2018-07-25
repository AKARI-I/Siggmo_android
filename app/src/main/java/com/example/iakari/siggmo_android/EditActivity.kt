package com.example.iakari.siggmo_android

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_edit.*
import android.widget.EditText
import android.widget.TextView


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
            val edit = m_name_edit.text.toString()
            update(tapid, record, edit)
            //DetailActivityにもどる
            val intent: Intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID",tapid)
            startActivity(intent)
        }


    }

        //id(tapid),record,曲名を渡す
    fun update(id: String,record: SiggmoDB?, edit: String){
        mRealm.executeTransaction{
            //sgm配列に項目を入れて曲名から順番にDB(record)の中身と一緒かどうかを調べる
            //今は項目一つしか入れてないのでループとかはせず曲名だけ見てる
            val sgm = arrayOf(edit)
            if (record != null) {
                //sgmとrecordが違ったらsgmの中身をrecordに入れる
                if(record.music_name != sgm[0]){
                    record.music_name = sgm[0]
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