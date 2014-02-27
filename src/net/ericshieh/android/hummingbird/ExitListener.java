package net.ericshieh.android.hummingbird;

import java.util.Observable;
import java.util.Observer;

public abstract class ExitListener implements Observer{

    @Override
    public void update(Observable observable, Object data) {
        // TODO Auto-generated method stub
        memberExit((MemberEvent)data);
        
    }
    
    public abstract void memberExit(MemberEvent event);
}
