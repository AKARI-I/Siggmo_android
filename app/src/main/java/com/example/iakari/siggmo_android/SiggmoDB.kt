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
        @PrimaryKey open var id : String = UUID.randomUUID().toString(),

    // @Requiredでnull指定を禁止
        @Required
        open var music_name     : String = "",  // 曲名
        open var music_phonetic : String  = "",  // 曲名の読み仮名
        open var singer_name    : String = "",  // 歌手名
        open var singer_phonetic: String = "",  // 歌手名の読み仮名
        open var first_line     : String = "",  // 歌いだし
        open var proper_key     : String = "",  // 適正キー
        open var movie_link     : String = "",  // 動画のリンク
        open var free_memo      : String = "",  // 自由記入欄
        open var list_id        : String = "",  // リスト照合用のID
        open var score_id       : String = ""   // 採点結果照合用ID(同曲でも別々に付与、消すかも)
) : RealmObject(){}

// openをとると継承ができなくなるらしい
open class ListDB (
        @PrimaryKey open var list_id : String = UUID.randomUUID().toString(),
        @Required
        open var list_name      : String = ""
) : RealmObject(){}

open class ScoreResultDB(
        @PrimaryKey open var score_id : String = UUID.randomUUID().toString(),
        //@Required
        open var music_id : String = "",        // どの曲の採点結果かを保存
        open var score : Float = 0F

) : RealmObject(){}