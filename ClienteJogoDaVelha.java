import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.ConnectException;

public class ClienteJogoDaVelha {
    private JFrame frame;
    private JButton[][] botoes = new JButton[3][3];
    private char meuSimbolo = 'O';
    private char simboloOponente = 'X';
    private boolean minhaVez = false;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JLabel statusLabel;
    private JButton reiniciarButton;
    private JPanel mainPanel; // Painel principal que contém tudo
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton enviarButton;
    private AudioManager audioManager;
    private JButton gravarAudioButton;
    private JButton pararAudioButton;
    private String servidorIP = "localhost";


    public static void main(String[] args) {
        ClienteJogoDaVelha cliente = new ClienteJogoDaVelha();
        SwingUtilities.invokeLater(() -> {
            try {
                cliente.inicializar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Adiciona shutdown hook para fechar o AudioManager
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (cliente.audioManager != null) {
                cliente.audioManager.close();
            }
        }));
    }

    private void inicializar() {
        frame = new JFrame("Jogo da Velha - Cliente (O)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);// Aumentei um pouco o tamanho
        
        // Painel principal com BorderLayout
        mainPanel = new JPanel(new BorderLayout());
        
        // Painel de status (topo)
        statusLabel = new JLabel("Conectando ao servidor...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Painel do tabuleiro (centro)
        JPanel tabuleiroPanel = new JPanel(new GridLayout(3, 3));
        tabuleiroPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Inicializa os botões do tabuleiro
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                botoes[i][j] = new JButton();
                botoes[i][j].setFont(new Font("Arial", Font.BOLD, 60));
                botoes[i][j].setFocusPainted(false);
                botoes[i][j].setEnabled(false); // Desabilitado até conectar
                botoes[i][j].addActionListener(new BotaoClickListener(i, j));
                tabuleiroPanel.add(botoes[i][j]);
            }
        }
        
        // Painel de conexão (embaixo do tabuleiro)
        JPanel conexaoPanel = new JPanel(new FlowLayout());
        JTextField ipField = new JTextField("localhost", 15);
        JButton conectarButton = new JButton("Conectar ao Servidor");
        
        conectarButton.addActionListener(e -> {
            String ip = ipField.getText();
            servidorIP = ip; // Atualiza o IP do servidor
            new Thread(() -> conectarServidor(ip)).start();
        });
        
        conexaoPanel.add(new JLabel("IP:"));
        conexaoPanel.add(ipField);
        conexaoPanel.add(conectarButton);
        
        // Botão de reiniciar (só aparece após conectar)
        reiniciarButton = new JButton("Reiniciar Jogo");
        reiniciarButton.setFont(new Font("Arial", Font.PLAIN, 16));
        reiniciarButton.addActionListener(e -> reiniciarJogo());
        reiniciarButton.setVisible(false);

        // Painel do chat
        chatArea = new JTextArea(8, 35);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        chatInput = new JTextField();
        enviarButton = new JButton("Enviar");

        enviarButton.addActionListener(e -> enviarMensagemChat());
        chatInput.addActionListener(e -> enviarMensagemChat());

        // Botões de áudio
        gravarAudioButton = new JButton("🎤 Abrir Microfone");
        pararAudioButton = new JButton("⏹️ Fechar Microfone");
        pararAudioButton.setEnabled(false);
        
        gravarAudioButton.addActionListener(e -> iniciarGravacao());
        pararAudioButton.addActionListener(e -> pararGravacao());

        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(enviarButton, BorderLayout.EAST);

        JPanel audioPanel = new JPanel(new FlowLayout());
        audioPanel.add(gravarAudioButton);
        audioPanel.add(pararAudioButton);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);
        chatPanel.add(audioPanel, BorderLayout.NORTH);

        // Adiciona o chat ao frame (ao lado ou abaixo do tabuleiro)
        frame.add(chatPanel, BorderLayout.EAST); // ou BorderLayout.SOUTH se preferir embaixo

        
        // Adiciona todos os componentes ao painel principal
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        mainPanel.add(tabuleiroPanel, BorderLayout.CENTER);
        
        // Painel inferior que alterna entre conexão e reinício
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(conexaoPanel, BorderLayout.NORTH);
        bottomPanel.add(reiniciarButton, BorderLayout.SOUTH);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void conectarServidor(String ip) {
        try {
            socket = new Socket(ip, 12345);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Conectado! Aguardando jogada do X");
                frame.setTitle("Jogo da Velha - O (Conectado)");
                
                // Habilita os botões do tabuleiro
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        botoes[i][j].setEnabled(true);
                    }
                }
                
                // Alterna os painéis inferiores
                reiniciarButton.setVisible(true);
                
                // Inicializa o AudioManager após conectar
                inicializarAudioManager();
            });
            
            new Thread(this::receberJogadas).start();
            
        } catch (ConnectException ce) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(frame, 
                "Servidor não encontrado. Certifique-se que o servidor está rodando primeiro!"));
        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> 
                JOptionPane.showMessageDialog(frame, "Erro de conexão: " + e.getMessage()));
        }
    }

    private void enviarJogada(int linha, int coluna) {
    out.println("JOGADA:" + linha + "," + coluna);
    out.flush();  // Adicionando flush para garantir envio imediato
}

private void receberJogadas() {
    try {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Cliente recebeu: " + inputLine);  // Log para depuração
            
            if (inputLine.startsWith("JOGADA:")) {
                String[] partes = inputLine.substring(7).split(",");
                final int linha = Integer.parseInt(partes[0]);
                final int coluna = Integer.parseInt(partes[1]);

                SwingUtilities.invokeLater(() -> {
                    botoes[linha][coluna].setText(String.valueOf(simboloOponente));
                    botoes[linha][coluna].setForeground(Color.BLUE);
                    minhaVez = true;
                    statusLabel.setText("Sua vez (O)");
                    verificarFimDeJogo();
                });
            }else if (inputLine.startsWith("CHAT:")) {
                String mensagem = inputLine.substring(5);
                SwingUtilities.invokeLater(() -> chatArea.append("Oponente: " + mensagem + "\n"));
            }

            // ... (outros comandos)
        }
    } catch (IOException e) {
        SwingUtilities.invokeLater(() -> 
            JOptionPane.showMessageDialog(frame, "Conexão com servidor perdida: " + e.getMessage()));
    }
}

private class BotaoClickListener implements ActionListener {
    private int linha;
    private int coluna;

    public BotaoClickListener(int linha, int coluna) {
        this.linha = linha;
        this.coluna = coluna;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (minhaVez && botoes[linha][coluna].getText().isEmpty()) {
            botoes[linha][coluna].setText(String.valueOf(meuSimbolo));
            botoes[linha][coluna].setForeground(Color.RED);
            enviarJogada(linha, coluna);  // Usando o novo método
            minhaVez = false;
            statusLabel.setText("Aguardando jogada do oponente (X)...");
            verificarFimDeJogo();
        }
    }
}

    private void verificarFimDeJogo() {
        if (verificarVitoria(meuSimbolo)) {
            JOptionPane.showMessageDialog(frame, "Você venceu!");
            desabilitarTabuleiro();
        } else if (verificarVitoria(simboloOponente)) {
            JOptionPane.showMessageDialog(frame, "Oponente venceu!");
            desabilitarTabuleiro();
        } else if (verificarEmpate()) {
            JOptionPane.showMessageDialog(frame, "Empate!");
            desabilitarTabuleiro();
        }
    }

    private boolean verificarVitoria(char simbolo) {
        // Mesma implementação do servidor
        for (int i = 0; i < 3; i++) {
            if (botoes[i][0].getText().equals(String.valueOf(simbolo)) &&
                botoes[i][1].getText().equals(String.valueOf(simbolo)) &&
                botoes[i][2].getText().equals(String.valueOf(simbolo))) {
                return true;
            }
        }

        for (int j = 0; j < 3; j++) {
            if (botoes[0][j].getText().equals(String.valueOf(simbolo)) &&
                botoes[1][j].getText().equals(String.valueOf(simbolo)) &&
                botoes[2][j].getText().equals(String.valueOf(simbolo))) {
                return true;
            }
        }

        if (botoes[0][0].getText().equals(String.valueOf(simbolo)) &&
            botoes[1][1].getText().equals(String.valueOf(simbolo)) &&
            botoes[2][2].getText().equals(String.valueOf(simbolo))) {
            return true;
        }

        if (botoes[0][2].getText().equals(String.valueOf(simbolo)) &&
            botoes[1][1].getText().equals(String.valueOf(simbolo)) &&
            botoes[2][0].getText().equals(String.valueOf(simbolo))) {
            return true;
        }

        return false;
    }

    private boolean verificarEmpate() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (botoes[i][j].getText().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private void desabilitarTabuleiro() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                botoes[i][j].setEnabled(false);
            }
        }
    }

    private void reiniciarJogo() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                botoes[i][j].setText("");
                botoes[i][j].setEnabled(true);
            }
        }
        minhaVez = (meuSimbolo == 'X'); // Só é true se for o jogador X
        statusLabel.setText(minhaVez ? "Sua vez (O)" : "Aguardando jogada do oponente (X)...");
        out.println("REINICIAR");
    }
    private void enviarMensagemChat() {
    String mensagem = chatInput.getText().trim();
    if (!mensagem.isEmpty()) {
        chatArea.append("Você: " + mensagem + "\n");
        out.println("CHAT:" + mensagem);
        chatInput.setText("");
    }
}

    private void inicializarAudioManager() {
        try {
            // Cliente usa porta 12347 para áudio, servidor usa 12346
            audioManager = new AudioManager(12347, servidorIP, 12346);
            audioManager.startPlaying(); // Cliente sempre escuta áudio do servidor
            chatArea.append("Sistema: Áudio inicializado. Pressione 'Gravar Áudio' para falar.\n");
        } catch (Exception e) {
            chatArea.append("Sistema: Erro ao inicializar áudio: " + e.getMessage() + "\n");
            e.printStackTrace();
        }
    }

    private void iniciarGravacao() {
        if (audioManager != null && !audioManager.isRecording()) {
            audioManager.startRecording();
            gravarAudioButton.setEnabled(false);
            pararAudioButton.setEnabled(true);
            chatArea.append("Sistema: Gravando áudio...\n");
        }
    }

    private void pararGravacao() {
        if (audioManager != null && audioManager.isRecording()) {
            audioManager.stopRecording();
            gravarAudioButton.setEnabled(true);
            pararAudioButton.setEnabled(false);
            chatArea.append("Sistema: Gravação parada.\n");
        }
    }
}