package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.util.Linkify;
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


public class BarcodeReader extends ActionBarActivity {
    String tipoMov;
    String serieMov;
    String documentoMov;
    ImageButton btnReader;
    ImageButton btnCamera;
    ListView lvMovimientoStock;
    MovStockAdapter movStockAdapter;
    DbAdapter dbAdapter;
    TextView txtBarcode;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_reader);
        
        Bundle bundle = getIntent().getExtras();
        tipoMov = bundle.getString("tipoMov");
        serieMov = bundle.getString("serieMov","");
        documentoMov = bundle.getString("documentoMov","");

        addListenerOnButtonRead();
        linkButtonCamera();
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

    public void linkButtonCamera() {
        btnCamera = (ImageButton) findViewById(R.id.btn_camara);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getApplicationContext(),"Camera", Toast.LENGTH_SHORT).show();
                new IntentIntegrator(BarcodeReader.this).initiateScan();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        handleResult(scanResult);
    }

    private void handleResult(IntentResult scanResult) {
        if (scanResult != null) {
            updateUITextViews(scanResult.getContents(), scanResult.getFormatName());
        } else {
            Toast.makeText(this, "No se ha leído nada :(", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void updateUITextViews(String scan_result, String scan_result_format) {
        final EditText tvResult = (EditText)findViewById(R.id.txt_barcodeReader);
        tvResult.setText(scan_result);
        Linkify.addLinks(tvResult, Linkify.ALL);
    }

    public class MovStockAdapter extends BaseAdapter{
        Context context;
        Cursor cursor;
        
        public MovStockAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            if (tipoMov.equals("1")){
                cursor = dbAdapter.getMovimientoStock(MainActivity.codigoEmpresa, tipoMov);
            }else {
                cursor = dbAdapter.getMovimientoStock(MainActivity.codigoEmpresa, tipoMov, serieMov, documentoMov);
            }
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
            txtMatricula.setText("(" + getMovimiento("FechaRegistro") + ")");
//            txtMatricula.setText("(" + getMovimiento("MatriculaTransporte_") + ")");
            txtCodigoCarga.setText(getMovimiento("Documento"));
            int leidos = dbAdapter.getNumSerieLeidosCount(getMovimiento("MovPosicion"));
            txtTotalLecturas.setText(Integer.toString(leidos) +" de " + getMovimiento("Unidades"));
            if (leidos >= Integer.valueOf(getMovimiento("Unidades"))){
                myView.findViewById(R.id.lay_item_movStock).setBackgroundColor(getResources().getColor(R.color.green));
            }else {
                myView.findViewById(R.id.lay_item_movStock).setBackgroundColor(getResources().getColor(R.color.white));
            }

            return myView;
        }
        
        private String getMovimiento(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }        
    }
    
    private void leerCodigo(String barCode){
        String code = barCode.substring(3,10);
        if (tipoMov.equals(TipoMovimiento.ENTRADA)) {
            Cursor cur = dbAdapter.getMovArticuloSerieNumSerie(TipoMovimiento.origenMov(tipoMov), code);
            if (cur.moveToFirst()) {
                dbAdapter.updateMovimientoArticuloSerie(cur.getString(cur.getColumnIndex("MovPosicion")));
                dbAdapter.updateMovimientoStock(cur.getString(cur.getColumnIndex("MovPosicionOrigen")));
            } else {
                //TODO. La bobina no esta en la base de datos.
            }
        }else {
            expedirBarcode(code);
        }
        movStockAdapter.cursor.requery();
        movStockAdapter.notifyDataSetChanged();
    }
    
    private void expedirBarcode(String code){
        Cursor cursor = dbAdapter.getArticulosSeries(MainActivity.codigoEmpresa, code);
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            String codArticulo = cursor.getString(cursor.getColumnIndex("CodigoArticulo"));
            String codTalla = cursor.getString(cursor.getColumnIndex("CodigoTalla01_"));
            Cursor curMovStock = dbAdapter.getMovimientoStock(MainActivity.codigoEmpresa, tipoMov, serieMov, documentoMov, codArticulo, codTalla);
            if (curMovStock.getCount()>0){
                curMovStock.moveToFirst();
                Cursor movArtSerie = dbAdapter.getMovArticuloSerieNumSerie(TipoMovimiento.origenMov(tipoMov), code);
                if (movArtSerie.getCount() == 0){
                    dbAdapter.createMovimientoArticuloSerie(curMovStock, code);
                    dbAdapter.updateMovimientoStock(curMovStock.getString(curMovStock.getColumnIndex("MovPosicion")));
                }else{
                    Toast.makeText(getApplicationContext(),R.string.msg_articulo_escaneado, Toast.LENGTH_SHORT).show();
                }
            }else {
                //TODO. bobina incorrecta
                Toast.makeText(getApplicationContext(),R.string.msg_tipo_incorrecto, Toast.LENGTH_SHORT).show();
            }
        }
    }
    
}
