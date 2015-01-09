package es.incaser.apps.stockcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class DbAdapter extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "StockControl";
    private static final int DATABASE_VER = 1;
    private static Connection conSQL;
    private SQLiteDatabase db;
    private static Context ctx;
    public static String[][] QUERY_LIST = {
            //Tablas a importar
            {"Articulos", "SELECT * FROM VIS_INC_ArticulosAndroid", ""},
            {"GrupoTallas_", "SELECT * FROM GrupoTallas_", ""},
            {"MovimientoStock", "SELECT * FROM VIS_INC_MovimientoStockAndroid", "StatusAndroidSync=1"},
            //{"INC_Incidencias", "SELECT * FROM INC_Incidencias", "INC_PendienteSync <> 0"},
            //Fin Tablas a importar
            //{"MArtSerie", "SELECT * FROM MovimientosArticuloSerie", "(FechaRegistro > GETDATE() - 3)"},
            {"MovimientoArticuloSerie", "SELECT * FROM VIS_INC_MovArticuloSerieAnd", "(FechaRegistro > '2014-12-23') AND StatusAndroidSync=1"},
    };
    public static int tablesToImport = 4; // Modificar en caso de añadir mas tablas
    public static int tablesToExport = 4; // Exportar tablas a partir de este indice
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
        return db.update(table, cv,"StatusAndroidSync=?", new String[]{oldStatus});
    }

    public int updateStatusSyncGuid(String table, String guidList, String newStatus){
        ContentValues cv = new ContentValues();
        cv.put("StatusAndroidSync", newStatus);
        return db.update(table, cv,"MovPosicion in (?)", new String[]{guidList});
    }
    
    public Cursor getMovArticuloSerie(String statusSync) {
        Cursor cur = db.query("MovimientoArticuloSerie", new String[]{"*"}, "StatusAndroidSync=?",
                new String[]{statusSync}, "", "", "");
        return cur;
    }

    
    //*******************************************************************************////
    public Cursor getMaquinasEstablecimiento(String codigoEmpresa, String codigoEstablecimiento) {
        return db.query("Maquinas", new String[]{"*"}, "CodigoEmpresa=? AND INC_CodigoEstablecimiento=?",
                new String[]{codigoEmpresa, codigoEstablecimiento}, "", "", "");
    }

    public Cursor getMaquina(String id) {
        return db.query("Maquinas", new String[]{"*"}, "id=?", new String[]{id}, "", "", "");
    }

    public Cursor getRecaudacion(String codigoEmpresa, String codigoEstablecimiento, String codigoMaquina) {
        return db.query("INC_LineasRecaudacion", new String[]{"*"}, "CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_CodigoMaquina=?",
                new String[]{codigoEmpresa, codigoEstablecimiento, codigoMaquina}, "", "", "");
    }

    public Cursor getCabeceraRecaudacion(String codigoEmpresa, String codigoEstablecimiento) {
        return db.query("INC_CabeceraRecaudacion", new String[]{"*"}, "CodigoEmpresa=? AND INC_CodigoEstablecimiento=?",
                new String[]{codigoEmpresa, codigoEstablecimiento}, "", "", "");
    }

//    public Cursor getUltimaRecaudacion(String codigoEmpresa, String codigoMaquina){
//        String order = "INC_FechaRecaudacion DESC, INC_HoraRecaudacion DESC";
//        return db.query("RecaudacionesAnteriores",new String[]{"*"},"CodigoEmpresa=? AND INC_CodigoMaquina=?",
//                new String[]{codigoEmpresa, codigoMaquina},"","",order,"1");
//    }

    public Cursor getPrestamosEstablecimiento(String codigoEmpresa, String codigoEstablecimiento) {
        return db.query("Prestamos", new String[]{"*"}, "CodigoEmpresa=? AND INC_CodigoEstablecimiento=?",
                new String[]{codigoEmpresa, codigoEstablecimiento}, "", "", "");
    }

    public Cursor getPrestamo(String codigoPrestamo) {
        return db.query("Prestamos", new String[]{"*"}, "INC_CodigoPrestamo=?",
                new String[]{codigoPrestamo}, "", "", "");
    }

    public Map<String, String> getDicRelLineasCabecerax() {
        Map<String, String> dicRelLineasCabecera = new HashMap<String, String>();
        dicRelLineasCabecera.put("INC_ImporteRecaudacion", "INC_TotalRecaudacion");
        dicRelLineasCabecera.put("INC_ImporteRetencion", "INC_TotalRetencion");
        dicRelLineasCabecera.put("INC_ImporteNeto", "INC_TotalNeto");
        dicRelLineasCabecera.put("INC_ImporteEstablecimiento", "INC_TotalEstablecimiento");

        return dicRelLineasCabecera;
    }

    public Cursor getTotalesRecaudacion(String empresa, String establecimiento, String fecha) {
//        Map<String, String> dicRelLineasCabecera = getDicRelLineasCabecera();
//        String[] campos = dicRelLineasCabecera.keySet().toString().replace("[","").replace("]","").split(",");
        String[] campos = new String[]{
                "SUM(INC_ImporteRecaudacion) AS INC_TotalRecaudacion",
                "SUM(INC_ImporteRetencion) AS INC_TotalRetencion",
                "SUM(INC_ImporteNeto) AS INC_TotalNeto",
                "SUM(INC_ImporteEstablecimiento) AS INC_TotalEstablecimiento",
                "SUM(INC_ImporteNeto + INC_ImporteRetencion)  AS INC_TotalNetoMasRetencion",
                "SUM(INC_RecuperaCargaEmpresa) AS INC_TotalRecuperaCarga",
                "0 AS INC_TotalRecuperaPrestamo",
                "0 AS INC_TotalSaldo",
                "0 AS INC_MaquinasInstaladas",
                "COUNT() AS INC_MaquinasRecaudadas",
                "0 AS INC_StatusAlbaran",
                "0 AS INC_Porcentaje",
        };

        String where = "Printable=1 AND CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_FechaRecaudacion=?";
        String groupBy = "CodigoEmpresa, INC_CodigoEstablecimiento, INC_FechaRecaudacion";
        String[] whereArgs = new String[]{empresa, establecimiento, fecha};
        return db.query("INC_LineasRecaudacion", campos, where, whereArgs, groupBy, "", "");
    }

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

    public Cursor getUltimoArqueo(String empresa, String establecimiento, String maquina) {
        String[] cols = new String[]{"INC_FechaRecaudacion", "INC_ValorArqueoTeorico"};
        String where = "INC_ArqueoRealizado<>0 AND CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_CodigoMaquina=?";
        String[] whereArgs = new String[]{empresa, establecimiento, maquina};

        return db.query("RecaudacionesAnteriores", cols, where, whereArgs, "", "", "INC_FechaRecaudacion DESC, INC_HoraRecaudacion DESC", "1");
    }

    public Cursor getUltimaRecaudacion(String empresa, String establecimiento, String maquina) {
        String[] cols = new String[]{"*"};
        String where = "CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_CodigoMaquina=?";
        String[] whereArgs = new String[]{empresa, establecimiento, maquina};

        return db.query("RecaudacionesAnteriores", cols, where, whereArgs, "", "", "INC_FechaRecaudacion DESC, INC_HoraRecaudacion DESC", "1");
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


    public Cursor getRecuperacionesPrestamo(String empresa, String codigoPrestamo) {
        String[] cols = new String[]{"*"};
        String where = "CodigoEmpresa=? AND INC_CodigoPrestamo=?";
        String[] whereArgs = new String[]{empresa, codigoPrestamo};

        return db.query("INC_RecuperacionesPrestamo", cols, where, whereArgs,
                "", "", "INC_FechaRecuperacion DESC");
    }

    public Cursor getSumasDesde(String empresa, String establecimiento, String maquina, String fechaDesde) {
        String[] cols = new String[]{"SUM(INC_Bruto) AS SumaBruto",
                "SUM(INC_JugadoTeorico) AS SumaJugadoTeorico",
                "SUM(INC_PremioTeorico) AS SumaPremioTeorico",
                "SUM(INC_ImporteRetencion) AS SumaImporteRetencion",
                "SUM(INC_RecuperaCargaEmpresa) AS SumaRecuperaCargaEmpresa",
                "SUM(INC_RecuperaCargaEstablecimiento) AS SumaRecuperaCargaEstablecimiento",
                "SUM(INC_CargaHopperEmpresa) AS SumaCargaHopperEmpresa",
                "SUM(INC_CargaHopperEstablecimiento) AS SumaCargaHopperEstablecimiento"};
        String where = "CodigoEmpresa=? AND INC_CodigoEstablecimiento=? AND INC_CodigoMaquina=? AND INC_FechaRecaudacion > ?";
        String[] whereArgs = new String[]{empresa, establecimiento, maquina, fechaDesde};

        return db.query("RecaudacionesAnteriores", cols, where, whereArgs, "", "", "INC_FechaRecaudacion DESC, INC_HoraRecaudacion DESC", "1");
    }

    public void deleteRecaudacion(String idRecaudacion) {
        db.delete("INC_LineasRecaudacion", "id=?", new String[]{idRecaudacion});
    }

    public void deleteCabRecaudacion(String idCabRecaudacion) {
        db.delete("INC_CabeceraRecaudacion", "id=?", new String[]{idCabRecaudacion});
    }

    public void deleteRecuperacion(String idRecuperacion) {
        db.delete("INC_RecuperacionesPrestamo", "id=?", new String[]{idRecuperacion});
    }

    public float getTotalRecuperaPrestamo(String codigoRecaudacion) {
        String[] cols = new String[]{"SUM(ImporteLiquido) AS SumaImporteLiquido"};
        String where = "INC_CodigoRecaudacion=?";
        String[] whereArgs = new String[]{codigoRecaudacion};

        Cursor cur = db.query( "INC_RecuperacionesPrestamo", cols, where, whereArgs, "", "", "");
        if (cur.moveToFirst()){
            return cur.getFloat(0);
        }else {
            return 0;
        }
    }

    public Cursor getTotalRecaudadoAll() {
        String[] campos = new String[]{
                "SUM(INC_TotalRecaudacion) AS INC_TotalRecaudacion",
                "SUM(INC_TotalRetencion) AS INC_TotalRetencion",
                "SUM(INC_TotalNeto) AS INC_TotalNeto",
                "SUM(INC_TotalEstablecimiento) AS INC_TotalEstablecimiento",
                "SUM(INC_TotalNetoMasRetencion)  AS INC_TotalNetoMasRetencion",
                "SUM(INC_TotalRecuperaCarga) AS INC_TotalRecuperaCarga",
                "SUM(INC_TotalRecuperaPrestamo) AS INC_TotalRecuperaPrestamo",
                "SUM(INC_TotalSaldo) AS INC_TotalSaldo",
                "SUM(INC_MaquinasRecaudadas) AS INC_MaquinasRecaudadas"
        };
        String where = "";
        String[] whereArgs = new String[]{};

        return db.query( "INC_CabeceraRecaudacion", campos, where, whereArgs, "", "", "");
    }

}
