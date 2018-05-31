package com.example.iakari.siggmo_android

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
    val MName       = "春雷"
    val MPhonetic   = "しゅんらい"
    val SName       = "米津玄師"
    val SPhonetic   = "よねづけんし"
    val FLine       = "現れたそれは春のまっ最中"
    val PKey        = 3
    val MLink       = "https://www.youtube.com/watch?v=zkNzxsaCunU"
    val Score       = 87.261F
    val FMemo       = "口が回らない"

    val MName2      = "打上花火"
    val MPhonetic2  = "うちあげはなび"
    val SName2      = "米津玄師"
    val SPhonetic2  = "よねづけんし"
    val FLine2      = "あの日見渡した渚を 今も思い出すんだ"
    val PKey2       = 0
    val MLink2      = "https://youtu.be/-tKVN2mAKRI"
    val Score2      = 85.579F
    val FMemo2      = "1人で歌う時の忙しさ半端ないシリーズ代表"

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

        /*---------- Realm ----------*/
        // Realmのセットアップ
        // realmConfigにRealmの設定を書き込む

        Realm.init(this)
        val realmConfig = RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build()
        mRealm = Realm.getInstance(realmConfig)

        // createテスト(テスト用レコードの追加)
        // ※ここではテスト用データを事前に宣言し突っ込む
        // ToDo: 変数名が紛らわしいから変更の必要ありかも
        create(MName, MPhonetic, SName, SPhonetic, FLine, PKey, MLink, Score, FMemo)
        create(MName2, MPhonetic2, SName2, SPhonetic2, FLine2, PKey2, MLink2, Score2, FMemo2)

        /*---------- ListView ----------*/
        // 曲名をリスト表示
        val getData = read()
        val dataList: MutableList<String> = mutableListOf()

        getData.forEach{
            dataList.add(it.music_name)
        }
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, dataList)
        MainListView.adapter = arrayAdapter

        // ToDo:仕様に合わせて編集・移動
        // 長押しで削除する
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

    // データベースから全てのデータを取り出す
    fun read() : RealmResults<SiggmoDB> {
        return mRealm.where(SiggmoDB::class.java).findAll()
    }

    // Realmの削除についての定義
    override fun onDestroy(){
        super.onDestroy()
        mRealm.close()
    }
}
