package br.com.zenitech.zcallmobile.interfaces;

import br.com.zenitech.zcallmobile.domais.RelatarErrosDomains;
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

public interface IRelatarErros {

    @FormUrlEncoded
    @POST("enviar_email.php")
    Call<RelatarErrosDomains> enviarEmail(
            @Field("msg") String msg
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
