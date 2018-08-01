package com.example.iakari.siggmo_android

import android.content.Intent
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_lists.*
import kotlinx.android.synthetic.main.content_lists.*
import java.util.*

class ListsActivity : AppCompatActivity() {
    lateinit var mRealm: Realm

    /* ここでActivityが初めて生成される。初期化は全てここに書く。 */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lists)

        /*-------------------- Realm --------------------*/
        // Realmのセットアップ
        Log.d("TAG", "Realmセットアップ開始")
        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)
        Log.d("TAG", "Realmセットアップ終了")




    }
    /* Activityが表示されたときの処理を書く(別の画面から戻った時とか) */
    override fun onResume() {
        super.onResume()

        // リストの再表示
        setLists()

    }


    fun setLists(){
        // データベースの値をすべて取り出す
        val getData = readList()
        // 全データをdataListに取り出す
        val dataList: MutableList<Item> = mutableListOf()

        // リスト名をリスト表示
        getData.forEach{
            dataList.add(Item(it.list_id, it.list_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        ListsView.adapter = arrayAdapter

        // 各項目をタップしたときの処理
        ListsView.setOnItemClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as ListsActivity.Item    // タップした項目の要素名を取得

            // idを渡す
            val intent = Intent(this, com.example.iakari.siggmo_android.ListActivity::class.java)
            intent.putExtra("TapID", item.id)
            startActivity(intent)
        }
        // フローティングアクションボタン
        lists_fab.setOnClickListener{ _ ->
            // 曲リストの追加
            // テキスト入力用Viewの作成
            val editView = EditText(this@ListsActivity)

            val dialog = AlertDialog.Builder(this@ListsActivity)

            dialog.setTitle("リスト名を入力してください")
            dialog.setView(editView)

            // OKボタンの設定
            dialog.setPositiveButton("OK") { dialog, whichButton ->
                // OKボタンをタップした時の処理をここに記述
                if(editView.isEnabled) {
                    mRealm.executeTransaction {
                        // ListDBをlist_idをランダムで作成
                        var listDB = mRealm.createObject(ListDB::class.java, UUID.randomUUID().toString())
                        val sb = editView.getText() as SpannableStringBuilder
                        // Listの名前をListDB.list_nameに保存
                        listDB.list_name = sb.toString()
                    }
                }
                setLists()
            }

            // キャンセルボタンの設定
            dialog.setNegativeButton("キャンセル") { dialog, whichButton ->
                // キャンセルボタンをタップした時の処理をここに記述
            }

            dialog.show()

        }
        // 長押しで削除する
        ListsView.setOnItemLongClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as ListsActivity.Item    // タップした項目の要素名を取得
            // アラートの表示
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setMessage("削除しますか？")
                setPositiveButton("Yes", DialogInterface.OnClickListener{ _, _ ->
                    Log.d("TAG", "YES!!")
                    // クエリを発行し結果を取得
                    val results: RealmResults<ListDB> = mRealm.where(ListDB::class.java)
                            .equalTo("list_id", item.id).findAll()
                    mRealm.executeTransaction(Realm.Transaction {
                        Log.d("TAG", "in realm delete process")
                        //results.deleteFromRealm(0)
                        results.deleteLastFromRealm()
                    })

                    //
                    arrayAdapter.remove(arrayAdapter.getItem(position))
                    arrayAdapter.notifyDataSetChanged()
                    ListsView.invalidateViews()
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
    fun readList() : RealmResults<ListDB> {
        return mRealm.where(ListDB::class.java).findAll()
    }
}