package br.com.zenitech.zcallmobile.interfaces;

import br.com.zenitech.zcallmobile.domais.DadosResetApp;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static br.com.zenitech.zcallmobile.ConfigApp.url_servidor;

/**
 * Created by kma_s on 1/5/18.
 */

public interface IResetApp {

    //RESETAR APP
    @FormUrlEncoded
    @POST("reset_app.php")
    Call<DadosResetApp> resetApp(@Field("opcao") String opcao, @Field("serial") String serial, @Field("codigo") String codigo);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
