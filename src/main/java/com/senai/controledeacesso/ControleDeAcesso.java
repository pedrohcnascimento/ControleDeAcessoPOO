package com.senai.controledeacesso;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ControleDeAcesso {
    // Caminho para a pasta ControleDeAcesso no diretório do usuário
    private static final File pastaControleDeAcesso = new File("src/main/resources");

    // Caminho para o arquivo bancoDeDados.txt e para a pasta imagens
    private static final File arquivoBancoDeDados = new File("src/main/resources/RegistroDeUsuarios.txt");
    private static final File arquivoRegistroAcesso = new File( "src/main/resources/RegistroDeAcessos.txt");

    public static final File pastaImagens = new File(pastaControleDeAcesso, "imagens");

    static volatile boolean modoCadastrarIdAcesso = false;
    static int idUsuarioRecebidoPorHTTP = 0;
    static String dispositivoRecebidoPorHTTP = "Disp1";
    public static ArrayList<Usuario> listaUsuarios = new ArrayList<>();
    public static ArrayList<RegistroDeAcesso> listaDeRegistros = new ArrayList<>();


    static String brokerUrl = "tcp://localhost:1883";  // Exemplo de
    static String topico = "IoTKIT1/UID";

    static CLienteMQTT conexaoMQTT;
    static ServidorHTTPS servidorHTTPS;
    static Scanner scanner = new Scanner(System.in);
    static ExecutorService executorIdentificarAcessos = Executors.newFixedThreadPool(4);
    static ExecutorService executorCadastroIdAcesso = Executors.newSingleThreadExecutor();

    public static void main(String[] args) {
        verificarEstruturaDeDiretorios();
        carregarDadosDoArquivo();//feito
        carregarRegistros();
        conexaoMQTT = new CLienteMQTT(brokerUrl, topico, ControleDeAcesso::processarMensagemMQTTRecebida);
        servidorHTTPS = new ServidorHTTPS(); // Inicia o servidor HTTPS
        menuPrincipal();

        // Finaliza o todos os processos abertos ao sair do programa
        scanner.close();
        executorIdentificarAcessos.shutdown();
        executorCadastroIdAcesso.shutdown();
        conexaoMQTT.desconectar();
        servidorHTTPS.pararServidorHTTPS();
    }

    private static void menuPrincipal() {
        int opcao;
        do {
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
            System.out.println(menu);
            opcao = scanner.nextInt();
            scanner.nextLine();

            //Deixei a parte do switch mais clean - DIEGO

            switch (opcao) {
                case 1 -> exibirCadastro();
                case 2 -> cadastrarUsuario();
                case 3 -> atualizarUsuario();
                case 4 -> deletarUsuario();
                case 5 -> aguardarCadastroDeIdAcesso();
                case 6 -> limparRegistros();
                case 7 -> pesquisarRegistrosPorId();
                case 8 -> System.out.println("Encerrando o programa...");
                default -> System.out.println("Opção inválida! Tente novamente.");
            }
        } while (opcao != 8);
    }

    private static void pesquisarRegistrosPorId(){
        StringBuilder registros = new StringBuilder();
        System.out.println("Digite o id do usuário que deseja visualizar:");
        int idEscolhido = scanner.nextInt();
        for (RegistroDeAcesso registroDeAcesso : listaDeRegistros){
            if (idEscolhido == registroDeAcesso.usuario.ID){
                registros.append(registroDeAcesso).append("\n");
            }
        }
        System.out.println(registros);
    }

    private static void aguardarCadastroDeIdAcesso() {//N MEXE
        modoCadastrarIdAcesso = true;
        System.out.println("Aguardando nova tag ou cartão para associar ao usuário");
        // Usar Future para aguardar até que o cadastro de ID seja concluído
        Future<?> future = executorCadastroIdAcesso.submit(() -> {
            while (modoCadastrarIdAcesso) {
                // Loop em execução enquanto o modoCadastrarIdAcesso estiver ativo
                try {
                    Thread.sleep(100); // Evita uso excessivo de CPU
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        try {
            future.get(); // Espera até que o cadastro termine
        } catch (Exception e) {
            System.err.println("Erro ao aguardar cadastro: " + e.getMessage());
        }
    }

    private static void processarMensagemMQTTRecebida(String mensagem) {//N MEXE
        if (!modoCadastrarIdAcesso) {
            executorIdentificarAcessos.submit(() -> criarNovoRegistroDeAcesso(mensagem)); // Processa em thread separada
        } else {
            cadastrarNovoIdAcesso(mensagem); // Processa em thread separada
            modoCadastrarIdAcesso = false;
            idUsuarioRecebidoPorHTTP = 0;
        }
    }

    // Função que busca e atualiza a tabela com o ID recebido
    private static void criarNovoRegistroDeAcesso(String idAcessoRecebido) {
        boolean usuarioEncontrado = false; // Variável para verificar se o usuário foi encontrado

        // Loop para percorrer a lista e buscar o idAcesso
        for (Usuario usuario : listaUsuarios) {
            int idNaLista = usuario.ID;
            String idAcessoNaLista = String.valueOf(usuario.IDAcesso); // A coluna do idAcesso é a segunda coluna (índice 1)

            // Verifica se o idAcesso da matriz corresponde ao idAcesso recebido
            if (idAcessoNaLista.equals(idAcessoRecebido)) {
                listaDeRegistros.add(new RegistroDeAcesso(listaUsuarios.get(idNaLista-1), LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))));

                System.out.println("Usuário encontrado: " + usuario.ID + " - " + usuario.nome);
                usuarioEncontrado = true; // Marca que o usuário foi encontrado
                salvarRegistros();
                break; // Sai do loop, pois já encontrou o usuário
            }
        }
        // Se não encontrou o usuário, imprime uma mensagem
        if (!usuarioEncontrado) {
            System.out.println("Id de Acesso " + idAcessoRecebido + " não cadastrado.");
        }
    }

    private static void cadastrarNovoIdAcesso(String novoIdAcesso) {//Feito
        boolean encontrado = false; // Variável para verificar se o usuário foi encontrado
        int idUsuarioEscolhido = idUsuarioRecebidoPorHTTP;
        String dispositivoEscolhido = dispositivoRecebidoPorHTTP;

        if (idUsuarioRecebidoPorHTTP == 0) {
            // Exibe a lista de usuários para o administrador escolher
            for (Usuario usuario : listaUsuarios) {
                System.out.println(usuario.ID + " - " + usuario.nome); // Exibe ID e Nome do usuário
            }
            // Pede ao administrador que escolha o ID do usuário
            System.out.print("Digite o ID do usuário para associar ao novo idAcesso: ");
            idUsuarioEscolhido = scanner.nextInt();
            conexaoMQTT.publicarMensagem(topico, dispositivoEscolhido);
        }

        modoCadastrarIdAcesso = true;
        // Verifica se o ID do usuário existe na matriz
        for (Usuario usuario : listaUsuarios) {
            if (usuario.ID == idUsuarioEscolhido){
                usuario.IDAcesso = Integer.parseInt(novoIdAcesso);
                System.out.println("id de acesso " + novoIdAcesso + " associado ao usuário " + usuario.nome);
                conexaoMQTT.publicarMensagem("cadastro/disp", "CadastroConcluido");
                encontrado = true;
                salvarDadosNoArquivo();
                break;
            }
        }
        // Se não encontrou o usuário, imprime uma mensagem
        if (!encontrado) {
            System.out.println("Usuário com id" + idUsuarioEscolhido + " não encontrado.");
        }
    }

    // Funções de CRUD
    private static void exibirCadastro() {
        System. out .printf( "----------------------------------------------------------------------%n" );
        System. out .printf( " ID   ID Acesso   Nome        Cargo       Email       Telefone %n" );
        System. out .printf( "----------------------------------------------------------------------%n" );

        for (Usuario usuario : listaUsuarios){
            System.out.printf( "| %-2d | %-4d | %-10s | %-12s | %-10s | %-10s |%n",usuario.ID , usuario.IDAcesso , usuario.nome , usuario.Cargo , usuario.email , usuario.telefone );
        }
        System. out .printf( "----------------------------------------------------------------------%n" );
        System.out.println();
    }

    private static void cadastrarUsuario() {
        System.out.println("Escolha a opção que deseja cadastrar: \n1-Funcionário           2-Aluno");
        int opcao = scanner.nextInt();
            System.out.print("Digite a quantidade de usuarios que deseja cadastrar:");
            int qtdUsuarios = scanner.nextInt();
            scanner.nextLine();

        for (int i = 0; i < qtdUsuarios; i++) {
            System.out.println("\nPreencha os dados a seguir:");

            System.out.println("Nome:");
            String nome = scanner.nextLine();
            System.out.println("Email: ");
            String email = scanner.nextLine();
            System.out.println("Telefone");
            String telefone = scanner.nextLine();

            listaUsuarios.add(new Usuario((listaUsuarios.size()+1),0,nome,(opcao == 2) ? "Aluno" : "Funcionario",email,telefone, "-"));
            salvarDadosNoArquivo();
        }
    }
    private static void atualizarUsuario() {
        exibirCadastro();
        System.out.println("Escolha um id para atualizar o cadastro:");
        int idUsuario = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Que dado deseja atualizar?");
        System.out.println("1-Nome      2-Telefone      3-Email");
        int opcao = scanner.nextInt();
        scanner.nextLine();

        switch (opcao){
            case 1:
                System.out.print("Informe o novo nome: ");
                listaUsuarios.get(idUsuario-1).nome = scanner.nextLine();
                break;
            case 2:
                System.out.print("Informe o novo telefone: ");
                listaUsuarios.get(idUsuario-1).telefone = scanner.nextLine();
                break;
            case 3:
                System.out.print("Informe o novo email: ");
                listaUsuarios.get(idUsuario-1).email = scanner.nextLine();
                break;
        }

        System.out.println("---------Atualizado com sucesso-----------");
        exibirCadastro();
        salvarDadosNoArquivo();
    }

    public static void deletarUsuario() {

            exibirCadastro();
            System.out.println("Escolha um id para deletar o cadastro:");
            int idUsuario = scanner.nextInt();
            scanner.nextLine();
            listaUsuarios.remove(idUsuario-1);

        salvarDadosNoArquivo();
        System.out.println("-----------------------Deletado com sucesso------------------------\n");
        idUsuarioRecebidoPorHTTP = 0;
    }

    // Funções para persistência de dados
    private static void carregarDadosDoArquivo() {

        try (BufferedReader reader = new BufferedReader(new FileReader(arquivoBancoDeDados))) {
            String linha;

            while ((linha = reader.readLine()) != null) {
                String[] conteudo = linha.split(",");
                listaUsuarios.add(new Usuario(Integer.parseInt(conteudo[0]), Integer.parseInt(conteudo[1]), conteudo[2], conteudo[3], conteudo[4], conteudo[5], "-"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void salvarDadosNoArquivo() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoBancoDeDados))) {
            for (Usuario usuario : listaUsuarios) {
                usuario.ID = listaUsuarios.indexOf(usuario) + 1;
                writer.write(usuario + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void verificarEstruturaDeDiretorios() {
        // Verifica se a pasta ControleDeAcesso existe, caso contrário, cria
        if (!pastaControleDeAcesso.exists()) {
            if (pastaControleDeAcesso.mkdir()) {
                System.out.println("Pasta ControleDeAcesso criada com sucesso.");
            } else {
                System.out.println("Falha ao criar a pasta ControleDeAcesso.");
            }
        }

        // Verifica se o arquivo bancoDeDados.txt existe, caso contrário, cria
        if (!arquivoBancoDeDados.exists()) {
            try {
                if (arquivoBancoDeDados.createNewFile()) {
                    System.out.println("Arquivo bancoDeDados.txt criado com sucesso.");
                } else {
                    System.out.println("Falha ao criar o arquivo bancoDeDados.txt.");
                }
            } catch (IOException e) {
                System.out.println("Erro ao criar arquivo bancoDeDados.txt: " + e.getMessage());
            }
        }
        if (!arquivoRegistroAcesso.exists()){
            try{
                if (arquivoRegistroAcesso.createNewFile()){
                    System.out.println("Arquivo arquivoRegistroAcesso.txt criado com sucesso");
                }else {
                    System.out.println("Falha ao criar arquivo arquivoRegistroAcesso.txt.");
                }
            } catch (IOException e){
                System.out.println("Erro ao criar arquivo arquivoRegistroAcesso.txt: "+e.getMessage());
            }
        }

        // Verifica se a pasta imagens existe, caso contrário, cria
        if (!pastaImagens.exists()) {
            if (pastaImagens.mkdir()) {
                System.out.println("Pasta imagens criada com sucesso.");
            } else {
                System.out.println("Falha ao criar a pasta imagens.");
            }
        }
    }
    private static void salvarRegistros(){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(arquivoRegistroAcesso))) {
            for (RegistroDeAcesso registroDeAcesso : listaDeRegistros) {
                writer.write(registroDeAcesso + "\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static void carregarRegistros(){
        try (BufferedReader reader = new BufferedReader(new FileReader(arquivoRegistroAcesso))) {
            String linha;

            while ((linha = reader.readLine()) != null) {
                String[] conteudo = linha.split(",");
                listaDeRegistros.add(new RegistroDeAcesso(listaUsuarios.get(Integer.parseInt(conteudo[1])), conteudo[0]));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void limparRegistros(){
        listaDeRegistros.clear();
        salvarRegistros();
        System.out.println("-----------------------Deletado com sucesso------------------------\n");
    }
}
