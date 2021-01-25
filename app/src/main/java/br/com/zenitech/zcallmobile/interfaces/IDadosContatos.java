package br.com.zenitech.zcallmobile.interfaces;

import java.util.List;

import br.com.zenitech.zcallmobile.domais.DadosContatos;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

import static br.com.zenitech.zcallmobile.ConfigApp.url_servidor;

public interface IDadosContatos {

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("contatos.php")
    Call<List<DadosContatos>> contatos(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
