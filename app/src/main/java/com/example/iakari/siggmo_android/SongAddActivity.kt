package com.example.iakari.siggmo_android

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import android.widget.ListView
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_song_add.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.content_song_add.*
import android.widget.Spinner



class SongAddActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_add)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Log.d("TAG", "Realmセットアップ開始")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了")

        Log.d("activity", "finish DetailActivity")

    }
    /* Activityが表示されたときの処理を書く(別の画面から戻った時とか) */
    override fun onResume() {
        super.onResume()

        // リストの再表示
        setSongs()
    }
    fun setSongs(){
        // ListActivityから送ってきたlist_id
        val listid = intent.getStringExtra("ListId")
        // データベースの値をすべて取り出す
        val getData = read()
        Log.d("DBdata", getData.toString())
        // 全データをdataListに取り出す
        val dataList: MutableList<SongAddActivity.Item>
        dataList = mutableListOf()

        // 曲名をリスト表示
        getData.forEach{
            dataList.add(Item(it.id, it.music_name))
        }
        val arrayAdapter = ArrayAdapter<SongAddActivity.Item>(this,  android.R.layout.simple_list_item_multiple_choice, dataList)
        SongsAddListView.adapter = arrayAdapter

        // フローティングアクションボタン
        song_add_fab.setOnClickListener{ _ ->
            val check = SongsAddListView.checkedItemPositions
            val cnt = SongsAddListView.count
            for (i in 0 until cnt){
                if (check.get(i)){
                    val item = SongsAddListView.getItemAtPosition(i) as Item
                    val record = quaryById(item.id)
                    mRealm.executeTransaction{
                        if (record != null) {
                            record.list_id = listid
                        }
                    }
                }
            }
            val intent = Intent(this , ListActivity::class.java)
            intent.putExtra("TapID", listid)
            startActivity(intent)
        }


    }
    // データベースから "全ての" データを取り出す
    fun read() : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java).findAll()
    }
    fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }
}
