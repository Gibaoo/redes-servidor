import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlackJack extends Thread {

    private Connection connection;
    private HashMap<String, Integer> bets = new HashMap<>();
    private HashMap<String, Player> playerObjects = new HashMap<>();
    private Dealer dealer;

    public BlackJack(Connection connection) {
        this.connection = connection;
    }
    
    // O método broadcastMessage permanece o mesmo
    private void broadcastMessage(String message, String excludeNickname) {
        if (connection == null) return;
        Map<String, Socket> allPlayers = connection.getPlayers(); 
        
        for (String nickname : allPlayers.keySet()) {
            if (!nickname.equals(excludeNickname)) {
                try {
                    PrintWriter out = connection.getPlayerOutput(nickname);
                    if (out != null) {
                        out.println(message);
                        out.flush();
                    }
                } catch (Exception e) {
                    // Ignora
                }
            }
        }
    }


    public void run() {
        while (true) {
            Map<String, Socket> allPlayersInLobby = null;
            Map<String, Integer> amount = null;
            
            Baralho baralho = new Baralho();
            dealer = new Dealer();
            playerObjects.clear();
            bets.clear();

            if (connection != null) {
                allPlayersInLobby = connection.getPlayers();
                amount = connection.getAmount();
            }

            if (allPlayersInLobby == null || allPlayersInLobby.size() == 0) {
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
                continue;
            } else {
                
                Map<String, Socket> playersForThisRound = new HashMap<>(allPlayersInLobby);
                
                connection.setGameInProgress(true);
                System.out.println("Rodada Iniciando com " + playersForThisRound.size() + " jogadores...\n");

                //#1 ciclo: Collecting bets
                // ... (Ciclo 1 permanece o mesmo) ...
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        if (entrada1 == null || outToClient == null) continue; 
                        playerObjects.put(nickname, new Player(nickname));
                        outToClient.println("aa0");
                        outToClient.println(amount.get(nickname).toString());
                        if (amount.get(nickname) == 0) {
                            outToClient.println("Seu saldo é 0. Voce nao pode mais jogar.");
                            connection.removePlayer(nickname);
                            broadcastMessage("PLAYER_ACTION:" + nickname + " foi removido (saldo 0).", nickname);
                        } else {
                            outToClient.println(nickname + ", quanto você gostaria de apostar (somente números)? ");
                            outToClient.println("Saldos: " + amount.toString());
                            outToClient.println(amount.get(nickname).toString());
                            String resposta_jogador = entrada1.readLine();
                            bets.put(nickname, Integer.parseInt(resposta_jogador));
                            outToClient.println("Voce apostou: " + resposta_jogador);
                        }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 1).");
                        connection.removePlayer(nickname);
                        broadcastMessage("PLAYER_ACTION:" + nickname + " desconectou.", nickname);
                    } catch (NumberFormatException e) {
                        System.out.println("Jogador " + nickname + " enviou aposta inválida (Ciclo 1).");
                        connection.removePlayer(nickname);
                        broadcastMessage("PLAYER_ACTION:" + nickname + " foi desconectado (aposta inválida).", nickname);
                    }
                }
                playersForThisRound.keySet().retainAll(connection.getPlayers().keySet());
                if (playersForThisRound.isEmpty()) { 
                    connection.setGameInProgress(false);
                    continue;
                }

                //#2 ciclo: dealing the cards
                // ... (Ciclo 2 permanece o mesmo) ...
                dealer.clearHand();
                dealer.getCard(new Carta(baralho.retirarcarta()));
                dealer.getCard(new Carta(baralho.retirarcarta()));
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        if (entrada1 == null || outToClient == null) continue;
                        outToClient.println("aa1");
                        String resposta_jogador = entrada1.readLine();
                        if (resposta_jogador.equals("true")) {
                            outToClient.println(dealer.getHand().get(0).toString());
                            Player currentPlayer = playerObjects.get(nickname);
                            Integer carta_1_id = baralho.retirarcarta();
                            currentPlayer.getCard(new Carta(carta_1_id));
                            outToClient.println(carta_1_id.toString());
                            Integer carta_2_id = baralho.retirarcarta();
                            currentPlayer.getCard(new Carta(carta_2_id));
                            outToClient.println(carta_2_id.toString());
                            System.out.println("Mao do jogador " + nickname + ": " + currentPlayer.showHand());
                        }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 2).");
                        connection.removePlayer(nickname);
                        broadcastMessage("PLAYER_ACTION:" + nickname + " desconectou.", nickname);
                    }
                }

                //#3 ciclo: Deciding the course of action
                // ... (Ciclo 3 permanece o mesmo) ...
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    broadcastMessage("WAIT_PLAYER_TURN:" + nickname, nickname);
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        if (entrada1 == null || outToClient == null) continue;
                        outToClient.println("aa2");
                        String resposta_jogador = entrada1.readLine();
                        if (resposta_jogador.equals("true")) {
                            boolean playerTurnActive = true;
                            boolean firstAction = true;
                            while (playerTurnActive) {
                                Player currentPlayer = playerObjects.get(nickname);
                                int currentAmount = amount.get(nickname);
                                int currentBet = bets.get(nickname);
                                String prompt;
                                if (firstAction) {
                                    if ((currentBet * 2) <= currentAmount) { prompt = nickname + ", voce tem tres opcoes: stand, hit ou double"; }
                                    else { prompt = nickname + ", voce tem duas opcoes: stand ou hit (sem saldo para double)"; }
                                } else { prompt = nickname + ", sua vez: stand ou hit"; }
                                outToClient.println(prompt);
                                resposta_jogador = entrada1.readLine();
                                String broadcastResult = "";
                                if (resposta_jogador.equals("stand")) {
                                    playerTurnActive = false;
                                    outToClient.println("STAND_CONFIRMED");
                                    broadcastResult = "PLAYER_ACTION:" + nickname + " parou (stand). Mão: " + currentPlayer.showHand();
                                } else if (resposta_jogador.equals("hit")) {
                                    firstAction = false;
                                    Integer carta_id = baralho.retirarcarta();
                                    currentPlayer.getCard(new Carta(carta_id));
                                    int pontos_jogador = currentPlayer.calcularPontos();
                                    if (pontos_jogador > 21) {
                                        playerTurnActive = false;
                                        connection.setAmount(nickname, currentAmount - currentBet);
                                        outToClient.println("BUST:" + carta_id);
                                        broadcastResult = "PLAYER_ACTION:" + nickname + " ESTOUROU! Mão: " + currentPlayer.showHand();
                                    } else {
                                        outToClient.println("HIT_SUCCESS:" + carta_id);
                                        broadcastResult = "PLAYER_ACTION:" + nickname + " pediu 'hit'. Mão atual: " + currentPlayer.showHand();
                                    }
                                } else if (resposta_jogador.equals("double") && firstAction) {
                                    firstAction = false;
                                    playerTurnActive = false;
                                    if ((currentBet * 2) > currentAmount) {
                                        playerTurnActive = true;
                                        firstAction = true;
                                        outToClient.println("DOUBLE_REJECTED:Saldo insuficiente");
                                    } else {
                                        currentBet *= 2;
                                        bets.put(nickname, currentBet);
                                        Integer carta_id = baralho.retirarcarta();
                                        currentPlayer.getCard(new Carta(carta_id));
                                        int pontos_jogador = currentPlayer.calcularPontos();
                                        if (pontos_jogador > 21) {
                                            connection.setAmount(nickname, currentAmount - currentBet);
                                            outToClient.println("BUST:" + carta_id);
                                            broadcastResult = "PLAYER_ACTION:" + nickname + " dobrou e ESTOUROU! Mão: " + currentPlayer.showHand();
                                        } else {
                                            outToClient.println("DOUBLE_SUCCESS:" + carta_id);
                                            broadcastResult = "PLAYER_ACTION:" + nickname + " dobrou. Mão: " + currentPlayer.showHand();
                                        }
                                    }
                                } else {
                                    outToClient.println("INVALID_INPUT:Tente 'hit', 'stand' ou 'double'.");
                                }
                                if (!broadcastResult.isEmpty()) {
                                    broadcastMessage(broadcastResult, nickname);
                                }
                            }
                        }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 3).");
                        connection.removePlayer(nickname);
                        broadcastMessage("PLAYER_ACTION:" + nickname + " desconectou.", nickname);
                    }
                }

                // --- MUDANÇA: CICLO #4 REESCRITO ---
                
                // 1. Encontrar o "Alvo a Bater" e checar se todos estouraram
                int targetScore = 17; // Regra base: parar em 17
                boolean allPlayersBust = true; // Assumimos que todos estouraram

                for (String nickname : playersForThisRound.keySet()) {
                    Player p = playerObjects.get(nickname);
                    if (p == null) continue;
                    
                    int playerScore = p.calcularPontos();
                    
                    if (playerScore <= 21) {
                        allPlayersBust = false; // Encontramos um jogador que não estourou
                        if (playerScore > targetScore) {
                            targetScore = playerScore; // Esse é o novo alvo
                        }
                    }
                }
                
                System.out.println("Vez do Dealer." + targetScore);
                broadcastMessage("DEALER_TURN:Vez do Dealer...", "none");
                
                int pontos_do_dealer = dealer.calcularPontos();
                
                // 2. Loop do Dealer (SÓ JOGA SE HOUVER ALGUÉM PARA VENCER)
                if (!allPlayersBust) {
                    System.out.println("Pelo menos um jogador está ativo. Dealer joga.");
                    while (pontos_do_dealer < targetScore) { 
                        
                        dealer.getCard(new Carta(baralho.retirarcarta()));
                        pontos_do_dealer = dealer.calcularPontos();
                        
                        String maoDealer = dealer.showHand() + " (" + pontos_do_dealer + " pontos)";
                        broadcastMessage("DEALER_ACTION:Dealer puxou uma carta. Mão: " + maoDealer, "none");
                        
                        if (pontos_do_dealer > 21) {
                            break;
                        }
                    }
                } else {
                    // Se todos estouraram, o Dealer não faz nada
                    System.out.println("Todos os jogadores estouraram. Dealer não puxa cartas.");
                    broadcastMessage("DEALER_ACTION:Todos os jogadores estouraram. Dealer revela a mão.", "none");
                }
                
                // 3. Anunciar a mão final
                broadcastMessage("DEALER_HAND:Mão final do Dealer: " + dealer.showHand() + " (" + pontos_do_dealer + " pontos)", "none");
                
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        if (entrada1 == null || outToClient == null) continue;

                        outToClient.println("aa3");
                        String resposta_jogador = entrada1.readLine();
                        if (resposta_jogador.equals("true")) {
                            Player currentPlayer = playerObjects.get(nickname);
                            int pontos_jogador = currentPlayer.calcularPontos();
                            
                            // A LÓGICA DE PAGAMENTO JÁ ESTÁ CORRETA
                            // Se o jogador estourou (pontos > 21), ele pula o 'if' e não é pago
                            if (pontos_jogador > 21) continue;
                            
                            outToClient.println("Cartas do Dealer: " + dealer.showHand() + " | Pontos do Dealer: " + pontos_do_dealer);
                            outToClient.println(Integer.toString(pontos_do_dealer));
                            
                            String resultadoFinal;
                            if (pontos_do_dealer > 21) {
                                connection.setAmount(nickname, amount.get(nickname) + bets.get(nickname));
                                resultadoFinal = "Dealer estourou! Voce venceu e ganhou " + bets.get(nickname) + " balestretas!";
                            } else if (pontos_jogador > pontos_do_dealer) {
                                connection.setAmount(nickname, amount.get(nickname) + bets.get(nickname));
                                resultadoFinal = "Voce venceu e ganhou " + bets.get(nickname) + " balestretas!";
                            } else if (pontos_jogador < pontos_do_dealer) {
                                connection.setAmount(nickname, amount.get(nickname) - bets.get(nickname));
                                resultadoFinal = "Voce perdeu e se foram " + bets.get(nickname) + " balestretas.";
                            } else {
                                resultadoFinal = "Empate (Push)! Sua aposta foi devolvida.";
                            }
                            outToClient.println(resultadoFinal);
                            System.out.println("Resultado para " + nickname + ": " + resultadoFinal);
                        }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 4).");
                        connection.removePlayer(nickname);
                        broadcastMessage("PLAYER_ACTION:" + nickname + " desconectou.", nickname);
                    }
                }
                // --- FIM DA MUDANÇA DO CICLO #4 ---
                
                
                connection.setGameInProgress(false);
                System.out.println("Rodada finalizada. Lobby aberto.");

                //#5 ciclo: Perguntar para sair
                // ... (Ciclo 5 permanece o mesmo) ...
                ArrayList<String> playersToRemove = new ArrayList<>();
                for (String nickname : playersForThisRound.keySet()) {
                    Socket socket_jogador = playersForThisRound.get(nickname);
                    if (socket_jogador == null || socket_jogador.isClosed() || connection.getPlayerSocket(nickname) == null) {
                        continue;
                    }
                    try {
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        if(outToClient == null || entrada1 == null) continue;
                        socket_jogador.setSoTimeout(10000);
                        outToClient.println("aa_EXIT_PROMPT");
                        outToClient.println("Deseja continuar? (s/n) Você tem 10 segundos para responder (padrão=sair).");
                        String resposta = entrada1.readLine();
                        if (resposta == null || !resposta.equalsIgnoreCase("s")) {
                            playersToRemove.add(nickname);
                            outToClient.println("SAINDO");
                        } else {
                            outToClient.println("OK. Próxima rodada em 10 segundos...");
                        }
                    } catch (SocketTimeoutException e) {
                        System.out.println("Jogador " + nickname + " não respondeu a tempo (Ciclo 5). Removendo.");
                        playersToRemove.add(nickname);
                        try {
                            PrintWriter outToClient = connection.getPlayerOutput(nickname);
                            if (outToClient != null) {
                                outToClient.println("Você foi desconectado por inatividade.");
                            }
                        } catch (Exception innerE) { /* Ignora */ }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 5).");
                        playersToRemove.add(nickname);
                    } finally {
                        try {
                            if (socket_jogador != null && !socket_jogador.isClosed()) {
                                socket_jogador.setSoTimeout(0);
                            }
                        } catch (IOException e) { /* Ignora */ }
                    }
                }
                for (String nickname : playersToRemove) {
                    connection.removePlayer(nickname);
                    broadcastMessage("PLAYER_ACTION:" + nickname + " saiu do jogo.", nickname);
                }
                try {
                    System.out.println("Aguardando 10 segundos para a próxima rodada...");
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    e.printStackTrace();
                }
                System.out.println("----------------------------------\n");
            }
        }
    }
}