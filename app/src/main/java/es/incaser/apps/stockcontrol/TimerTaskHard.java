package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

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
        String syncDate;
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            String lastSyncDate = pref.getString("pref_last_sync", "2000-01-01 00:00:00.0");
            SyncData syncData = new SyncData(context);

            syncData.conSQL = new SQLConnection();
            syncDate = syncData.conSQL.getDate();

            if (SQLConnection.connection == null) {
                msg.obj = "errorSQLconnection";
            }
            syncData.exportMovArticuloSerie(TipoMovimiento.ENTRADA, lastSyncDate);
            syncData.exportMovArticuloSerie(TipoMovimiento.SALIDA, lastSyncDate);
            syncData.emptyLocalTables();
            syncData.importRecords();
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("pref_last_sync", syncDate);
            editor.commit();
            msg.obj = "Datos Sincronizados";
            syncData = null;
        }catch(Exception ee){
            msg.obj = ee.toString();
        }
        handler.sendMessage(msg);
    }
}
