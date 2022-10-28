package br.com.zenitech.zcallmobile.interfaces;

import static br.com.zenitech.zcallmobile.ConfigApp.url_servidor;

import java.util.List;

import br.com.zenitech.zcallmobile.domais.DadosEntrega;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IDadosEntrega {

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<DadosEntrega> confirmarTelefone(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<DadosEntrega> getPonto(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("token") String token
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<DadosEntrega> getSairPonto(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA @POST("consultar_entregas.php")
    @FormUrlEncoded
    @POST("notificacoes_novo.php")
    Call<DadosEntrega> getInforEntrega(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA @POST("consultar_entregas.php")
    @FormUrlEncoded
    @POST("notificacoes_novo.php")
    Call<DadosEntrega> finalizarEntrega(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido,
            @Field("coord_latitude") double coord_latitude,
            @Field("coord_longitude") double coord_longitude
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA @POST("consultar_entregas.php")
    @FormUrlEncoded
    @POST("notificacoes_novo.php")
    Call<DadosEntrega> entregasFinalizadasOperador(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido
    );

    //
    @FormUrlEncoded
    @POST("notificacoes_novo_t2.php")
    Call<DadosEntrega> entregaMudouEntregador(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido,
            @Field("md5") String md5
    );

    //
    @FormUrlEncoded
    @POST("notificacoes_novo_t2.php")
    Call<DadosEntrega> pedidoAlterado(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido,
            @Field("md5") String md5
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA @POST("consultar_entregas.php")
    @FormUrlEncoded
    @POST("atualizar_entregas.php")
    Call<DadosEntrega> atualizarStatus(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("atualizar_entregas.php")
    Call<DadosEntrega> marcarComoVisto(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone
    );

    // MARCA O PEDIDO COMO VISTO PELO OPERADOR
    @FormUrlEncoded
    @POST("atualizar_entregas.php")
    Call<DadosEntrega> vistoPeloEntregador(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone,
            @Field("id_pedido") String id_pedido
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<DadosEntrega> getAceitarEntrega(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("id_pedido") String id_pedido,
            @Field("status") String status
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("finalizar_entregas.php")
    Call<DadosEntrega> getFinalizarEntrega(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("id_pedido") String id_pedido,
            @Field("status") String status,
            @Field("coord_latitude_pedido") double coord_latitude_pedido,
            @Field("coord_longitude_pedido") double coord_longitude_pedido
    );

    //CANCELAR A ENTREGA
    @FormUrlEncoded
    @POST("finalizar_entregas.php")
    Call<DadosEntrega> getCancelarEntrega(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("id_pedido") String id_pedido
    );

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    @FormUrlEncoded
    @POST("finalizar_entregas.php")
    Call<DadosEntrega> atualizarLocalCliente(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("id_cliente") String id_cliente,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );

    //RETORNA LISTA DE ENTREGAS
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<List<DadosEntrega>> getEntregas(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("telefone") String telefone
    );

    //RETORNA LISTA DE ENTREGAS
    @FormUrlEncoded
    @POST("consultar_entregas.php")
    Call<DadosEntrega> getDetalhesEntregas(
            @Field("id_empresa") String id_empresa,
            @Field("opcao") String opcao,
            @Field("id_pedido") String id_pedido
    );

    //RETORNA LISTA DE ENTREGAS
    @FormUrlEncoded
    @POST("sistematica.php")
    Call<DadosEntrega> vedaSistematica(
            @Field("opcao") String opcao,
            @Field("id_empresa") String id_empresa,
            @Field("celular") String celular,
            @Field("id_produto") String id_produto,
            @Field("id_forma_pagamento") String id_forma_pagamento,
            @Field("valor") String valor,
            @Field("data") String data,
            @Field("hora_recebimento") String hora_recebimento,
            @Field("quantidade") String quantidade
    );

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(url_servidor)
            .addConverterFactory(GsonConverterFactory.create())
            .build();
}
