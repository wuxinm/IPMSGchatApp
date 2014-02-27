package net.ericshieh.android.hummingbird;

import java.util.Observable;
import java.util.Observer;

public abstract class MessageListener implements Observer{

    @Override
    public void update(Observable observable, Object data) {

        messageReceive((MessageEvent) data);
        
    }
    
    public abstract void messageReceive(MessageEvent event);
}
