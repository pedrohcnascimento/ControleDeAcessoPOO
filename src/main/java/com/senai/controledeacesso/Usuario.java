package com.senai.controledeacesso;

public class Usuario {
    int ID;
    int IDAcesso;
    String nome;
    String Cargo;
    String email;
    String telefone;

    public Usuario(int ID, String nome, String cargo, String email, String telefone) {
        this.ID = ID;
        this.nome = nome;
        Cargo = cargo;
        this.email = email;
        this.telefone = telefone;
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
