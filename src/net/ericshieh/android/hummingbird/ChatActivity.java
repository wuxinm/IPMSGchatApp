package net.ericshieh.android.hummingbird;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import net.ericshieh.android.hummingbird.ipmsg.IPMSG;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ChatActivity extends BaseActivity {
    
    public static final int MENU_SENDFILE = 0;
    private TextView mLabel_usernickname;
    
    private ListView mListView_chatlog;
    private ArrayList<HashMap<String, String>> mlist;
    SimpleAdapter mListAdapter;
    
    private EditText mEdit_msg;
    private Button mBtn_send;
    private int i;
    private String hostnickname;
    static String nickname;
    private String ip;
    private String time;
    private String msg;
    
    public HBDatabase hbDB;
    private String fileSize;
    private int hour, minute, second;
    private ProgressDialog progressDialog;
    
    MessageBroadcastReceiver msgBrReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);
        
        //设置聊天标题
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        Resources res = getResources();
        String nicknameKey = getResources().getString(R.string.nickname_key);
        hostnickname = settings.getString(nicknameKey, "");
        
        hbDB=HBDatabase.getInstance();
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        nickname = bundle.getString("nickname");
        ip = bundle.getString("ip");
        msg = bundle.getString("msg");
        time = bundle.getString("time");
        
        String title_string = String.format(res.getString(R.string.chat_title_user), nickname);
        this.setTitle(title_string);
        
        // 设置界面组件
        mLabel_usernickname=(TextView)findViewById(R.id.label_user_nickname);
        
        mListView_chatlog = (ListView)findViewById(R.id.listview_chatlog);
        mListView_chatlog.setScrollbarFadingEnabled(false);
        mListView_chatlog.setOnItemClickListener(new ChatlogOnClickListener());
        
        mlist = new ArrayList<HashMap<String, String>>();
        
        getUserMessage(ip,mlist);

        mListAdapter = new SimpleAdapter(this, mlist, R.layout.chatlog_item_view,
                new String[] { "nickname", "time", "msg" }, new int[] { R.id.label_chatlog_name,
                        R.id.label_chatlog_time, R.id.label_chatlog_msg });
        mListView_chatlog.setAdapter(mListAdapter);
        
        /*
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("nickname", nickname);
        map.put("time", time);
        map.put("msg", msg);
        mlist.add(map);
        */
        
        mEdit_msg=(EditText)findViewById(R.id.edit_sendmsg);
        mBtn_send=(Button)findViewById(R.id.btn_sendmsg);
        
        mBtn_send.setOnClickListener(new SendOnClickListener());
        
        msgBrReceiver = new MessageBroadcastReceiver();
        msgBrReceiver.setChatList(mlist, mListAdapter);
    }
    private void getUserMessage(String ip,ArrayList<HashMap<String, String>> mlist){
        ArrayList<HashMap<String, String>> dbList = hbDB.getUserMessage(ip);
        for (HashMap<String, String> hashMap : dbList) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("nickname", Integer.valueOf(hashMap.get("type"))==HBDatabase.DB_MSG_TYPE_RECEIVE?nickname:hostnickname);
            Log.v(HB_TAG, hashMap.get("datetime"));
            long ltime= Long.valueOf(hashMap.get("datetime"));
            
            Date date = new Date(ltime/1000);
            String time = new SimpleDateFormat("HH:mm:ss").format(date);
            map.put("time", time);
            map.put("msg", hashMap.get("msg"));
            mlist.add(map);
        }
        
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }
    
    
    class ChatlogOnClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
            /*Intent intent=new Intent();
            intent.setClass(ChatActivity.this,ChatActivity.class);
            startActivity(intent);*/
            String str = ((TextView)view.findViewById(R.id.label_chatlog_msg)).getText().toString();
            fileSize = str.substring(str.lastIndexOf("|") + 1);
            Intent intent = new Intent();
            intent.putExtra("fileMsg", str);
            intent.putExtra("judge", 1);
            intent.setClass(ChatActivity.this, SDcardBrowser.class);
            startActivityForResult(intent, 1); 

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if (resultCode == 1) {
            Parcel inSend = Parcel.obtain();
            Bundle sendBundle = new Bundle();
            sendBundle.putString("ip", ip);
            sendBundle.putString("fileMsg", data.getStringExtra("fileMsg"));
            sendBundle.putString("path", data.getStringExtra("path"));
            inSend.writeBundle(sendBundle);
            int inCode = 2;
            try {
                msgBinder.transact(inCode, inSend, null, IBinder.FLAG_ONEWAY);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            showProgressDialog(ProgressDialog.STYLE_HORIZONTAL);

        } else if (resultCode == 2) {
            Parcel inSend = Parcel.obtain();
            Bundle sendBundle = new Bundle();
            sendBundle.putString("ip", ip);
            sendBundle.putString("msg", "sendFile");
            sendBundle.putString("path", data.getStringExtra("path"));
            inSend.writeBundle(sendBundle);
            int inCode = 3;
            try {
                msgBinder.transact(inCode, inSend, null, IBinder.FLAG_ONEWAY);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void showProgressDialog(int style) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setIcon(R.drawable.folder);
        progressDialog.setTitle("正在传输文件...");
        progressDialog.setMessage("请稍后...");
        progressDialog.setProgressStyle(style);
        progressDialog.setMax(Integer.valueOf(fileSize));

        progressDialog.setButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
            }
        });
        msgBrReceiver.setProgressDialog(progressDialog);
        progressDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SENDFILE, 0, R.string.menu_sendfile);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SENDFILE:
                Intent intent = new Intent();
                intent.putExtra("judge", 2);
                intent.setClass(ChatActivity.this, SDcardBrowser.class);
                startActivityForResult(intent, 2);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(HBApplication.HB_ACTION_BE_MESSAGE);
        filter.addAction(HBApplication.HB_ACTION_BE_PROGRESS_UPDATE);
        filter.addAction(HBApplication.HB_ACTION_BE_REC_MESSAGE);
        this.registerReceiver(msgBrReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(msgBrReceiver);
    }

    class SendOnClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
           /* i = 0;            
            task = new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Message message = new Message();
                    message.what = i;
                    System.out.println("timer:----" + i);
                    handler.sendMessage(message);
                    i = i + 1; 
                }
            };*/
            HashMap<String, String> map = new HashMap<String, String>();
            time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            map.put("nickname", hostnickname);
            map.put("time", time);
            map.put("msg", mEdit_msg.getEditableText().toString());
            mlist.add(map);
            mListView_chatlog.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            mListAdapter.notifyDataSetChanged();
            mListView_chatlog.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
            // mListView_chatlog.setSelection(mlist.size());

            Parcel inSend = Parcel.obtain();
            Bundle sendBundle = new Bundle();
            sendBundle.putString("ip", ip);
            sendBundle.putString("msg", mEdit_msg.getEditableText().toString());
            mEdit_msg.setText(null);
            inSend.writeBundle(sendBundle);
            int inCode = 1;
            try {
                msgBinder.transact(inCode, inSend, null, IBinder.FLAG_ONEWAY);
            } catch (RemoteException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
//            timer.schedule(task, 1000, 1000);
            
//            msgBrReceiver.setTimer(timer);

        }

    }

}
