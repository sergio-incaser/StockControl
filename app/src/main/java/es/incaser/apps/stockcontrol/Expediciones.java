package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static es.incaser.apps.stockcontrol.Tools.dateStr2str;


public class Expediciones extends ActionBarActivity{
    ListView lvMovimientoStock;
    MovStockAdapter movStockAdapter;
    DbAdapter dbAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expediciones);

    }

    public void linkListViewMovimientoStock(){
        lvMovimientoStock = (ListView) findViewById(R.id.lv_expediciones);
        movStockAdapter = new MovStockAdapter(this);
        lvMovimientoStock.setAdapter(movStockAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        linkListViewMovimientoStock();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_expediciones, menu);
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


    public class MovStockAdapter extends BaseAdapter implements View.OnClickListener{
        Context context;
        Cursor cursor;

        public MovStockAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            cursor = dbAdapter.getExpediciones(MainActivity.codigoEmpresa);
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
                myView = myInflater.inflate(R.layout.item_expedicionxxy, null);
            } else {
                myView = convertView;
            }
            cursor.moveToPosition(position);
            
            myView.setOnClickListener(this);
            myView.setTag(position);

            //TextView txtFecha = (TextView) myView.findViewById(R.id.tv_mov_stock_fecha);
            TextView txtSerie = (TextView) myView.findViewById(R.id.tv_mov_stock_serie);
            TextView txtDocumento = (TextView) myView.findViewById(R.id.tv_mov_stock_documento);
            TextView txtTotalArticulos = (TextView) myView.findViewById(R.id.tv_mov_stock_unidades_total);
            TextView txtMatricula = (TextView) myView.findViewById(R.id.tv_mov_stock_matricula);
            TextView txtMatriculaRemolque = (TextView) myView.findViewById(R.id.tv_mov_stock_matricula_remolque);
            TextView txtChofer = (TextView) myView.findViewById(R.id.tv_mov_stock_chofer);
            
            ImageButton btn_chofer = (ImageButton) myView.findViewById(R.id.btn_asignar_transporte);
            btn_chofer.setTag(position);
            btn_chofer.setOnClickListener(this);

            //txtFecha.setText(dateStr2str(getMovimiento("Fecha")));
            txtSerie.setText(getMovimiento("Serie"));
            txtDocumento.setText(getMovimiento("Documento"));
            txtTotalArticulos.setText(getMovimiento("Unidades"));
            txtMatricula.setText(getMovimiento("Matricula"));
            txtMatriculaRemolque.setText(getMovimiento("MatriculaRemolque"));
            txtChofer.setText(getMovimiento("RazonSocial"));


            return myView;
        }

        public String getMovimiento(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }

        @Override
        public void onClick(View v) {
            Intent intentSearch = new Intent(v.getContext(),AsignarTransporte.class);
            cursor.moveToPosition((int) v.getTag());

            intentSearch.putExtra("tipoMov", TipoMovimiento.SALIDA);
            intentSearch.putExtra("serieMov", cursor.getString(cursor.getColumnIndex("Serie")));
            intentSearch.putExtra("documentoMov", cursor.getString(cursor.getColumnIndex("Documento")));
            intentSearch.putExtra("Matricula", cursor.getString(cursor.getColumnIndex("Matricula")));
            intentSearch.putExtra("MatriculaRemolque", cursor.getString(cursor.getColumnIndex("MatriculaRemolque")));
            intentSearch.putExtra("CodigoChofer", cursor.getString(cursor.getColumnIndex("CodigoChofer")));
            intentSearch.putExtra("Chofer", cursor.getString(cursor.getColumnIndex("RazonSocial")));
            switch (v.getId()) {
                case R.id.btn_asignar_transporte:
                    startActivity(intentSearch);
                    break;
                default:
                    Intent intent = new Intent(v.getContext(), BarcodeReader.class);
                    intent.putExtra("tipoMov", TipoMovimiento.SALIDA);
                    intent.putExtra("serieMov", cursor.getString(cursor.getColumnIndex("Serie")));
                    intent.putExtra("documentoMov", cursor.getString(cursor.getColumnIndex("Documento")));
                    startActivity(intent);
                    break;
            }
        }
    }
}
