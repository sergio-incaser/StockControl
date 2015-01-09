package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import java.util.TimerTask;

/**
 * Created by sergio on 29/12/14.
 */
public class TimerTaskHard extends TimerTask{
    Handler handler;
    Message msg;
    Context context;
        
    public TimerTaskHard(Handler handler1, Context ctx){
        handler = handler1;
        context = ctx;
    }
    
    @Override
    public void run() {
        msg = Message.obtain(handler, 0, "Run Hard");
        try {
            SyncData syncData = new SyncData(context);
            syncData.conSQL = new SQLConnection();
            if (SQLConnection.connection == null) {
                msg.obj = "errorSQLconnection";
            }
            if (syncData.exportRecords() >= 0) {
                syncData.importRecords();
                msg.obj = "Datos Sincronizados";
            } else {
                msg.obj = "ERROR EN LA SINCRONIZACION";
            }
            syncData = null;
        }catch(Exception ee){
            msg.obj = ee.toString();
        }
        handler.sendMessage(msg);
    }
}
