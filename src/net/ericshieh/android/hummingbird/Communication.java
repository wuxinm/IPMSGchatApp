
package net.ericshieh.android.hummingbird;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import net.ericshieh.android.hummingbird.ipmsg.IPMSG;
import android.util.Log;

public class Communication {
    public static final String HB_TAG = HBApplication.HB_TAG;

    static final byte separator = 58;
    static final byte fileseparator = 7; // "\a" 对应的byte值

    private int packetNo = 0;

    static final byte zero = 0;

    protected String charset = null;

    private ByteBuffer sendBuffer = null;
    private InetSocketAddress sendAddress = null;

    private DatagramSocket socket = null;

    DatagramChannel channel = null;

    public String nickname;

    public String groupname;
    
    public String hostname;

    protected Observable[] listeners = null;

    String localAddress;

    InetAddress inetBrAddress;
    InetSocketAddress inetSocketBrAddress;

    ServerRunnable serverRunnable;

    String senderIp = null;

    protected ServerSocket server = null;

    protected ServerSockThread serverThread = null;

    protected boolean isclosed = false;

    protected ArrayList<FileData> waitingList = null;
    
    public ReceiveMsgListener rec;

    public Communication(String nickname, String groupname) {
        this.nickname = nickname;
        this.groupname = groupname;
        charset = "GB2312";
        sendBuffer = ByteBuffer.allocateDirect(IPMSG.MAX_UDPBUF);
        listeners = new Observable[3];
        for (int i = 0; i < 3; i++)
            listeners[i] = new Observable() {
                public void notifyObservers(Object arg) {
                    super.setChanged();
                    super.notifyObservers(arg);
                }
            };
        serverRunnable = new ServerRunnable();

        try {
            server = new ServerSocket(2425);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        serverThread = new ServerSockThread();
        serverThread.start();
        waitingList = new ArrayList<FileData>();
    }

    public void setBrInetAddress(InetAddress inetBrAddress) {
        this.inetBrAddress = inetBrAddress;
        this.inetSocketBrAddress = new InetSocketAddress(inetBrAddress, 2425);
    }

    protected void entry() throws IOException {

        // socket = new DatagramSocket(2425);
        // socket.setReuseAddress(true);

        sendBuffer.clear();
        sendBuffer.put(composeHeader(IPMSG.IPMSG_BR_ENTRY));
        sendBuffer.put(nickname.getBytes(charset));

        sendBuffer.put(zero);
        sendBuffer.put(groupname.getBytes(charset));
        sendBuffer.flip();

        Log.v(HB_TAG, "ready to entry");
        
        channel.send(sendBuffer, inetSocketBrAddress);
        //channel.register(serverRunnable.selector, SelectionKey.OP_READ|SelectionKey.OP_WRITE, serverRunnable.new ClientRecord());
        //serverRunnable.selector.wakeup();
        Log.v(HBApplication.HB_TAG, "entry packet sent");

    }

    protected void brExit() throws IOException {

        // socket = new DatagramSocket(2425);
        // socket.setReuseAddress(true);

        sendBuffer.clear();
        sendBuffer.put(composeHeader(IPMSG.IPMSG_BR_EXIT));
        sendBuffer.put(nickname.getBytes(charset));

        sendBuffer.put(zero);
        sendBuffer.put(groupname.getBytes(charset));
        sendBuffer.flip();

        Log.v(HBApplication.HB_TAG, "exit_br_sent");
        channel.send(sendBuffer, inetSocketBrAddress);

    }

    protected void sendMessage(String ip, String msg) throws IOException {
        sendBuffer.clear();
        sendBuffer.put(composeHeader(IPMSG.IPMSG_SENDMSG));

        try {
            sendBuffer.put(msg.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            sendBuffer.put(msg.getBytes(charset));
            e.printStackTrace();
        }
        sendBuffer.flip();
        
        channel.send(sendBuffer, new InetSocketAddress(ip, 2425));

        Log.v(HB_TAG, "---send: (" +ip+"):"+ msg );
        
    }

    public void sendMessage(String ip, String msg, String filepath) {

        synchronized (sendBuffer) {
            sendBuffer.clear();
            if (filepath != null)
                sendBuffer.put(composeHeader(IPMSG.IPMSG_SENDMSG | IPMSG.IPMSG_FILEATTACHOPT));
            else
                sendBuffer.put(composeHeader(IPMSG.IPMSG_SENDMSG));

            try {
                sendBuffer.put(msg.getBytes(charset));
            } catch (UnsupportedEncodingException e) {
                sendBuffer.put(msg.getBytes());
                e.printStackTrace();
            }
            sendBuffer.put(zero);
        }
        File file = new File(filepath);

        // FileInputStream fileInputStream = new FileInputStream(file);

        FileData fileData = new FileData();
        fileData.packetID = packetNo - 1;
        fileData.fileID = 1;
        fileData.name = file.getName();
        fileData.mtime = file.lastModified();
        fileData.size = file.length();
        fileData.attr = IPMSG.IPMSG_FILE_REGULAR;

        synchronized (sendBuffer) {
            sendBuffer.put(composeFileData(fileData));
            sendBuffer.put(fileseparator);
        }
        fileData.name = file.getAbsolutePath();

        synchronized (waitingList) {
            waitingList.add(fileData);
        }

        sendBuffer.flip();
        try {
            channel.send(sendBuffer, new InetSocketAddress(ip, 2425));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    class ServerRunnable implements Runnable {
        
        private boolean isRunning = true;
        Selector selector;

        @Override
        public void run() {
            if (channel == null) {
                try {
                    selector = Selector.open();
                    channel = DatagramChannel.open();
                    channel.configureBlocking(false);
                    socket = channel.socket();// HBApplication.HB_SOCKET_PORT
                    socket.setBroadcast(true);
                    // socket.setSoTimeout(1000);
                    socket.setReuseAddress(true);
                    socket.bind(new InetSocketAddress(2425));

                    channel.register(selector, SelectionKey.OP_READ, new ClientRecord());

                    Log.v(HBApplication.HB_TAG, "socket binded");
                } catch (SocketException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    this.isRunning = false;
                    Log.v(HB_TAG, "socket bind failed");
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            while (isRunning) {
                try {
                    if (selector.select() == 0) {
                        //Log.v(HB_TAG, "selector timeout");
                        continue;
                    }
                    Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
                    while (keyIter.hasNext()) {
                        SelectionKey key = keyIter.next(); // Key is bit mask
                        Log.v(HB_TAG, "GOT ONE,read:"+(key.isReadable()?1:0)+",write:"+(key.isWritable()?1:0));
                        if (key.isReadable())
                            handleRead(key);
                        if (key.isValid() && key.isWritable())
                            handleWrite(key);
                        keyIter.remove();
                    }

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                channel.close();
                // socket.close();
            } catch (IOException e) {
                // TODO: handle exception
                e.printStackTrace();
            }finally{                
                socket = null;
                channel = null;
            }
            
        }

        public void setRunning(boolean isRunning) {
            this.isRunning = isRunning;
        }

        public void handleRead(SelectionKey key) throws IOException {
            DatagramChannel channel = (DatagramChannel)key.channel();
            ClientRecord clntRec = (ClientRecord)key.attachment();
            clntRec.buffer.clear();
            // Prepare buffer for receiving
            clntRec.clientAddress = (InetSocketAddress)channel.receive(clntRec.buffer);
            if (clntRec.clientAddress != null) {// Did we receive something?
                // Register write with the selector
                // key.interestOps(SelectionKey.OP_WRITE);

                clntRec.buffer.flip();
                byte[] data = new byte[clntRec.buffer.remaining()];
                clntRec.buffer.get(data);

                int command = getCommandNo(data, data.length);
                int mode = IPMSG.GET_MODE(command);
                int opt = IPMSG.GET_OPT(command);
                Log.v(HB_TAG, "------reciveMsg" + command);
                if (mode == IPMSG.IPMSG_NOOPERATION)
                    return;
                processDatagramPacket(clntRec.clientAddress.getAddress(), mode, opt, data);
            }
        }

        public void handleWrite(SelectionKey key) throws IOException {
            DatagramChannel channel = (DatagramChannel)key.channel();
            ClientRecord clntRec = (ClientRecord)key.attachment();
            //clntRec.buffer.flip(); // Prepare buffer for sending
            //int bytesSent = channel.send(clntRec.buffer, clntRec.clientAddress);
            sendBuffer.flip(); // Prepare buffer for sending
            Log.v(HB_TAG, "nio-send start,buff:"+sendBuffer.remaining()+",addr:"+sendAddress.getHostName());
            /*
            int bytesSent = ch``````annel.send(sendBuffer, sendAddress);
            if (bytesSent != 0) { // Buffer completely written?
                // No longer interested in writes
                
                Log.v(HB_TAG, "nio-send complete");
                key.interestOps(SelectionKey.OP_READ);
            }else{
                Log.v(HB_TAG, "nio-send not complete");
            }*/

        }

        class ClientRecord {
            public InetSocketAddress clientAddress;
            public ByteBuffer buffer = ByteBuffer.allocate(IPMSG.MAX_UDPBUF);
        }

    }

    protected void processDatagramPacket(InetAddress packetAddress, int mode, int opt, byte[] data)
            throws IOException {

        Member men = new Member();
        men.address = packetAddress.getHostAddress();
        men.name = getMemName(data, data.length);
        men.hostname = getMemHostName(data, data.length);
        men.nickname = getNickName(data, data.length);
        men.groupname = getGroupName(data, data.length);
        Log.v(HB_TAG, "" + mode);

        switch (mode) {
            case IPMSG.IPMSG_BR_ENTRY:

                sendBuffer.clear();
                sendBuffer.put(composeHeader(IPMSG.IPMSG_ANSENTRY));
                sendBuffer.put(nickname.getBytes(charset));
                sendBuffer.put(zero);
                sendBuffer.put(groupname.getBytes(charset));

                sendBuffer.flip();
                
                channel.send(sendBuffer, new InetSocketAddress(packetAddress, 2425));

                System.out.println("---------send");

                listeners[IPMSG.ENTRY].notifyObservers(new MemberEvent(men, false));

                break;
            case IPMSG.IPMSG_ANSENTRY:

                listeners[IPMSG.ENTRY].notifyObservers(new MemberEvent(men, false));

                break;
            case IPMSG.IPMSG_BR_EXIT:

                listeners[IPMSG.EXIT].notifyObservers(new MemberEvent(men, true));

                break;
            case IPMSG.IPMSG_SENDMSG:

                MessageEvent msgEvent = new MessageEvent(men, getMessage(data, data.length));

                if (0 != (opt & IPMSG.IPMSG_FILEATTACHOPT)) {
                    FileData[] files = getFileDataList(data, data.length);
                    String packetID = getPacketNo(data, data.length);
                    for (FileData file : files) {
                        file.packetID = Integer.parseInt(packetID);
                        msgEvent.message += "id:" + file.packetID + "," + file.fileID + " name:"
                                + file.name + "|" + file.size;
                    }
                    msgEvent.fileDataList = files;
                }

                listeners[IPMSG.MESSAGE].notifyObservers(msgEvent);

                if (0 != (opt & IPMSG.IPMSG_SENDCHECKOPT)) {
                    synchronized (sendBuffer) {
                        sendBuffer.clear();
                        sendBuffer.put(composeHeader(IPMSG.IPMSG_RECVMSG));
                        try {
                            sendBuffer.put(getPacketNo(data, data.length).getBytes(charset));
                        } catch (UnsupportedEncodingException e) {
                            sendBuffer.put(getPacketNo(data, data.length).getBytes());
                            e.printStackTrace();
                        }
                        sendBuffer.put(zero);
                        sendBuffer.flip();
                        channel.send(sendBuffer, new InetSocketAddress(packetAddress, 2425));
                    }
                }
                break;
            case IPMSG.IPMSG_RECVMSG:
                System.out.println("!!!!rec stop");
                rec.receiverMsg();
                break;

            default:
                break;
        }
    }

    protected int getCommandNo(byte[] data, int length) {
        String str = getHeaderPart(data, length, 4);
        if (str == null) {
            return IPMSG.IPMSG_NOOPERATION;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return IPMSG.IPMSG_NOOPERATION;
            }
        }
    }

    protected String getHeaderPart(byte[] data, int length, int part) {
        int start = 0;
        for (int i = 0; i < part; ++i) {
            start = getNextSeparator(data, start, length - start);
            if (start == -1)
                return null;
            ++start;
        }
        int end = getNextSeparator(data, start, length - start);
        if (end <= start) {
            return null;
        } else {
            return new String(data, start, end - start);
        }
    }

    protected int getNextSeparator(byte[] data, int offset, int length) {
        while (length > 0) {
            if (data[offset] == separator) {
                if (length == 1 || data[offset + 1] != separator) {
                    return offset;
                } else {
                    ++offset;
                    --length;
                }
            }
            ++offset;
            --length;
        }
        return -1;
    }

    protected String getMessage(byte[] data, int length) {
        int start = length, end = length;
        int count = 0;
        int i = 0;
        for (; i < length; ++i) {
            if (data[i] == separator) {
                ++count;
                if (count == 5) {
                    start = ++i;
                    break;
                }
            }
        }
        for (; i < length; ++i) {
            if (data[i] == 0) {
                end = i;
                break;
            }
        }
        if (end <= start) {
            return "";
        } else {
            try {
                return new String(data, start, end - start, charset);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return new String(data, start, end - start);
            }

        }
    }

    protected byte[] composeFileData(FileData file) {
        StringBuilder str = new StringBuilder();
        str.append(Integer.toHexString(file.fileID));
        str.append(IPMSG.SEPARATOR);
        str.append(file.name);
        str.append(IPMSG.SEPARATOR);
        str.append(Long.toHexString(file.size));
        str.append(IPMSG.SEPARATOR);
        str.append(Long.toHexString(file.mtime));
        str.append(IPMSG.SEPARATOR);
        str.append(Integer.toHexString(file.attr));
        str.append(IPMSG.SEPARATOR);
        try {
            return str.toString().getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str.toString().getBytes();
        }
    }

    protected String getMemName(byte[] data, int length) {
        return getHeaderPart(data, length, 2);
    }

    protected String getMemHostName(byte[] data, int length) {
        return getHeaderPart(data, length, 3);
    }

    protected String getPacketNo(byte[] data, int length) {
        return getHeaderPart(data, length, 1);
    }

    protected String getNickName(byte[] data, int length) {
        return getMessage(data, length);
    }

    protected String getGroupName(byte[] data, int length) {
        int start = 0;
        while (start < length && data[start++] != 0)
            ;
        if (start < length) {
            return new String(data, start, length - start);
        } else {
            return "";
        }
    }

    protected byte[] composeHeader(int commandNo) {
        StringBuffer str = new StringBuffer();
        str.append(IPMSG.IPMSG_VERSION);
        str.append(IPMSG.SEPARATOR);
        str.append(packetNo++);
        str.append(IPMSG.SEPARATOR);
        str.append(nickname);
        str.append(IPMSG.SEPARATOR);
        str.append(hostname);
        str.append(IPMSG.SEPARATOR);
        str.append(commandNo);// | IPMSG.IPMSG_FILEATTACHOPT |
                              // IPMSG.IPMSG_ENCRYPTOPT
        str.append(IPMSG.SEPARATOR);
        try {
            return str.toString().getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str.toString().getBytes();
        }

    }

    public void addListener(int eventType, Observer listener) {
        if (eventType >= listeners.length) {
            throw new IllegalArgumentException("unknown event type");
        }
        if (listener == null) {
            throw new IllegalArgumentException(" listener cannot be null");
        }
        listeners[eventType].addObserver(listener);
    }

    public void addEntryListener(EntryListener listener) {
        addListener(IPMSG.ENTRY, listener);
    }

    public void addExitListener(ExitListener listener) {
        addListener(IPMSG.EXIT, listener);
    }

    public void addMessageListener(MessageListener listener) {
        addListener(IPMSG.MESSAGE, listener);
    }

    protected FileData[] getFileDataList(byte[] data, int length) {
        ArrayList<FileData> files = new ArrayList<FileData>();
        int start = 0;
        while (start < length && data[start++] != 0)
            ;

        if (start < length) {
            for (int i = start; i < length; ++i) {
                if (data[i] == fileseparator) {
                    FileData file = getFileData(data, start, i - start);
                    if (file != null)
                        files.add(file);
                    start = ++i;
                }
            }
        }

        return files.toArray(new FileData[0]);
    }

    public void close() {
        isclosed = true;
        try {
            if (channel != null)
                channel.close();
            if (server != null) {
                server.close();
                server = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected FileData getFileData(byte[] data, int offset, int length) {
        if (length <= 0)
            return null;

        try {
            FileData file = new FileData();
            length += offset;

            int start = offset;
            int end = getNextSeparator(data, start, length - start);
            try {
                file.fileID = Integer.parseInt(new String(data, start, end - start, charset));
            } catch (UnsupportedEncodingException e) {
                file.fileID = Integer.parseInt(new String(data, start, end - start));
                e.printStackTrace();
            }

            start = end + 1;
            end = getNextSeparator(data, start, length - start);
            try {
                file.name = new String(data, start, end - start, charset);
            } catch (UnsupportedEncodingException e) {
                file.name = new String(data, start, end - start);
                e.printStackTrace();
            }

            start = end + 1;
            end = getNextSeparator(data, start, length - start);
            try {
                file.size = Long.parseLong(new String(data, start, end - start, charset), 16);
            } catch (UnsupportedEncodingException e) {
                file.size = Long.parseLong(new String(data, start, end - start), 16);
                e.printStackTrace();
            }

            start = end + 1;
            end = getNextSeparator(data, start, length - start);
            try {
                file.mtime = Long.parseLong(new String(data, start, end - start, charset), 16);
            } catch (UnsupportedEncodingException e) {
                file.mtime = Long.parseLong(new String(data, start, end - start), 16);
                e.printStackTrace();
            }

            start = end + 1;
            end = getNextSeparator(data, start, length - start);
            try {
                file.attr = Integer.parseInt(new String(data, start, end - start, charset), 16);
            } catch (UnsupportedEncodingException e) {
                file.attr = Integer.parseInt(new String(data, start, end - start), 16);
                e.printStackTrace();
            }

            return file;
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

    }

    private class ServerSockThread extends Thread {


        public void run() {
            try {
                while (!isclosed) {
                    
                    Socket sock = server.accept();

                    byte[] buffer = new byte[300];
                    InputStream input = sock.getInputStream();
                    int length = input.read(buffer);
                    FileData file = getFileData(buffer, length);

                    if (file == null) {
                        sock.close();
                        System.out.println("closed");
                    } else {
                        int mode = IPMSG.GET_MODE(file.attr);

                        AutoPipe pipe = null;
                        System.out.println("mode:"+mode+", regular=1");
                          if (mode == IPMSG.IPMSG_FILE_REGULAR) {
                            System.out.println("mode? REGULAR");
                            pipe = new AutoPipe(new FileInputStream(file.name),
                                    sock.getOutputStream());
                        }
                        if (pipe != null)
                            pipe.start();
                        
                        else
                            sock.close();
                    }
                }
            } catch (IOException e) {
                // e.printStackTrace();
            } finally {
                if (server != null) {
                    try {
                        server.close();
                        server = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private FileData getFileData(byte[] buffer, int length) {

            String msg = getMessage(buffer, length);
            System.out.println("return-----:" + msg);
            String[] parts = msg.split(":");
            int packetID = Integer.parseInt(parts[0], 16);
            int fileID = Integer.parseInt(parts[1], 16);

            // int offset = Integer.parseInt(parts[2], 16);

            synchronized (waitingList) {
                for (FileData file : waitingList) {
                    if (file.packetID == packetID && file.fileID == fileID)
                        return file;
                }
            }

            return null;
        }
    }
}
