package com.senai.controledeacesso.atualizacao;

import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static ArrayList<AtualizarUsuario> atualizarUsuarios=new ArrayList<>();
static Scanner scanner=new Scanner(System.in);
    public static void main(String[] args) {

        int opcao;

        String menu = """
                    _________________________________________________________
                    |   Escolha uma opção:                                  |
                    |       1- Exibir cadastro completo                     |
                    |       2- Inserir novo cadastro                        |
                    |       3- Atualizar cadastro por id                    |
                    |       4- Deletar um cadastro por id                   |
                    |       5- Associar TAG ou cartão de acesso ao usuário  |
                    |       6- Limpa registros de acesso                    |
                    |       7- Pesquisar registros por ID                   |
                    |       8- Sair                                         |
                    _________________________________________________________
                    """;

        do {
            System.out.println(menu);
            opcao = scanner.nextInt();
            scanner.nextLine();
            switch (opcao) {
                case 1:

//
                    break;
                case 2:

                    break;
                case 3: //------Leh------tualizar//
                    atualizarUsuario();
                    System.out.println("Insira o ID para atualizar o cadastro");
                    int idUsuario=scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("Que dado você deseja atualizar?");
                    System.out.println("1-Nome     2-Telefone     3-Email");
                    int opcaoAtualizar=scanner.nextInt();
                    switch (opcaoAtualizar){
                        case 1:
                            System.out.print("Informe o nome: ");
                            String nomeUsuario=scanner.nextLine();
                            for (int index = 0; index < atualizarUsuarios.size(); index++) {
                                if (atualizarUsuarios.get(index).nome.equals(nomeUsuario)){
                                    System.out.println("Digite o novo nome para esse cadastro");
                                    atualizarUsuarios.get(index).nome=scanner.nextLine();
                                }
                            }
                            break;
                        case 2:
                            System.out.println("Para uma ação mais segura,insira o nome do usuario para acessar seus dados");
                            String nomeUsuarioB=scanner.nextLine();
                            int numeroTelefone=scanner.nextInt();
                            scanner.nextLine();
                            for (int index = 0; index < atualizarUsuarios.size(); index++) {
                                if (atualizarUsuarios.get(index).nome.equals(nomeUsuarioB)){
                                    System.out.println("Agora digite o novo numero de telefone");
                                    atualizarUsuarios.get(index).telefone=scanner.nextInt();
                                    scanner.nextLine();
                                }else {
                                    System.out.println("ops...Usuario não encontrado");
                                }
                            }
                            break;
                        case 3:
                            System.out.println("Infome o novo email que deseja inserir");
                            String emailUsuario=scanner.nextLine();
                            for (int index= 0; index < atualizarUsuarios.size();index++){
                                if (atualizarUsuarios.get(index).email.equals(emailUsuario)){
                                    System.out.println("Digite o novo email");
                                    atualizarUsuarios.get(index).email=scanner.nextLine();
                                }
                            }
                            break;
                    }
                    break;
                case 4:

                    break;
                case 5:

                    break;
                case 6:

                    break;
            case 7:

                break;
            case 8:
                System.out.println("Fim do programa...");
                break;
            }

        }while (opcao != 8);
    }
    static void atualizarUsuario(){
        for (AtualizarUsuario atualizar: atualizarUsuarios){
            System.out.println(atualizarUsuarios.indexOf(atualizar)+1+ "-"+atualizar);
        }
    }
    }
