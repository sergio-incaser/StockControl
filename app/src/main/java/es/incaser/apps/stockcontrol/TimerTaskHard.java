package es.incaser.apps.stockcontrol;

import android.os.Handler;
import android.os.Message;

import java.util.TimerTask;

/**
 * Created by sergio on 29/12/14.
 */
public class TimerTaskHard extends TimerTask{
    Handler handler;
    Message msg;
        
    public TimerTaskHard(Handler handler1){
        handler = handler1;
    }
    
    @Override
    public void run() {
        msg = Message.obtain(handler, 15, "Run Hard");
        handler.sendMessage(msg);
    }
}

