package br.com.zenitech.zcallmobile.interfaces;

import br.com.zenitech.zcallmobile.domais.JSonZCall;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IDadosZCall {

    //RETORNA AS INFORMAÇÕES DA ENTREGA
    /*@Headers({
            "Content-Type: application/json",
            "User-Agent: App-ZCall",
            "Cache-Control: max-age=640000"
    })*/
    /*@Headers({"Content-Type: application/json"})
    @POST("dados.php")
    Call<JSonZCall> enviarDados(
            @Body String dados
    );*/


    //@Headers("Content-Type: application/json")
    @POST("dados.php")
    Call<JSonZCall> enviarDados(@Body JSonZCall dadosJsonZcall);

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://appgas.zenitech.com.br/app_zcall/") //"http://192.168.0.13/app_zcall/"
            .addConverterFactory(GsonConverterFactory.create())
            .build();

}
