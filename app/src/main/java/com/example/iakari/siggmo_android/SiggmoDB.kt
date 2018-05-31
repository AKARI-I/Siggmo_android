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

    // 多分このへんが項目名
    @Required
        open var music_name : String = "",
        open var music_phonetic : String = ""
) : RealmObject(){}