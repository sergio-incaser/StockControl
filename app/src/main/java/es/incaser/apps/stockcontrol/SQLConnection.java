package es.incaser.apps.stockcontrol;

import android.text.TextUtils;

import net.sourceforge.jtds.jdbc.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 * Created by sergio on 5/09/14.
 */
public class SQLConnection {
    private static SQLConnection instance = null;
    public static String host;
    public static String port;
    public static String user;
    public static String password;
    public static String database;

    public static Connection connection = null;
    public static Statement statement;
    public static Statement statementWrite;

    public SQLConnection() {
        if (connection == null)
            connection = connectSQL();
    }

    public static SQLConnection getInstance() {
        if (instance == null)
            instance = new SQLConnection();
        return instance;
    }

    public Connection getConnection() {
        if (connection == null)
            connection = connectSQL();
        return connection;
    }

    private Connection connectSQL() {
        Connection conn = null;
        (new Driver()).getClass();
        try {
            String uri = "jdbc:jtds:sqlserver://" + host + ":" + port + "/" + database + ";";
            conn = DriverManager.getConnection(uri, user, password);
            statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            statementWrite = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }

    public ResultSet getEstablecimientos() {
        if (connection == null)
            connection = connectSQL();
        String sql = "Select INC_CodigoEstablecimiento as id, * From INC_Establecimientos";
        ResultSet rs = null;

        try {
//            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
//                    ResultSet.CONCUR_READ_ONLY);
            rs = statement.executeQuery(sql);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public ResultSet getResultset(String query) {
        return getResultset(query, false);
    }

    public ResultSet getResultset(String query, boolean writable) {
        ResultSet rs = null;
        try {
            if (writable) {
                rs = statementWrite.executeQuery(query);
            } else {
                rs = statement.executeQuery(query);
            }
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
    
    public String getDate(){
        ResultSet rs = getResultset("SELECT GETDATE()");
        String res  = "2000-01-01 00:00:00.0";
        try {
            if (rs.next()){
                res = rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
    
    public ArrayList<String> getGuidsInexistentes(ArrayList<String> guidList){
        ResultSet rs = getResultset("SELECT MovPosicion FROM MovimientoArticuloSerie " +
                "WHERE MovPosicion IN (" + TextUtils.join(",", guidList) + ")");
        try {
            while (rs.next()){
                guidList.remove("'"+rs.getString(0).toString()+"'");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  guidList;
    };

    public int updateSQL(String query) {
        int res = 0;
        try {
            res = statementWrite.executeUpdate(query);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
