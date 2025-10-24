import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Connection2 extends Thread {

    private ServerSocket servidorSocket;
    private ConcurrentHashMap<String, Socket> players;
    private ConcurrentHashMap<String, Integer> amount;
    private static final Map<String, String> username = new HashMap<>();
    private static final Map<String, String> fichas = new HashMap<>();
    private final ObjectMapper map = new ObjectMapper();
    private ConcurrentHashMap<String, BufferedReader> playerInputs;
    private ConcurrentHashMap<String, PrintWriter> playerOutputs;

    private volatile boolean isGameInProgress = false;
    private int num;

    public Map<String, Socket> getPlayers() {
        return players;
    }

    public ServerSocket getServidorSocket() {
        return servidorSocket;
    }

    public Map<String, Integer> getAmount() {
        return amount;
    }

    //Melhorar mensagens de status
    public synchronized boolean isGameInProgress() {
        return isGameInProgress;
    }
    public synchronized void setGameInProgress(boolean status) {
        this.isGameInProgress = status;
    }

//    public BufferedReader getPlayerInput(String nickname) {
//        return playerInputs.get(nickname);
//    }
//    public PrintWriter getPlayerOutput(String nickname) {
//        return playerOutputs.get(nickname);
//    }
//    public synchronized void removePlayer(String nickname) {
//        Socket s = players.remove(nickname);
//        if (s != null) {
//            try { s.close(); } catch (IOException e) { /* ignora */ }
//        }
//        amount.remove(nickname);
//        playerInputs.remove(nickname);
//        playerOutputs.remove(nickname);
//        System.out.println("Jogador '" + nickname + "' removido da conexão.");
//    }

    public void run() {
        Load();

        try (ServerSocket servidorSocket = new ServerSocket(6789)) {
            this.servidorSocket = servidorSocket;
            System.out.println("Servidor iniciado na porta 6789");
            while (true) {
                System.out.println("Esperando acesso de jogador " + (num + 1) + "...");
                Socket socket = servidorSocket.accept();

                try {
                    InputStreamReader isr = new InputStreamReader(socket.getInputStream());
                    BufferedReader inFromClient = new BufferedReader(isr);
                    PrintWriter outToClient = new PrintWriter(socket.getOutputStream(), true);
                    nickname = inFromClient.readLine();
                    if (nickname == null || nickname.trim().isEmpty() || players.containsKey(nickname)) {
                        System.out.println("Cliente com nick inválido ou duplicado. Desconectando.");
                        outToClient.println("NICK_INVALIDO");
                        socket.close();
                        continue;
                    }
                    System.out.println("Jogador '" + nickname + "' (ID: " + (num + 1) + ") conectou.");
                    players.put(nickname, socket);
                    amount.put(nickname, 100);
                    playerInputs.put(nickname, inFromClient);
                    playerOutputs.put(nickname, outToClient);
                    if (isGameInProgress()) {
                        outToClient.println("WAIT");
                        System.out.println("Jogador '" + nickname + "' colocado em espera.");
                    } else {
                        outToClient.println("JOIN_OK");
                        System.out.println("Jogador '" + nickname + "' entrou no lobby.");
                    }
                    System.out.println("Tamanho do array: " + players.size() + "\n");
                    if (players.size() == 1) {
                        synchronized (this) {
                            System.out.println("Notificando que um jogador conectou...");
                            this.notifyAll();
                        }
                    }
                    num++;
                } catch (IOException e) {
                    System.out.println("Erro ao ler nickname. Desconectando socket.");
                    if (nickname != null) {
                        removePlayer(nickname);
                    }
                    socket.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

