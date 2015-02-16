package es.incaser.apps.stockcontrol;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
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

import java.util.UUID;


public class BarcodeReader extends ActionBarActivity{
    String tipoMov;
    String serieMov;
    String documentoMov;
    ImageButton btnReader;
    ImageButton btnCamera;
    ListView lvMovimientoStock;
    MovStockAdapter movStockAdapter;
    DbAdapter dbAdapter;
    EditText txtBarcode;
    TextView tvPeso;

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
        tvPeso = (TextView) findViewById(R.id.tv_barcode_peso_total);
        setPesoTotal();
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
        
        txtBarcode.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP){
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
//                            if (txtBarcode.getText().toString().length() < 13) {
//                                txtBarcode.setText(String.format("%013d", Long.parseLong(txtBarcode.getText().toString())));
//                            }
                            leerCodigo(txtBarcode.getText().toString());
                            txtBarcode.setText("");
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
        
        
        btnReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtBarcode.getText().toString().length() > 10){
                    leerCodigo(txtBarcode.getText().toString());
                    txtBarcode.setText("");
                }else {
                    Toast.makeText(v.getContext(), "Código demasiado corto", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    public void linkListViewMovimientoStock(){
        lvMovimientoStock = (ListView) findViewById(R.id.lv_movimientoStock);
        movStockAdapter = new MovStockAdapter(this);
        lvMovimientoStock.setAdapter(movStockAdapter);
        lvMovimientoStock.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), MovArticuloSerie.class);
                intent.putExtra("tipoMov", tipoMov);
                intent.putExtra("serieMov", serieMov);
                intent.putExtra("documentoMov", documentoMov);
                intent.putExtra("codArticulo", movStockAdapter.getMovimiento("CodigoArticulo"));
                startActivity(intent);
            }
        });
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
        leerCodigo(scan_result);
    }

    public class MovStockAdapter extends BaseAdapter{
        Context context;
        Cursor cursor;

        public MovStockAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            cursor = dbAdapter.getMovimientoStock(MainActivity.codigoEmpresa, tipoMov, serieMov, documentoMov);
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

            TextView txtArticulo = (TextView) myView.findViewById(R.id.tv_mov_stock_articulo);
            txtArticulo.setText(getMovimiento("CodigoSubfamilia"));
            TextView txtGramaje = (TextView) myView.findViewById(R.id.tv_mov_stock_gramaje);
            txtGramaje.setText(getMovimiento("PesoNetoUnitario_"));
            TextView txtAncho = (TextView) myView.findViewById(R.id.tv_mov_stock_ancho);
            txtAncho.setText(getMovimiento("VolumenUnitario_"));
            TextView txtTalla = (TextView) myView.findViewById(R.id.tv_mov_stock_diametro);
            txtTalla.setText(getMovimiento("CodigoTalla01_"));

            TextView txtCodigoCarga = (TextView) myView.findViewById(R.id.tv_mov_stock_documento);
            txtCodigoCarga.setText(getMovimiento("Documento"));

            TextView txtTotalLecturas = (TextView) myView.findViewById(R.id.tv_mov_stock_unidades_total);
            int leidos = dbAdapter.getNumSerieLeidosCount(getMovimiento("MovPosicion"));
            txtTotalLecturas.setText(Integer.toString(leidos) +" de " + getMovimiento("Unidades"));
            if (leidos >= Integer.valueOf(getMovimiento("Unidades"))){
                myView.findViewById(R.id.lay_item_mov_stock).setBackgroundColor(getResources().getColor(R.color.green));
            }else {
                myView.findViewById(R.id.lay_item_mov_stock).setBackgroundColor(getResources().getColor(R.color.white));
            }
            if (tipoMov.equals(TipoMovimiento.ENTRADA)){
                myView.findViewById(R.id.mov_stock_lay_matricula).setVisibility(View.VISIBLE);
                TextView txtMatricula = (TextView) myView.findViewById(R.id.tv_mov_stock_matricula);
                txtMatricula.setText(getMovimiento("MatriculaTransporte_"));
            }else {
                myView.findViewById(R.id.mov_stock_lay_matricula).setVisibility(View.GONE);
            };

            return myView;
        }
        
        private String getMovimiento(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }

    }
    
    private void leerCodigo(String barCode){
        try {
            String code = barCode.substring(3,10);
            if (tipoMov.equals(TipoMovimiento.ENTRADA)) {
                Cursor cur = dbAdapter.getMovArticuloSerieNumSerie(TipoMovimiento.origenMov(tipoMov), code);
                if (cur.moveToFirst()) {
                    dbAdapter.updateMovimientoArticuloSerie(cur.getString(cur.getColumnIndex("MovPosicion")));
                    dbAdapter.updateMovimientoStock(cur.getString(cur.getColumnIndex("MovPosicionOrigen")));
                } else {
                    //TODO. La bobina no esta en la base de datos.
                    Toast.makeText(this,"Código no disponible",Toast.LENGTH_SHORT).show();
                }
            }else {
                expedirBarcode(code);
            }
            movStockAdapter.cursor.requery();
            movStockAdapter.notifyDataSetChanged();
            setPesoTotal();
        }catch (Exception e){
            Toast.makeText(this, e.toString(),Toast.LENGTH_SHORT).show();
        };
    }
    
    private void setPesoTotal(){
        tvPeso.setText(dbAdapter.getPesoEscaneado(MainActivity.codigoEmpresa, tipoMov, serieMov, documentoMov));
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
                    dbAdapter.createMovimientoArticuloSerie(curMovStock, cursor, code);
                    int numArticulos = dbAdapter.getNumSerieLeidosCount(curMovStock.getString(curMovStock.getColumnIndex("MovPosicion")));
                    int res = 0;
                    if (numArticulos > curMovStock.getInt(curMovStock.getColumnIndex("Unidades"))){
                        res = dbAdapter.updateUnidadesMovStock(curMovStock.getString(curMovStock.getColumnIndex("MovPosicion")), numArticulos);
                    }else {
                        res = dbAdapter.updateMovimientoStock(curMovStock.getString(curMovStock.getColumnIndex("MovPosicion")));
                    }
                    if (res == 0){
                       crearMovimientoStock(cursor, code);
                    }
                }else{
                    Toast.makeText(getApplicationContext(),R.string.msg_articulo_escaneado, Toast.LENGTH_SHORT).show();
                }
            }else {
                //No se ha encontrado movimientoStock. Hay que crearlo como si fuese una linea mas de esta expedición
                crearMovimientoStock(cursor, code);
                Toast.makeText(getApplicationContext(),"Nueva bobina expedida", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(getApplicationContext(),"El artículo no existe", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void crearMovimientoStock(Cursor curArticuloSerie, String code){
        Bundle bundle = getIntent().getExtras();
        ContentValues cv = new ContentValues();
        cv.put("CodigoEmpresa", MainActivity.codigoEmpresa);
        cv.put("EmpresaOrigen", MainActivity.codigoEmpresa);
        cv.put("Ejercicio",MainActivity.contextNow("yyyy"));
        cv.put("Periodo", Tools.getToday("MM"));
        cv.put("Fecha", Tools.getToday());
        cv.put("FechaRegistro", MainActivity.contextNow());
        cv.put("Serie", serieMov);
        cv.put("Documento",documentoMov);
        cv.put("CodigoArticulo", curArticuloSerie.getString(curArticuloSerie.getColumnIndex("CodigoArticulo")));
        cv.put("CodigoAlmacen",curArticuloSerie.getString(curArticuloSerie.getColumnIndex("CodigoAlmacen")));
        cv.put("GrupoTalla_", 999);
        cv.put("CodigoTalla01_",curArticuloSerie.getString(curArticuloSerie.getColumnIndex("CodigoTalla01_")));
        cv.put("TipoMovimiento", tipoMov);
        cv.put("Unidades", 1);
        cv.put("Unidades2_", 1);
        cv.put("Precio", 1);
        cv.put("FactorConversion_", 1);
        cv.put("Comentario", "Articulo manual");
        cv.put("StatusAcumulado", 0);
        cv.put("OrigenMovimiento", "S");
        String uuid = UUID.randomUUID().toString().toUpperCase();
        cv.put("MovPosicion", uuid);
        cv.put("MovOrigen", uuid);
        cv.put("UsuarioProceso", 0);
        cv.put("EjercicioDocumento", MainActivity.contextNow("yyyy"));
        cv.put("Proceso", UUID.randomUUID().toString().toUpperCase());
        cv.put("CodigoDestinatario", bundle.getString("CodigoDestinatario"));
        cv.put("NumeroExpedicion", bundle.getString("NumeroExpedicion"));
        cv.put("CodigoSubfamilia",curArticuloSerie.getString(curArticuloSerie.getColumnIndex("CodigoSubfamilia")));
        cv.put("StatusAndroidSync", StatusSync.PARA_CREAR);
        //cv.put("MatriculaTransporte_",);
        cv.put("PesoNetoUnitario_", curArticuloSerie.getString(curArticuloSerie.getColumnIndex("PesoNetoUnitario_")));
        cv.put("VolumenUnitario_",curArticuloSerie.getString(curArticuloSerie.getColumnIndex("VolumenUnitario_")));
        cv.put("Matricula", bundle.getString("Matricula"));
        cv.put("MatriculaRemolque", bundle.getString("MatriculaRemolque"));
        cv.put("CodigoChofer", bundle.getString("CodigoChofer"));
        cv.put("CodigoDestinatario", bundle.getString("CodigoDestinatario"));

        if (dbAdapter.createMovimientoStock(cv) != -1){
            //Actualizo la fecha del resto de movStock que forman la expedicion
            //de lo contrario no se updatan estos movimientos a no ser que se escaneen productos
            //dbAdapter.updateMovimientoStock(tipoMov, serieMov, documentoMov, new ContentValues());
            expedirBarcode(code);
        }
    }
    
}
