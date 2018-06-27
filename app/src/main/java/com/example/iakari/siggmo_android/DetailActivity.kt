package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 受け取ったIDをToastで表示
        val tapid = intent.getStringExtra("TapID")
        //Toast.makeText(this, tapid, Toast.LENGTH_SHORT).show()

    }
}
