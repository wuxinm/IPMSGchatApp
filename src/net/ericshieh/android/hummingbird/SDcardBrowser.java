package net.ericshieh.android.hummingbird;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SDcardBrowser extends Activity implements OnFileBrowserListener
{
    private Button selectButton = null;
    private String fileMsg;
    private String SDpath;
    private String judge;
	@Override
	public void onFileItemClick(String filename)
	{
		setTitle(filename);
        SDpath = filename;
	}

	@Override
	public void onDirItemClick(String path)
	{
		setTitle(path);		
		SDpath = path;
	}
 
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sdcardbrowser);
		selectButton = (Button)findViewById(R.id.btn_SDpathSelect);
		FileBrowser fileBrowser = (FileBrowser)findViewById(R.id.filebrowser);
		fileBrowser.setOnFileBrowserListener(this);
		Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle.getInt("judge") == 1) {
        fileMsg = bundle.getString("fileMsg");
		System.out.println("!!!!!!!!");
		selectButton.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                
                Intent intent = new Intent();
                intent.putExtra("path", SDpath);
                intent.putExtra("fileMsg", fileMsg);
                    setResult(1, intent);
                finish();
            }
        });
	}
        else if (bundle.getInt("judge") == 2){
            selectButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub

                    Intent intent = new Intent();
                    intent.putExtra("path", SDpath);
                    setResult(2, intent);
                    finish();
                }
            });
        }
    }
}
