package net.ericshieh.android.hummingbird;


public class MessageEvent {
    public Member member = null;
    
    public String message = null;
    
    public MessageEvent(Member mem, String msg){
        member = mem;
        message = msg;
    }

    public FileData[] fileDataList;
    
}
