package es.incaser.apps.stockcontrol;

import android.app.SearchManager;
import android.app.SearchableInfo;
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
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class Recepciones extends ActionBarActivity implements SearchView.OnQueryTextListener{
    ListView lvMovimientoStock;
    MovStockAdapter movStockAdapter;
    DbAdapter dbAdapter;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recepciones);
    }

    @Override
    protected void onResume() {
        super.onResume();
        linkListViewMovimientoStock();
    }


    public void linkListViewMovimientoStock(){
        lvMovimientoStock = (ListView) findViewById(R.id.lv_recepciones);
        lvMovimientoStock.setEmptyView(findViewById(R.id.lay_empty_listview));
        movStockAdapter = new MovStockAdapter(this);
        lvMovimientoStock.setAdapter(movStockAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recepciones, menu);
        setupSearchView(menu);
        return true;
    }

    private void setupSearchView(Menu menu) {
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(this);
        searchView.setIconifiedByDefault(true);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        movStockAdapter.search(newText);
        movStockAdapter.notifyDataSetChanged();
        return false;
    }

    public class MovStockAdapter extends BaseAdapter implements View.OnClickListener{
        Context context;
        Cursor cursor;

        public MovStockAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            search("");
        }

        public void search(String searchText){
            cursor = dbAdapter.getRecepcionesSearch(MainActivity.codigoEmpresa, searchText);
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
                myView = myInflater.inflate(R.layout.item_recepcion, null);
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

            txtSerie.setText(getMovimiento("Serie"));
            txtDocumento.setText(getMovimiento("Documento"));
            txtTotalArticulos.setText(getMovimiento("Unidades"));
            txtMatricula.setText(getMovimiento("MatriculaTransporte_"));


            return myView;
        }

        public String getMovimiento(String column) {
            return cursor.getString(cursor.getColumnIndex(column));
        }

        @Override
        public void onClick(View v) {
            cursor.moveToPosition((int) v.getTag());
            switch (v.getId()) {
                case R.id.btn_asignar_transporte:
                    //startActivity(intentSearch);
                    break;
                default:
                    Intent intent = new Intent(v.getContext(), BarcodeReader.class);
                    intent.putExtra("tipoMov", TipoMovimiento.ENTRADA);
                    intent.putExtra("serieMov", cursor.getString(cursor.getColumnIndex("Serie")));
                    intent.putExtra("documentoMov", cursor.getString(cursor.getColumnIndex("Documento")));
                    startActivity(intent);
                    break;
            }
        }
    }
}
