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
import mensagens.Mensagem;

public class Connection extends Thread {

    private ServerSocket servidorSocket;
    private ConcurrentHashMap<String, Socket> players;
    private ConcurrentHashMap<String, Integer> amount; // Mapa de saldos em memória
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

    public Socket getPlayerSocket(String nickname) {
        return players.get(nickname);
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
                System.out.println("Cliente conectado: " + socket.getInetAddress());
                new Thread(() -> tratamento(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Erro: " + e.getMessage());
        }
    }

    private void tratamento(Socket socket){
        try(BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true)){

            String mensagem_json = in.readLine();

            if(mensagem_json == null) return; //retorna nada caso tenha enviado uma mensagem nula

            Mensagem ack = Mensagem.criar(Mensagem.TipoMensagem.ACK,0,null);
            String Jsonack = map.writeValueAsString(ack);
            out.println(Jsonack);
            System.out.println("ACK enviado... \n");

            Mensagem mensagem = map.readValue(mensagem_json,Mensagem.class);

            if(mensagem.getTipo() == Mensagem.TipoMensagem.LOGIN){
                processarLogin(mensagem, out);
            }else{
                //mensagem de erro
            }



        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

