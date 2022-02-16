package br.com.zenitech.zcallmobile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import dmax.dialog.SpotsDialog;

public class ConfirmarTelefone extends AppCompatActivity {

    private String celular, codigo;
    private Context context;
    private EditText etCodigo;
    private SharedPreferences prefs;
    private SharedPreferences.Editor ed;
    private EditText codA, codB, codC, codD, codE, codF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmar_telefone);

        //
        //getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        context = this;
        //
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        ed = prefs.edit();

        //
        TextView copyright = findViewById(R.id.copyright);
        copyright.setText("Zenitech");

        //SpotsDialog dialog = new SpotsDialog(this, R.style.Custom);
        SpotsDialog dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        TextView cod = findViewById(R.id.cod);

        //RECEBE O NÚMERO DO CELULAR INFORMADO
        Intent intent = getIntent();

        if (intent != null) {
            Bundle params = intent.getExtras();

            if (params != null) {

                celular = params.getString("celular");
                codigo = params.getString("codigo");

                cod.setText(codigo);
            }
        }

        etCodigo = findViewById(R.id.codA);
        //etCodigo.addTextChangedListener(Mask.insert("####", etCodigo));


        etCodigo.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                //ESCODER O TECLADO
                // TODO Auto-generated method stub
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                if (etCodigo.getText().toString().equals("")) {
                    Toast.makeText(getBaseContext(), "Informe o código que enviamos!", Toast.LENGTH_LONG).show();
                } else {
                    Confirmar();
                }

                handled = true;
            }
            return handled;
        });

        //
        codA = findViewById(R.id.codA);
        codA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    codA.clearFocus();
                    codB.requestFocus();
                    codB.setCursorVisible(true);
                }
            }
        });
        //
        codB = findViewById(R.id.codB);
        codB.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    codB.clearFocus();
                    codC.requestFocus();
                    codC.setCursorVisible(true);
                }
            }
        });
        //
        codC = findViewById(R.id.codC);
        codC.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    codC.clearFocus();
                    codD.requestFocus();
                    codD.setCursorVisible(true);
                }
            }
        });
        //
        codD = findViewById(R.id.codD);
        codD.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    codD.clearFocus();
                    codE.requestFocus();
                    codE.setCursorVisible(true);
                }
            }
        });
        //
        codE = findViewById(R.id.codE);
        codE.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {
                    codE.clearFocus();
                    codF.requestFocus();
                    codE.setCursorVisible(true);
                }
            }
        });
        //
        codF = findViewById(R.id.codF);
        codF.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 1) {

                    //
                    Confirmar();
                }
            }
        });
        codF.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                //ESCODER O TECLADO
                // TODO Auto-generated method stub
                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                //
                Confirmar();

                handled = true;
            }
            return handled;
        });

        //
        findViewById(R.id.btnConfirmarCodigo).setOnClickListener(v -> {
            if (
                    etCodigo.getText().toString().equals("")) {
                Toast.makeText(getBaseContext(), "Informe o código que enviamos!", Toast.LENGTH_LONG).show();
            } else {

                Confirmar();
            }
        });

        //
        findViewById(R.id.btnReenviarCodigo).setOnClickListener(v -> {
            Intent i = new Intent(context, Configuracao.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            finish();
        });

        //
        //EnviarSms();
    }

    private void Confirmar() {

        String vCod = codA.getText().toString() + codB.getText().toString() + codC.getText().toString() + codD.getText().toString() + codE.getText().toString() + codF.getText().toString();
        if (vCod.equals(codigo)) {
            Toast.makeText(context, "Validação concluída!", Toast.LENGTH_LONG).show();

            ed.putString("validado", "ok").apply();
            ed.putString("telefone", celular).apply();

            //
            if (Objects.requireNonNull(prefs.getString("cadastrado", "")).equals("")) {
                //
                Intent i = new Intent(context, Ponto.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            } else {
                //
                Intent i = new Intent(context, Principal2.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }

            finish();
        } else {
            Toast.makeText(context, "Código inválido!", Toast.LENGTH_LONG).show();
        }
    }
}
