
package net.ericshieh.android.hummingbird;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class HBDatabase {
    public HBApplication app;

    public static final String HB_DATABASE_NAME = "hb.db";
    public DBHelper dbHelper;
    public SQLiteDatabase db;

    public static final int DB_USER_OFFLINE = 0;
    public static final int DB_USER_ONLINE = 1;

    public static final int DB_MSG_TYPE_DRAFT = 0;
    public static final int DB_MSG_TYPE_SEND = 1;
    public static final int DB_MSG_TYPE_RECEIVE = 2;

    public static final int DB_MSG_STATE_NONE = 0;
    public static final int DB_MSG_STATE_SENDING = 1;
    public static final int DB_MSG_STATE_SENT = 2;
    public static final int DB_MSG_STATE_FAILED = 3;
    public static final int DB_MSG_STATE_OPENED = 4;
    public static final int DB_MSG_STATE_UNREAD = 5;
    public static final int DB_MSG_STATE_READ = 6;

    public static final int DB_FILE_STATE_WAIT = 1;
    public static final int DB_FILE_STATE_TRANS = 2;
    public static final int DB_FILE_STATE_SUCCESS = 3;
    public static final int DB_FILE_STATE_FAILED = 4;
    public static final int DB_FILE_STATE_REJECTED = 5;

    private static HBDatabase hbDB;

    private HBDatabase() {

    }

    public static HBDatabase init(HBApplication app) {
        if (hbDB == null) {
            // System.out.println("new table data!");
            hbDB = new HBDatabase();
            hbDB.app = app;
            hbDB.dbHelper = new DBHelper((Context)app, HB_DATABASE_NAME);

        }
        return hbDB;
    }

    /**
     * 获取HBDatabase单例对象
     * 
     * @return HBDatabase 单例
     */
    public static HBDatabase getInstance() {
        return hbDB;
    }

    public SQLiteDatabase openDB() {
        if (db == null || db.isOpen() == false) {
            db = dbHelper.getWritableDatabase();
        }
        return this.db;
    }

    public void closeDB() {
        if (db != null && db.isOpen()) {
            db.close();
            dbHelper.close();
        }
    }

    public void onlineInitDB() {
        db = openDB();
        // 加入当前自己的用户信息
        Cursor cursor = db.query(DBHelper.TABLENAME_USER, new String[] { "_ID", "name", "ip" },
                "_ID=?", new String[] { "1" }, null, null, null);

        ContentValues values = new ContentValues();
        values.put("name", app.nickname);
        values.put("workgroup", app.workgroup);
        values.put("hostname", app.hostname);
        values.put("ip", app.ipAddress);
        values.put("state", DB_USER_ONLINE);
        values.put("last_active", System.currentTimeMillis());
        if (cursor != null && cursor.getCount() > 0) {
            db.update(DBHelper.TABLENAME_USER, values, "_ID=?", new String[] { "1" });
        } else {
            values.put("_ID", 1);
            db.insert(DBHelper.TABLENAME_USER, null, values);
        }
        // 设置其他用户为OFFLINE
        values = new ContentValues();
        values.put("state", DB_USER_OFFLINE);
        db.update(DBHelper.TABLENAME_USER, values, "_ID<>?", new String[] { "1" });
        cursor.close();
        closeDB();
    }

    public void userOnline(String nickname, String workgroup, String hostname, String ip) {
        /*
                        查询数据库有没有name=nickname,
                            有,把IP改为这个IP,
                            没有,查询是否有ip=当前ip,
                                有,把name改为当前name,
                                没有,新增User,
                        把用户设为online
        */
        db = openDB();
        boolean updateUser = false;
        long user_id = 0;

        Cursor cursor = db.query(DBHelper.TABLENAME_USER, null, "name=?",
                new String[] { nickname }, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            // 有name=nickname
            updateUser = true;
            cursor.moveToNext();
            user_id = cursor.getLong(cursor.getColumnIndex("_ID"));
            // update
        } else {
            cursor.close();
            cursor = db.query(DBHelper.TABLENAME_USER, null,
                    "ip=?", new String[] { ip }, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                // 有ip
                updateUser = true;
                cursor.moveToNext();
                user_id = cursor.getLong(cursor.getColumnIndex("_ID"));
                // update
            } else {
                // insert
            }

        }
        ContentValues values;

        values = new ContentValues();
        values.put("name", nickname);
        values.put("workgroup", workgroup);
        values.put("hostname", hostname);
        values.put("ip", ip);
        values.put("state", DB_USER_ONLINE);
        values.put("last_active", System.currentTimeMillis());
        if (updateUser && user_id != 0) {
            db.update(DBHelper.TABLENAME_USER, values, "_ID=?",
                    new String[] { String.valueOf(user_id) });
        } else {
            db.insert(DBHelper.TABLENAME_USER, null, values);
        }
        cursor.close();
        closeDB();

    }

    public void userOffline(String ip) {
        db = openDB();

        Cursor cursor = db.query(DBHelper.TABLENAME_USER, null, "and ip=?", new String[] { ip },
                null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToNext();
            long user_id = cursor.getLong(cursor.getColumnIndex("_ID"));
            ContentValues values = new ContentValues();
            values.put("state", DB_USER_OFFLINE);
            values.put("last_active", System.currentTimeMillis());
            db.update(DBHelper.TABLENAME_USER, values, "_ID=?",
                    new String[] { String.valueOf(user_id) });
        }
        cursor.close();
        closeDB();

    }

    public void addReceiveMessage(String name, String workgroup, String hostname, String ip,
            String message, FileData[] fileData) {
        db = openDB();
        Cursor cursor = db.query(DBHelper.TABLENAME_USER, null, "ip=?", new String[] { ip }, null,
                null, null);
        long user_id = 0;
        String nickname;
        // 检查是否有该IP用户
        if (cursor != null && cursor.getCount() > 0) {
            // 有
            cursor.moveToNext();
            user_id = cursor.getLong(cursor.getColumnIndex("_ID"));
            nickname = cursor.getString(cursor.getColumnIndex("name"));
            // 检查是否online
            int isOnline = cursor.getInt(cursor.getColumnIndex("state"));
            if (isOnline != DB_USER_ONLINE) {
                ContentValues values = new ContentValues();
                values.put("state", DB_USER_ONLINE);
                values.put("last_active", System.currentTimeMillis());
                db.update(DBHelper.TABLENAME_USER, values, "_ID=?",
                        new String[] { String.valueOf(user_id) });
            }

        } else {
            // 没该ip用户
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("workgroup", workgroup);
            values.put("hostname", hostname);
            values.put("ip", ip);
            values.put("state", DB_USER_ONLINE);
            values.put("last_active", System.currentTimeMillis());
            user_id = db.insert(DBHelper.TABLENAME_USER, null, values);

            nickname = name;

        }
        ContentValues values = new ContentValues();
        values.put("userid", user_id);
        values.put("name", nickname);
        values.put("workgroup", workgroup);
        values.put("hostname", hostname);
        values.put("ip", ip);
        values.put("message", message);
        values.put("type", DB_MSG_TYPE_RECEIVE);
        values.put("state", DB_MSG_STATE_UNREAD);
        values.put("datetime", System.currentTimeMillis());
        long msg_id = db.insert(DBHelper.TABLENAME_MESSAGE, null, values);

        if (fileData != null && fileData.length > 0) {

        }
        cursor.close();
        closeDB();
    }

    public void addSendMessage(String ip, String message, FileData[] fileData) {
        db = openDB();
        Cursor cursor = db.query(DBHelper.TABLENAME_USER, null, "ip=?", new String[] { ip }, null,
                null, null);
        long user_id = 0;
        long msg_id = 0;
        if (cursor != null && cursor.getCount() > 0) {
            // 有
            cursor.moveToNext();
            user_id = cursor.getLong(cursor.getColumnIndex("_ID"));
            String nickname = cursor.getString(cursor.getColumnIndex("name"));
            String workgroup = cursor.getString(cursor.getColumnIndex("workgroup"));
            String hostname = cursor.getString(cursor.getColumnIndex("hostname"));
            
            ContentValues values = new ContentValues();
            values.put("userid", user_id);
            values.put("name", nickname);
            values.put("workgroup", workgroup);
            values.put("hostname", hostname);
            values.put("ip", ip);
            values.put("message", message);
            values.put("type", DB_MSG_TYPE_SEND);
            values.put("state", DB_MSG_STATE_SENDING);
            values.put("datetime", System.currentTimeMillis());
            msg_id = db.insert(DBHelper.TABLENAME_MESSAGE, null, values);
        } else {
            // error
        }
        if (fileData != null && fileData.length > 0) {

        }
        cursor.close();
        closeDB();
    }
    
    public ArrayList<HashMap<String,String>> getUserMessage(String ip){
        db=openDB();
        ArrayList<HashMap<String, String>> list=new ArrayList<HashMap<String,String>>();
        Cursor cursor = db.query(DBHelper.TABLENAME_MESSAGE, null, "ip=?", new String[] { ip }, null,
                null, null);
        while (cursor.moveToNext()) {
            HashMap<String,String> map=new HashMap<String, String>();
            map.put("msg",cursor.getString(cursor.getColumnIndex("message")));
            map.put("datetime",cursor.getString(cursor.getColumnIndex("datetime")));
            map.put("type",cursor.getString(cursor.getColumnIndex("type")));
            list.add(map);

        }
        cursor.close();
        closeDB();
        return list;
    }

    public void addFile(long msg_id, String filename, long filesize) {
        db = openDB();
        ContentValues values = new ContentValues();
        values.put("messageid", msg_id);
        values.put("filename", filename);
        values.put("savename", filename);
        values.put("savepath", "");
        values.put("filesize", filesize);
        values.put("state", DB_FILE_STATE_WAIT);
        values.put("datetime", System.currentTimeMillis());
        values.put("start_time", 0);
        values.put("end_time", 0);
        db.insert(DBHelper.TABLENAME_FILE, null, values);

        closeDB();
    }
    
    
}
