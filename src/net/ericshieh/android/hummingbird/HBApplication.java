
package net.ericshieh.android.hummingbird;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

public class HBApplication extends Application {
    public static final String HB_TAG = BaseActivity.HB_TAG;

    public static final String HB_ACTION_BR_ENTRY = "hummingbird.ui_br_entry";
    public static final String HB_ACTION_BR_EXIT = "hummingbird.ui_br_exit";
    public static final String HB_ACTION_BE_MESSAGE = "hummingbird.ui.br_message";
    public static final String HB_ACTION_BE_PROGRESS_UPDATE = "hummingbird.ui.br_progress_update";
    public static final String HB_ACTION_BE_REC_MESSAGE = "hummingbird.ui.br_rec_message";

    public static final int HB_SOCKET_PORT = 2425;

    public NotifyManager mNotifyMgr;
    private int mActivities = 0;
    private boolean isLogin = false;
    private boolean isCfgChanged = false;

    public SharedPreferences settings;
    
    public HBDatabase hbDB;

    public static final int STATE_BG = 0;
    public static final int STATE_FG = 1;
    private int mLastState = STATE_FG;

    public HashMap<String, Member> client_list;
    public ArrayList<FileData> file_list_receive;
    // public ArrayList<FileData> file_list_send;

    // settings
    public String nickname;
    public String workgroup;
    public String hostname;
    public String ipAddress;

    @Override
    public void onCreate() {
        Log.v(HB_TAG, "app start");
        this.mNotifyMgr = new NotifyManager(this);
        this.client_list = new HashMap<String, Member>();

        this.hbDB = HBDatabase.init(this);

        this.settings = PreferenceManager.getDefaultSharedPreferences(this);

        String nicknameKey = getResources().getString(R.string.nickname_key);
        String workgroupKey = getResources().getString(R.string.workgroup_key);
        this.nickname = settings.getString(nicknameKey, "");
        this.workgroup = settings.getString(workgroupKey, "");

        super.onCreate();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
        this.isCfgChanged = true;
        Log.v(HB_TAG, "Config changed");
    }

    public void setLogin(boolean login) {
        this.isLogin = login;
    }

    public void setCfgChanged(boolean value) {
        this.isCfgChanged = value;
    }

    public void addActivity() {
        this.mActivities++;
        Log.v(HB_TAG, "+++,now:" + this.mActivities);
    }

    public void removeActivity() {
        this.mActivities--;
        Log.v(HB_TAG, "---,now:" + this.mActivities);
    }

    public int getState() {
        return mActivities <= 0 ? STATE_BG : STATE_FG;

    }

    public void showHBNotify(Context context, boolean isShow) {
        if (this.mLastState == STATE_FG && this.getState() == STATE_BG && this.isLogin
                && !this.isCfgChanged) {
            this.mNotifyMgr.showHBNotify(context, true);
            Log.v(HB_TAG, "show notify");
        } else if (this.mLastState == STATE_BG && this.getState() == STATE_FG) {
            this.mNotifyMgr.showHBNotify(context, false);
            Log.v(HB_TAG, "hide notify");
        }
        this.mLastState = this.getState();
    }

    public boolean addClient(Member client) {
        boolean isNew = false;
        if (!client_list.containsKey(client.address)) {
            isNew = true;
        }
        client_list.put(client.address, client);
        return isNew;
    }

    public boolean removeClient(Member client) {
        client_list.remove(client.address);
        return true;
    }

}
