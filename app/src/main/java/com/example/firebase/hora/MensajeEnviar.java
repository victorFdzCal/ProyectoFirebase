package com.example.firebase.hora;

import com.example.firebase.chat.Mensaje;

import java.util.Map;

public class MensajeEnviar extends Mensaje {
    private Map hora;

    public MensajeEnviar(Map hora) {
        this.hora = hora;
    }

    public MensajeEnviar() {
    }

    public MensajeEnviar(String mensaje, String nombre, String urlFoto, String fotoPerfil, String typeMensaje, Map hora) {
        super(mensaje, nombre, urlFoto, fotoPerfil, typeMensaje);
        this.hora = hora;
    }

    public MensajeEnviar(String mensaje, String nombre, String fotoPerfil, String typeMensaje, Map hora) {
        super(mensaje, nombre, fotoPerfil, typeMensaje);
        this.hora = hora;
    }

    public Map getHora() {
        return hora;
    }

    public void setHora(Map hora) {
        this.hora = hora;
    }
}
