package es.incaser.apps.stockcontrol;

import android.content.Context;

/**
 * Created by sergio on 9/01/15.
 */
public class StatusSync {
    public static final String PEND_IMPORTAR = "-2";
    public static final String IMPORTANDO = "-1";
    public static final String PREVISTO = "0";
    public static final String ESCANEADO = "1";
    public static final String EXPORTANDO = "2";
    public static final String EXPORTADO = "3";
    public static final String SYNC_SAICA = "4";
    public static final String PARA_CREAR = "98";
    public static final String NOT_INSQL = "99";
    
    public static String getSyncDescription(String statusSync, Context ctx){
        String res = "";
        switch (statusSync){
            case PEND_IMPORTAR:
                res = ctx.getResources().getString(R.string.status_sync_pend_importar);
                break;
            case IMPORTANDO:
                res = ctx.getResources().getString(R.string.status_sync_importando);
                break;
            case PREVISTO:
                res = ctx.getResources().getString(R.string.status_sync_previsto);
                break;
            case ESCANEADO:
                res = ctx.getResources().getString(R.string.status_sync_escaneado);
                break;
            case EXPORTANDO:
                res = ctx.getResources().getString(R.string.status_sync_exportando);
                break;
            case EXPORTADO:
                res = ctx.getResources().getString(R.string.status_sync_exportado);
                break;
            case SYNC_SAICA:
                res = ctx.getResources().getString(R.string.status_sync_sync_saica);
                break;
            case PARA_CREAR:
                res = ctx.getResources().getString(R.string.status_sync_para_crear);
                break;
            case NOT_INSQL:
                res = ctx.getResources().getString(R.string.status_sync_not_insql);
                break;
        }
        return res;
    }
}
