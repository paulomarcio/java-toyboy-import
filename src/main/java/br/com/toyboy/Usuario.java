package br.com.toyboy;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paulo on 14/03/16.
 */
public class Usuario {

    @SerializedName("usuario")
    String usuario;

    @SerializedName("senha")
    String senha;

    @SerializedName("produtos")
    List<Produto> produtos = new ArrayList<Produto>();

    @SerializedName("mensagens")
    List<Mensagem> mensagens = new ArrayList<Mensagem>();

    @SerializedName("success")
    Boolean success;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public void setMensagens(List<Mensagem> mensagens) {
        this.mensagens = mensagens;
    }

    public List<Mensagem> getMensagens() {
        return mensagens;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
