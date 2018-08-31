package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_song_add.*
import kotlinx.android.synthetic.main.content_song_add.*

class SongAddActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_add)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
    }
    /* Activityが表示されたときの処理を書く(別の画面から戻った時とか) */
    override fun onResume() {
        super.onResume()
        // ListActivityから送ってきたlist_id
        val listid = intent.getStringExtra("ListId")

        // タイトルの表示
        val getListData = quaryByListId(listid)
        if (getListData != null) {
            title = getListData.list_name + "に曲追加"
        }
        // リストの再表示
        setSongs(listid)
    }

    private fun setSongs(listid: String){
        val getData = read()    // データベースの値をすべて取り出す
        val dataList: MutableList<SongAddActivity.Item> = mutableListOf() // 全データをdataListに取り出す

        // 曲名をリスト表示
        getData.forEach{
            dataList.add(Item(it.id, it.music_name, it.list_id))
        }
        val arrayAdapter = ArrayAdapter<SongAddActivity.Item>(this,  android.R.layout.simple_list_item_multiple_choice, dataList)
        SongsAddListView.adapter = arrayAdapter
        val cnt = SongsAddListView.count    // リストの中の曲の数

        // リスト追加済みの曲をチェックする
        for (i in 0 until cnt){
            val item = SongsAddListView.getItemAtPosition(i) as Item
            if(item.list_id == listid){
                SongsAddListView.setItemChecked(i,true)
            }
        }

        updatebutton.setOnClickListener{_ ->
            // 歌のリスト追加機能
            val check = SongsAddListView.checkedItemPositions // チェックされているアイテムのポジションを取得
            for (i in 0 until cnt){
                if (check.get(i)){
                    val item = SongsAddListView.getItemAtPosition(i) as Item
                    val record = quaryById(item.id)
                    mRealm.executeTransaction{
                        if (record != null) {
                            record.list_id = listid
                        }
                    }
                }else{
                    val item = SongsAddListView.getItemAtPosition(i) as Item
                    val record = quaryById(item.id)
                    mRealm.executeTransaction{
                        if (record != null) {
                            record.list_id = ""
                        }
                    }
                }
            }
            finish()    // リスト一覧に戻る
        }
    }

    // データベースから "全ての" データを取り出す
    private fun read() : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java).findAll()
    }

    private fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }

    // SiggmoDBからlist_idが一致したレコードだけ取り出す
    private fun quaryByListId(listId: String) : ListDB? {
        return mRealm.where(ListDB::class.java)
                .equalTo("list_id", listId)
                .findFirst()
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String, val list_id: String){
        override fun toString(): String{
            return name
        }

    }
}
