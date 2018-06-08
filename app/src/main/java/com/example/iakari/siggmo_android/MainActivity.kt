package com.example.iakari.siggmo_android

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import io.realm.Realm
import io.realm.RealmConfiguration
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var mRealm: Realm

    val MName     = arrayOf("春雷","君の知らない物語")
    val MPhonetic = arrayOf("しゅんらい", "きみのしらないものがたり")
    val SName     = arrayOf("米津玄師", "supercell")
    val SPhonetic = arrayOf("よねづけんし", "すーぱーせる")
    val FLine     = arrayOf("現れたそれは春のまっ最中", "いつも通りのある日のこと")
    val PKey      = arrayOf(3, 0)
    val MLink     = arrayOf("https://www.youtube.com/watch?v=zkNzxsaCunU",
                            "https://www.youtube.com/watch?v=CEwQ-xp7aiU")
    val Score     = arrayOf(87.261F, 85.579F)
    val FMemo     = arrayOf("口が回らない", "1人で歌う時の忙しさ半端ないシリーズ代表")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
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

        // createテスト(テスト用レコードの追加)
        // ※ここではテスト用データを事前に宣言してレコードを作成

        create(MName[0], MPhonetic[0], SName[0], SPhonetic[0], FLine[0], PKey[0],
                MLink[0], Score[0], FMemo[0])
        create(MName[1], MPhonetic[1], SName[1], SPhonetic[1], FLine[1], PKey[1],
                MLink[1], Score[1], FMemo[1])

        // データベースの値をすべて取り出す
        val getData = read()

        /*-------------------- ListView --------------------*/
        // 曲名をリスト表示
        val dataList: MutableList<String> = mutableListOf()

        getData.forEach{
            dataList.add(it.music_name)
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataList)
        MainListView.adapter = arrayAdapter

        // 各項目をタップで詳細画面に遷移
        MainListView.setOnItemClickListener{_, _, _, _ ->
            val intent: Intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("TapID", "タップした項目のID")
            startActivity(intent)
        }

        // ToDo:その場では消えるけど多分データベースからは消えてないし、微妙に挙動がおかしい
        // 長押しで削除する
        MainListView.setOnItemLongClickListener{_, _, position, _ ->
            arrayAdapter.remove(arrayAdapter.getItem(position))
            arrayAdapter.notifyDataSetChanged()

            return@setOnItemLongClickListener true
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // メニューを膨張させます。アクションバーが存在する場合、アクションバーに項目が追加される
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // アクションバー(ホームボタンとかある場所)のアイテムのクリックをここで処理
        // アクションバーは、AndroidManifest.xmlで親アクティビティを指定する限り
        // Home / Upボタンのクリックを自動的に処理する
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    //ここでナビゲーションビューアイテムのクリックを処理
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_camera -> {

            }
            R.id.nav_gallery -> {

            }
            R.id.nav_slideshow -> {

            }
            R.id.nav_manage -> {

            }
            R.id.nav_share -> {

            }
            R.id.nav_send -> {

            }
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    // データベースにレコードを追加する
    // ToDo:曲の追加画面の処理に移す
    fun create(mName:String, mPhonetic:String, sName:String, sPhonetic:String,
               fLine:String, pKey:Int, mLink:String, Score:Float, fMemo:String){
        mRealm.executeTransaction{
            var siggmoDB = mRealm.createObject(SiggmoDB::class.java, UUID.randomUUID().toString())
            siggmoDB.music_name      = mName
            siggmoDB.music_phonetic  = mPhonetic
            siggmoDB.singer_name     = sName
            siggmoDB.singer_phonetic = sPhonetic
            siggmoDB.first_line      = fLine
            siggmoDB.proper_key      = pKey
            siggmoDB.movie_link      = mLink
            siggmoDB.score           = Score
            siggmoDB.free_memo       = fMemo
            mRealm.copyToRealm(siggmoDB)
        }
    }

    // データベースから "全ての" データを取り出す
    fun read() : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java).findAll()
    }

    // Realmの削除についての定義
    override fun onDestroy(){
        super.onDestroy()
        mRealm.close()
    }
}
