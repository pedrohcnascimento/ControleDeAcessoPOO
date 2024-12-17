package com.senai.controledeacesso;

public class Usuario {
    int ID;
    int IDAcesso;
    String nome;
    String Cargo;
    String email;
    String telefone;

    public Usuario(int ID, int IDAcesso, String nome, String cargo, String email, String telefone) {
        this.ID = ID;
        this.IDAcesso = IDAcesso;
        this.nome = nome;
        Cargo = cargo;
        this.email = email;
        this.telefone = telefone;
    }
}
