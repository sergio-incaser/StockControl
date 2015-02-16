package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


public class MovArticuloSerie extends ActionBarActivity {
    ListView lvMovArticuloSerie;
    MovArticuloSerieAdapter movArticuloSerieAdapter;
    DbAdapter dbAdapter;
    String tipoMov;
    String serieMov;
    String documentoMov;
    String codArticulo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mov_articulo_serie);
        
        getInitParams();
    }

    void getInitParams(){
        Bundle bundle = getIntent().getExtras();
        tipoMov = bundle.getString("tipoMov");
        serieMov = bundle.getString("serieMov","");
        documentoMov = bundle.getString("documentoMov","");
        codArticulo = bundle.getString("codArticulo","");

        lvMovArticuloSerie = (ListView) findViewById(R.id.lv_mov_articulo_serie);
        movArticuloSerieAdapter = new MovArticuloSerieAdapter(this);
        lvMovArticuloSerie.setAdapter(movArticuloSerieAdapter);
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mov_articulo_serie, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class MovArticuloSerieAdapter extends BaseAdapter {
        Context context;
        Cursor cursor;

        public MovArticuloSerieAdapter(Context ctx) {
            context = ctx;
            dbAdapter = new DbAdapter(context);
            cursor = dbAdapter.getMovArticuloSerieByDoc(MainActivity.codigoEmpresa, tipoMov,
                    serieMov, documentoMov, codArticulo);
            cursor.moveToFirst();
        }

        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            cursor.moveToPosition(position);
            return cursor;
        }

        @Override
        public long getItemId(int position) {
            cursor.moveToPosition(position);
            return cursor.getLong(cursor.getColumnIndex("id"));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View myView = null;

            if (convertView == null) {
                LayoutInflater myInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                myView = myInflater.inflate(R.layout.item_mov_art_serie, null);
            } else {
                myView = convertView;
            }
            cursor.moveToPosition(position);

            TextView txtSubFamilia = (TextView) myView.findViewById(R.id.tv_mov_art_serie_articulo);
            TextView txtGramaje = (TextView) myView.findViewById(R.id.tv_mov_art_serie_gramaje);
            TextView txtAncho = (TextView) myView.findViewById(R.id.tv_mov_art_serie_ancho);
            TextView txtDiametro = (TextView) myView.findViewById(R.id.tv_mov_art_serie_diametro);
            TextView txtPeso = (TextView) myView.findViewById(R.id.tv_mov_art_serie_peso);
            TextView txtLongitud = (TextView) myView.findViewById(R.id.tv_mov_art_serie_longitud);
            TextView txtAnoBobina = (TextView) myView.findViewById(R.id.tv_mov_art_serie_ano);
            TextView txtStatusSync = (TextView) myView.findViewById(R.id.tv_mov_art_serie_status_sync);
            TextView txtNumSerie = (TextView) myView.findViewById(R.id.tv_mov_art_serie_num_serie);

            txtSubFamilia.setText(getRecord("CodigoSubfamilia"));
            txtGramaje.setText(getRecord("PesoNetoUnitario_"));
            txtAncho.setText(getRecord("VolumenUnitario_"));
            txtDiametro.setText(getRecord("CodigoTalla01_"));
            txtPeso.setText(getRecord("TBL_Peso"));
            txtLongitud.setText(getRecord("TBL_Longitud"));
            txtAnoBobina.setText(getRecord("TBL_AnoBobina"));
            txtNumSerie.setText(getRecord("NumeroSerieLc"));

            String statusSync =getRecord("StatusAndroidSync");
            txtStatusSync.setText(StatusSync.getSyncDescription(statusSync, myView.getContext()));

            switch (statusSync){
                case StatusSync.PREVISTO:
                    txtStatusSync.setBackgroundColor(getResources().getColor(R.color.status_previsto));
                    break;
                case StatusSync.ESCANEADO:
                    txtStatusSync.setBackgroundColor(getResources().getColor(R.color.status_escaneado));
                    break;
                case StatusSync.PARA_CREAR:
                    txtStatusSync.setBackgroundColor(getResources().getColor(R.color.status_para_crear));
                    break;
                default:
                    txtStatusSync.setBackgroundColor(getResources().getColor(R.color.white));
            }
            return myView;
        }

        public String getRecord(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }
    }
}
