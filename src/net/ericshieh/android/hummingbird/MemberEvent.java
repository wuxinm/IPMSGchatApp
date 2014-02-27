package net.ericshieh.android.hummingbird;


public class MemberEvent {
    public Member member = null;
    public boolean exit = false;
    
    public MemberEvent(Member member,boolean exit){
        this.member = member;
        this.exit = exit;
    }
}
