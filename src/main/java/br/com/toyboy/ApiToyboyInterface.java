package br.com.toyboy;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

import java.util.ArrayList;

/**
 * Created by Paulo on 14/03/16.
 */
public interface ApiToyboyInterface {

    /* Envia os dados de acesso do Usuario e retorna um array de produtos do E-Commerce com id e seus respectivos códigos Lynx */
    @POST("products/list")
    Call<Usuario> listaProdutos(@Body Usuario usuario);

    /* Envia os produtos para atualizar preço e quantidade em estoque */
    @POST("update/produto")
    Call<Usuario> atualizaProdutos(@Body Usuario usuario);

}
