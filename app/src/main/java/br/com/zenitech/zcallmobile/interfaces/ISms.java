package br.com.zenitech.zcallmobile.interfaces;

import br.com.zenitech.zcallmobile.domais.SmsDomains;
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

public interface ISms {

    @FormUrlEncoded
    @POST("sms.php")
    Call<SmsDomains> Sms(
            @Field("id_empresa") String id_empresa,
            @Field("celular") String celular,
            @Field("codigo") String codigo
    );

    //.baseUrl("http://177.153.22.33/sistemas/apps/pedir_gas/")
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
