
package net.ericshieh.android.hummingbird;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;

import net.ericshieh.android.hummingbird.ipmsg.IPMSG;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class MessageService extends Service {
    public static final String HB_TAG = BaseActivity.HB_TAG;

    public HBApplication app;
    public NotifyManager mNotifyMgr;
    public HBDatabase hbDB;

    public HashMap<String, Member> client_list;

    private final MsgBinder msgBinder = new MsgBinder();

    private Communication comm;

    private Thread serverThread;

    // private ServerAsyncTask serverAsyncTask;

    private ProgressUpdate prolis;
    
    private ReceiveMsgListener rec;
    @Override
    public void onCreate() {
        super.onCreate();
        app = (HBApplication)getApplication();
        mNotifyMgr = app.mNotifyMgr;
        client_list = app.client_list;
        hbDB = app.hbDB;
        

        Log.v(HB_TAG, "service created");
        startForeground(mNotifyMgr.NOTI_HB, mNotifyMgr.getHBNotification());
    }

    @Override
    public void onDestroy() {
        Log.v(HB_TAG, "service destroyed");

        comm.serverRunnable.setRunning(false);
        try {
            comm.brExit();
        } catch (IOException e) {
            e.printStackTrace();
            serverThread.interrupt();

        }
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        Log.v(HB_TAG, "service: config changed");
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(HB_TAG, "service start cmd");

        comm = new Communication(app.nickname, app.workgroup);
        InetAddress brAddress = getBroadcastAddress();
        comm.setBrInetAddress(brAddress);
        app.hostname = brAddress.getHostName();
        app.ipAddress = getLocalIpAddress();
        comm.hostname=app.hostname;
        
        hbDB.onlineInitDB();
        
        Log.v(HB_TAG, "ip:"+app.ipAddress+", hostname:"+app.hostname+", workgroup:"+app.workgroup);

        comm.addEntryListener(new EntryListener() {

            @Override
            public void memberEntry(MemberEvent event) {
                Log.v(HB_TAG, "---memberEntry");
                hbDB.userOnline(event.member.nickname, event.member.groupname, event.member.hostname, event.member.address);
                boolean isNew = app.addClient(event.member);
                Intent intent = new Intent();
                intent.putExtra("isNew", isNew);
                intent.putExtra("nickname", event.member.nickname);
                intent.putExtra("address", event.member.address);
                intent.setAction(HBApplication.HB_ACTION_BR_ENTRY);
                MessageService.this.sendBroadcast(intent);
                
            }
        });

        comm.addExitListener(new ExitListener() {

            @Override
            public void memberExit(MemberEvent event) {
                // TODO Auto-generated method stub
                
                hbDB.userOffline(event.member.address);
                
                app.removeClient(event.member);
                Intent intent = new Intent();
                intent.putExtra("nickname", event.member.nickname);
                intent.putExtra("address", event.member.address);
                intent.setAction(HBApplication.HB_ACTION_BR_EXIT);
                MessageService.this.sendBroadcast(intent);
            }
        });

        comm.addMessageListener(new MessageListener() {

            public void messageReceive(MessageEvent event) {
                // TODO Auto-generated method stub
                hbDB.addReceiveMessage(event.member.name, event.member.groupname, event.member.hostname, event.member.address, event.message, null);
                
                Intent intent = new Intent();
                intent.putExtra("ip",event.member.address);
                intent.putExtra("msg", event.message);
                intent.putExtra("nickname", event.member.nickname);
                intent.setAction(HBApplication.HB_ACTION_BE_MESSAGE);
                MessageService.this.sendBroadcast(intent);
                
            }

        });
        
        comm.rec = new ReceiveMsgListener(){

            @Override
            public void receiverMsg() {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setAction(HBApplication.HB_ACTION_BE_REC_MESSAGE);
                MessageService.this.sendBroadcast(intent);
                System.out.println("stop broadcast");
            }
            
        };
        serverThread = new Thread(comm.serverRunnable);
        serverThread.start();
        // serverAsyncTask=new ServerAsyncTask();
        // serverAsyncTask.execute(comm.serverRunnable);

        /*Notification notification = new Notification(R.drawable.icon, getText(R.string.ticker_text),
                System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, ExampleActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        notification.setLatestEventInfo(this, getText(R.string.notification_title),
                getText(R.string.notification_message), pendingIntent);
        startForeground(ONGOING_NOTIFICATION, notification);*/

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return this.msgBinder;

    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        return super.onUnbind(intent);
    }

    public boolean handleTransactions(int code, Parcel data, Parcel reply, int flags) {
        Log.v(HB_TAG, "service transact~");
        String ip;
        String msg;
        String fileMsg;
        String path;
        if (code == 0) {
            try {

                comm.entry();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (code == 1) {
            Bundle bundle = data.readBundle();
            ip = bundle.getString("ip");
            Log.v(HB_TAG, "--------->  " + ip);
            msg = bundle.getString("msg");
            hbDB.addSendMessage(ip, msg, null);

            try {
                comm.sendMessage(ip, msg);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        else if (code == 3){
            Bundle bundle = data.readBundle();
            ip = bundle.getString("ip");
            msg = bundle.getString("msg");
            path = bundle.getString("path");
            comm.sendMessage(ip, msg, path);
        }
        else if (code == 2) {
            System.out.println("code 2 start");
            Bundle bundle = data.readBundle();
            fileMsg = bundle.getString("fileMsg");
            ip = bundle.getString("ip");
            path = bundle.getString("path");
            String packetId = fileMsg.substring(fileMsg.indexOf(':') + 1, fileMsg.indexOf(','));
            String fileName = fileMsg.substring(fileMsg.lastIndexOf(':')+1,fileMsg.lastIndexOf('|'));                

            Socket sock = new Socket();
            try {
                sock.connect(new InetSocketAddress(ip, 2425));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            msg = Integer.toHexString(Integer.valueOf(packetId));
            msg += ":"+Integer.toHexString(1)+":";

            byte[] header = null;
            header = comm.composeHeader(IPMSG.IPMSG_GETFILEDATA);
            msg += "0:";
            System.out.println(":::::"+msg);

            byte[] body = null;
            /*StringBuffer sbuff = new StringBuffer();
            sbuff.append(IPMSG.IPMSG_VERSION);
            sbuff.append(IPMSG.SEPARATOR);
            sbuff.append(327824);
            sbuff.append(IPMSG.SEPARATOR);
            sbuff.append("jluzh515");
            sbuff.append(IPMSG.SEPARATOR);
            sbuff.append("lym");
            sbuff.append(IPMSG.SEPARATOR);
            sbuff.append(IPMSG.IPMSG_GETFILEDATA);
            sbuff.append(IPMSG.SEPARATOR);
            try {
                header = sbuff.toString().getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }*/
            
//            msg += "0:";

            try {
                body = msg.getBytes("GB2312");
            } catch (UnsupportedEncodingException e) {
                body = msg.getBytes();
                e.printStackTrace();
            }
            OutputStream output;
            try {
                output = sock.getOutputStream();
                byte[] data1 = new byte[header.length + body.length];
                System.arraycopy(header, 0, data1, 0, header.length);
                System.arraycopy(body, 0, data1, header.length, body.length);
                output.write(data1);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            AutoPipe pipe = null;
            
                try {
                    pipe = new AutoPipe(sock.getInputStream(),new FileOutputStream(path +"/"+fileName));
                    
                    pipe.pro = new ProgressUpdate() {
                        
                        @Override
                        public void update(int progress, int speed) {
                            // TODO Auto-generated method stub
                            Intent intent = new Intent();
                            intent.putExtra("progress", progress);
                            intent.putExtra("speed", speed);
                            intent.setAction(HBApplication.HB_ACTION_BE_PROGRESS_UPDATE);
                            MessageService.this.sendBroadcast(intent);
                        }
                    };
                    pipe.start();
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }
        /*
        Intent intent = new Intent();
        intent.setAction(HBApplication.HB_ACTION_BR_ENTRY);
        this.sendBroadcast(intent);*/
        return false;
    }

    class MsgBinder extends Binder {

        public MessageService getService() {
            return MessageService.this;
        }

        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags)
                throws RemoteException {
            // 这里处理Service调用
            // 接收Activity发向服务的消息，data为发送消息时向服务传入的对象，replay是由服务返回的对象

            return handleTransactions(code, data, reply, flags);
        }

    }

    class ServerAsyncTask extends AsyncTask<Runnable, String, String> {

        @Override
        protected String doInBackground(Runnable... params) {
            params[0].run();
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
        }

    }

    private static long ipToLong(String strIP)
    // 将127.0.0.1 形式的IP地址转换成10进制整数，这里没有进行任何错误处理
    {
        long[] ip = new long[4];
        int position1 = strIP.indexOf(".");
        int position2 = strIP.indexOf(".", position1 + 1);
        int position3 = strIP.indexOf(".", position2 + 1);
        ip[3] = Long.parseLong(strIP.substring(0, position1));
        ip[2] = Long.parseLong(strIP.substring(position1 + 1, position2));
        ip[1] = Long.parseLong(strIP.substring(position2 + 1, position3));
        ip[0] = Long.parseLong(strIP.substring(position3 + 1));
        return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3]; // ip1*256*256*256+ip2*256*256+ip3*256+ip4
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en
                    .hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
                        .hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    InetAddress getBroadcastAddress() {

        int ip1 = (int)ipToLong(getLocalIpAddress());

        WifiManager wifi = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow
        int ip2 = dhcp.ipAddress;
        int broadcast = 0;
        // System.out.println("ip1: "+Formatter.formatIpAddress(ip1)+",  ip2: "+Formatter.formatIpAddress(ip2));
        if (ip1 != ip2) {
            broadcast = (ip1 & (int)ipToLong("255.255.255.0")) | ~(int)ipToLong("255.255.255.0");
        } else {
            broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        }
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte)((broadcast >> k * 8) & 0xFF);
        InetAddress address = null;
        try {
            address = InetAddress.getByAddress(quads);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return address;

    }
}
