package br.com.zenitech.siaclocalizacaocliente;

import android.util.Log;

import br.com.zenitech.siaclocalizacaocliente.domais.RelatarErrosDomains;
import br.com.zenitech.siaclocalizacaocliente.interfaces.IRelatarErros;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

class RelatarErros {


    // ENVIA ERROS PARA OS DESENVOLVEDORES RESOLVER
    void enviarErro(String msg) {

        //
        final IRelatarErros iRelatarErros = IRelatarErros.retrofit.create(IRelatarErros.class);

        //
        final Call<RelatarErrosDomains> call = iRelatarErros.enviarEmail(
                msg
        );

        call.enqueue(new Callback<RelatarErrosDomains>() {
            @Override
            public void onResponse(Call<RelatarErrosDomains> call, Response<RelatarErrosDomains> response) {

                try {
                    //
                    if (response.isSuccessful()) {

                        RelatarErrosDomains dados = response.body();

                        if (dados != null) {
                            //
                            Log.i("zcall: ", "email enviado!");

                        }

                    } else {

                        //
                        Log.i("zcall: ", "email não enviado!");

                    }

                } catch (Exception e) {


                    //
                    Log.i("zcall: ", "email não enviado!");
                }
            }

            @Override
            public void onFailure(Call<RelatarErrosDomains> call, Throwable t) {

                //
                Log.i("zcall: ", "email não enviado!");
            }
        });
    }
}
