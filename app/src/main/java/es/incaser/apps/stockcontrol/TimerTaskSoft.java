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
public class TimerTaskSoft extends TimerTask{
    Handler handler;
    Message msg;
    Context context;

    public TimerTaskSoft(Handler handler1, Context ctx){
        handler = handler1;
        context = ctx;
    }
    
    @Override
    public void run() {
        msg = Message.obtain(handler, 0, "Run Soft");
        String syncDate;        
        try {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            SyncData syncData = new SyncData(context);
            syncData.conSQL = new SQLConnection();
            syncDate = syncData.conSQL.getDate();

            if (SQLConnection.connection == null) {
                msg.obj = "errorSQLconnection";
            }
            if (syncData.exportMovArticuloSerie(TipoMovimiento.ENTRADA, syncDate) >= 0) {
                msg.obj = "Export Articulos serie recepcion";
            } else {
                msg.obj = "ERROR EN LA SINCRONIZACION";
            }
            if (syncData.exportMovArticuloSerie(TipoMovimiento.SALIDA, syncDate) >= 0) {
                msg.obj = "Export Articulos serie expediciones";
            } else {
                msg.obj = "ERROR EN LA SINCRONIZACION";
            }
            if (syncData.exportMovStock(TipoMovimiento.SALIDA, syncDate) >= 0) {
                msg.obj = "Export MovStock expediciones";
            } else {
                msg.obj = "ERROR EN LA SINCRONIZACION";
            }

            String lastSyncDate = pref.getString("pref_last_sync", "2000-01-01 00:00:00.0");
            if (syncData.importMovArticuloSerie(lastSyncDate)){
                msg.obj = "Import MovimientoArticuloSerie";
            }else{
                msg.obj = "ERROR EN LA SINCRONIZACION";
            }
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("pref_last_sync", syncDate);
            editor.commit();
            syncData = null;
        }catch(Exception ee){
            msg.obj = ee.toString();
        }
        handler.sendMessage(msg);
    }
}

