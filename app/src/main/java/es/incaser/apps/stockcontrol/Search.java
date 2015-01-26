package es.incaser.apps.stockcontrol;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static es.incaser.apps.tools.Tools.dateStr2str;


public class Search extends ActionBarActivity implements TextWatcher{
    ListView lv_search;
    EditText txt_search;
    SearchAdapter searchAdapter;
    DbAdapter dbAdapter;
    String tabla;
    String campoBusqueda;
    String campoRetorno;
    Bundle bundleOrig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        bundleOrig = getIntent().getExtras();
        tabla = bundleOrig.getString("tabla");
        campoBusqueda = bundleOrig.getString("campoBusqueda","");
        campoRetorno = bundleOrig.getString("campoRetorno","");
        
        txt_search = (EditText) findViewById(R.id.txt_buscador);
        txt_search.addTextChangedListener(this);
        
        lv_search = (ListView) findViewById(R.id.lv_result_search);
        searchAdapter = new SearchAdapter(this);
        lv_search.setAdapter(searchAdapter);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        searchAdapter.search(s.toString());
        searchAdapter.notifyDataSetChanged();
    }

    public class SearchAdapter extends BaseAdapter implements View.OnClickListener{
        Context context;
        Cursor cursor;

        public SearchAdapter(Context ctx){
            context = ctx;
            dbAdapter = new DbAdapter(context);
            search("");
        }

        public void search(String searchText){
            cursor = dbAdapter.getBusqueda(tabla, campoBusqueda, campoRetorno, searchText);
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
                myView = myInflater.inflate(R.layout.item_search, null);
            } else {
                myView = convertView;
            }
            cursor.moveToPosition(position);
            myView.setOnClickListener(this);
            TextView tvName = (TextView) myView.findViewById(R.id.tv_item_search);
            tvName.setText(cursor.getString(cursor.getColumnIndex(campoRetorno)));
            return myView;
        }


        @Override
        public void onClick(View v) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("id",cursor.getString(0));
            returnIntent.putExtra(campoRetorno, cursor.getString(cursor.getColumnIndex(campoRetorno)));
            returnIntent.putExtra("tipoMov",bundleOrig.getString("tipoMov"));
            returnIntent.putExtra("serieMov",bundleOrig.getString("serieMov"));
            returnIntent.putExtra("documentoMov",bundleOrig.getString("documentoMov"));

            
            setResult(RESULT_OK,returnIntent);
            finish();
        }
    }    
}
