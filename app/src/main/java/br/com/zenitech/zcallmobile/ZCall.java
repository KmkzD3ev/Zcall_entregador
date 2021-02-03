package br.com.zenitech.zcallmobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.Objects;

public class ZCall extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_CONTACTS = 1;
    private static final int REQUEST_CALL = 2;

    // ligar
    private Uri uriContact;
    private String contactID;
    private Context context;
    private String nome = "", telefone = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zcall);

        //
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;

        //
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {
                nome = params.getString("nome");
                telefone = params.getString("telefone");
            }
        }

        ligar(telefone);
    }

    private void ligar(String tel) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ZCall.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
        } else {
            Uri call = Uri.parse(String.format("tel:%s", tel));
            Intent surf = new Intent(Intent.ACTION_CALL, call);
            startActivity(surf);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_PICK_CONTACTS && resultCode == RESULT_OK) {
            Log.d("Principal", "Response: " + data.toString());
            uriContact = data.getData();

            //retrieveContactName();
            //retrieveContactPhoto();
            String telefone = retrieveContactNumber();

            Uri uri = Uri.parse("tel:" + telefone);
            Intent intent = new Intent(Intent.ACTION_DIAL, uri);

            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ligar(telefone );
            }else{
                Toast.makeText(context, "Sem permissão para ligar", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String retrieveContactNumber() {

        String contactNumber = null;

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (Objects.requireNonNull(cursorID).moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d("Principal", "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (Objects.requireNonNull(cursorPhone).moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

        Log.d("Principal", "Contact Phone Number: " + contactNumber);

        return contactNumber;
    }
}