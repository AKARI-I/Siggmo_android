package com.example.iakari.siggmo_android

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        //Toast.makeText(this, "${intent.extras.get("TapID")}",
        //        Toast.LENGTH_SHORT).show()

        // 受け取ったIDをToastで表示
        val intent = getIntent()
        val tapid = intent.getStringExtra("TapID")
        Toast.makeText(this, tapid, Toast.LENGTH_SHORT).show()
    }
}
