package org.xiaoxingqi.shengxi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.xiaoxingqi.shengxi.model.CacheBean;
import org.xiaoxingqi.shengxi.utils.IConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VoiceCacheDao {
    private VoiceCacheDB db;

    public VoiceCacheDao(Context context) {
        db = new VoiceCacheDB(context);
    }

    /**
     * 插入数据
     */
    public synchronized boolean insetData(String userId, String url, int type, String typeId, String topicId, String topicName, String imgs, String resourceId, String score, String voiceLength) {
        SQLiteDatabase database = db.getWritableDatabase();
        ContentValues value = new ContentValues();
        value.put("userId", userId);
        value.put("url", url);
        value.put("time", System.currentTimeMillis());
        value.put("type", type);
        value.put("typeId", typeId);
        value.put("voiceLength", voiceLength);
        if (!TextUtils.isEmpty(score)) {
            value.put("score", score);
        }
        if (!TextUtils.isEmpty(imgs)) {
            value.put("imgs", imgs);
        }
        if (!TextUtils.isEmpty(topicId)) {
            value.put("topicId", topicId);
        }
        if (!TextUtils.isEmpty(topicName)) {
            value.put("topicName", topicName);
        }
        if (!TextUtils.isEmpty(resourceId)) {
            value.put("resourceId", resourceId);
        }
        long insert = database.insert(IConstant.TABLENAME, null, value);
        if (insert == 1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 删除某一条数据
     *
     * @param key
     * @return
     */
    public boolean delete(String key) {
        if (isExistsInTable(key)) {
            SQLiteDatabase database = db.getWritableDatabase();
            int delete = database.delete(IConstant.TABLENAME, "url=?", new String[]{key});
            return delete != -1;
        } else {
            return false;
        }
    }

    /**
     * 查询本地数据库
     *
     * @return
     */
    public List<CacheBean> getDate(String uid) {
        try {
            Cursor cursor = db.getReadableDatabase().query(IConstant.TABLENAME, new String[]{}, "userId=?", new String[]{uid}, null, null, "_id desc");
            List<CacheBean> list = null;
            if (null != cursor) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String file = cursor.getString(cursor.getColumnIndex("url"));
                    if (isExistsInFile(file)) {//判断文件是否存在
                        CacheBean bean = new CacheBean();
                        bean.setTime(cursor.getLong(cursor.getColumnIndex("time")));
                        bean.setImages(cursor.getString(cursor.getColumnIndex("imgs")));
                        bean.setTopicId(cursor.getString(cursor.getColumnIndex("topicId")));
                        bean.setTopicName(cursor.getString(cursor.getColumnIndex("topicName")));
                        bean.setType(cursor.getInt(cursor.getColumnIndex("type")));
                        bean.setTypeId(cursor.getString(cursor.getColumnIndex("typeId")));
                        bean.setResourceId(cursor.getString(cursor.getColumnIndex("resourceId")));
                        bean.setScore(cursor.getString(cursor.getColumnIndex("score")));
                        bean.setVoiceLength(cursor.getString(cursor.getColumnIndex("voiceLength")));
                        bean.setUrl(file);
                        list.add(bean);
                    } else {
                        continue;
                    }
                }
                cursor.close();
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 判断此文件是否保存
     *
     * @return
     */
    private boolean isExistsInTable(String key) {
        SQLiteDatabase database = db.getReadableDatabase();
        Cursor cursor = database.query(IConstant.TABLENAME, null, "url=?", new String[]{key}, null, null, null);
        boolean toNext = cursor.moveToNext();
        cursor.close();
        return toNext;
    }

    /**
     * 判断文件是否存在
     *
     * @return
     */
    private boolean isExistsInFile(String key) {
        File file = new File(key);
        return file.exists();
    }
}
