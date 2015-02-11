package es.incaser.apps.stockcontrol;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.incaser.apps.stockcontrol.Tools.*;

/**
 * Created by sergio on 23/09/14.
 */
public class SyncData {
    private static DbAdapter dbAdapter;
    private Context myContext;
    public SQLConnection conSQL;

    public SyncData(Context ctx) {
        myContext = ctx;
        dbAdapter = new DbAdapter(myContext);
    }

    public int importRecords() {
        ResultSet resultSet;
        for (int i = 0; i < DbAdapter.tablesToImport; i++) {
            resultSet = conSQL.getResultset(String.valueOf(DbAdapter.QUERY_LIST[i][1]));
            copyRecords(resultSet, DbAdapter.QUERY_LIST[i][0]);
        }
        // TODO Devolver numero de registros importados
        return 0;
    }

    public int exportRecords() {
        Cursor cursor;
        ResultSet resultSet = null;
        int numReg = 0;

        for (int i = DbAdapter.tablesToExport; i < DbAdapter.QUERY_LIST.length + 1; i++) {
            cursor = dbAdapter.getTable(DbAdapter.QUERY_LIST[i - 1][0], DbAdapter.QUERY_LIST[i - 1][2]);
            resultSet = conSQL.getResultset("Select * FROM " + DbAdapter.QUERY_LIST[i - 1][0] + " WHERE 1=2", true);
            if (resultSet != null) {
                int x;
                x = copyRecords(cursor, DbAdapter.QUERY_LIST[i - 1][0], resultSet);
                if (x < 0) {
                    return -1;
                }
                dbAdapter.emptyTables(DbAdapter.QUERY_LIST[i - 1][0]);
                numReg += x;
            }
        }
        //Consultas de postprocesado
        //conSQL.updateSQL("UPDATE INC_Incidencias SET INC_PendienteSync=0 WHERE INC_PendienteSync<>0");
        return numReg;
    }

    public int exportMovArticuloSerieXXX(String syncDate) {
        ArrayList <String> guidListRec = new ArrayList<String>();
        ArrayList <String> guidListExp = new ArrayList<String>();
        int numReg = dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.ESCANEADO, StatusSync.EXPORTANDO);
        int numRegRec = 0;
        int numRegExp = 0;
        if (numReg > 0){
            Cursor cursor = dbAdapter.getMovArticuloSerie(StatusSync.EXPORTANDO);
            while (cursor.moveToNext()) {
                if (TipoMovimiento.ORIGEN_ENTRADA.equals(cursor.getString(cursor.getColumnIndex("OrigenDocumento")))){
                    guidListRec.add("'" + cursor.getString(cursor.getColumnIndex("MovPosicion")) + "'");
                    numRegRec++;
                }
                if (TipoMovimiento.ORIGEN_SALIDA.equals(cursor.getString(cursor.getColumnIndex("OrigenDocumento")))){
                    guidListExp.add("'" + cursor.getString(cursor.getColumnIndex("MovPosicion")) + "'");
                    numRegExp++;
                }
            };

/*
            if (conSQL.updateSQL("UPDATE MovimientoArticuloSerie SET StatusAndroidSync="+StatusSync.EXPORTADO+", " +
                    "FechaRegistro = " + date2Sql(syncDate) + " WHERE MovPosicion IN (" + TextUtils.join(",",guidListRec) + ")") == numRegRec){
                //Correcto
                dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.EXPORTANDO, StatusSync.EXPORTADO);
            }else{
                String guidListInexist = conSQL.getGuidsInexistentes(guidListRec);
                //TODO. diferencia de bobinas. Que pasa con estas bobinas???
                dbAdapter.updateStatusSyncGuid("MovimientoArticuloSerie", guidListInexist,StatusSync.NOT_INSQL);
                dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.EXPORTANDO, StatusSync.EXPORTADO);
            };

            if (conSQL.updateSQL("UPDATE MovimientoArticuloSerie SET StatusAndroidSync="+StatusSync.EXPORTADO+", " +
                    "FechaRegistro = " + date2Sql(syncDate) + " WHERE MovPosicion IN (" + TextUtils.join(",",guidListExp) + ")") == numRegExp){
                //Correcto
                dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.EXPORTANDO, StatusSync.EXPORTADO);
            }else{
                String guidListInexist = conSQL.getGuidsInexistentes(guidListExp);
                dbAdapter.updateStatusSyncGuid("MovimientoArticuloSerie", guidListInexist,StatusSync.PARA_CREAR);
                dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.EXPORTANDO, StatusSync.EXPORTADO);
            };
*/
            // Actualiza
            updateStatusMoves(syncDate, guidListRec, numRegRec, StatusSync.NOT_INSQL);
            //TODO. diferencia de bobinas. Que pasa con las bobinas NOT_INSQL???

            updateStatusMoves(syncDate, guidListExp, numRegExp, StatusSync.PARA_CREAR);

            Cursor movArtSerie = dbAdapter.getMovArticuloSerie(StatusSync.PARA_CREAR);
            copyRecords(movArtSerie, "MovimientoArticuloSerie", conSQL.getResultset("SELECT * FROM MovimientoArticuloSerie WHERE 1=2",true));

            dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.PARA_CREAR, StatusSync.EXPORTADO);

        };
        return numReg;
    }

    public int  exportMovStock(String tipoMov, String syncDate) {
        int numReg = 0;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this.myContext);
        String lastSyncDate = pref.getString("pref_last_sync", "2000-01-01 00:00:00.0");

        Cursor curNuevos = dbAdapter.getMovimientoStockToCreate(TipoMovimiento.SALIDA, lastSyncDate);
        Log.w("Exportando Nuevos MovStock "+tipoMov,String.valueOf(curNuevos.getCount()));
        if (curNuevos.getCount()>0){
            int nuevos = copyRecords(curNuevos, "MovimientoStock", conSQL.getResultset("SELECT * FROM MovimientoStock WHERE 1=2",true));
            if (nuevos > 0){
                dbAdapter.updateStatusSync("MovimientoStock", StatusSync.PARA_CREAR, StatusSync.EXPORTADO);
                conSQL.updateSQL("UPDATE MovimientoStock " +
                        "SET StatusAndroidSync=" + StatusSync.EXPORTADO+", "+
                        "FechaRegistro= "+ date2Sql(syncDate) +
                        " WHERE StatusAndroidSync=" + StatusSync.PARA_CREAR);
            }
            Log.w("Exportados Nuevos MovStock "+tipoMov,String.valueOf(nuevos));
        }
        
        Cursor cursor = dbAdapter.getMovimientoStockModif(TipoMovimiento.SALIDA, lastSyncDate);
        Log.w("Exportando MovStock "+tipoMov,String.valueOf(cursor.getCount()));
        while (cursor.moveToNext()) {
            int x = conSQL.updateSQL("UPDATE MovimientoStock SET " +
                    "Matricula='"+cursor.getString(cursor.getColumnIndex("Matricula"))+"'," +
                    "MatriculaRemolque='" + cursor.getString(cursor.getColumnIndex("MatriculaRemolque"))+"'," +
                    "CodigoChofer='"+cursor.getString(cursor.getColumnIndex("CodigoChofer"))+"',"+
                    "FechaRegistro = " + date2Sql(syncDate) +","+
                    "Unidades = " +  cursor.getString(cursor.getColumnIndex("Unidades"))+","+
                    "Unidades2_ = " +  cursor.getString(cursor.getColumnIndex("Unidades"))+","+
                    "StatusAndroidSync = " + StatusSync.EXPORTADO +
                    " WHERE MovPosicion='" + cursor.getString(cursor.getColumnIndex("MovPosicion")) + "'");
            numReg++;
        };
        return numReg;
    }

    
    public int exportMovArticuloSerie(String tipoMov, String syncDate) {
        ArrayList <String> guidList = new ArrayList<String>();
        dbAdapter.updateStatusSync("MovimientoArticuloSerie", tipoMov, StatusSync.ESCANEADO, StatusSync.EXPORTANDO);
        Cursor cursor = dbAdapter.getMovArticuloSerie(StatusSync.EXPORTANDO);
        int numReg = cursor.getCount();
        if (numReg > 0){
            Log.w("Exportando "+tipoMov,String.valueOf(cursor.getCount()));
            while (cursor.moveToNext()) {
                guidList.add("'" + cursor.getString(cursor.getColumnIndex("MovPosicion")) + "'");
            };

            // Actualiza 
            if (tipoMov.equals(TipoMovimiento.ENTRADA)){
                updateStatusMoves(syncDate, guidList, numReg, StatusSync.NOT_INSQL);
                //TODO. diferencia de bobinas. Que pasa con las bobinas NOT_INSQL???
            }
            if (tipoMov.equals(TipoMovimiento.SALIDA)) {
                updateStatusMoves(syncDate, guidList, numReg, StatusSync.PARA_CREAR);
                Cursor movArtSerie = dbAdapter.getMovArticuloSerie(StatusSync.PARA_CREAR);
                if (copyRecords(movArtSerie, "MovimientoArticuloSerie", conSQL.getResultset("SELECT * FROM MovimientoArticuloSerie WHERE 1=2",true)) > 0){
                    int x = dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.PARA_CREAR, StatusSync.EXPORTADO);
                    Log.w("Update PARA_CREAR -> EXPORTADO ", String.valueOf(x));
                    rebajeArticulosSeries();
                    x = conSQL.updateSQL("UPDATE MovimientoArticuloSerie SET StatusAndroidSync="+StatusSync.EXPORTADO+", " +
                            "FechaRegistro = " + date2Sql(syncDate) + " WHERE StatusAndroidSync='" + StatusSync.PARA_CREAR + "'");
                };
            }
        };
        return numReg;
    }

    private void rebajeArticulosSeries(){
        String sql = "UPDATE ArticulosSeries" +
                "        SET UnidadesSerie = UnidadesSerie - 1" +
                "        WHERE CONVERT(CHAR,CodigoEmpresa)+CodigoArticulo+NumeroSerieLc =" +
                "           (SELECT CONVERT(CHAR,CodigoEmpresa)+CodigoArticulo+NumeroSerieLc FROM MovimientoArticuloSerie" +
                "               WHERE (ArticulosSeries.CodigoEmpresa = MovimientoArticuloSerie.CodigoEmpresa) AND" +
                "                (ArticulosSeries.CodigoArticulo = MovimientoArticuloSerie.CodigoArticulo) AND" +
                "                (ArticulosSeries.NumeroSerieLc = MovimientoArticuloSerie.NumeroSerieLc)AND" +
                "                (MovimientoArticuloSerie.StatusAndroidSync = " + StatusSync.PARA_CREAR + "))";
        int x = conSQL.updateSQL(sql);
    }
    
    void updateStatusMoves(String syncDate, ArrayList <String> guidList, int numReg, String statusNotExist){
        String guidListStr = TextUtils.join(",",guidList);
        int x = 0;
        if (conSQL.updateSQL("UPDATE MovimientoArticuloSerie SET StatusAndroidSync="+StatusSync.EXPORTADO+", " +
                "FechaRegistro = " + date2Sql(syncDate) + " WHERE MovPosicion IN (" + guidListStr + ")") == numReg){
            //Correcto
            x = dbAdapter.updateStatusSyncGuid("MovimientoArticuloSerie", guidList , StatusSync.EXPORTADO);
            Log.w("Update EXPORTADO Coinciden registros ", String.valueOf(x) + " / " +guidList);

        }else{
            ArrayList<String> guidListInexist = conSQL.getGuidsInexistentes(guidList);
            x = dbAdapter.updateStatusSyncGuid("MovimientoArticuloSerie", guidListInexist, statusNotExist);
            Log.w("Update TipoMov:"+statusNotExist, String.valueOf(x) + " / " +guidListInexist);
            x = dbAdapter.updateStatusSync("MovimientoArticuloSerie", StatusSync.EXPORTANDO, StatusSync.EXPORTADO);
            Log.w("Update EXPORTANDO -> EXPORTADO ", String.valueOf(x));

        };
    }
    
    public boolean importMovArticuloSerie(String syncDate) {
        ResultSet rs = conSQL.getResultset("Select * FROM MovimientoArticuloSerie WHERE FechaRegistro > " + date2Sql(syncDate));
        try {
            ArrayList<String> guidList = new ArrayList<String>();
            Cursor cursor;
            if (rs != null) {
                while (rs.next()) {
                    //TODO. Upsert en la base de datos local
                    cursor = dbAdapter.getMovArticuloSerieGuid(rs.getString("MovPosicion").toString());
                    if (cursor.moveToFirst()) {
                        //Tengo el registro... hay que updatarlo
                        dbAdapter.updateStatusSyncGuid("MovimientoArticuloSerie",
                                rs.getString("MovPosicion"),
                                rs.getString("StatusAndroidSync"));
                    } else {
                        //Insertar en la base de datos
                        guidList.add("'" + rs.getString("MovPosicion") + "'");
                    }
                }
                if (guidList.size() > 0) {
                    // Se han encontrado registros que no tengo. hay que insertarlos
                    rs = conSQL.getResultset("Select * FROM MovimientoArticuloSerie WHERE MovPosicion IN (" + TextUtils.join(",", guidList) + ")");
                    copyRecords(rs, "MovimientoArticuloSerie");
                }
            };
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int copyRecords(Cursor source, String tableSource, ResultSet target) {
        ResultSetMetaData RSmd;
        List<String> columnList = new ArrayList();
        List<String> columnListBynary = new ArrayList();
        List<String> columnListDate = new ArrayList();
        List<String> columnListInt = new ArrayList();
        List<String> columnListDecimal = new ArrayList();
        Integer colInt;

        int numReg = source.getCount();
        int progresscount = 0;
        Log.w(tableSource, "A exportar: " + numReg);
        try {
            RSmd = target.getMetaData();
            String[] localColumns = source.getColumnNames();
            for (int i = 1; i <= RSmd.getColumnCount(); i++) {
                if (Arrays.asList(localColumns).contains(RSmd.getColumnName(i))) {
                    switch (RSmd.getColumnType(i)) {
                        case 3: //decimal
                            columnListDecimal.add(RSmd.getColumnName(i));
                            break;
                        case 5: //smallint
                            columnListInt.add(RSmd.getColumnName(i));
                            break;
                        case 93: //datetime
                            columnListDate.add(RSmd.getColumnName(i));
                            break;
                        case 1: //uniqueidentifier
                            columnListBynary.add(RSmd.getColumnName(i));
                            break;
                        default:
                            columnList.add(RSmd.getColumnName(i));
                    }
                }
            }
            while (source.moveToNext()) {
                progresscount++;
                target.moveToInsertRow();
                for (String col : columnListDecimal) {
                    colInt = source.getColumnIndex(col);
                    target.updateDouble(col, source.getDouble(colInt));
                }
                for (String col : columnListInt) {
                    colInt = source.getColumnIndex(col);
                    target.updateInt(col, source.getInt(colInt));
                }
                for (String col : columnListDate) {
                    colInt = source.getColumnIndex(col);
                    if (source.getString(colInt) == null){
                        //target.updateDate(col, null);
                    }else {
                        Date dateSql = new Date(str2date(source.getString(colInt), "yyyy-MM-dd HH:mm:ss").getTime());
                        target.updateDate(col, dateSql);
                    }
                }
                for (String col : columnListBynary) {
                    colInt = source.getColumnIndex(col);

                    if (source.getBlob(colInt) != null) {
                        target.updateString(col, source.getString(colInt));
                    }
                }
                for (String col : columnList) {
                    colInt = source.getColumnIndex(col);
                    String val = source.getString(colInt);
                    if (val == null) {
                        val = "";
                    }
                    target.updateString(col, val);
                }
                target.insertRow();
            }
            Log.w(tableSource, "Exportados: " + source.getCount());
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            numReg = -1;
        }
        return numReg;
    }

    public Integer testData() {
        ResultSet source = conSQL.getResultset("SELECT * FROM VIS_INC_RecaudaInstalaActivas");
        String target = "RecaudacionesAnteriores";
        return copyRecords(source, target);
    }

    public int copyRecords(ResultSet source, String target) {
        ResultSetMetaData RSmd;
        ArrayList<String> columnList = new ArrayList();
        int numReg = 0;
        //dbAdapter.emptyTables(target);
        Log.w(target, "Inicio importacion");
        try {
            RSmd = source.getMetaData();
            SQLiteDatabase db = dbAdapter.getDb();
            Cursor cursor = dbAdapter.getTable(target, 1);
            String[] localColumns = cursor.getColumnNames();
            String campos = "";
            String valores = "";
            String aux = "";
            for (int i = 1; i <= RSmd.getColumnCount(); i++) {
                if (Arrays.asList(localColumns).contains(RSmd.getColumnName(i))) {
                    columnList.add(RSmd.getColumnName(i));
                    if (campos == "") {
                        campos += RSmd.getColumnName(i);
                        valores += "?";
                    } else {
                        campos += ", " + RSmd.getColumnName(i);
                        valores += ", ?";
                    }
                }
            }
            String sql = "INSERT OR REPLACE INTO " + target + " (" + campos + ") VALUES (" + valores + ")";

            db.beginTransactionNonExclusive();
            SQLiteStatement stmt = db.compileStatement(sql);
            while (source.next()) {
                numReg++;
                int i = 1;
                for (String col : columnList) {
                    aux = source.getString(col);
                    if (aux == null) {
                        aux = "";
                    }
                    stmt.bindString(i, aux);
                    i++;
                }
                stmt.execute();
                stmt.clearBindings();
            }
            db.setTransactionSuccessful();
            db.endTransaction();

            Log.w(target, "Reg Importados: " + numReg);

        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return numReg;
    }

    public void emptyLocalTables(){
        dbAdapter.emptyTables();
    }

    private class SynchronizeTest extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            conSQL = new SQLConnection();
            if (SQLConnection.connection == null) {
                return "errorSQLconnection";
            }
            return "Test Realizado N:" + testData();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == "errorSQLconnection") {
                result = myContext.getString(R.string.errorSQLconnection);
            }
            Toast.makeText(myContext, result, Toast.LENGTH_SHORT).show();
        }
    }
    
}
