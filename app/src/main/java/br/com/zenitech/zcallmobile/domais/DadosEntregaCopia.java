package br.com.zenitech.zcallmobile.domais;


public class DadosEntregaCopia {
    private String id_pedido;
    private String hora_recebimento;
    private String nome_atendente;
    private String telefone_pedido;
    private String status;
    private String troco_para;
    private String valor;
    private String id_cliente;
    private String cliente;
    private String apelido;
    private String endereco;
    private String localidade;
    private String numero;
    private String complemento;
    private String ponto_referencia;
    private String coord_latitude;
    private String coord_longitude;
    private String produtos;
    private String brindes;
    private String observacao;
    private String forma_pagamento;
    private String ativar_btn_ligar;

    public DadosEntregaCopia(String id_pedido, String hora_recebimento, String nome_atendente, String telefone_pedido, String status, String troco_para, String valor, String id_cliente, String cliente, String apelido, String endereco, String localidade, String numero, String complemento, String ponto_referencia, String coord_latitude, String coord_longitude, String produtos, String brindes, String observacao, String forma_pagamento, String ativar_btn_ligar) {
        this.id_pedido = id_pedido;
        this.hora_recebimento = hora_recebimento;
        this.nome_atendente = nome_atendente;
        this.telefone_pedido = telefone_pedido;
        this.status = status;
        this.troco_para = troco_para;
        this.valor = valor;
        this.id_cliente = id_cliente;
        this.cliente = cliente;
        this.apelido = apelido;
        this.endereco = endereco;
        this.localidade = localidade;
        this.numero = numero;
        this.complemento = complemento;
        this.ponto_referencia = ponto_referencia;
        this.coord_latitude = coord_latitude;
        this.coord_longitude = coord_longitude;
        this.produtos = produtos;
        this.brindes = brindes;
        this.observacao = observacao;
        this.forma_pagamento = forma_pagamento;
        this.ativar_btn_ligar = ativar_btn_ligar;
    }

    public String getId_pedido() {
        return id_pedido;
    }

    public void setId_pedido(String id_pedido) {
        this.id_pedido = id_pedido;
    }

    public String getHora_recebimento() {
        return hora_recebimento;
    }

    public void setHora_recebimento(String hora_recebimento) {
        this.hora_recebimento = hora_recebimento;
    }

    public String getNome_atendente() {
        return nome_atendente;
    }

    public void setNome_atendente(String nome_atendente) {
        this.nome_atendente = nome_atendente;
    }

    public String getTelefone_pedido() {
        return telefone_pedido;
    }

    public void setTelefone_pedido(String telefone_pedido) {
        this.telefone_pedido = telefone_pedido;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTroco_para() {
        return troco_para;
    }

    public void setTroco_para(String troco_para) {
        this.troco_para = troco_para;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getId_cliente() {
        return id_cliente;
    }

    public void setId_cliente(String id_cliente) {
        this.id_cliente = id_cliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getLocalidade() {
        return localidade;
    }

    public void setLocalidade(String localidade) {
        this.localidade = localidade;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getPonto_referencia() {
        return ponto_referencia;
    }

    public void setPonto_referencia(String ponto_referencia) {
        this.ponto_referencia = ponto_referencia;
    }

    public String getCoord_latitude() {
        return coord_latitude;
    }

    public void setCoord_latitude(String coord_latitude) {
        this.coord_latitude = coord_latitude;
    }

    public String getCoord_longitude() {
        return coord_longitude;
    }

    public void setCoord_longitude(String coord_longitude) {
        this.coord_longitude = coord_longitude;
    }

    public String getProdutos() {
        return produtos;
    }

    public void setProdutos(String produtos) {
        this.produtos = produtos;
    }

    public String getBrindes() {
        return brindes;
    }

    public void setBrindes(String brindes) {
        this.brindes = brindes;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getForma_pagamento() {
        return forma_pagamento;
    }

    public void setForma_pagamento(String forma_pagamento) {
        this.forma_pagamento = forma_pagamento;
    }

    public String getAtivar_btn_ligar() {
        return ativar_btn_ligar;
    }

    public void setAtivar_btn_ligar(String ativar_btn_ligar) {
        this.ativar_btn_ligar = ativar_btn_ligar;
    }
}
