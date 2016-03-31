package br.com.toyboy;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Paulo on 12/03/16.
 */
public class Produto {
    @SerializedName("id")
    int id;

    @SerializedName("referencia")
    String referencia;

    @SerializedName("preco")
    float preco = 0;

    @SerializedName("quantidade")
    int quantidade = 0;

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return this.id;
    }

    public void setReferencia(String referencia){
        this.referencia = referencia;
    }

    public String getReferencia(){
        return this.referencia;
    }

    public float getPreco() {
        return preco;
    }

    public void setPreco(float preco) {
        this.preco = preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
