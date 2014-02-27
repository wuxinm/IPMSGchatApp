package net.ericshieh.android.hummingbird;

import java.util.Observable;
import java.util.Observer;

public abstract class EntryListener implements Observer{

    @Override
    public void update(Observable observable, Object data) {
        
            memberEntry((MemberEvent)data);

    }
    
    public abstract void memberEntry(MemberEvent event);
     
}
