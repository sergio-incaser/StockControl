package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
        linkListViewMovimientoStock();
        
    }

    public void linkListViewMovimientoStock(){
        lvMovimientoStock = (ListView) findViewById(R.id.lv_expediciones);
        movStockAdapter = new MovStockAdapter(this);
        lvMovimientoStock.setAdapter(movStockAdapter);
        lvMovimientoStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String a="2";
            }
        });
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
            EditText txtMatricula = (EditText) myView.findViewById(R.id.tv_mov_stock_matricula);
            EditText txtMatriculaRemolque = (EditText) myView.findViewById(R.id.tv_mov_stock_matricula_remolque);
            TextView txtChofer = (TextView) myView.findViewById(R.id.tv_mov_stock_chofer);
            
            ImageButton btn_chofer = (ImageButton) myView.findViewById(R.id.btn_asignar_chofer);
            btn_chofer.setTag(position);
            btn_chofer.setOnClickListener(this);
            ImageButton btn_matricula = (ImageButton) myView.findViewById(R.id.btn_asignar_matricula);
            btn_matricula.setTag(position);
            btn_matricula.setOnClickListener(this);
            ImageButton btn_remolque = (ImageButton) myView.findViewById(R.id.btn_asignar_matricula_remolque);
            btn_remolque.setTag(position);
            btn_remolque.setOnClickListener(this);

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
            Intent intentSearch = new Intent(v.getContext(),Search.class);
            cursor.moveToPosition((int) v.getTag());

            intentSearch.putExtra("tipoMov", TipoMovimiento.SALIDA);
            intentSearch.putExtra("serieMov", cursor.getString(cursor.getColumnIndex("Serie")));
            intentSearch.putExtra("documentoMov", cursor.getString(cursor.getColumnIndex("Documento")));
            switch (v.getId()) {
                case R.id.btn_asignar_chofer:
                    intentSearch.putExtra("tabla", "Choferes");
                    intentSearch.putExtra("campoBusqueda", "RazonSocial");
                    intentSearch.putExtra("campoRetorno", "CodigoChofer");
                    startActivityForResult(intentSearch, 1);
                    break;
                case R.id.btn_asignar_matricula:
                    intentSearch.putExtra("tabla", "TRA_Vehiculos");
                    intentSearch.putExtra("campoBusqueda", "Matricula");
                    intentSearch.putExtra("campoRetorno", "Matricula");
                    startActivityForResult(intentSearch, 2);
                    break;
                case R.id.btn_asignar_matricula_remolque:
                    intentSearch.putExtra("tabla", "TRA_Remolques");
                    intentSearch.putExtra("campoBusqueda", "MatriculaRemolque");
                    intentSearch.putExtra("campoRetorno", "MatriculaRemolque");
                    startActivityForResult(intentSearch, 3);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            String id = data.getStringExtra("id");
            String tipoMov = data.getStringExtra("tipoMov");
            String serieMov = data.getStringExtra("serieMov");
            String documentoMov = data.getStringExtra("documentoMov");
            String campoRetorno = data.getStringExtra("campoRetorno");
            String valorRetorno = data.getStringExtra("valorRetorno");
            dbAdapter.updateMovimientoStock(tipoMov, serieMov, documentoMov, campoRetorno, valorRetorno);
            movStockAdapter.cursor.requery();
            movStockAdapter.notifyDataSetChanged();
        }
        if (resultCode == RESULT_CANCELED) {
            //Write your code if there's no result
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
