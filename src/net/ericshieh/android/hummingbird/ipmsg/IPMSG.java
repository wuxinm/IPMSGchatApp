package net.ericshieh.android.hummingbird.ipmsg;

public class IPMSG {
    public final static int ENTRY = 0;

    public final static int EXIT = 1;

    public final static int MESSAGE = 2;

    public final static int FILE = 3;

    public final static int EVENT_COUNT = 4;

    /* macro */
    public static int GET_MODE(int command) {
        return command & 0x000000ff;
    }

    public static int GET_OPT(int command) {
        return command & 0xffffff00;
    }

    /* header */
    public final static int IPMSG_VERSION = 0x0001;//版本
    public final static int IPMSG_DEFAULT_PORT = 0x0979;//默认端口

    /* command 濠?濠?*/
    public final static int IPMSG_NOOPERATION = 0x00000000;//不进行任何操作

    public final static int IPMSG_BR_ENTRY = 0x00000001;//用户上线   
    public final static int IPMSG_BR_EXIT = 0x00000002;//用户退出   
    public final static int IPMSG_ANSENTRY = 0x00000003;//通报在线  
    public final static int IPMSG_BR_ABSENCE = 0x00000004;//广播离开

    public final static int IPMSG_BR_ISGETLIST = 0x00000010;//
    public final static int IPMSG_OKGETLIST = 0x00000011;// Host list sending
                                                         // notice
    public final static int IPMSG_GETLIST = 0x00000012;// Host list sending
                                                       // request
    public final static int IPMSG_ANSLIST = 0x00000013;// Host list sending
    public final static int IPMSG_BR_ISGETLIST2 = 0x00000018;

    public final static int IPMSG_SENDMSG = 0x00000020;//发送消息 
    public final static int IPMSG_RECVMSG = 0x00000021;//通报收到消息  
    public final static int IPMSG_READMSG = 0x00000030;//
    public final static int IPMSG_DELMSG = 0x00000031;//
    public final static int IPMSG_ANSREADMSG = 0x00000032;//

    public final static int IPMSG_GETINFO = 0x00000040;
    public final static int IPMSG_SENDINFO = 0x00000041;

    public final static int IPMSG_GETABSENCEINFO = 0x00000050;//获取离开信息
    public final static int IPMSG_SENDABSENCEINFO = 0x00000051;//发送离开信息
    public final static int IPMSG_GETFILEDATA = 0x00000060;// tcp获取文件数据
    public final static int IPMSG_RELEASEFILES = 0x00000061;//发布文件
    public final static int IPMSG_GETDIRFILES = 0x00000062;//获取目录文件
    public final static int IPMSG_GETPUBKEY = 0x00000072;//获取公匙
    public final static int IPMSG_ANSPUBKEY = 0x00000073;//应答公匙

    /* option for all command */
    public final static int IPMSG_ABSENCEOPT = 0x00000100;//离开选项
    public final static int IPMSG_SERVEROPT = 0x00000200;
    public final static int IPMSG_DIALUPOPT = 0x00010000;//拨号选项
    public final static int IPMSG_FILEATTACHOPT = 0x00200000;//附件选项
    public final static int IPMSG_ENCRYPTOPT = 0x00400000;
    public final static int IPMSG_UTF8OPT = 0x00800000;

    /* option for send command */
    public final static int IPMSG_SENDCHECKOPT = 0x00000100;//发送检查
    public final static int IPMSG_SECRETOPT = 0x00000200;//加密检查
    public final static int IPMSG_BROADCASTOPT = 0x00000400;//广播
    public final static int IPMSG_MULTICASTOPT = 0x00000800;//群发广播
    public final static int IPMSG_NOPOPUPOPT = 0x00001000;//来信息不弹出
    public final static int IPMSG_AUTORETOPT = 0x00002000;//自动加回复信息
    public final static int IPMSG_RETRYOPT = 0x00004000;//重试
    public final static int IPMSG_PASSWORDOPT = 0x00008000;//密码
    public final static int IPMSG_NOLOGOPT = 0x00020000;//
    public final static int IPMSG_NEWMUTIOPT = 0x00040000;//
    public final static int IPMSG_NOADDLISTOPT = 0x00080000;// 乱码
    public final static int IPMSG_READCHECKOPT = 0x00100000;// 是否读取
    public final static int IPMSG_SECRETEXOPT = (IPMSG_READCHECKOPT | IPMSG_SECRETOPT);
    /* 乱码*/

    /* encryption flags for encrypt command */
    public final static int IPMSG_RSA_512 = 0x00000001;
    public final static int IPMSG_RSA_1024 = 0x00000002;
    public final static int IPMSG_RSA_2048 = 0x00000004;
    public final static int IPMSG_RC2_40 = 0x00001000;
    public final static int IPMSG_RC2_128 = 0x00004000;
    public final static int IPMSG_RC2_256 = 0x00008000;
    public final static int IPMSG_BLOWFISH_128 = 0x00020000;
    public final static int IPMSG_BLOWFISH_256 = 0x00040000;
    public final static int IPMSG_AES_128 = 0x00080000;
    public final static int IPMSG_SIGN_MD5 = 0x10000000;
    public final static int IPMSG_SIGN_SHA1 = 0x20000000;

    /* compatibilty for Win beta version */
    public final static int IPMSG_RC2_40OLD = 0x00000010; // for beta1-4 only
    public final static int IPMSG_RC2_128OLD = 0x00000040; // for beta1-4 only
    public final static int IPMSG_BLOWFISH_128OLD = 0x00000400; // for beta1-4
                                                                // only
    public final static int IPMSG_RC2_40ALL = (IPMSG_RC2_40 | IPMSG_RC2_40OLD);
    public final static int IPMSG_RC2_128ALL = (IPMSG_RC2_128 | IPMSG_RC2_128OLD);
    public final static int IPMSG_BLOWFISH_128ALL = (IPMSG_BLOWFISH_128 | IPMSG_BLOWFISH_128OLD);

    /* 文件类型*/
    /* file types for fileattach command */
    public final static int IPMSG_FILE_REGULAR = 0x00000001;// 正常文件
    public final static int IPMSG_FILE_DIR = 0x00000002;   // 目录
    public final static int IPMSG_FILE_RETPARENT = 0x00000003; // return parent

    // directory
    public final static int IPMSG_FILE_SYMLINK = 0x00000004;
    public final static int IPMSG_FILE_CDEV = 0x00000005; // for UNIX
    public final static int IPMSG_FILE_BDEV = 0x00000006; // for UNIX
    public final static int IPMSG_FILE_FIFO = 0x00000007; // for UNIX
    public final static int IPMSG_FILE_RESFORK = 0x00000010; // for Mac

    /* 文件属性*/
    /* file attribute options for fileattach command */
    public final static int IPMSG_FILE_RONLYOPT = 0x00000100;
    public final static int IPMSG_FILE_HIDDENOPT = 0x00001000;
    public final static int IPMSG_FILE_EXHIDDENOPT = 0x00002000; // for MacOS X
    public final static int IPMSG_FILE_ARCHIVEOPT = 0x00004000;
    public final static int IPMSG_FILE_SYSTEMOPT = 0x00008000;

    /* 文件附件数据*/
    /* extend attribute types for fileattach command */
    public final static int IPMSG_FILE_UID = 0x00000001;
    public final static int IPMSG_FILE_USERNAME = 0x00000002; // uid by string
    public final static int IPMSG_FILE_GID = 0x00000003;
    public final static int IPMSG_FILE_GROUPNAME = 0x00000004; // gid by string
    public final static int IPMSG_FILE_PERM = 0x00000010; // for UNIX
    public final static int IPMSG_FILE_MAJORNO = 0x00000011; // for UNIX devfile
    public final static int IPMSG_FILE_MINORNO = 0x00000012; // for UNIX devfile
    public final static int IPMSG_FILE_CTIME = 0x00000013; // for UNIX
    public final static int IPMSG_FILE_MTIME = 0x00000014;
    public final static int IPMSG_FILE_ATIME = 0x00000015;
    public final static int IPMSG_FILE_CREATETIME = 0x00000016;
    public final static int IPMSG_FILE_CREATOR = 0x00000020; // for Mac
    public final static int IPMSG_FILE_FILETYPE = 0x00000021; // for Mac
    public final static int IPMSG_FILE_FINDERINFO = 0x00000022; // for Mac
    public final static int IPMSG_FILE_ACL = 0x00000030;
    public final static int IPMSG_FILE_ALIASFNAME = 0x00000040; // alias fname
    public final static int IPMSG_FILE_UNICODEFNAME = 0x00000041; // UNICODE
                                                                  // fname

    public final static String FILELIST_SEPARATOR = "\07";// '\a'
    public final static String HOSTLIST_SEPARATOR = "\07";// '\a'
    public final static String HOSTLIST_DUMMY = "\010";// '\b'
    public final static String SEPARATOR = ":";

    /* end of IP Messenger Communication Protocol version 1.2 define */


    /* IP Messenger for Windows internal define */
    public final static int IPMSG_REVERSEICON = 0x0100;
    public final static int IPMSG_TIMERINTERVAL = 500;
    public final static int IPMSG_ENTRYMINSEC = 5;
    public final static int IPMSG_GETLIST_FINISH = 0;

    public final static int IPMSG_BROADCAST_TIMER = 0x0101;
    public final static int IPMSG_SEND_TIMER = 0x0102;
    public final static int IPMSG_LISTGET_TIMER = 0x0104;
    public final static int IPMSG_LISTGETRETRY_TIMER = 0x0105;
    public final static int IPMSG_ENTRY_TIMER = 0x0106;
    public final static int IPMSG_DUMMY_TIMER = 0x0107;
    public final static int IPMSG_RECV_TIMER = 0x0108;
    public final static int IPMSG_ANS_TIMER = 0x0109;

    public final static int IPMSG_NICKNAME = 1;
    public final static int IPMSG_FULLNAME = 2;

    public final static int IPMSG_NAMESORT = 0x00000000;
    public final static int IPMSG_IPADDRSORT = 0x00000001;
    public final static int IPMSG_HOSTSORT = 0x00000002;
    public final static int IPMSG_NOGROUPSORTOPT = 0x00000100;
    public final static int IPMSG_ICMPSORTOPT = 0x00000200;
    public final static int IPMSG_NOKANJISORTOPT = 0x00000400;
    public final static int IPMSG_ALLREVSORTOPT = 0x00000800;
    public final static int IPMSG_GROUPREVSORTOPT = 0x00001000;
    public final static int IPMSG_SUBREVSORTOPT = 0x00002000;

    /* General define */
    public final static int MAX_SOCKBUF = 32768;// 65536
    public final static int MAX_UDPBUF = 32768;
    public final static int MAX_BUF = 1024;
    public final static int MAX_CRYPTLEN = ((MAX_UDPBUF - MAX_BUF) / 2);
    public final static int MAX_BUF_EX = (MAX_BUF * 3);
    public final static int MAX_NAMEBUF = 80;
    public final static int MAX_LISTBUF = (MAX_NAMEBUF * 4);
    public final static int MAX_ANSLIST = 100;
    public final static int MAX_FILENAME = 256;

    public final static String HS_TOOLS = "HSTools";
    public final static String IP_MSG = "IPMsg";
    public final static String NO_NAME = "no_name";
    public final static String URL_STR = "://";
    public final static String MAILTO_STR = "mailto:";
    public final static String MSG_STR = "msg";

}
