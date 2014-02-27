package net.ericshieh.android.hummingbird;


public class Member {
    public String address = null;
    public String name = null;
    public String hostname = null;
    public String nickname = null;
    public String groupname = null;
    public boolean absence = false;
    
    public boolean equals(Object member){
        if(member==null){
            return false;
        }else if(member instanceof Member){
            return address.equals(((Member)member).address);
        }else
            return false;
    }
}   
