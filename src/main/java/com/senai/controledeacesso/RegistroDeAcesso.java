package com.senai.controledeacesso;

public class RegistroDeAcesso {
    String horararioDeRegistro;
    Usuario usuario;

    public RegistroDeAcesso(Usuario usuario, String horararioDeRegistro) {
        this.horararioDeRegistro = horararioDeRegistro;
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return horararioDeRegistro + "," + usuario.ID + "," + usuario.nome;
    }
}
