package br.com.zenitech.zcallmobile.domais;

/**
 * Created by kma_s on 1/5/18.
 */

public class SmsDomains {
    private String id;
    private String status;

    public SmsDomains(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
