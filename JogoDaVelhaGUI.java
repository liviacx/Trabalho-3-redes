import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JogoDaVelhaGUI {
    private JFrame frame;
    private JButton[][] botoes = new JButton[3][3];
    private char jogadorAtual = 'X';
    private boolean jogoTerminado = false;
    private JLabel statusLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new JogoDaVelhaGUI().inicializar();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void inicializar() {
        // Configuração da janela principal
        frame = new JFrame("Jogo da Velha - 2 Jogadores");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 450);
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
        statusLabel = new JLabel("Vez do Jogador: " + jogadorAtual, JLabel.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Botão de reiniciar
        JButton reiniciarButton = new JButton("Reiniciar Jogo");
        reiniciarButton.setFont(new Font("Arial", Font.PLAIN, 16));
        reiniciarButton.addActionListener(e -> reiniciarJogo());

        // Adiciona componentes à janela
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(tabuleiroPanel, BorderLayout.CENTER);
        frame.add(reiniciarButton, BorderLayout.SOUTH);

        frame.setVisible(true);
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
            if (!jogoTerminado && botoes[linha][coluna].getText().isEmpty()) {
                // Faz a jogada
                botoes[linha][coluna].setText(String.valueOf(jogadorAtual));
                botoes[linha][coluna].setForeground(jogadorAtual == 'X' ? Color.BLUE : Color.RED);

                // Verifica se houve vencedor
                if (verificarVitoria(linha, coluna)) {
                    statusLabel.setText("Jogador " + jogadorAtual + " venceu!");
                    jogoTerminado = true;
                    destacarVitoria();
                } else if (verificarEmpate()) {
                    statusLabel.setText("Empate!");
                    jogoTerminado = true;
                } else {
                    // Alterna jogador
                    jogadorAtual = (jogadorAtual == 'X') ? 'O' : 'X';
                    statusLabel.setText("Vez do Jogador: " + jogadorAtual);
                }
            }
        }
    }

    private boolean verificarVitoria(int linha, int coluna) {
        // Verifica linha
        if (botoes[linha][0].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[linha][1].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[linha][2].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        // Verifica coluna
        if (botoes[0][coluna].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[1][coluna].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[2][coluna].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        // Verifica diagonal principal
        if (linha == coluna &&
            botoes[0][0].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[1][1].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[2][2].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        // Verifica diagonal secundária
        if (linha + coluna == 2 &&
            botoes[0][2].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[1][1].getText().equals(String.valueOf(jogadorAtual)) &&
            botoes[2][0].getText().equals(String.valueOf(jogadorAtual))) {
            return true;
        }

        return false;
    }

    private void destacarVitoria() {
        // Encontra e destaca a linha, coluna ou diagonal vencedora
        // (Implementação mais completa seria necessária para destacar exatamente as células vencedoras)
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (botoes[i][j].getText().equals(String.valueOf(jogadorAtual))) {
                    botoes[i][j].setBackground(Color.GREEN);
                }
            }
        }
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

    private void reiniciarJogo() {
        jogadorAtual = 'X';
        jogoTerminado = false;
        statusLabel.setText("Vez do Jogador: " + jogadorAtual);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                botoes[i][j].setText("");
                botoes[i][j].setBackground(null);
                botoes[i][j].setEnabled(true);
            }
        }
    }
}