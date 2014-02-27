package net.ericshieh.android.hummingbird;

import java.util.HashMap;
import java.util.Map;

import net.ericshieh.android.hummingbird.MessageService.MsgBinder;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

public class MainActivity extends Activity implements OnTouchListener,
OnFocusChangeListener, OnKeyListener {
    public static final String HB_TAG = BaseActivity.HB_TAG;
    
    HBApplication app;
    EditText loginNickname;
    EditText loginGroupname;
    //temp
    ImageButton mBtn_login=null;
    private Map<Integer, int[]> drawableIds = new HashMap<Integer, int[]>();
	private View lastFocusview;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v(HB_TAG, "main_start");
        loginNickname=(EditText)findViewById(R.id.login_nickname);
        loginGroupname=(EditText)findViewById(R.id.login_groupname);;
        
        drawableIds.put(R.id.btn_login, new int[] { R.drawable.login1,
				R.drawable.login2 });
		app = (HBApplication) getApplication();
        
		mBtn_login = (ImageButton) findViewById(R.id.btn_login);
		mBtn_login.setOnTouchListener(this);
		mBtn_login.setOnFocusChangeListener(this);
		mBtn_login.setOnKeyListener(this);
		mBtn_login.setOnClickListener(new LoginOnClickListener());
		lastFocusview = mBtn_login;
		
		
		String nicknamekey = getResources().getString(R.string.nickname_key);
		String workgroupKey = getResources().getString(R.string.workgroup_key);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        String nicknameStr = settings.getString(nicknamekey , "");
        String workgroupStr=settings.getString(workgroupKey , "");
		loginNickname.setText(nicknameStr);
	    loginGroupname.setText(workgroupStr);
	     
	     
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        //new InitTask().execute(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // android.os.Process.killProcess(android.os.Process.myPid());
        
        Intent intent=new Intent();
        intent.setClass(MainActivity.this, MessageService.class);
        stopService(intent);
        android.os.Process.killProcess(android.os.Process.myPid());

    }

    @Override
    protected void onResume() {
        super.onResume();
        app.setLogin(false);
    }

    @Override
    protected void onStop() {
        app.setLogin(true);
        super.onStop();
    }

    private class InitTask extends AsyncTask<Integer, Integer, Long> {
        protected Long doInBackground(Integer... i) {
            try {

                Thread.sleep(100);

            } catch (InterruptedException e) {

                e.printStackTrace();

            }

            return (long)1;
        }

        protected void onProgressUpdate(Integer... progress) {
            //publishProgress 更新进度时调用
        }

        protected void onPostExecute(Long result) {
            //处理完成,可以跳转了.
            finish();
            Intent intent=new Intent();
            intent.setClass(MainActivity.this,ClientListActivity.class);
            startActivity(intent);
            Log.v(HB_TAG, "jump to list");
            

        }
    }
    
    class LoginOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent=new Intent();
            intent.setClass(MainActivity.this,ClientListActivity.class);
            startActivity(intent);
            
            
            intent=new Intent();
            intent.setClass(MainActivity.this, MessageService.class);
            startService(intent);
            
            
            Log.v(HB_TAG, "jump to list");
        }
        
    }
    @Override
	public void onFocusChange(View view, boolean hasFocus) {
		lastFocusview.setBackgroundResource(drawableIds.get(lastFocusview
				.getId())[0]);
		view.setBackgroundResource(drawableIds.get(view.getId())[1]);
		lastFocusview = view;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		lastFocusview.setBackgroundResource(drawableIds.get(lastFocusview
				.getId())[0]);
		if (event.getAction() == MotionEvent.ACTION_UP) {
			view.setBackgroundResource(drawableIds.get(view.getId())[0]);
		} else if (event.getAction() == MotionEvent.ACTION_DOWN)
			view.setBackgroundResource(drawableIds.get(view.getId())[1]);

		return false;
	}

	@Override
	public boolean onKey(View view, int keyCode, KeyEvent event) {
		if (KeyEvent.ACTION_DOWN == event.getAction())
			view.setBackgroundResource(drawableIds.get(view.getId())[2]);
		else if (KeyEvent.ACTION_UP == event.getAction())
			view.setBackgroundResource(drawableIds.get(view.getId())[1]);
		return false;
	}
}