package br.com.toyboy;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Paulo on 14/03/16.
 */
public class Mensagem {

    @SerializedName("message")
    String message;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
