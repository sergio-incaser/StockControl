package es.incaser.apps.stockcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class DbAdapter extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StockControl";
    private static final int DATABASE_VER = 1;
    private SQLiteDatabase db;
    private static Context ctx;
    public static String[][] QUERY_LIST = {
            //Tablas a importar
            {"Articulos", "SELECT * FROM VIS_INC_ArticulosAndroid", ""},
            {"GrupoTallas_", "SELECT * FROM GrupoTallas_", ""},
            {"MovimientoStock", "SELECT * FROM VIS_INC_MovimientoStockAndroid", "StatusAndroidSync=1"},
            {"ArticulosSeries", "SELECT * FROM VIS_INC_ArticulosSeriesAnd", ""},
            {"TRA_Vehiculos", "SELECT * FROM VIS_AND_Vehiculos", ""},
            {"TRA_Remolques", "SELECT * FROM VIS_AND_Remolques", ""},
            {"Choferes", "SELECT * FROM VIS_AND_Choferes", ""},
            //{"INC_Incidencias", "SELECT * FROM INC_Incidencias", "INC_PendienteSync <> 0"},
            //Fin Tablas a importar
            //{"MArtSerie", "SELECT * FROM MovimientosArticuloSerie", "(FechaRegistro > GETDATE() - 3)"},
            {"MovimientoArticuloSerie", "SELECT * FROM VIS_INC_MovArticuloSerieAnd", "(FechaRegistro > '2014-12-23') AND StatusAndroidSync=1"},
    };
    public static int tablesToImport = 8; // Modificar en caso de añadir mas tablas
    public static int tablesToExport = 7; // Exportar tablas a partir de este indice
    private SQLConnection sqlConnection;

    public DbAdapter(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VER);
        ctx = context;
        openDB();
    }

    @Override
    protected void finalize() throws Throwable {
        closeDB();
        super.finalize();
    }

    private class GetDBConnection extends AsyncTask<Integer, Void, String> {
        @Override
        protected String doInBackground(Integer... params) {
            sqlConnection = new SQLConnection();
            if (SQLConnection.connection == null) {
                db.setVersion(db.getVersion() - 1);
                return "errorSQLconnection";
            }
            ResultSet rs;
            ResultSetMetaData rsmd;
            String colname;
            String coltype;
            String columnsSql = "";
            String createSql = "";

            for (String[] query : QUERY_LIST) {
                columnsSql = "";
                try {
                    rs = sqlConnection.getResultset(query[1] + " WHERE 1=2");
                    rsmd = rs.getMetaData();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        colname = rsmd.getColumnName(i);
                        Log.d(query[0] + " " + String.valueOf(columnsSql.length()), colname + " " + String.valueOf(i));

                        coltype = rsmd.getColumnTypeName(i);
                        columnsSql += ", '" + colname + "' " + coltype;
                    }
                    createSql = "CREATE TABLE " + query[0] + " ('id' INTEGER PRIMARY KEY AUTOINCREMENT" + columnsSql + ");";
                    db.execSQL(createSql);
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
            return "Base de datos actualizada";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == "errorSQLconnection") {
                result = ctx.getString(R.string.errorSQLconnection);
            }
            Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        //SQLConnection conInstance = SQLConnection.getInstance();
        new GetDBConnection().execute(1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        Log.w("SlotCollet", "Actualizando base de datos version:" + newVersion);
        for (String[] query : QUERY_LIST) {
            db.execSQL("DROP TABLE IF EXISTS " + query[0]);
        }
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void recreateDb(){
        for (String[] query : QUERY_LIST) {
            db.execSQL("DROP TABLE IF EXISTS " + query[0]);
        }
        onCreate(db);
    }
    
    public boolean checkTables(){
        String tableList = "";
        int i = 0;
        for (String[] query : QUERY_LIST) {
            if (i > 0){
                tableList = tableList + ",";
            }
            tableList = tableList + "'" + query[0] + "'";
            i ++;
        }
        Cursor curtmp = getCursor("Select * from sqlite_master WHERE name IN (" + tableList + ")");
        if (curtmp.getCount() == i){
            return true;
        }else {
            return false;
        }
    }

    public void emptyTables() {
        for (String[] query : QUERY_LIST) {
            db.execSQL("DELETE FROM " + query[0]);
        }
    }

    public void emptyTables(String table) {
        db.execSQL("DELETE FROM " + table);
    }

    public void openDB() {
        if (db == null) {
            db = this.getWritableDatabase();
        }
    }

    public void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    public Cursor getCursorBuscador(String textSearch, String tableSearch, String order) {
        textSearch = textSearch.replace("'", "''");
        String[] fields = new String[]{"*", "id  AS _id", "RazonSocial AS name"};
        String where = "";
        String[] selectionArgs = new String[]{};
        String orderBy = "id";
        String table = tableSearch.toString();

        if (order.length() > 0) orderBy = order;

        if (textSearch.length() > 0) {
            selectionArgs = new String[]{"%" + textSearch + "%"};
            if (where.length() > 0) {
                where += " AND ";
            }
            where += "name LIKE ?";
        }
        return db.query(table, fields, where, selectionArgs, "", "", orderBy);
    }

    public Cursor getTable(String tableName) {
        return db.query(tableName, new String[]{"*"}, "", new String[]{}, "", "", "");
    }

    public Cursor getTable(String tableName, Integer limit) {
        return db.query(tableName, new String[]{"*"}, "", new String[]{}, "", "", "", limit.toString());
    }

    public Cursor getTable(String tableName, String where) {
        return db.query(tableName, new String[]{"*"}, where, null, "", "", "");
//        if (where == "") {
//            return db.query(tableName, new String[]{"*"}, "?", new String[]{where}, "", "", "");
//        }else{
//            return db.rawQuery("SELECT * FROM " + tableName+ " WHERE " + where,null);
//        }
    }

    public Cursor getTableToExport(String tableName) {
        return db.query(tableName, new String[]{"*"}, "printable=?", new String[]{"-1"}, "", "", "");
    }

    public String getMaxTimeStamp(){
        Cursor cursor = db.rawQuery("Select MAX(FechaRegistro) FROM MovimientoArticuloSerie",new String[]{});
        if (cursor.moveToFirst()){
            return cursor.getString(0);
        }else {
            return "2015-01-01 00:00:00.0";
        }
    }

    public int updateStatusSync(String table, String oldStatus, String newStatus){
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        SQLConnection conSQL = new SQLConnection();
        cv.put("FechaRegistro", conSQL.getDate());
        return db.update(table, cv,"StatusAndroidSync=?", new String[]{oldStatus});
    }

    public int updateStatusSync(String table, String tipoMov, String oldStatus, String newStatus){
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        SQLConnection conSQL = new SQLConnection();
        cv.put("FechaRegistro", conSQL.getDate());
        return db.update(table, cv,"OrigenDocumento=? AND StatusAndroidSync=?",new String[]{TipoMovimiento.origenMov(tipoMov), oldStatus});
    }

    public int updateStatusSyncGuid(String table, String guid, String newStatus){
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        //SQLConnection conSQL = new SQLConnection();
        //cv.put("FechaRegistro", conSQL.getDate());
        return db.update(table, cv,"MovPosicion=?", new String[]{guid});
    }

    public int updateStatusSyncGuid(String table, ArrayList<String> guidList, String newStatus){
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        SQLConnection conSQL = new SQLConnection();
        cv.put("FechaRegistro", conSQL.getDate());
        String strList = TextUtils.join(",", guidList);
        //No funciona por parametros
        return db.update(table, cv, "MovPosicion in ("+strList+")", new String[]{});
    }

    
/*
    public int updateMovimientoArticuloSerie(String numeroSerie) {
        //numeroSerie = "'" + numeroSerie + "'";
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", 1);
        return db.update("MovimientoArticuloSerie", cv,"NumeroSerieLc = ?", new String[]{numeroSerie});
    }
*/

    public int updateMovimientoArticuloSerie(String movPosicion) {
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", StatusSync.ESCANEADO);
        return db.update("MovimientoArticuloSerie", cv,"MovPosicion = ?", new String[]{movPosicion});
    }
    
    public Cursor getMovArticuloSerie(String statusSync) {
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"*"}, "StatusAndroidSync=?",
                new String[]{statusSync}, "", "", "");
        return cur;
    }

    public Cursor getMovArticuloSerieGuid(String movPosicion) {
        //String a= "'" + movPosicion + "'";
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"*"}, "MovPosicion=?",
                new String[]{movPosicion}, "", "", "");
        return cur;
    }

    public Cursor getMovimientoStockModif(String tipoMov, String lastSyncDate) {
        Cursor cur = db.query("MovimientoStock", new String[]{"*"}, "TipoMovimiento=? AND FechaRegistro>? AND StatusAndroidSync=?",
                new String[]{tipoMov, lastSyncDate, StatusSync.ESCANEADO}, "", "", "FechaRegistro");
        return cur;
    }

    public Cursor getMovimientoStockToCreate(String tipoMov, String lastSyncDate) {
        return db.query("MovimientoStock", new String[]{"*"}, "TipoMovimiento=? AND FechaRegistro>? AND StatusAndroidSync=?",
                new String[]{tipoMov, lastSyncDate, StatusSync.PARA_CREAR}, "", "", "FechaRegistro");
    }

    public Cursor getMovimientoStock(String codigoEmpresa, String tipoMov) {
        return db.query("MovimientoStock", new String[]{"*"}, "CodigoEmpresa=? AND TipoMovimiento=?",
                new String[]{codigoEmpresa, tipoMov}, "", "", "FechaRegistro DESC");
    }
    public Cursor getMovimientoStock(String codigoEmpresa, String tipoMov, String serie, String documento) {
        String sqlWhere = "CodigoEmpresa=? AND TipoMovimiento=? AND Serie=? AND Documento=?";
        return db.query("MovimientoStock", new String[]{"*"}, sqlWhere,
                new String[]{codigoEmpresa, tipoMov, serie, documento}, "", "", "FechaRegistro DESC");
    }

    public Cursor getMovimientoStock(String codigoEmpresa, String tipoMov, String serie, String documento, String codArticulo, String codTalla) {
        String sqlWhere = "CodigoEmpresa=? AND TipoMovimiento=? AND Serie=? AND Documento=? AND CodigoArticulo=? AND CodigoTalla01_=?";
        return db.query("MovimientoStock", new String[]{"*"}, sqlWhere,
                new String[]{codigoEmpresa, tipoMov, serie, documento, codArticulo, codTalla}, "", "", "FechaRegistro DESC");
    }

    public Cursor getRecepciones(String codigoEmpresa) {
        String table = "MovimientoStock";
        String campos ="MAX(id) as id, CodigoEmpresa, Ejercicio, Serie, Documento,  MatriculaTransporte_, SUM(Unidades) AS Unidades";
        String groupBy = "CodigoEmpresa, Ejercicio, Serie, Documento,  MatriculaTransporte_";
        return db.query(table, new String[]{campos}, "CodigoEmpresa=? AND TipoMovimiento=?",
                new String[]{codigoEmpresa, TipoMovimiento.ENTRADA}, groupBy, "", "FechaRegistro DESC");
    }

    public Cursor getRecepcionesSearch(String codigoEmpresa, String searchText) {
        searchText = "%" + searchText +"%";
        String table = "MovimientoStock";
        String campos ="MAX(id) as id, CodigoEmpresa, Ejercicio, Serie, Documento,  MatriculaTransporte_, SUM(Unidades) AS Unidades";
        String groupBy = "CodigoEmpresa, Ejercicio, Serie, Documento,  MatriculaTransporte_";
        return db.query(table, new String[]{campos}, "CodigoEmpresa=? AND TipoMovimiento=? AND (Documento like ? OR MatriculaTransporte_ like ?)",
                new String[]{codigoEmpresa, TipoMovimiento.ENTRADA, searchText, searchText}, groupBy, "", "FechaRegistro DESC");
    }

    public Cursor getExpediciones(String codigoEmpresa) {
        // MovimientosStock Distinct serie-documento
        String table = "MovimientoStock LEFT JOIN Choferes ON MovimientoStock.CodigoChofer=Choferes.CodigoChofer";
        String campos ="MAX(MovimientoStock.id) as id, MovimientoStock.CodigoEmpresa, Serie, Documento," +
                "MAX(FechaRegistro) AS FechaRegistro, SUM(Unidades) AS Unidades," +
                "MAX(Matricula) as Matricula, MAX(MatriculaRemolque) as MatriculaRemolque," +
                "MAX(MovimientoStock.CodigoChofer) as CodigoChofer, MAX(Choferes.RazonSocial) as RazonSocial," +
                "MIN(MovimientoStock.StatusAndroidSync) AS StatusAndroidSync," +
                "MAX(NumeroExpedicion) AS NumeroExpedicion, MAX(CodigoDestinatario) AS CodigoDestinatario";
        return db.query(table, new String[]{campos}, "MovimientoStock.CodigoEmpresa=? AND TipoMovimiento=?",
                new String[]{codigoEmpresa, TipoMovimiento.SALIDA}, "MovimientoStock.CodigoEmpresa, Serie, Documento", "", "Serie, Documento");
    }

    public int getNumSerieLeidosCount(String movPosicion) {
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"id"}, "MovPosicionOrigen=? AND StatusAndroidSync >= ?",
                new String[]{movPosicion, StatusSync.ESCANEADO}, "", "", "");
        return cur.getCount();
    }

    public Cursor getSumMovArticuloSerie(String codigoEmpresa, String tipoMovimiento, String serie, String documento) {
        String where ="CodigoEmpresa=? AND OrigenDocumento=? AND SerieDocumento=? AND Documento=? AND StatusAndroidSync >= ?";
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"SUM(TBL_Peso) AS TBL_Peso"}, where,
                new String[]{codigoEmpresa, TipoMovimiento.origenMov(tipoMovimiento), serie, documento, StatusSync.ESCANEADO}, "", "", "");
        return cur;
    }

    public Cursor getMovArticuloSerieByDoc(String codigoEmpresa, String tipoMovimiento, String serie, String documento) {
        String where ="CodigoEmpresa=? AND OrigenDocumento=? AND SerieDocumento=? AND Documento=?";
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"*"}, where,
                new String[]{codigoEmpresa, TipoMovimiento.origenMov(tipoMovimiento), serie, documento}, "", "", "");
        return cur;
    }

    public Cursor getMovArticuloSerieByDoc(String codigoEmpresa, String tipoMovimiento, String serie, String documento, String articulo) {
        String where ="CodigoEmpresa=? AND OrigenDocumento=? AND SerieDocumento=? AND Documento=? AND CodigoArticulo=?";
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"*"}, where,
                new String[]{codigoEmpresa, TipoMovimiento.origenMov(tipoMovimiento), serie, documento, articulo}, "", "", "");
        return cur;
    }

    public void updateUnidadesMovStock(String codigoEmpresa, String tipoMovimiento, String serie, String documento) {
        String sql = "UPDATE MovimientoStock SET Unidades = (SELECT COUNT(id) " +
                    "FROM MovimientoArticuloSerie " +
                    "WHERE MovPosicionOrigen = MovimientoStock.MovPosicion), " +
                    "Unidades2_ =  (SELECT COUNT(id) " +
                    "FROM MovimientoArticuloSerie " +
                    "WHERE MovPosicionOrigen = MovimientoStock.MovPosicion) " +
                    "WHERE CodigoEmpresa=? AND TipoMovimiento=? AND Serie=? AND Documento=?";
        db.execSQL(sql, new String[]{codigoEmpresa, tipoMovimiento, serie, documento});
    }
    
    public String getPesoEscaneado(String codigoEmpresa, String tipoMovimiento, String serie, String documento) {
        Cursor cur = getSumMovArticuloSerie(codigoEmpresa, tipoMovimiento, serie, documento);
        Double peso = 0.0;
        if(cur.moveToFirst()){
            peso = cur.getDouble(cur.getColumnIndex("TBL_Peso"))/1000;
        }
        return String.valueOf(peso);
    }
    

    public long createMovimientoArticuloSerie(Cursor curMovStock, Cursor curArticulosSeries, String numeroSerie) {
        ContentValues cv = new ContentValues();
        cv.put("CodigoEmpresa", MainActivity.codigoEmpresa);
        cv.put("CodigoArticulo", curMovStock.getString(curMovStock.getColumnIndex("CodigoArticulo")));
        cv.put("NumeroSerieLc", numeroSerie);
        cv.put("Fecha", Tools.getToday());
        cv.put("FechaRegistro", MainActivity.contextNow());
        cv.put("OrigenDocumento", TipoMovimiento.ORIGEN_SALIDA);
        cv.put("EjercicioDocumento", curMovStock.getString(curMovStock.getColumnIndex("Ejercicio")));
        cv.put("SerieDocumento", curMovStock.getString(curMovStock.getColumnIndex("Serie")));
        cv.put("Documento", curMovStock.getString(curMovStock.getColumnIndex("Documento")));
        cv.put("MovPosicion", UUID.randomUUID().toString().toUpperCase());
        cv.put("MovPosicionOrigen", curMovStock.getString(curMovStock.getColumnIndex("MovPosicion")));
        cv.put("CodigoTalla01_", curMovStock.getString(curMovStock.getColumnIndex("CodigoTalla01_")));
        cv.put("CodigoAlmacen", curMovStock.getString(curMovStock.getColumnIndex("CodigoAlmacen")));
        cv.put("Ubicacion", curMovStock.getString(curMovStock.getColumnIndex("Ubicacion")));
        cv.put("UnidadesSerie", 1);
        cv.put("TBL_Peso", curArticulosSeries.getString(curArticulosSeries.getColumnIndex("TBL_Peso")));
        cv.put("TBL_Longitud", curArticulosSeries.getString(curArticulosSeries.getColumnIndex("TBL_Longitud")));
        cv.put("TBL_AnoBobina", curArticulosSeries.getString(curArticulosSeries.getColumnIndex("TBL_AnoBobina")));
        cv.put("StatusAndroidSync", StatusSync.ESCANEADO);
        return db.insert("MovimientoArticuloSerie", null, cv);
    }

    public long createMovimientoStock(ContentValues cv){
        return db.insert("MovimientoStock", null, cv);
    }
    
    public Cursor getArticulosSeries(String codigoEmpresa, String numeroSerie) {
        String campos = " ArticulosSeries.*, Articulos.CodigoSubfamilia, Articulos.PesoNetoUnitario_, Articulos.VolumenUnitario_";
        String table = "ArticulosSeries LEFT JOIN Articulos ON\n" +
                " ArticulosSeries.CodigoEmpresa = Articulos.CodigoEmpresa AND ArticulosSeries.CodigoArticulo = Articulos.CodigoArticulo";
        return db.query(table, new String[]{campos}, "ArticulosSeries.CodigoEmpresa=? AND ArticulosSeries.NumeroSerieLc=?",
                new String[]{codigoEmpresa, numeroSerie}, "", "", "");
    }

/*
    public int updateMovimientoStock(String numeroSerie) {
        int res = 0;
        //numeroSerie = "'" + numeroSerie + "'";
        Cursor cursor = db.query("MovimientoArticuloSerie", new String[]{"MovPosicionOrigen"}, "StatusAndroidSync=? AND NumeroSerieLc=?",
                new String[]{StatusSync.ESCANEADO, numeroSerie}, "", "", "");
        if (cursor.moveToFirst()){
            ContentValues cv = new ContentValues();
            cv.put("FechaRegistro", getToday("yyyy-MM-dd HH:mm:ss.S"));
            String movPosicionOrigen = cursor.getString(cursor.getColumnIndex("MovPosicionOrigen"));
            //movPosicionOrigen = "'" + movPosicionOrigen + "'";
            res = db.update("MovimientoStock", cv, "MovPosicion = ?", new String[]{movPosicionOrigen});
        };
        return res;
    }
*/

    public int updateMovimientoStock(String movPosicion) {
        ContentValues cv = new ContentValues();
        cv.put("FechaRegistro", MainActivity.contextNow());
        return db.update("MovimientoStock", cv, "MovPosicion = ?", new String[]{movPosicion});
    }

    public int updateUnidadesMovStock(String movPosicion, int numArticulos) {
        ContentValues cv = new ContentValues();
        cv.put("FechaRegistro", MainActivity.contextNow());
        cv.put("Unidades", numArticulos);
        cv.put("Unidades2_", numArticulos);
        return db.update("MovimientoStock", cv, "MovPosicion = ?", new String[]{movPosicion});
    }

    public int updateMovimientoStock(String tipoMov, String serie, String documento, ContentValues cv) {
        cv.put("FechaRegistro", MainActivity.contextNow());
        return db.update("MovimientoStock", cv, "TipoMovimiento=? AND Serie=? AND Documento=?", new String[]{tipoMov, serie, documento});
    }

    public int updateMovimientoStock(String tipoMov, String serie, String documento, String oldStatus, String newStatus) {
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        cv.put("FechaRegistro", MainActivity.contextNow());
        String where = "TipoMovimiento=? AND Serie=? AND Documento=? AND StatusAndroidSync=?";
        return db.update("MovimientoStock", cv, where, new String[]{tipoMov, serie, documento, oldStatus});
    }
    
    public Cursor getMovArticuloSerieNumSerie(String origenDoc, String numeroSerie){
        String where = "OrigenDocumento=? AND NumeroSerieLc=?";
        Cursor cursor = db.query("MovimientoArticuloSerie", new String[]{"MovPosicion","MovPosicionOrigen"}, where,
                new String[]{origenDoc, numeroSerie}, "", "", "");
        return cursor;
    }

    public Cursor getBusqueda(String tabla, String campoBusqueda, String campoRetorno, String searchText){
        searchText = "%" + searchText +"%";
        String where = campoBusqueda + " like ?";
        Cursor cursor = db.query(tabla, new String[]{"id", campoBusqueda, campoRetorno}, where,
                new String[]{searchText}, "", "", campoRetorno);
        return cursor;
    }


    //*******************************************************************************////

    public Cursor getCursor(String query) {
        return db.rawQuery(query, new String[]{});
    }

    public int recordCount(String tableName) {
        Cursor cursor = db.rawQuery("SELECT count() FROM " + tableName, new String[]{});
        if (cursor != null) {
            return cursor.getCount();
        } else {
            return 0;
        }
    }

    public long insertRecord(String tableName, ContentValues values) {
        return db.insert(tableName, null, values);
    }

    ;

    public int updateRecord(String table, ContentValues values, String whereClause, String[] whereArgs) {
        return db.update(table, values, whereClause, whereArgs);
    }

    ;

    public String getColumnData(Cursor cur, String column) {
        return cur.getString(cur.getColumnIndex(column));
    }

    ;

    public void initTransaction() {
        db.beginTransaction();
    }

    public void endTransaction() {
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    SQLiteDatabase getDb() {
        return db;
    }

    public Cursor getIncidencias(String empresa, String establecimiento, String maquina) {
        return getIncidencias(empresa, establecimiento, maquina, false);
    }

    public Cursor getIncidencias(String empresa, String establecimiento, String maquina, boolean onlyPendingSync) {
        String[] cols = new String[]{"*"};
        String where = "CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_CodigoMaquina=? ";
        if (onlyPendingSync) {
            where += " AND INC_PendienteSync = -1";
        }
        String[] whereArgs = new String[]{empresa, establecimiento, maquina};

        return db.query("INC_Incidencias", cols, where, whereArgs, "", "", "Fecha DESC");
    }

    public Cursor getIncidencia(long idIncidencia) {
        String[] cols = new String[]{"*"};
        String where = "id=? ";
        String[] whereArgs = new String[]{idIncidencia + ""};

        return db.query("INC_Incidencias", cols, where, whereArgs, "", "", "");
    }
}
