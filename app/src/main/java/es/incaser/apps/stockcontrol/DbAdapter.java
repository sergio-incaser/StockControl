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
        return db.query("MovimientoStock", new String[]{"*"}, "TipoMovimiento=? AND FechaRegistro>? AND StatusAndroidSync=?",
                new String[]{tipoMov, lastSyncDate, StatusSync.PREVISTO}, "", "", "FechaRegistro");
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
    public Cursor getExpediciones(String codigoEmpresa) {
        // MovimientosStock Distinct serie-documento
        String table = "MovimientoStock LEFT JOIN Choferes ON MovimientoStock.CodigoChofer=Choferes.CodigoChofer";
        String campos ="MAX(MovimientoStock.id) as id, MovimientoStock.CodigoEmpresa, Serie, Documento," +
                "MAX(FechaRegistro) AS FechaRegistro, SUM(Unidades) AS Unidades," +
                "MAX(Matricula) as Matricula, MAX(MatriculaRemolque) as MatriculaRemolque," +
                "MAX(MovimientoStock.CodigoChofer) as CodigoChofer, MAX(Choferes.RazonSocial) as RazonSocial, MIN(MovimientoStock.StatusAndroidSync) AS StatusAndroidSync";
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

    public String getPesoEscaneado(String codigoEmpresa, String tipoMovimiento, String serie, String documento) {
        Cursor cur = getSumMovArticuloSerie(codigoEmpresa, tipoMovimiento, serie, documento);
        Double peso = 0.0;
        if(cur.moveToFirst()){
            peso = cur.getDouble(cur.getColumnIndex("TBL_Peso"))/1000;
        }
        return String.valueOf(peso);
    }
    

    public long createMovimientoArticuloSerie(Cursor curMovStock, String numeroSerie) {
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
        //cv.put("FechaRegistro", MainActivity.contextNow());
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
