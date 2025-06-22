# Jogo da Velha com Chat de Áudio

Este projeto implementa um jogo da velha multiplayer com funcionalidade de chat de texto e áudio.

## Funcionalidades

- **Jogo da Velha**: Jogo tradicional 3x3 com interface gráfica
- **Chat de Texto**: Troca de mensagens em tempo real
- **Chat de Áudio**: Comunicação por voz via UDP
- **Conexão TCP**: Para o jogo e chat de texto
- **Conexão UDP**: Para transmissão de áudio


## Funcionalidade de Áudio

### Configuração
- **Servidor**: Escuta na porta 12346, envia para porta 12347
- **Cliente**: Escuta na porta 12347, envia para porta 12346

### Como Usar
1. **Conectar**: Primeiro conecte o cliente ao servidor
2. **Inicialização**: O áudio é inicializado automaticamente após a conexão
3. **Gravar**: Clique no botão "🎤 Abrir Áudio" para começar a falar
4. **Parar**: Clique no botão "⏹️ Fechar Áudio" para parar a gravação
5. **Ouvir**: O áudio do oponente é reproduzido automaticamente

### Requisitos de Hardware
- **Microfone**: Para fazer a comunicação de voz
- **Alto-falantes/Fones**: Para ouvir o áudio do oponente
- **Placa de som**: Compatível com Java Sound API

## Estrutura do Projeto

- `ServidorJogoDaVelha.java`: Servidor do jogo (Jogador X)
- `ClienteJogoDaVelha.java`: Cliente do jogo (Jogador O)
- `AudioManager.java`: Gerenciador de áudio via UDP

## Protocolo de Comunicação

### TCP (Jogo e Chat de Texto)
- `JOGADA:linha,coluna`: Envia jogada
- `CHAT:mensagem`: Envia mensagem de chat
- `REINICIAR`: Reinicia o jogo

### UDP (Áudio)
- Pacotes de áudio brutos são enviados continuamente durante a fala
- Formato: PCM 16-bit, 44.1kHz, Mono
