# Jogo da Velha com Chat de Áudio

Este projeto implementa um jogo da velha multiplayer com funcionalidade de chat de texto e áudio via UDP.

## Funcionalidades

- **Jogo da Velha**: Jogo tradicional 3x3 com interface gráfica
- **Chat de Texto**: Troca de mensagens em tempo real
- **Chat de Áudio**: Comunicação por voz via UDP
- **Conexão TCP**: Para o jogo e chat de texto
- **Conexão UDP**: Para transmissão de áudio

## Como Executar

### 1. Compilar os arquivos
```bash
javac *.java
```

### 2. Executar o Servidor (Jogador X)
```bash
java ServidorJogoDaVelha
```
O servidor aguardará conexões na porta 12345 (TCP) e 12346 (UDP para áudio).

### 3. Executar o Cliente (Jogador O)
```bash
java ClienteJogoDaVelha
```
O cliente se conectará ao servidor e usará a porta 12347 (UDP para áudio).

## Funcionalidade de Áudio

### Configuração
- **Servidor**: Escuta na porta 12346, envia para porta 12347
- **Cliente**: Escuta na porta 12347, envia para porta 12346

### Como Usar
1. **Conectar**: Primeiro conecte o cliente ao servidor
2. **Inicialização**: O áudio é inicializado automaticamente após a conexão
3. **Gravar**: Clique no botão "🎤 Gravar Áudio" para começar a falar
4. **Parar**: Clique no botão "⏹️ Parar Áudio" para parar a gravação
5. **Ouvir**: O áudio do oponente é reproduzido automaticamente

### Requisitos de Hardware
- **Microfone**: Para gravar áudio
- **Alto-falantes/Fones**: Para ouvir o áudio do oponente
- **Placa de som**: Compatível com Java Sound API

## Estrutura do Projeto

- `ServidorJogoDaVelha.java`: Servidor do jogo (Jogador X)
- `ClienteJogoDaVelha.java`: Cliente do jogo (Jogador O)
- `AudioManager.java`: Gerenciador de áudio via UDP
- `JogoDaVelha.java`: Lógica do jogo (não usado na versão atual)
- `JogoDaVelhaGUI.java`: Interface gráfica (não usado na versão atual)

## Protocolo de Comunicação

### TCP (Jogo e Chat de Texto)
- `JOGADA:linha,coluna`: Envia jogada
- `CHAT:mensagem`: Envia mensagem de chat
- `REINICIAR`: Reinicia o jogo

### UDP (Áudio)
- Pacotes de áudio brutos são enviados continuamente durante a gravação
- Formato: PCM 16-bit, 44.1kHz, Mono

## Solução de Problemas

### Áudio não funciona
1. Verifique se o microfone está conectado e funcionando
2. Verifique se os alto-falantes/fones estão funcionando
3. Verifique se o Java tem permissão para acessar o áudio
4. Verifique se as portas UDP não estão bloqueadas pelo firewall

### Conexão falha
1. Certifique-se de que o servidor está rodando primeiro
2. Verifique se a porta 12345 não está sendo usada por outro programa
3. Verifique se o firewall não está bloqueando as conexões

### Qualidade do áudio
- O áudio é transmitido em tempo real sem compressão
- A qualidade depende da conexão de rede
- Para melhor qualidade, use uma conexão local ou com baixa latência

## Notas Técnicas

- **Latência**: O áudio UDP pode ter latência variável
- **Qualidade**: Sem compressão para minimizar latência
- **Compatibilidade**: Funciona em Windows, Linux e macOS
- **Java Sound API**: Utilizada para captura e reprodução de áudio 