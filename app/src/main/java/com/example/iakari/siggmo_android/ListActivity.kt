/* リスト内の曲の一覧を表示する画面 */

package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.KeyEvent
import android.widget.ArrayAdapter
import android.widget.ListView
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.content_list.*

class ListActivity : AppCompatActivity() {
    private lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
    }
    // Activityが表示されたときの処理を書く(別の画面から戻った時とか)
    override fun onResume() {
        super.onResume()
        val tapid = intent.getStringExtra("TapID")

        // タイトルの表示
        val getListData = quaryByListId(tapid)
        if (getListData != null) {
            title = getListData.list_name
        }
        // リストの再表示
        setSongs(tapid)
    }
    // 標準Backkeyの遷移先変更
    override fun onKeyDown(keyCode: Int,event: KeyEvent?): Boolean{
        if(keyCode== KeyEvent.KEYCODE_BACK) {
            val intent = Intent(this,ListsActivity::class.java)
            startActivity(intent)
            return true
        }
        return false
    }

    private fun setSongs(tapid: String){
        // データベースの値をすべて取り出す
        val getData = read(tapid)
        // 全データをdataListに取り出す
        val dataList: MutableList<Item> = mutableListOf()

        // 曲名をリスト表示
        getData.forEach{
            dataList.add(Item(it.id, it.music_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        SongsListView.adapter = arrayAdapter

        // 各項目をタップしたときの処理
        SongsListView.setOnItemClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as Item    // タップした項目の要素名を取得

            // idを渡す
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID", item.id)
            startActivity(intent)
        }
        // フローティングアクションボタン
        list_fab.setOnClickListener{ _ ->
            val intent = Intent(this , SongAddActivity::class.java)
            intent.putExtra("ListId", tapid)
            startActivity(intent)
        }

        // 長押しで削除する
        SongsListView.setOnItemLongClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as Item
            // アラートの表示
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setMessage("削除しますか？")
                setPositiveButton("Yes", { _, _ ->
                    Log.d("TAG", "YES!!")
                    // クエリを発行し結果を取得
                    val record = quaryById(item.id)
                    mRealm.executeTransaction {
                        if (record != null) { record.list_id = "" }
                    }

                    arrayAdapter.remove(arrayAdapter.getItem(position))
                    arrayAdapter.notifyDataSetChanged()
                    SongsListView.invalidateViews()
                })
                setNegativeButton("Cancel", null)
                show()
            }

            return@setOnItemLongClickListener true
        }
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }
    private fun quaryById(id: String): SiggmoDB? {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("id", id)
                .findFirst()
    }
    // データベースから "全ての" データを取り出す
    private fun read(id: String) : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("list_id", id)
                .findAll()
    }
    private fun quaryByListId(id: String) : ListDB? {
        return mRealm.where(ListDB::class.java)
                .equalTo("list_id", id)
                .findFirst()
    }
}