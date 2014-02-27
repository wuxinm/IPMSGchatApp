
package net.ericshieh.android.hummingbird;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;

public class MessageBroadcastReceiver extends BroadcastReceiver {
    public static final String HB_TAG = HBApplication.HB_TAG;
    
    private SimpleAdapter clientListAdapter;
    private SimpleAdapter mListAdapter;
    private ArrayList<HashMap<String, String>> clientList = new ArrayList<HashMap<String, String>>();
    private ArrayList<HashMap<String, String>> mlist = new ArrayList<HashMap<String, String>>();
    private ProgressDialog progressDialog;
    private Timer timer;
    private String time;
    private int hour, minute, second;

    public void setClientList(ArrayList<HashMap<String, String>> list, SimpleAdapter adapter) {
        this.clientList=list;
        this.clientListAdapter = adapter;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }
    
    public void setTimer(Timer timer) {
        this.timer = timer;
    }
    
    public void setChatList(ArrayList<HashMap<String, String>> list, SimpleAdapter adapter){
        this.mlist = list;
        this.mListAdapter = adapter;
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(HB_TAG, "msgReceived");
        if (intent.getAction().equals(HBApplication.HB_ACTION_BR_ENTRY)) {
            Bundle bundle = intent.getExtras();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("nickname", bundle.getString("nickname"));
            map.put("ip", bundle.getString("address"));
            clientList.add(map);
            clientListAdapter.notifyDataSetChanged();
        }
        else if (intent.getAction().equals(HBApplication.HB_ACTION_BE_MESSAGE)) {
            
            time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            Bundle bundle = intent.getExtras();
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("nickname", ChatActivity.nickname);
            map.put("time", time);
            map.put("msg", bundle.getString("msg"));
            mlist.add(map);
            mListAdapter.notifyDataSetChanged();
        }

        else if (intent.getAction().equals(HBApplication.HB_ACTION_BE_PROGRESS_UPDATE)) {
            Bundle bundle = intent.getExtras();
            int progress = bundle.getInt("progress");
            int speed = bundle.getInt("speed");
            Log.v(HB_TAG, "speed:" + speed);
            Log.v(HB_TAG, "pro:" + progress);
            if (progressDialog.getProgress() != progressDialog.getMax()) {
                progressDialog.setMessage("�����ٶ�:" + speed + "kb/s");
                progressDialog.setProgress(progress);
                if(progress == progressDialog.getMax()){
                    progressDialog.setMessage("�������");
                }
            }

        }
        else if (intent.getAction().equals(HBApplication.HB_ACTION_BE_REC_MESSAGE)){
            System.out.println("not stop");
            timer.cancel();
        }
    }

}
