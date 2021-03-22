package br.com.zenitech.zcallmobile;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.messaging.FirebaseMessaging;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import br.com.zenitech.zcallmobile.interfaces.IDadosEntrega;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Ponto extends AppCompatActivity {
    //
    private SharedPreferences prefs;
    private Context context;
    private SpotsDialog dialog;
    private String newToken;
    EditText etSenhaSeguranca;
    LinearLayout llSenhaSeguranca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ponto);
        //Objects.requireNonNull(getSupportActionBar()).setTitle("Zcall Mobile");
        //getSupportActionBar().setSubtitle("Ponto");

        //
        prefs = getSharedPreferences("preferencias", Context.MODE_PRIVATE);
        context = this;
        //dialog = new SpotsDialog(context, R.style.Custom);
        dialog = (SpotsDialog) new SpotsDialog.Builder()
                .setContext(context)
                .setTheme(R.style.Custom)
                .build();

        //Gerar token firebase **********
        /*FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Ponto.this, instanceIdResult -> {
            newToken = instanceIdResult.getToken();
            Log.e("Ponto", newToken);

            //prefs.edit().putString("fcmToken", newToken).apply();
        });*/

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            newToken = Objects.requireNonNull(task.getResult());
            Log.e("Ponto", newToken);
        });

        findViewById(R.id.btnIniciarPonto).setOnClickListener(view -> {

            //
            VerificarOnline online = new VerificarOnline();
            if (online.isOnline(context)) {

                //
                iniciarPonto();
            }
        });

        //
        llSenhaSeguranca = findViewById(R.id.llSenhaSeguranca);

        //
        /*SwitchCompat screenOnSwitch = findViewById(R.id.switch1);
        screenOnSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
            if (isChecked) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });*/

        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
            setBrightness(0);

            //setScreenOffTimeOut();
            //restoreScreenOffTimeOut();

            findViewById(R.id.btnDesligarTela).setOnClickListener(
                    view -> {
                        //
                        setBrightness(25);
                        llSenhaSeguranca.setVisibility(View.VISIBLE);
                    }
            );

            etSenhaSeguranca = findViewById(R.id.etSenhaSeguranca);
            etSenhaSeguranca.setOnEditorActionListener((v, actionId, event) -> {
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
                    validarSenhaSeguranca();

                    handled = true;
                }
                return handled;
            });
        } else {
            findViewById(R.id.btnDesligarTela).setVisibility(View.GONE);
        }
    }

    private void validarSenhaSeguranca() {
        if (etSenhaSeguranca.getText().toString().equals("")) {
            Toast.makeText(getBaseContext(), "Informe a senha de segurança!", Toast.LENGTH_LONG).show();
        } else if (!etSenhaSeguranca.getText().toString().equals("857496")) {
            Toast.makeText(getBaseContext(), "Senha de segurança incorreta!", Toast.LENGTH_LONG).show();
        } else {
            //
            Toast.makeText(getBaseContext(), "Desligando tela...", Toast.LENGTH_LONG).show();
            llSenhaSeguranca.setVisibility(View.GONE);

            setScreenOffTimeOut(1);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    //private static final int SCREEN_OFF_TIME_OUT = 1;
    private int mSystemScreenOffTimeOut;

    private void setScreenOffTimeOut(int SCREEN_OFF_TIME_OUT) {
        try {
            mSystemScreenOffTimeOut = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIME_OUT);
        } catch (Exception e) {
            Log.e("Ponto", e.getMessage());
            //Utils.handleException(e);
        }
    }

    private void restoreScreenOffTimeOut() {
        if (mSystemScreenOffTimeOut == 0) return;
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, mSystemScreenOffTimeOut);
        } catch (Exception e) {
            Log.e("Ponto", e.getMessage());
            //Utils.handleException(e);
        }
    }

    public void iniciarPonto() {
        //
        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1"))
            setScreenOffTimeOut(30000);

        //barra de progresso pontos
        dialog.show();

        //
        final IDadosEntrega iEmpregos = IDadosEntrega.retrofit.create(IDadosEntrega.class);

        //
        final Call<DadosEntrega> call = iEmpregos.getPonto(
                prefs.getString("id_empresa", ""),
                "iniciarponto",
                prefs.getString("telefone", ""),
                newToken
        );

        call.enqueue(new Callback<DadosEntrega>() {
            @Override
            public void onResponse(@NotNull Call<DadosEntrega> call, @NotNull Response<DadosEntrega> response) {


                int code = response.code();

                if (code == 200) {

                    DadosEntrega dados = response.body();

                    if (Objects.requireNonNull(dados).status.equals("OK")) {

                        prefs.edit().putString("ponto", "ok").apply();
                        prefs.edit().putString("ativar_btn_ligar", dados.ativar_btn_ligar).apply();

                        if (prefs.getString("usa_case", "0").equalsIgnoreCase("1")) {
                            // DEFINE O TEMPO DE DESLIGAMENTO DA TELA
                            setScreenOffTimeOut(86400000);
                            // AUMENTA O BRILHO DA TELA NO MÁXIMO
                            setBrightness(25);
                            // IMPEDE O DESLIGAMENTO DA TELA
                            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                        }

                        //
                        Intent i = new Intent(Ponto.this, Principal2.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }

                } else {
                    Toast.makeText(Ponto.this, "Falha" + code,
                            Toast.LENGTH_SHORT).show();
                }

                //
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<DadosEntrega> call, @NonNull Throwable t) {

                //
                dialog.dismiss();

                //
                Toast.makeText(context, "Problema de acesso: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();

                Log.e("Ponto", t.getMessage());
            }
        });
    }

    public void setBrightness(int brightness) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(context)) {
                // Do stuff here
                //constrain the value of brightness
                if (brightness < 0)
                    brightness = 0;
                else if (brightness > 255)
                    brightness = 255;

                ContentResolver cResolver = this.getApplicationContext().getContentResolver();
                Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            } else {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
    }
}
