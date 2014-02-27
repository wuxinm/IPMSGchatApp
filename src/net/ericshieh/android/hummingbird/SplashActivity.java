package net.ericshieh.android.hummingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class SplashActivity extends Activity {
    public static final String HB_TAG = BaseActivity.HB_TAG;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_activity);
        Log.v(HB_TAG, "splash_start");
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        new InitTask().execute(1);
        /*
                new Handler().postDelayed(new Runnable(){
 
             @Override
             public void run() {
                 
             }
 
            }, 2000);
         */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // android.os.Process.killProcess(android.os.Process.myPid());

    }

    private class InitTask extends AsyncTask<Integer, Integer, Long> {
        protected Long doInBackground(Integer... i) {
            try {

                Thread.sleep(600);

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
            Intent intent=new Intent();
            intent.setClass(SplashActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
            Log.v(HB_TAG, "splash jump to main");

        }
    }
}