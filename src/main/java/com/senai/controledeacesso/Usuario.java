package com.senai.controledeacesso;

public class Usuario {
    int ID;
    int IDAcesso;
    String nome;
    String Cargo;
    String email;
    String telefone;
    String imagem;

    public Usuario(int ID, int IDAcesso, String nome, String cargo, String email, String telefone, String imagem) {
        this.ID = ID;
        this.IDAcesso = IDAcesso;
        this.nome = nome;
        Cargo = cargo;
        this.email = email;
        this.telefone = telefone;
        this.imagem = imagem;
    }

    @Override
    public String toString() {
        return
                ID +
                "," + IDAcesso +
                "," + nome +
                "," + Cargo +
                "," + email +
                "," + telefone;
    }
}
