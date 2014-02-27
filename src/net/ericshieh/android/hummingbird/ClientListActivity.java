
package net.ericshieh.android.hummingbird;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ClientListActivity extends BaseActivity {
    
    private ListView mListView_client;
    private Button btn_refresh;
    
    public static final int MENU_ABOUT = 0;
    public static final int MENU_OPTIONS = 1;
    public static final int MENU_QUIT = 2;
    private boolean force_quit = false;

    private static final int REQ_SYSTEM_SETTINGS = 0;
    
    private TextView ipTextView;
    private TextView nicknameTextView;
    private TextView msgTextView;
    private TextView timeTextView;
    
    MessageBroadcastReceiver msgBrReceiver;
    ClientActivityMsgBrReceiver cMsgBrReceiver;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.client_list_activity);
        

        // 设置界面组件
        mListView_client = (ListView)findViewById(R.id.listview_client);
        mListView_client.setOnItemClickListener(new ClientOnClickListener());

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        
        HashMap<String, String> map1 = new HashMap<String, String>();
        //map1.put("nickname", "xxfh9382yy298r992t017tr02fsguifgis");
       // map1.put("ip", "123");
        map1.put("nickname", "yy");
        map1.put("ip", "456");
        map1.put("msg", "hello world!!!!!");
        map1.put("time", "22:12:22");
       // list.add(map1);
        list.add(map1);
        
        SimpleAdapter listAdapter = new SimpleAdapter(this, list, R.layout.client_item_view,
                new String[] { "nickname", "ip", "msg", "time" }, new int[] { R.id.label_clientlist_nickname,
                        R.id.label_clientlist_ip, R.id.label_clientlist_msg, R.id.label_clientlist_time });
        mListView_client.setAdapter(listAdapter);

        btn_refresh=(Button)findViewById(R.id.btn_refresh_clients);
        btn_refresh.setOnClickListener(new RefreshOnClickListener());
        
//        msgBrReceiver = new MessageBroadcastReceiver();
//        msgBrReceiver.setClientList(list, listAdapter);
        
        cMsgBrReceiver = new ClientActivityMsgBrReceiver();
        cMsgBrReceiver.setClientList(list, listAdapter);
        cMsgBrReceiver.setClientDataList(app.client_list);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter= new IntentFilter();
        filter.addAction(HBApplication.HB_ACTION_BR_ENTRY);
        filter.addAction(HBApplication.HB_ACTION_BR_EXIT);
        filter.addAction(HBApplication.HB_ACTION_BE_MESSAGE);
        this.registerReceiver(cMsgBrReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(cMsgBrReceiver);
    }
    

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        
        if(force_quit){
            Intent intent=new Intent();
            intent.setClass(ClientListActivity.this, MessageService.class);
            stopService(intent);
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_ABOUT, 0, R.string.menu_about);
        menu.add(0, MENU_OPTIONS , 0, R.string.menu_options);
        menu.add(0, MENU_QUIT, 0, R.string.menu_quit);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ABOUT:
                Toast.makeText(this, R.string.menu_about, Toast.LENGTH_SHORT).show();
                break;
            case MENU_OPTIONS:
                Toast.makeText(this, R.string.menu_options, Toast.LENGTH_SHORT).show();
                startActivityForResult(new Intent(this, HBPreferenceActivity.class), REQ_SYSTEM_SETTINGS);
                break;
            case MENU_QUIT://123
                // Toast.makeText(this,
                // R.string.menu_quit,Toast.LENGTH_SHORT).show();
                force_quit=true;
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    class ClientOnClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
            ipTextView = (TextView)view.findViewById(R.id.label_clientlist_ip);
            nicknameTextView = (TextView)view.findViewById(R.id.label_clientlist_nickname);
            msgTextView = (TextView)view.findViewById(R.id.label_clientlist_msg);
            timeTextView = (TextView)view.findViewById(R.id.label_clientlist_time);
            Intent intent=new Intent();
            intent.putExtra("ip", ipTextView.getText().toString());
            intent.putExtra("nickname", nicknameTextView.getText().toString());
            intent.putExtra("msg", msgTextView.getText().toString());
            intent.putExtra("time", timeTextView.getText().toString());
            intent.setClass(ClientListActivity.this,ChatActivity.class);
            startActivity(intent);

        }

    }
    
    class RefreshOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            Parcel inSend = Parcel.obtain();
            int inCode = 0;
            try {
                msgBinder.transact( inCode , inSend , null , IBinder.FLAG_ONEWAY );
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }
        
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_SYSTEM_SETTINGS) {
            // 获取设置界面PreferenceActivity中各个Preference的值
            String nicknameKey = getResources().getString(R.string.nickname_key);
            String workgroupKey = getResources().getString(R.string.workgroup_key);
            String historyCheckKey = getResources().getString(R.string.history_check_key);
            String advancedMessageCheckKey = getResources().getString(R.string.advanced_message_check_key);
            String messageOvertimeLimitKey = getResources().getString(
                    R.string.message_overtime_limit_key);
            String Message_retry_times_key = getResources().getString(
                    R.string.message_retry_times_key);

            // 取得属于整个应用程序的SharedPreferences
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

            app.nickname = settings.getString(nicknameKey, "");
            app.workgroup = settings.getString(workgroupKey, "");
            Boolean historyCheck = settings.getBoolean(historyCheckKey, true);
            Boolean messageSwitch = settings.getBoolean(advancedMessageCheckKey, true);
            String messageOvertimeLimit = settings.getString(messageOvertimeLimitKey, "10");
            String messageRetryTimes = settings.getString(Message_retry_times_key, "3");
            // 打印结果
            Log.v("nickname:", app.nickname.toString());
            Log.v("workgroup:", app.workgroup.toString());
            Log.v("historySwitch:", historyCheck.toString());
            Log.v("messageSwitch:", messageSwitch.toString());
            Log.v(" messageOvertimeLimitValue", messageOvertimeLimit);
            Log.v("messageRetryTimesValue", messageRetryTimes);
        } else {
            // 其他Intent返回的结果
        }
    }

}
