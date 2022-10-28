package br.com.zenitech.zcallmobile.domais;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class JSonZCall {
    @SerializedName("id_empresa")
    public String id_empresa;
    @SerializedName("opcao")
    public String opcao;
    @SerializedName("telefone")
    public String telefone;
    @SerializedName("latitude")
    public String latitude;
    @SerializedName("longitude")
    public String longitude;
    @SerializedName("Pedidos")
    public List<DadosEntrega> Pedidos;
    @SerializedName("Posicoes")
    public List<DadosPosicoes> Posicoes;
    @SerializedName("Sistematicas")
    public List<DadosVendasSistematica> Sistematicas;

    // retorno
    public String retorno_posicao;
    public List<DadosPosicoes> retorno_posicao_off;
    public List<DadosVendasSistematica> retorno_sistematicas;
    public List<DadosEntrega> retorno_pedidos_fin; //PEDIDOS FINALIZADOS PELO ENTREGADOR
    public List<DadosEntrega> retorno_pedidos_operador;
    public List<DadosEntrega> retorno_pedidos_abertos;
    public List<DadosEntrega> retorno_pedidos_notificados;
    public List<DadosEntrega> retorno_pedidos_confirmado;
    public List<DadosEntrega> retorno_pedidos_mudou_entregador;
}
