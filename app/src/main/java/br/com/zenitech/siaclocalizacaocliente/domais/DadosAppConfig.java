package br.com.zenitech.siaclocalizacaocliente.domais;

public class DadosAppConfig {

    private String versao;

    public DadosAppConfig(String versao) {
        this.versao = versao;
    }

    public String getVersao() {
        return versao;
    }

    public void setVersao(String versao) {
        this.versao = versao;
    }
}
