package org.xiaoxingqi.shengxi.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

import org.xiaoxingqi.shengxi.utils.IConstant;

/**
 * 未发的语音本地缓存文件
 */
public class VoiceCacheDB extends SQLiteOpenHelper {
    /**
     * 建库
     *
     * @param context
     */
    public VoiceCacheDB(@Nullable Context context) {
        super(context, "voiceCache.db", null, 8);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * 建表字段  路径 保存时间  发布的类型(唱回忆, 书评, 影评,正常语音(是否带话题, 是否带图片)),  判断是否删除或更改图片
         */
        db.execSQL("create table " + IConstant.TABLENAME + " (_id Integer primary key autoincrement,userId varchar(10),url text,time long,type Integer,typeId varchar(10),topicId varchar(10),topicName varchar(20),imgs text,resourceId varchar(10),score varchat(3),voiceLength varchat(3))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
