package es.incaser.apps.stockcontrol;

import android.os.Handler;
import android.os.Message;
import java.util.TimerTask;

/**
 * Created by sergio on 29/12/14.
 */
public class TimerTaskSoft extends TimerTask{
    Handler handler;
    Message msg;

    public TimerTaskSoft(Handler handler1){
        handler = handler1;
    }
    
    @Override
    public void run() {
        msg = Message.obtain(handler, 5, "Run Hard");
        handler.sendMessage(msg);
        //Guardo fecha-hora de la sincronizacion ** Max fecharegistro de MovimientoArticuloSerie
    }
}

