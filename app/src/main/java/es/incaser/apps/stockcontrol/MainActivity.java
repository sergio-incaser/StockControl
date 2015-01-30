package es.incaser.apps.stockcontrol;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;


public class MainActivity extends ActionBarActivity {
    Timer timerHard = new Timer();
    Timer timerSoft = new Timer();
    static int pref_hard_sync;
    static int pref_soft_sync;
    static String codigoEmpresa = "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ReadPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id){
            case R.id.action_settings:
                Intent myIntent = new Intent(this, SettingsActivity.class);
                startActivity(myIntent);
                break;
            case R.id.action_db_rebuild:
                DbAdapter dbAdapter = new DbAdapter(getApplicationContext());
                dbAdapter.recreateDb();
                break;
            case R.id.action_soft_sync:
                launcSoftSync();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickLayout(View view){
        Intent intent;
        switch (view.getId()){
            case R.id.lay_hard_sincro:
                //forzamos el proceso de sincronizacion de los movimientos
                launchHardSync();
                break;
            case R.id.lay_entradas_previstas:
                intent = new Intent(this, BarcodeReader.class);
                intent.putExtra("tipoMov",TipoMovimiento.ENTRADA);
                startActivity(intent);
                break;
            case R.id.lay_expediciones:
                intent = new Intent(this, Expediciones.class);
                startActivity(intent);
                break;
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case 0:
                    Toast.makeText(getApplicationContext(),msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 15:
                    Toast.makeText(getApplicationContext(),"Hola Pepito", Toast.LENGTH_SHORT).show();
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void launchHardSync(){
        if (pref_hard_sync > 0){
            TimerTaskHard timerTaskHard = new TimerTaskHard(handler, getApplicationContext());
            timerHard.schedule(timerTaskHard, 1500, pref_hard_sync * 1000);
        }
    }

    private void launcSoftSync(){
        if(pref_soft_sync > 0){
            TimerTaskSoft timerTaskSoft = new TimerTaskSoft(handler, getApplicationContext());
            timerSoft.schedule(timerTaskSoft, 1500, pref_soft_sync * 1000);
        }
    }

    
    public static void ReadPreferences(Activity act) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(act);
        if (pref.getBoolean("pref_out_office", false)) {
            SQLConnection.host = pref.getString("pref_sql_host_remote", "");
        } else {
            SQLConnection.host = pref.getString("pref_sql_host", "");
        }
        SQLConnection.port = pref.getString("pref_sql_port", "");
        SQLConnection.user = pref.getString("pref_sql_user", "");
        SQLConnection.password = pref.getString("pref_sql_password", "");
        SQLConnection.database = pref.getString("pref_sql_database", "");
        pref_hard_sync = Integer.parseInt(pref.getString("pref_hard_sync", "0"));
        pref_soft_sync = Integer.parseInt(pref.getString("pref_soft_sync", "0"));
        //TODO. Leer codigoEmpresa de preferebnces
    }

    @Override
    protected void onDestroy() {
        timerHard.cancel();
        timerSoft.cancel();
        super.onDestroy();
    }
}