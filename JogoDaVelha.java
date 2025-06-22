import java.util.Scanner;

public class JogoDaVelha {
    private static char[][] tabuleiro = new char[3][3];
    private static char jogadorAtual = 'X';
    private static boolean jogoTerminado = false;

    public static void main(String[] args) {
        inicializarTabuleiro();
        exibirTabuleiro();

        Scanner scanner = new Scanner(System.in);

        while (!jogoTerminado) {
            System.out.println("Jogador " + jogadorAtual + ", é sua vez.");
            System.out.print("Digite a linha (1-3): ");
            int linha = scanner.nextInt() - 1;
            System.out.print("Digite a coluna (1-3): ");
            int coluna = scanner.nextInt() - 1;

            if (movimentoValido(linha, coluna)) {
                tabuleiro[linha][coluna] = jogadorAtual;
                exibirTabuleiro();
                
                if (verificarVitoria()) {
                    System.out.println("Jogador " + jogadorAtual + " venceu!");
                    jogoTerminado = true;
                } else if (verificarEmpate()) {
                    System.out.println("O jogo terminou em empate!");
                    jogoTerminado = true;
                } else {
                    jogadorAtual = (jogadorAtual == 'X') ? 'O' : 'X';
                }
            } else {
                System.out.println("Movimento inválido. Tente novamente.");
            }
        }
        
        scanner.close();
    }

    private static void inicializarTabuleiro() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                tabuleiro[i][j] = '-';
            }
        }
    }

    private static void exibirTabuleiro() {
        System.out.println("Tabuleiro:");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                System.out.print(tabuleiro[i][j] + " ");
            }
            System.out.println();
        }
    }

    private static boolean movimentoValido(int linha, int coluna) {
        if (linha < 0 || linha > 2 || coluna < 0 || coluna > 2) {
            return false;
        }
        return tabuleiro[linha][coluna] == '-';
    }

    private static boolean verificarVitoria() {
        // Verificar linhas
        for (int i = 0; i < 3; i++) {
            if (tabuleiro[i][0] == jogadorAtual && tabuleiro[i][1] == jogadorAtual && tabuleiro[i][2] == jogadorAtual) {
                return true;
            }
        }

        // Verificar colunas
        for (int j = 0; j < 3; j++) {
            if (tabuleiro[0][j] == jogadorAtual && tabuleiro[1][j] == jogadorAtual && tabuleiro[2][j] == jogadorAtual) {
                return true;
            }
        }

        // Verificar diagonais
        if (tabuleiro[0][0] == jogadorAtual && tabuleiro[1][1] == jogadorAtual && tabuleiro[2][2] == jogadorAtual) {
            return true;
        }
        if (tabuleiro[0][2] == jogadorAtual && tabuleiro[1][1] == jogadorAtual && tabuleiro[2][0] == jogadorAtual) {
            return true;
        }

        return false;
    }

    private static boolean verificarEmpate() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (tabuleiro[i][j] == '-') {
                    return false;
                }
            }
        }
        return true;
    }
}