
package net.ericshieh.android.hummingbird;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;

    public static final String TABLENAME_USER = "User";
    public static final String TABLENAME_MESSAGE = "Message";
    public static final String TABLENAME_FILE = "File";

    private static final String DB_INIT_USER = "CREATE TABLE User(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "name TEXT, workgroup TEXT, hostname TEXT, ip TEXT, state INTEGER, last_active TEXT);";
    private static final String DB_INIT_MESSAGE = "CREATE TABLE Message(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "userid INTEGER, name TEXT, workgroup TEXT, hostname TEXT, ip TEXT, message TEXT, type INTEGER, state INTEGER, datetime TEXT);";
    private static final String DB_INIT_FILE = "CREATE TABLE File(_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "messageid INTEGER, filename TEXT, savename TEXT, savepath TEXT, filesize REAL, state INTEGER, datetime TEXT, start_time TEXT, end_time TEXT);";

    public DBHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    public DBHelper(Context context, String name) {
        this(context, name, VERSION);
    }

    public DBHelper(Context context, String name, int version) {
        this(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_INIT_USER);
        db.execSQL(DB_INIT_MESSAGE);
        db.execSQL(DB_INIT_FILE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Log.d("ESDebug", "upgrade!");
    }

}
