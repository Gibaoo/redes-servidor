import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BlackJack extends Thread{

    private Connection connection;
    private Baralho baralho = new Baralho();
    private HashMap <Integer, String> bets = new HashMap<>();

    // Accept the Connection object so we can read its players live
    public BlackJack(Connection connection){
        this.connection = connection;
    }

    public void run(){
        while(true){
            Map<Integer, Socket> players = null;
            if (connection != null) {
                players = connection.getPlayers();
            }

            if (players==null || players.size()==0){
                try{
                    Thread.sleep(100);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    break;
                }
                continue;
            }else{
                System.out.println("Rodada Iniciando...\n");


                //#1 ciclo: Collecting bets from players
                System.out.println("Coletando apostas dos jogadores...\n");
                //Entrada do loop foi feita por IA.
                 for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Integer identificador = entrada.getKey();
                    Socket socket_jogador = entrada.getValue();

                        String mensagem="Quanto você gostaria de apostar (somente números)? ";
                        try {
                            BufferedReader entrada1 = new BufferedReader(new InputStreamReader(socket_jogador.getInputStream()));
                            DataOutputStream outToClient =new DataOutputStream(socket_jogador.getOutputStream());

                            //Código para o cliente saber em qual parte do jogo nós estamos 
                            outToClient.writeBytes("0"+'\n');
                            outToClient.writeBytes(mensagem + '\n');
                            System.out.println("Esperando resposta do Jogador "+identificador+"...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");
                            bets.put(identificador, resposta_jogador);

                            outToClient.writeBytes("Voce apostou: "+resposta_jogador+ '\n'); 
                            //System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                            //players.remove(identificador);
                            // small pause after sending to avoid tight-loop
                        
                    }catch (IOException e) {
                        //e.printStackTrace();

                } 
            }
            //Mandando as apostas para os jogadores
            for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Socket socket_jogador=entrada.getValue();
                    try {
                        DataOutputStream outToClient=new DataOutputStream(socket_jogador.getOutputStream());
                        outToClient.writeBytes("Apostas: "+bets.toString()+'\n');
                        
                    } catch (IOException e) {
                        
                        e.printStackTrace();
                    }
                }
        //#2 ciclo: dealing the cards
                System.out.println("Distribuindo cartas para os jogadores...");
                System.out.println(players);

                for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Integer identificador = entrada.getKey();
                    Socket socket_jogador = entrada.getValue();

                        String mensagem=identificador + ": " + baralho.retirarcarta().toString()+" "+baralho.retirarcarta().toString();
                        try {
                            BufferedReader entrada1 = new BufferedReader(new InputStreamReader(socket_jogador.getInputStream()));
                            DataOutputStream outToClient =new DataOutputStream(socket_jogador.getOutputStream());
                           
                            outToClient.writeBytes("1"+'\n');

                            System.out.println("Esperando confirmacao de validade...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Valor obtido: "+resposta_jogador+'\n');

                            if(resposta_jogador.equals("true")) {
                                System.out.println("Esperando resposta do Jogador "+identificador+"...");
                                resposta_jogador=entrada1.readLine();
                                System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");

                                outToClient.writeBytes(mensagem + '\n'); 
                                System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                                if(resposta_jogador.equals("sair")){
                                    players.remove(identificador);
                                    bets.remove(identificador);

                                    if(players.size()==0){
                                        System.out.println("\n\nZero, mai frendi\n\n");
                                        synchronized(connection){
                                            try {
                                                connection.wait();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            continue;
                                        }
                                    }
                                }
                                try{
                                    Thread.sleep(1000);
                                }catch(InterruptedException e){
                                    //Thread.currentThread().interrupt();
                                
                                }
                            } 
                    }catch (IOException e) {
                        //e.printStackTrace();

                } 
            }


        //#3 ciclo: Deciding the course of action
        System.out.println("Mostrando possibilidades de ação...");
                System.out.println(players);
        for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Integer identificador = entrada.getKey();
                    Socket socket_jogador = entrada.getValue();

                        try {
                            BufferedReader entrada1 = new BufferedReader(new InputStreamReader(socket_jogador.getInputStream()));
                            DataOutputStream outToClient =new DataOutputStream(socket_jogador.getOutputStream());

                            //Código para o cliente saber em qual parte do jogo nós estamos 
                            outToClient.writeBytes("2"+'\n');

                            System.out.println("Esperando confirmacao de validade...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Valor obtido: "+resposta_jogador+'\n');

                            if(resposta_jogador.equals("true")){

                                outToClient.writeBytes("Voce tem duas opcoes: stand ou hit" + '\n');
                                System.out.println("Esperando resposta do Jogador "+identificador+"...");
                                resposta_jogador=entrada1.readLine();
                                System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");
                                //bets.put(identificador, resposta_jogador);

                                if(resposta_jogador.equals("hit")){
                                    String mensagem="Carta Extra: "+baralho.retirarcarta().toString();
                                    outToClient.writeBytes(mensagem+"\n");
                                }

                                if(resposta_jogador.equals("stand")){
                                    String mensagem="OK. Confident, huh?";
                                    outToClient.writeBytes(mensagem+"\n");
                                }

                            //outToClient.writeBytes("Voce escolheu: "+resposta_jogador+ '\n'); 
                            //System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                        }
                        }catch (IOException e) {
                            //e.printStackTrace();

                        } 
            }





        }
    }
}
}
