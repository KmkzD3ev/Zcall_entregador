package br.com.zenitech.siaclocalizacaocliente.interfaces;

import br.com.zenitech.siaclocalizacaocliente.domais.PosicoesDomains;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static br.com.zenitech.siaclocalizacaocliente.ConfigApp.url_servidor;

public interface IPosicoes {
    @FormUrlEncoded
    @POST("posicoes.php")
    Call<PosicoesDomains> Posicoes(
            @Field("id_empresa") String id_empresa,
            @Field("telefone") String telefone,
            @Field("opcao") String opcao,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
