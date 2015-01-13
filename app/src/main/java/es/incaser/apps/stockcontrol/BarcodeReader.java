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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import es.incaser.apps.tools.Tools;


public class BarcodeReader extends ActionBarActivity {
    ImageButton btnReader;
    ListView lvMovimientoStock;
    MovStockAdapter movStockAdapter;
    DbAdapter dbAdapter;
    TextView txtBarcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);
        addListenerOnButtonRead();
        linkListViewMovimientoStock();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_barcode_reader, menu);
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

    public void addListenerOnButtonRead() {
        btnReader = (ImageButton) findViewById(R.id.btn_read);
        txtBarcode = (EditText) findViewById(R.id.txt_barcodeReader);
        
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Button pressed", Toast.LENGTH_SHORT).show();
                if (txtBarcode.getText().toString().length() > 0){
                    leerCodigo(txtBarcode.getText().toString());
                }
            }
        });
    }
    
    public void linkListViewMovimientoStock(){
        lvMovimientoStock = (ListView) findViewById(R.id.lv_movimientoStock);

        movStockAdapter = new MovStockAdapter(this);
        lvMovimientoStock.setAdapter(movStockAdapter);
    }
    
    
    
    
    public class MovStockAdapter extends BaseAdapter{
        Context context;
        Cursor cursor;
        
        public MovStockAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            cursor = dbAdapter.getMovimientoStock("1");
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
                myView = myInflater.inflate(R.layout.item_mov_stock, null);
            } else {
                myView = convertView;
            }
            cursor.moveToPosition(position);

            TextView txtArticulo = (TextView) myView.findViewById(R.id.tv_articulo);
            TextView txtMatricula = (TextView) myView.findViewById(R.id.tv_matricula);
            TextView txtCodigoCarga = (TextView) myView.findViewById(R.id.tv_codigoCarga);
            TextView txtTotalLecturas = (TextView) myView.findViewById(R.id.tv_totalLecturas);

            txtArticulo.setText(getMovimiento("CodigoArticulo"));
            txtMatricula.setText("(" + getMovimiento("MatriculaTransporte_") + ")");
            txtCodigoCarga.setText(getMovimiento("Documento"));
            txtTotalLecturas.setText(getMovimiento("Unidades"));

            return myView;
        }
        
        private String getMovimiento(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }        
    }
    
    private void leerCodigo(String barCode){
        String code = barCode.substring(3,10);
        if (dbAdapter.updateMovimientoArticuloSerie(code) > 0){
            dbAdapter.updateMovimientoStock(code);
            movStockAdapter.notifyDataSetChanged();
        }else {
            //TODO. La bobina no esta en la base de datos.
        };
    }
}
