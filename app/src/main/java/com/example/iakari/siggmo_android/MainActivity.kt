package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.*
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var mRealm: Realm

    // ここでActivityが初めて生成される, 初期化は全てここに書く
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        /*-------------------- 新規登録画面 --------------------*/
        // 新規登録画面に遷移
        fab.setOnClickListener { _ ->
            val intent = Intent(this , NewAdditionActivity::class.java)
            startActivity(intent)
        }

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

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

        setList()    // リストの再表示
        searchList() // 検索ツールバー
    }

    // 検索処理
    private fun searchList(){
        val searchView = listSearch as SearchView
        val filter = (MainListView.adapter as Filterable).filter // フィルター用オブジェクトの生成
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                return false    // 検索キーが押下された
            }
            override fun onQueryTextChange(text: String?): Boolean {
                // テキストが変更された
                if (TextUtils.isEmpty(text)){
                    filter.filter("")
                } else{
                    filter.filter(text.toString())
                }
                return false
            }
        })
    }

    // 曲の一覧を表示(デフォルト)
    private fun setList(){
        // データベースの値をすべて取り出す
        val getData = read()
        // 全データをdataListに取り出す
        val dataList: MutableList<Item> = mutableListOf()

        // 曲名をリスト表示
        getData.forEach{
            dataList.add(Item(it.id, it.music_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        MainListView.adapter = arrayAdapter

        // 各項目をタップしたときの処理
        MainListView.setOnItemClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as Item    // タップした項目の要素名を取得

            // 詳細画面に遷移(idを渡す)
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID", item.id)
            startActivity(intent)
        }

        // 長押しで削除する
        MainListView.setOnItemLongClickListener{parent, _, position, _ ->
            val listView = parent as ListView
            val item = listView.getItemAtPosition(position) as Item    // タップした項目の要素名を取得
            // アラートの表示
            AlertDialog.Builder(this).apply {
                setTitle("Are you sure?")
                setMessage("削除しますか？")
                setPositiveButton("Yes", { _, _ ->
                    // クエリを発行し結果を取得
                    val results: RealmResults<SiggmoDB> = mRealm.where(SiggmoDB::class.java)
                            .equalTo("id", item.id)
                            .findAll()
                    mRealm.executeTransaction({
                        results.deleteFromRealm(0)
                        results.deleteLastFromRealm()
                    })

                    arrayAdapter.remove(arrayAdapter.getItem(position))
                    arrayAdapter.notifyDataSetChanged()
                    MainListView.invalidateViews()
                })
                setNegativeButton("Cancel", null)
                show()
            }

            return@setOnItemLongClickListener true
        }
    }

    // ソートしたデータを表示する
    private fun setSortList(sortId: Int){
        // データベースの値をすべて取り出す
        Log.d("sortID", sortId.toString())
        // 全データをdataListに取得
        val dataList: MutableList<Item> = mutableListOf()
        // ソートしたデータを取得
        readSorted(sortId)?.forEach {
            dataList.add(Item(it.id, it.music_name))
        }
        val arrayAdapter = ArrayAdapter<Item>(this, android.R.layout.simple_list_item_1, dataList)
        MainListView.adapter = arrayAdapter
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // アクションバー(ホームボタンとかある場所)のアイテムのクリックをここで処理
        // アクションバーは、AndroidManifest.xmlで親アクティビティを指定する限りHome / Upボタンのクリックを自動的に処理する
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ナビゲーションドロワーのイベント処理
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search -> {
                // 検索
                val editView = EditText(this@MainActivity)
                val dialog = AlertDialog.Builder(this@MainActivity)

                dialog.setTitle("Search")
                dialog.setView(editView)
                // OKボタンの設定
                dialog.setPositiveButton("OK") { _, _ ->
                    // OKボタンをタップした時の処理をここに記述
                    if(editView.isEnabled) {
                        val filter = (MainListView.adapter as Filterable).filter // フィルター用オブジェクトの生成
                        val sb = editView.text as SpannableStringBuilder
                        filter.filter(sb.toString())
                    }
                }
                // キャンセルボタンの設定
                dialog.setNegativeButton("キャンセル") { _, _ ->
                    MainListView.clearTextFilter()
                }
                dialog.show()

            }
            R.id.nav_sort -> {
                // 並び変え(曲名の昇順・降順、歌手名の昇順・降順)

                var selectedId = 0  // どの選択肢が選ばれたかを保持
                val dialogMenu = arrayOf("曲名昇順", "曲名降順", "歌手名昇順", "歌手名降順")

                // ダイアログを作成して表示
                AlertDialog.Builder(this).apply {
                    setTitle("タイトル")
                    setSingleChoiceItems(dialogMenu, 0) { _, i ->
                        selectedId = i  // 選択した項目を保持
                    }
                    setPositiveButton("OK") { _, _ ->
                        setSortList(selectedId)
                    }
                    setNegativeButton("Cancel", null)
                    show()
                }
            }
            R.id.nav_lists -> {
                // リスト画面に遷移
                val intent = Intent(this , ListsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_add -> {
                // 新規登録画面に遷移
                val intent = Intent(this , NewAdditionActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_search_clear -> {
                setList()
            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // データベースから全てのデータを取り出す
    private fun read() : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java).findAll()
    }

    // 並び変え処理(0:曲名昇順, 1:曲名降順, 2:歌手名昇順, 3:歌手名降順)
    private fun readSorted(sortId: Int) : RealmResults<SiggmoDB>? {
        when(sortId){
            0 -> return mRealm.where(SiggmoDB::class.java).findAll().sort("music_phonetic")
            1 -> return mRealm.where(SiggmoDB::class.java).findAll().sort("music_phonetic", Sort.DESCENDING)
            2 -> return mRealm.where(SiggmoDB::class.java).findAll().sort("singer_phonetic")
            3 -> return mRealm.where(SiggmoDB::class.java).findAll().sort("singer_phonetic", Sort.DESCENDING)
        }
        return null
    }

    // 表示する項目名とidをペアにして扱うためのクラス
    private inner class Item(val id: String, val name: String){
        override fun toString(): String{
            return name
        }
    }

    // Realmの削除についての定義
    override fun onDestroy(){
        super.onDestroy()
        mRealm.close()
    }
}