import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//Possíveis problemas de condição de corrida aqui. É bom prestar atenção.
public class Connection extends Thread{
    private ServerSocket servidorSocket;
    private ConcurrentHashMap<Integer, Socket> players;
    private int num;

    public Map<Integer, Socket> getPlayers() {
        return players;
    }

    public ServerSocket getServidorSocket() {
        return servidorSocket;
    }

    public void run(){
    num=0;
    players=new ConcurrentHashMap<>();

        try (ServerSocket servidorSocket = new ServerSocket(6789)) {
            this.servidorSocket = servidorSocket;
            System.out.println("Servidor iniciado na porta 6789");
            while (true) {
                System.out.println("Esperando acesso de jogador "+(num+1)+"...");
                Socket socket = servidorSocket.accept();

                players.put(num+1, socket);
                System.out.println("Tamanho do array: "+players.size()+"\n");
                if(players.size()==1){
                    synchronized(this){
                        System.out.println("Notificando que um jogador conectou...");
                        this.notifyAll();
                    }
                }
                num++;
                }

            }catch (Exception e) {
            e.printStackTrace();
         
        }
        
        
    }
}
