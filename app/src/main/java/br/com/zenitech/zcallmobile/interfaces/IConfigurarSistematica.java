package br.com.zenitech.zcallmobile.interfaces;

import java.util.List;

import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaFormPag;
import br.com.zenitech.zcallmobile.domais.DadosConfigSistematicaProdutos;
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

public interface IConfigurarSistematica {

    @FormUrlEncoded
    @POST("configuracoes.php")
    Call<List<DadosConfigSistematicaFormPag>> FormasPagamento(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao
    );

    @FormUrlEncoded
    @POST("configuracoes.php")
    Call<List<DadosConfigSistematicaProdutos>> Produtos(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
