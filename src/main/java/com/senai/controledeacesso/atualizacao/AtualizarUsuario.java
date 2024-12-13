package com.senai.controledeacesso.atualizacao;

public class AtualizarUsuario {
    int idAcesso;
    int telefone;
    String nome;
    String cargo;
    String email;

    public AtualizarUsuario(int idAcesso, int telefone, String nome, String cargo, String email) {
        this.idAcesso = idAcesso;
        this.telefone = telefone;
        this.nome = nome;
        this.cargo = cargo;
        this.email = email;
    }

    @Override
    public String toString() {
        return String.format("%s  |  %s  |  %s  |  %s  |  %s  |",idAcesso,cargo,nome,telefone,email);
    }
}
