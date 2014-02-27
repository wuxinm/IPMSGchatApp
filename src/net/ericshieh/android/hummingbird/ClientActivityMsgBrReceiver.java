
package net.ericshieh.android.hummingbird;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;

public class ClientActivityMsgBrReceiver extends BroadcastReceiver {

    public static final String HB_TAG = HBApplication.HB_TAG;
    private SimpleAdapter clientListAdapter;
    private ArrayList<HashMap<String, String>> clientList;// = new ArrayList<HashMap<String, String>>();
    private HashMap<String,Member> client_data_list;
    private String time;

    public void setClientList(ArrayList<HashMap<String, String>> list, SimpleAdapter adapter) {
        this.clientList = list;
        this.clientListAdapter = adapter;

    }
    
    public void setClientDataList(HashMap<String,Member> list){
        this.client_data_list=list;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        if (intent.getAction().equals(HBApplication.HB_ACTION_BR_ENTRY)) {
            Bundle bundle = intent.getExtras();
            String nickname = bundle.getString("nickname");
            String ip = bundle.getString("address");
            boolean isNew = bundle.getBoolean("isNew");
            boolean isChanged = bundle.getBoolean("isChanged");
            if(isNew){
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("nickname", nickname);
                map.put("ip", ip);
                clientList.add(map);
                clientListAdapter.notifyDataSetChanged();
            }
            /*else{
                clientList = new ArrayList<HashMap<String, String>>();
                for (HashMap.Entry<String, Member> entry : client_data_list.entrySet()) { 
                    Member client = entry.getValue();
                    HashMap<String, String> map1 = new HashMap<String, String>();
                    map1.put("nickname", client.nickname);
                    map1.put("ip", client.address);
                    map1.put("msg", "");
                    map1.put("time", "");
                    clientList.add(map1);
                }
            }*/
            
        } 
        else if (intent.getAction().equals(HBApplication.HB_ACTION_BR_EXIT)) {
            Bundle bundle = intent.getExtras();
            String ip = bundle.getString("address");
            Log.v(HB_TAG, "Find Client exit");
            for(int i = 0; i < clientList.size(); i++){
                HashMap<String, String>  a = clientList.get(i);
                if(a.get("ip").equals(ip)){
                    clientList.remove(i);
                    Log.v(HB_TAG, "Got it! "+a.get("nickname"));
                    break;
                }
            }
            clientListAdapter.notifyDataSetChanged();
        }
        else if (intent.getAction().equals(HBApplication.HB_ACTION_BE_MESSAGE)) {
            
            Bundle bundle = intent.getExtras();
            
            Log.v(HB_TAG, bundle.getString("msg"));
            
            //FOR:
            for(int i = 0; i < clientList.size(); i++){
                HashMap<String, String>  a = clientList.get(i);
                Iterator<String> iterator = a.keySet().iterator();
                while(iterator.hasNext()){
                    Object o = iterator.next();
                    String s = a.get(o);
                    Log.v(HB_TAG, s);
                    if(s.equals(bundle.getString("ip"))){
                        time = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        a.put("time", time);
                        a.put("msg", bundle.getString("msg"));
                        //break FOR;
                        
                    }
                }

//            map.put("nickname", ChatActivity.nickname);

//           clientList.add(map);
//            mlist.add(map);
//            mListAdapter.notifyDataSetChanged();
            }
            clientListAdapter.notifyDataSetChanged();
        }
    }
}
