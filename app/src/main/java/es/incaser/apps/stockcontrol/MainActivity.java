package es.incaser.apps.stockcontrol;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.Timer;


public class MainActivity extends ActionBarActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        launchHardSync();
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
        if (id == R.id.action_settings) {
            Intent myIntent = new Intent(this, SettingsActivity.class);
            startActivity(myIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickButtons(View view){
        switch (view.getId()){
            case R.id.btn_sync:
                //forzamos el proceso de sincronizacion de los movimientos
                break;
            case R.id.btn_entradas_previstas:
                break;
            case R.id.btn_entradas_libres:
                break;
            case R.id.btn_expediciones:
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
        TimerTaskHard timerTaskHard = new TimerTaskHard(handler);
        Timer timer = new Timer();
        timer.schedule(timerTaskHard, 1500, 5000);
    }

    private void launcSoftSync(){
        TimerTaskHard timerTaskHard = new TimerTaskHard(handler);
        Timer timer = new Timer();
        timer.schedule(timerTaskHard, 1500, 5000);
    }
}