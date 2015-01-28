package es.incaser.apps.stockcontrol;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;


public class AsignarTransporte extends ActionBarActivity {
    Bundle bundleOrig;
    EditText txtMatricula;
    EditText txtMatriculaRemolque;
    TextView txtChofer;
    ContentValues cv = new ContentValues();

        
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_transporte);
        bundleOrig = getIntent().getExtras();
        linkTextViews();
    }
    
    public void linkTextViews(){
        txtMatricula = (EditText) findViewById(R.id.tv_mov_stock_matricula);
        txtMatriculaRemolque = (EditText) findViewById(R.id.tv_mov_stock_matricula_remolque);
        txtChofer = (TextView) findViewById(R.id.tv_mov_stock_chofer);

        txtMatricula.setText(bundleOrig.getString("Matricula"));
        txtMatriculaRemolque.setText(bundleOrig.getString("MatriculaRemolque"));
        txtChofer.setTag(bundleOrig.getString("CodigoChofer"));
        txtChofer.setText(bundleOrig.getString("Chofer"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_asignar_transporte, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_aceptar) {
            grabarMovStock();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        Intent intentSearch = new Intent(v.getContext(),Search.class);
        switch (v.getId()) {
            case R.id.btn_asignar_matricula:
                intentSearch.putExtra("tabla", "TRA_Vehiculos");
                intentSearch.putExtra("campoBusqueda", "Matricula");
                intentSearch.putExtra("campoRetorno", "Matricula");
                startActivityForResult(intentSearch, 1);
                break;
            case R.id.btn_asignar_matricula_remolque:
                intentSearch.putExtra("tabla", "TRA_Remolques");
                intentSearch.putExtra("campoBusqueda", "MatriculaRemolque");
                intentSearch.putExtra("campoRetorno", "MatriculaRemolque");
                startActivityForResult(intentSearch, 2);
                break;
            case R.id.btn_asignar_chofer:
                intentSearch.putExtra("tabla", "Choferes");
                intentSearch.putExtra("campoBusqueda", "RazonSocial");
                intentSearch.putExtra("campoRetorno", "CodigoChofer");
                startActivityForResult(intentSearch, 3);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            String id = data.getStringExtra("id");
            String tipoMov = data.getStringExtra("tipoMov");
            String serieMov = data.getStringExtra("serieMov");
            String documentoMov = data.getStringExtra("documentoMov");
            String campoRetorno = data.getStringExtra("campoRetorno");
            String valorRetorno = data.getStringExtra("valorRetorno");
            switch (requestCode){
                case 1:
                    txtMatricula.setText(data.getStringExtra("valorRetorno"));
                    break;
                case 2:
                    txtMatriculaRemolque.setText(data.getStringExtra("valorRetorno"));
                    break;
                case 3:
                    txtChofer.setText(data.getStringExtra("valorRetorno"));
                    txtChofer.setTag(data.getStringExtra("tagRetorno"));
                    break;
            }
        }
        if (resultCode == RESULT_CANCELED) {
            //Write your code if there's no result
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    public void grabarMovStock(){
        cv.put("Matricula", txtMatricula.getText().toString());
        cv.put("MatriculaRemolque", txtMatriculaRemolque.getText().toString());
        cv.put("CodigoChofer", txtChofer.getTag().toString());

        DbAdapter dbAdapter = new DbAdapter(this);
        dbAdapter.updateMovimientoStock(bundleOrig.getString("tipoMov"),
                bundleOrig.getString("serieMov"), bundleOrig.getString("documentoMov"), cv);
        cv.clear();
    }

}
