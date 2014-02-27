/*
* IPMSG - Java Version
*
* Copyright (C) 2008 FF <lemonutzf@hotmail.com>
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
*/


package net.ericshieh.android.hummingbird;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import net.ericshieh.android.hummingbird.ipmsg.IPMSG;

public class AutoPipe {

	protected InputStream input = null;
	
	protected InputStreamReader reader = null;

	protected OutputStream output = null;

	protected PipeThread thread = null;

	protected int piped = 0;

	protected boolean completed = false;

	protected boolean stoped = false;
	
	protected boolean closed = false;

	protected Exception error = null;
	
	protected long fileSize;
	
	protected byte[] buffer = new byte[IPMSG.MAX_SOCKBUF];
	
    public ProgressUpdate pro;

    private long startTime;
    private long currentTime;
    private int speed;
	public AutoPipe(InputStream input, OutputStream output) {
		if (input == null || output == null)
			throw new NullPointerException();
		this.input = input;
		this.output = output;
		thread = new PipeThread();
	}

	public void start() {
		stoped = false;
		thread.start();
	}

	public void stop() {
		stoped = true;
	}

	public void close() throws IOException {
        if (closed)
            return;
		IOException exception = null;
		try{
			input.close();
		}catch(IOException e){
			exception = e;
		}
		closed = true;
		output.close();
		if(exception!=null)
			throw exception;
	}

	public boolean completed() {
		return completed;
	}
	
	public boolean closed(){
		return closed;
	}

	public int piped() {
		return piped;
	}

	public Exception error() {
		return error;
	}
	
	protected void finalize() throws Throwable{
		close();
	}

	private class PipeThread extends Thread {
		
		public void run() {    
            System.out.println("start run");
			completed = false;
			byte[] buffer = new byte[IPMSG.MAX_SOCKBUF];
			int readlen = -1;
			stoped = false;
			try {
                startTime = System.currentTimeMillis();
				while (!stoped) {
					// 读数据
					readlen = input.read(buffer);
					// 到达数据尾部，执行finally 之后结束
					if(readlen==-1){
						completed = true;
						break;
					}
					
					//System.out.println("从网络读取了 " + readlen + " 个字节");

					// 写数据
					output.write(buffer, 0, readlen);	
					piped += readlen;
					
                    currentTime = System.currentTimeMillis();  
                    int i = (int)(currentTime - startTime);
                    
                    if (i != 0) {
                        speed = (readlen / i)*1000/1024;
                    }

                    startTime = currentTime;
                    if (pro != null)
                        pro.update(piped, speed);
                    // 如果无数据可读， flush output
                    if (input.available() == 0)
                        output.flush();
                }
                System.out.println("!!!!!!!!" + System.currentTimeMillis());
                System.out.println("!!!!!!!!" + piped);
            } catch (IOException e) {
                error = e;
                e.printStackTrace();
			} finally {
				try {
					close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		
	}
}
