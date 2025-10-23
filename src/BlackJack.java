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
    
    /**
     * Broadcast envia para TODOS os jogadores conectados,
     */
    private void broadcastMessage(String message, String excludeNickname) {
        if (connection == null) return;
        
        // Pega TODOS os jogadores, incluindo os do lobby (espectadores)
        Map<String, Socket> allPlayers = connection.getPlayers(); 
        
        for (String nickname : allPlayers.keySet()) {
            if (!nickname.equals(excludeNickname)) {
                try {
                    PrintWriter out = connection.getPlayerOutput(nickname);
                    if (out != null) {
                        out.println(message);
                        out.flush(); // Garante o envio
                    }
                } catch (Exception e) {
                    // Ignora, o jogador pode ter desconectado
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
                
                // O "Snapshot" dos jogadores que VÃO JOGAR
                Map<String, Socket> playersForThisRound = new HashMap<>(allPlayersInLobby);
                
                connection.setGameInProgress(true);
                System.out.println("Rodada Iniciando com " + playersForThisRound.size() + " jogadores...\n");

                //#1 ciclo: Collecting bets
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
                            outToClient.println("Seu saldo é 0. Voce perdeu.");
                            connection.removePlayer(nickname);
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
                    } catch (NumberFormatException e) {
                        System.out.println("Jogador " + nickname + " enviou aposta inválida (Ciclo 1).");
                        connection.removePlayer(nickname);
                    }
                }
                
                if (playersForThisRound.isEmpty()) { 
                    connection.setGameInProgress(false);
                    continue;
                }

                //#2 ciclo: dealing the cards
                dealer.clearHand();
                dealer.getCard(new Carta(baralho.retirarcarta()));
                dealer.getCard(new Carta(baralho.retirarcarta()));
                
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        outToClient.println("aa1");
                        String resposta_jogador = entrada1.readLine(); // Recebe "true" do ServerListener
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
                    }
                }

                //#3 ciclo: Deciding the course of action
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    
                    broadcastMessage("WAIT_PLAYER_TURN:" + nickname, nickname);

                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        outToClient.println("aa2");
                        String resposta_jogador = entrada1.readLine(); // Recebe "true" do ServerListener

                        if (resposta_jogador.equals("true")) {
                            boolean playerTurnActive = true;
                            boolean firstAction = true;
                            while (playerTurnActive) {
                                // ... (lógica do hit/stand/double loop) ...
                                Player currentPlayer = playerObjects.get(nickname);
                                int currentAmount = amount.get(nickname);
                                int currentBet = bets.get(nickname);
                                String prompt;
                                if (firstAction) {
                                    if ((currentBet * 2) <= currentAmount) { prompt = nickname + ", voce tem tres opcoes: stand, hit ou double"; }
                                    else { prompt = nickname + ", voce tem duas opcoes: stand ou hit (sem saldo para double)"; }
                                } else { prompt = nickname + ", sua vez: stand ou hit"; }
                                outToClient.println(prompt);
                                resposta_jogador = entrada1.readLine(); // Recebe a ação da Main thread do cliente
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

                //#4 ciclo: Determining who wins
                broadcastMessage("DEALER_TURN:Vez do Dealer...", "none");
                int pontos_do_dealer = dealer.calcularPontos();
                while (pontos_do_dealer < 17) {
                    dealer.getCard(new Carta(baralho.retirarcarta()));
                    pontos_do_dealer = dealer.calcularPontos();
                }
                broadcastMessage("DEALER_HAND:Mão final do Dealer: " + dealer.showHand() + " (" + pontos_do_dealer + " pontos)", "none");
                
                for (Map.Entry<String, Socket> entrada : playersForThisRound.entrySet()) {
                    String nickname = entrada.getKey();
                    try {
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        outToClient.println("aa3");
                        String resposta_jogador = entrada1.readLine(); // Recebe "true" do ServerListener
                        if (resposta_jogador.equals("true")) {
                            Player currentPlayer = playerObjects.get(nickname);
                            int pontos_jogador = currentPlayer.calcularPontos();
                            if (pontos_jogador > 21) continue;
                            outToClient.println("Cartas do Dealer: " + dealer.showHand() + " | Pontos do Dealer: " + pontos_do_dealer);
                            outToClient.println(Integer.toString(pontos_do_dealer));
                            String resultadoFinal;
                            if (pontos_do_dealer > 21) {
                                connection.setAmount(nickname, amount.get(nickname) + bets.get(nickname));
                                resultadoFinal = "Dealer estourou! Voce venceu " + bets.get(nickname) + "!";
                            } else if (pontos_jogador > pontos_do_dealer) {
                                connection.setAmount(nickname, amount.get(nickname) + bets.get(nickname));
                                resultadoFinal = "Voce venceu " + bets.get(nickname) + "!";
                            } else if (pontos_jogador < pontos_do_dealer) {
                                connection.setAmount(nickname, amount.get(nickname) - bets.get(nickname));
                                resultadoFinal = "Voce perdeu " + bets.get(nickname) + ".";
                            } else {
                                resultadoFinal = "Empate (Push)! Sua aposta foi devolvida.";
                            }
                            outToClient.println(resultadoFinal);
                            System.out.println("Resultado para " + nickname + ": " + resultadoFinal);
                        }
                    } catch (IOException e) {
                        System.out.println("Jogador " + nickname + " desconectou (Ciclo 4).");
                        connection.removePlayer(nickname);
                    }
                }
                
                connection.setGameInProgress(false);
                System.out.println("Rodada finalizada. Lobby aberto.");

                //#5 ciclo: Perguntar para sair
                ArrayList<String> playersToRemove = new ArrayList<>();
                
                // O Ciclo 5 só deve perguntar para quem JOGOU a rodada
                for (String nickname : playersForThisRound.keySet()) {
                    Socket socket_jogador = playersForThisRound.get(nickname);
                    if (socket_jogador == null || socket_jogador.isClosed() || connection.getPlayerSocket(nickname) == null) {
                        playersToRemove.add(nickname);
                        continue;
                    }
                    try {
                        PrintWriter outToClient = connection.getPlayerOutput(nickname);
                        BufferedReader entrada1 = connection.getPlayerInput(nickname);
                        if(outToClient == null || entrada1 == null) {
                            playersToRemove.add(nickname);
                            continue;
                        }
                        socket_jogador.setSoTimeout(10000);
                        outToClient.println("aa_EXIT_PROMPT");
                        outToClient.println("Deseja continuar? (s/n) Você tem 10 segundos para responder (padrão=sair).");
                        String resposta = entrada1.readLine(); // Recebe "s" ou "n" da Main thread do cliente
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