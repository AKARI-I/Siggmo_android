package com.example.iakari.siggmo_android

/**
 * Created by iakari on 2018/05/16.
 */

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.util.*

// openをとると継承ができなくなるらしい
open class SiggmoDB (
    // キー値をランダムで決める(とりあえず)
        @PrimaryKey open var id : Long = UUID.randomUUID() as Long,

    // 多分このへんが項目名
        @Required
        open var music_name     : String = "",  // 曲名
        open var music_phonetic : String = "",  // 曲名の読み仮名
        open var singer_name    : String = "",  // 歌手名
        open var singer_phonetic: String = "",  // 歌手名の読み仮名
        open var first_line     : String = "",  // 歌いだし
        open var proper_key     : Int = 0,      // 適正キー
        open var movie_link     : String = "",  // 動画のリンク
        open var score          : Float = 0F,   // 採点結果
        open var free_memo      : String = ""  // 自由記入欄
) : RealmObject(){}