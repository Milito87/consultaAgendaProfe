package org.izv.ad.jmunoz.consultaagendaprofe;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.izv.ad.jmunoz.consultaagendaprofe.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "xyzyx";
    private final int CONTACT_PERMISSION = 1;

    private Button btSearch;
    private EditText etPhone;
    private TextView tvResultado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.v(TAG, "onCreate"); //v de verbose w de warning ...

        initialize();

        btSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchIfPermited();
            }});

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            viewSettings();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "onRequestPerimissions");
        //requestCode, codigo de los permisos requeridos
        //permissions, lista de permisos requerida
        //grantResults, indica los permisos concedidos

        switch(requestCode){
            case CONTACT_PERMISSION:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permiso
                    search();
                }
                else{
                    //sin permiso
                }
                break;

        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void explain() {
        showRationaleDialog(getString(R.string.title), getString(R.string.message), Manifest.permission.READ_CONTACTS, CONTACT_PERMISSION);
        requestPermission();

    }

    private void initialize() {

        btSearch=findViewById(R.id.btSearch);
        etPhone=findViewById(R.id.etPhone);
        tvResultado=findViewById(R.id.tvResultado);

        SharedPreferences preferencias = getPreferences(Context.MODE_PRIVATE);
        String lastSearch = preferencias.getString(getString(R.string.last_search), "");
        if(!lastSearch.isEmpty()) {
            etPhone.setText(lastSearch);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)//si la version es inferior a 23
    private void requestPermission() {

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, CONTACT_PERMISSION);

    }

    private void search() {

        String phone = etPhone.getText().toString();


        tvResultado.setText("");

        SharedPreferences prefenciasActividad = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor ed1 = prefenciasActividad.edit();
        ed1.putString(getString(R.string.last_search), phone);
        ed1.commit();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        String email = sharedPreferences.getString(getString(R.string.email), getString(R.string.no_email));

        phone = searchFormat(phone);
        /*
        //formas de guardar o editar preferencias compartidas
        SharedPreferences p1 = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        SharedPreferences p2 = getPreferences(Context.MODE_PRIVATE);
        //SharedPreferences = PreferenceManager.getDefaultSharedPreferences --> nombre paquete_preferences.xml
        // ^ se utiliza cuando usamos settingActivity para guardar preferencias
        SharedPreferences.Editor ed1 = p1.edit();
        SharedPreferences.Editor ed2 = p2.edit();

        ed1.putString("ved1", "v1"); //preferencias compartidas.xml
        ed2.putString("ved2", "v2"); //se llama igual que la actividad
        */
        //tvResultado.append(email + "\n");
        
        /*buscar entre los contactos -> ContentProvider permite acceder a cualquier cosa almacenada ajena a la app
        ContentResolver -> consultor de contenido
        uri: protocolo://direccion/ruta/recurso*/
        /*Cursor cursor=getContentResolver().query(
                UserDictionary.Words.CONTENT_URI, //conteido de la tabla
                new String[]{"projection"}, //columnas a ver
                "selectionClause",  //consulta preparada sin especificar los argumentos 'firstname=?, lastname=?...
                new String[]{"selectionArgs"}, //argumentos para realizar la consulta preparada
                "sortOrder" //orden de retorno de las columnas seleccionadas
        );*/

        /*Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String proyeccion[] = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
        String seleccion = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = ? and " +
                           ContactsContract.Contacts.HAS_PHONE_NUMBER + "= ?";
        String argumentos[] = new String[]{"1","1"};

        String orden = ContactsContract.Contacts.DISPLAY_NAME + " collate localized asc"; //collate localized se adapta al idioma del dispositivo para ordenar
        Cursor cursor = getContentResolver().query(uri, proyeccion, seleccion, argumentos, orden);
        String[] columnas=cursor.getColumnNames();
        for(String s: columnas){
            Log.v(TAG, s);
        }
        String displayName;
        int columna=cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        while(cursor.moveToNext()){
            displayName=cursor.getString(columna);
            Log.v(TAG, displayName);
        }*/


        Uri uri2 = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String proyeccion2[] = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        String seleccion2 = ContactsContract.CommonDataKinds.Phone.NUMBER+ " like ?";
        String argumentos2[] = new String[]{phone};
        String orden2 = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;
        Cursor cursor2 = getContentResolver().query(uri2, proyeccion2, seleccion2, argumentos2, orden2);

        String[] columnas2=cursor2.getColumnNames();

        String nombre, numero;
        int columnaD=cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int columnaN=cursor2.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        while(cursor2.moveToNext()){
            nombre=cursor2.getString(columnaD);
            numero=cursor2.getString(columnaN);

            for(String s: columnas2) {
                int pos = cursor2.getColumnIndex(s);
                String valor = cursor2.getString(pos);
                tvResultado.append(s + " " + valor + "\n");

            }
        }



    }

    private String searchFormat(String phone) {

        String newString = "";

        for(char ch: phone.toCharArray()){
            newString += ch + "%";
        }
        return newString;
    }

    private void searchIfPermited() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//version de android posterior o igual a 6

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {//tengo permiso

                search();

            }

            else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {

                explain();//explicar al usuario el porque pide permiso

            }
            else {

                requestPermission(); //primera vez que pide permiso

            }
        }
        else{//version anterior a la 6 y tengo el permiso

            search();

        }

    }

    private void showRationaleDialog(String title, String message, String permission, int requestCode){

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title)
               .setMessage(message)
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       //no hace nada
                   }})
               .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                   @RequiresApi(api = Build.VERSION_CODES.M)
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                       requestPermission();

                   }});
        builder.create().show();

    }

    private void viewSettings() {
        //intent explicito ir de contexto actual a otro creado de la clase SettingActivity
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);

    }

}