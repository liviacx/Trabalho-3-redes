import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


public class ServidorJogoDaVelha {
    private JFrame frame;
    private JButton[][] botoes = new JButton[3][3];
    private char meuSimbolo = 'X';
    private char simboloOponente = 'O';
    private boolean minhaVez = true;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JLabel statusLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JButton enviarButton;
    private AudioManager audioManager;
    private JButton gravarAudioButton;
    private JButton pararAudioButton;
    private String clienteIP = "localhost"; // Será atualizado quando o cliente conectar


    public static void main(String[] args) {
        ServidorJogoDaVelha servidor = new ServidorJogoDaVelha();
        SwingUtilities.invokeLater(() -> {
            try {
                servidor.inicializar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        
        // Adiciona shutdown hook para fechar o AudioManager
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (servidor.audioManager != null) {
                servidor.audioManager.close();
            }
        }));
    }

    private void inicializar() {
        // Configuração da janela principal
            new Thread(() -> {
                try {
                    ServerSocket serverSocket = new ServerSocket(12345);
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("Servidor aguardando conexão..."));

                    socket = serverSocket.accept();
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    // Atualiza o IP do cliente
                    clienteIP = socket.getInetAddress().getHostAddress();

                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setText("Conectado ao jogador O! Sua vez (X)");
                        frame.setTitle("Jogo da Velha - X (Conectado)");
                        // Inicializa o AudioManager após conectar
                        inicializarAudioManager();
                    });

                    new Thread(this::receberJogadas).start();

                } catch (IOException e) {
                    SwingUtilities.invokeLater(() -> 
                        statusLabel.setText("Erro no servidor: " + e.getMessage()));
                }
            }).start();
        frame = new JFrame("Jogo da Velha - Servidor (X)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // Painel do tabuleiro
        JPanel tabuleiroPanel = new JPanel(new GridLayout(3, 3));
        tabuleiroPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Inicializa os botões
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                botoes[i][j] = new JButton();
                botoes[i][j].setFont(new Font("Arial", Font.BOLD, 60));
                botoes[i][j].setFocusPainted(false);
                botoes[i][j].addActionListener(new BotaoClickListener(i, j));
                tabuleiroPanel.add(botoes[i][j]);
            }
        }

        // Painel de status
        statusLabel = new JLabel("Aguardando conexão do jogador O...", JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botão de reiniciar
        JButton reiniciarButton = new JButton("Reiniciar Jogo");
        reiniciarButton.setFont(new Font("Arial", Font.PLAIN, 16));
        reiniciarButton.addActionListener(e -> reiniciarJogo());

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
        gravarAudioButton = new JButton("🎤 Abrir Áudio");
        pararAudioButton = new JButton("⏹️ Fechar Áudio");
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


        // Adiciona componentes à janela
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(tabuleiroPanel, BorderLayout.CENTER);
        frame.add(reiniciarButton, BorderLayout.SOUTH);

        frame.setVisible(true);

    }

   private void enviarJogada(int linha, int coluna) {
    out.println("JOGADA:" + linha + "," + coluna);
    out.flush();  // Adicionando flush para garantir envio imediato
}

private void receberJogadas() {
    try {
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            System.out.println("Servidor recebeu: " + inputLine);  // Log para depuração
            
            if (inputLine.startsWith("JOGADA:")) {
                String[] partes = inputLine.substring(7).split(",");
                final int linha = Integer.parseInt(partes[0]);
                final int coluna = Integer.parseInt(partes[1]);

                SwingUtilities.invokeLater(() -> {
                    botoes[linha][coluna].setText(String.valueOf(simboloOponente));
                    botoes[linha][coluna].setForeground(Color.RED);
                    minhaVez = true;
                    statusLabel.setText("Sua vez (X)");
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
            JOptionPane.showMessageDialog(frame, "Conexão com cliente perdida: " + e.getMessage()));
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
            botoes[linha][coluna].setForeground(Color.BLUE);
            enviarJogada(linha, coluna);  // Usando o novo método
            minhaVez = false;
            statusLabel.setText("Aguardando jogada do oponente (O)...");
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
        // Verifica linhas
        for (int i = 0; i < 3; i++) {
            if (botoes[i][0].getText().equals(String.valueOf(simbolo)) &&
                botoes[i][1].getText().equals(String.valueOf(simbolo)) &&
                botoes[i][2].getText().equals(String.valueOf(simbolo))) {
                return true;
            }
        }

        // Verifica colunas
        for (int j = 0; j < 3; j++) {
            if (botoes[0][j].getText().equals(String.valueOf(simbolo)) &&
                botoes[1][j].getText().equals(String.valueOf(simbolo)) &&
                botoes[2][j].getText().equals(String.valueOf(simbolo))) {
                return true;
            }
        }

        // Verifica diagonais
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
        minhaVez = true;
        statusLabel.setText("Sua vez (X)");
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
            // Servidor usa porta 12346 para áudio, cliente usa 12347
            audioManager = new AudioManager(12346, clienteIP, 12347);
            audioManager.startPlaying(); // Servidor sempre escuta áudio do cliente
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
