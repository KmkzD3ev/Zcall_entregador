package br.com.zenitech.zcallmobile;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

import br.com.zenitech.zcallmobile.domais.DadosResetApp;
import br.com.zenitech.zcallmobile.interfaces.IResetApp;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetApp extends AppCompatActivity {
    SharedPreferences prefs;
    EditText serial;
    EditText codA, codB, codC, codD, codE, codF;
    TextView txtMsgReset;
    ClassAuxiliar aux;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_app);
        /*Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        prefs = getSharedPreferences("preferencias", MODE_PRIVATE);
        aux = new ClassAuxiliar();

        txtMsgReset = findViewById(R.id.txtMsgReset);
        //
        serial = findViewById(R.id.serial);
        serial.setText(prefs.getString("telefone", ""));
        serial.addTextChangedListener(Mask.insert("(##)#####-####", serial));
        serial.setOnEditorActionListener((v, actionId, event) -> {
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
                //validarCampos();

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
            if (serial.getText().toString().equals("")) {
                Toast.makeText(getBaseContext(), "Informe o serial!", Toast.LENGTH_LONG).show();
            } else if (
                    codA.getText().toString().equals("") ||
                            codA.getText().toString().equals("") ||
                            codB.getText().toString().equals("") ||
                            codC.getText().toString().equals("") ||
                            codD.getText().toString().equals("") ||
                            codE.getText().toString().equals("") ||
                            codF.getText().toString().equals("")
            ) {
                Toast.makeText(getBaseContext(), "Informe o código que enviamos!", Toast.LENGTH_LONG).show();
            } else {
                Confirmar();
            }
        });

        //
        findViewById(R.id.btnReenviarCodigo).setOnClickListener(v -> {
            prefs.edit().putBoolean("reset", false).apply();
            Intent i = new Intent(getBaseContext(), Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            finish();
        });
    }

    private void Confirmar() {
        // ESCONDE O TECLADO
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception e) {
            //Log.d(TAG, Objects.requireNonNull(e.getMessage()));
        }
        //
        findViewById(R.id.formReset).setVisibility(View.GONE);
        findViewById(R.id.msgReset).setVisibility(View.VISIBLE);

        String vCod = codA.getText().toString() + codB.getText().toString() + codC.getText().toString() + codD.getText().toString() + codE.getText().toString() + codF.getText().toString();
        final IResetApp iSincronizar = IResetApp.retrofit.create(IResetApp.class);

        final Call<DadosResetApp> call = iSincronizar.resetApp(
                "reset_app",
                aux.soNumeros(serial.getText().toString()),
                vCod
        );

        call.enqueue(new Callback<DadosResetApp>() {
            @Override
            public void onResponse(@NonNull Call<DadosResetApp> call, @NonNull Response<DadosResetApp> response) {
                final DadosResetApp sincronizacao = response.body();
                if (Objects.requireNonNull(sincronizacao).getErro().equalsIgnoreCase("ok")) {
                    resetarApp();
                } else {
                    txtMsgReset.setText("Não foi possível resetar o App, verifique as informações e tente novamente.");
                    erro();
                }
            }

            @Override
            public void onFailure(@NonNull Call<DadosResetApp> call, @NonNull Throwable t) {
                Log.i("ResetApp", Objects.requireNonNull(t.getMessage()));
                txtMsgReset.setText(Objects.requireNonNull(t.getMessage()));
                erro();
            }
        });
    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager) getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear " + packageName);

                Toast.makeText(getBaseContext(), "O App foi resetado com sucesso!", Toast.LENGTH_LONG);
            }

            //prefs.edit().putBoolean("reset", false).apply();
            Intent i = new Intent(getBaseContext(), Splash.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);

            finish();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void resetarApp() {
        clearAppData();
    }

    void erro() {
        new Handler().postDelayed(() -> {
            txtMsgReset.setText("Validando o reset, aguarde...");
            findViewById(R.id.msgReset).setVisibility(View.GONE);
            findViewById(R.id.formReset).setVisibility(View.VISIBLE);

        }, 5000);
    }
}