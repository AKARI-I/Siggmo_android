/* リストの一覧を表示する画面 */

package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.text.SpannableStringBuilder
import android.view.KeyEvent
import android.view.Window
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
    private lateinit var mRealm: Realm

    // ここでActivityが初めて生成される, 初期化は全てここに書く
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        setContentView(R.layout.activity_lists)

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

        setLists()  // リストの再表示
    }

    private fun setLists(){
        val getData = readList()    // データベースの値をすべて取り出す
        val dataList: MutableList<Item> = mutableListOf()   // 全データをdataListに取り出す

        // リスト名をリスト表示
        getData.forEach{
            dataList.add(Item(it.list_id, it.list_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        ListsView.adapter = arrayAdapter

        // ダイアログでリストの追加をする
        lists_fab.setOnClickListener{ _ ->
            // 曲リストの追加, テキスト入力用Viewの作成
            val editView = EditText(this@ListsActivity)
            editView.inputType = InputType.TYPE_CLASS_TEXT

            val dialog = AlertDialog.Builder(this@ListsActivity)

            dialog.setTitle("リスト名を入力してください")
            dialog.setView(editView)

            // OKを押下した時の処理
            dialog.setPositiveButton("OK") { _, _ ->
                // OKボタンをタップした時の処理をここに記述
                if(editView.isEnabled) {
                    mRealm.executeTransaction {
                        // ListDBをlist_idをランダムで作成
                        val listDB = mRealm.createObject(ListDB::class.java, UUID.randomUUID().toString())
                        val sb = editView.text as SpannableStringBuilder
                        // Listの名前をListDB.list_nameに保存
                        listDB.list_name = sb.toString()
                    }
                }
                setLists()
            }

            // キャンセルボタンの設定
            dialog.setNegativeButton("キャンセル") { _, _ -> }

            dialog.show()

        }

        // 各項目をタップしたときの処理
        ListsView.setOnItemClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as ListsActivity.Item    // タップした項目の要素名を取得

            // リスト内の曲一覧画面に遷移
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra("TapID", item.id)
            startActivity(intent)
        }

        // 長押しで削除する
        ListsView.setOnItemLongClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as ListsActivity.Item    // タップした項目の要素名を取得
            // アラートの表示
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setMessage("削除しますか？")
                setPositiveButton("Yes", { _, _ ->
                    // クエリを発行し結果を取得
                    val results: RealmResults<ListDB> = mRealm.where(ListDB::class.java)
                            .equalTo("list_id", item.id).findAll()
                    mRealm.executeTransaction({
                        results.deleteLastFromRealm()
                    })

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

    // 標準BackKeyの遷移先変更
    override fun onKeyDown(keyCode: Int,event: KeyEvent?): Boolean{
        if(keyCode== KeyEvent.KEYCODE_BACK) {
            finish()    // ListsActivityの終了
            return true
        }
        return false
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }

    // リストの取得
    private fun readList() : RealmResults<ListDB> {
        return mRealm.where(ListDB::class.java).findAll()
    }
}