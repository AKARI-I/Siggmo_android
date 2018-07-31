package com.example.iakari.siggmo_android

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.content_list.*

class ListActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

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

        val tapid = intent.getStringExtra("TapID")
        Log.d("DBdata", tapid)
        // データベースの値をすべて取り出す
        val getData = read(tapid)
        // 全データをdataListに取り出す
        val dataList: MutableList<Item>
        dataList = mutableListOf()

        // 曲名をリスト表示
        getData.forEach{
            dataList.add(Item(it.id, it.music_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        Log.d("array", arrayAdapter.toString())
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
        SongsListView.setOnItemLongClickListener{_, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as Item
            // アラートの表示
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setMessage("削除しますか？")
                setPositiveButton("Yes", DialogInterface.OnClickListener{_, _ ->
                    Log.d("TAG", "YES!!")
                    // クエリを発行し結果を取得
                    val results: RealmResults<SiggmoDB> = mRealm.where(SiggmoDB::class.java)
                            .equalTo("id", item.id)
                            .findAll()
                    mRealm.executeTransaction(Realm.Transaction {
                        Log.d("TAG", "in realm delete process")
                        results.deleteFromRealm(0)
                        results.deleteLastFromRealm()
                    })

                    //
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

    // データベースから "全ての" データを取り出す
    fun read(id: String) : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java)
                .equalTo("list_id", id)
                .findAll()
    }
}