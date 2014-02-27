
package net.ericshieh.android.hummingbird;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BaseActivity extends Activity {
    public static final String HB_TAG = "HB";
    public HBApplication app;

    ServiceConnection mServiceConnection = new HBServiceConnection();
    MessageService msgService;
    MessageService.MsgBinder msgBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (HBApplication)getApplication();
        app.setCfgChanged(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, MessageService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.v(HB_TAG, "bind service");
    }

    @Override
    protected void onResume() {
        super.onResume();
        //app.addActivity();
        //app.showHBNotify(this, false);

    }

    @Override
    protected void onStop() {
        super.onStop();
        //app.removeActivity();
        //app.showHBNotify(this, true);
        
        if(msgBinder!=null){
            unbindService(mServiceConnection);
            Log.v(HB_TAG, "unbind service");
        }
        
    }

    class HBServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            msgBinder = (MessageService.MsgBinder)service;
            msgService = msgBinder.getService();
            Log.v(HB_TAG, "msgService connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            msgService = null;
            msgBinder = null;
            Log.v(HB_TAG, "msgService disconnected");
        }

    }
}
