package br.com.zenitech.zcallmobile.domais;

import java.util.List;

public class JSonZCallRetorno {
    public String posicao; // RETORNA 1 => SALVOU, 0 => NÃO SALVOU
    public String opcao;
    public String telefone;
    public String latitude;
    public String longitude;
    public List<DadosEntrega> Pedidos;
    public List<DadosPosicoes> Posicoes;
    public List<DadosVendasSistematica> Sistematicas;
}
