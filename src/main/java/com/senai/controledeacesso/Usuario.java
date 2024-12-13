package com.senai.controledeacesso;

public class Usuario {
    Integer ID;
    int IDAcesso;
    String nome;
    String Cargo;
    String email;
    String telefone;

    public Usuario(int ID, String telefone, String email, String cargo, String nome, int IDAcesso) {
        this.ID = ID;
        this.telefone = telefone;
        this.email = email;
        Cargo = cargo;
        this.nome = nome;
        this.IDAcesso = IDAcesso;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "ID=" + ID +
                ", IDAcesso=" + IDAcesso +
                ", nome='" + nome + '\'' +
                ", Cargo='" + Cargo + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                '}';
    }
}
