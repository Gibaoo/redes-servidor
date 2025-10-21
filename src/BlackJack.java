//Código servidor que com certeza funciona:
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BlackJack extends Thread{

    private Connection connection;
    private HashMap <Integer, Integer> bets = new HashMap<>();

    // Accept the Connection object so we can read its players live
    public BlackJack(Connection connection){
        this.connection = connection;
    }

    public void run(){
        while(true){
            Map<Integer, Socket> players = null;
            Map<Integer, Integer> amount = null;
            Baralho baralho = new Baralho();
            HashMap<Integer, ArrayList<Carta>> cards = new HashMap<>();
            ArrayList<Carta> Dealer = new ArrayList<>();

            if (connection != null) {
                players = connection.getPlayers();
                amount = connection.getAmount();
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
                            outToClient.writeBytes("aa0"+'\n');

                            outToClient.writeBytes(amount.get(identificador).toString() + '\n');

                            if(amount.get(identificador)==0){
                                    players.remove(identificador);
                                    amount.remove(identificador);
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
                            }else{
                                 //Sair
                                outToClient.writeBytes("Gostaria de sair?"+'\n');
                                String resposta_jogador=entrada1.readLine();
                                if(resposta_jogador.equals("sair")){
                                        players.remove(identificador);
                                        amount.remove(identificador);
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

                                //Saldo:

                                //Frasesinha:
                                outToClient.writeBytes(mensagem + '\n');

                                //Saldos
                                outToClient.writeBytes("Saldos: "+amount.toString() + '\n');

                                //Quantidade de dinheiro que o respectivo player possui
                                outToClient.writeBytes(amount.get(identificador).toString() + '\n');

                                System.out.println("Esperando resposta do Jogador "+identificador+"...");
                                resposta_jogador=entrada1.readLine();
                                System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");
                                bets.put(identificador, Integer.parseInt(resposta_jogador));

                                outToClient.writeBytes("Voce apostou: "+resposta_jogador+ '\n'); 
                                //System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                                //players.remove(identificador);
                                // small pause after sending to avoid tight-loop
                            }

                           
                        
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

                Dealer.add(new Carta(baralho.retirarcarta()));
                Dealer.add(new Carta(baralho.retirarcarta()));

                for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Integer identificador = entrada.getKey();
                    Socket socket_jogador = entrada.getValue();

                        //String mensagem=identificador + ": " + baralho.retirarcarta().toString()+" "+baralho.retirarcarta().toString();
                        try {
                            BufferedReader entrada1 = new BufferedReader(new InputStreamReader(socket_jogador.getInputStream()));
                            DataOutputStream outToClient =new DataOutputStream(socket_jogador.getOutputStream());
                           
                            outToClient.writeBytes("aa1"+'\n');

                            System.out.println("Esperando confirmacao de validade...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Valor obtido: "+resposta_jogador+'\n');

                            if(resposta_jogador.equals("true")) {
                                //System.out.println("Esperando resposta do Jogador "+identificador+"...");
                                //resposta_jogador=entrada1.readLine();
                                //System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");
                                
                                outToClient.writeBytes(Dealer.get(0).toString()+'\n');
                                
                                cards.put(identificador, new ArrayList<Carta>());

                                //Carta 1
                                Integer carta_1=baralho.retirarcarta();
                                cards.get(identificador).add(new Carta(carta_1));
                                String mensagem=carta_1.toString();
                                outToClient.writeBytes(mensagem + '\n'); 
                                
                                //Carta 2
                                Integer carta_2=baralho.retirarcarta();
                                cards.get(identificador).add(new Carta(carta_2));
                                mensagem=carta_2.toString();
                                outToClient.writeBytes(mensagem + '\n');

                                System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                                System.out.println("Mao do jogador "+identificador+": "+cards.get(identificador));
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
                            outToClient.writeBytes("aa2"+'\n');

                            System.out.println("Esperando confirmacao de validade...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Valor obtido: "+resposta_jogador+'\n');

                            if(resposta_jogador.equals("true")){

                                outToClient.writeBytes("Voce tem tres opcoes: stand, hit ou double" + '\n');
                                System.out.println("Esperando resposta do Jogador "+identificador+"...");
                                resposta_jogador=entrada1.readLine();
                                System.out.println("Resposta do Jogador: "+resposta_jogador+"\n");
                                //bets.put(identificador, resposta_jogador);

                                if(resposta_jogador.equals("hit")){
                                    Integer carta_1=baralho.retirarcarta();
                                    cards.get(identificador).add(new Carta(carta_1));
                                    outToClient.writeBytes(carta_1+"\n");

                                    Integer pontos_jogador=0;

                                    for(Carta carta : cards.get(identificador)){
                                        pontos_jogador=pontos_jogador+carta.getPontos();
                                    }

                                    if(pontos_jogador>21){
                                        connection.setAmount(identificador, amount.get(identificador)-bets.get(identificador));
                                    }
                                    
                                }

                                if(resposta_jogador.equals("stand")){
                                    String mensagem="OK. Confident, huh?";
                                    outToClient.writeBytes(mensagem+"\n");
                                }

                                if(resposta_jogador.equals("double")){
                                    bets.put(identificador, 2*bets.get(identificador));
                                    String mensagem="OK. REEEEEEEEAL Confident: "+bets.get(identificador);
                                    outToClient.writeBytes(mensagem+"\n");
                                }

                            //outToClient.writeBytes("Voce escolheu: "+resposta_jogador+ '\n'); 
                            //System.out.println("Eviando m\u00e3o para jogador "+identificador+": "+mensagem);
                        }
                        }catch (IOException e) {
                            //e.printStackTrace();

                        } 
            }

            //#4 ciclo: Determining who wins
            System.out.println("Vez do Dealer...");
                System.out.println(players);
                Integer pontos_do_dealer=Dealer.get(0).getPontos()+Dealer.get(1).getPontos();
                
                if(pontos_do_dealer<=16){
                    Dealer.add(new Carta(baralho.retirarcarta()));
                    pontos_do_dealer=0;

                    for (Carta carta:Dealer){
                        pontos_do_dealer=pontos_do_dealer+carta.getPontos();
                    }
                }

                for (Map.Entry<Integer, Socket> entrada : players.entrySet()) {
                    Integer identificador = entrada.getKey();
                    Socket socket_jogador = entrada.getValue();

                        try {
                            BufferedReader entrada1 = new BufferedReader(new InputStreamReader(socket_jogador.getInputStream()));
                            DataOutputStream outToClient =new DataOutputStream(socket_jogador.getOutputStream());

                            //Código para o cliente saber em qual parte do jogo nós estamos 
                            outToClient.writeBytes("aa3"+'\n');

                            //Sinal de validade do jogador
                            System.out.println("Esperando confirmacao de validade...");
                            String resposta_jogador=entrada1.readLine();
                            System.out.println("Valor obtido: "+resposta_jogador+'\n');

                            if(resposta_jogador.equals("true")){
                                //Cartas do Dealer + pontos do dealer (visualização, somente)
                                outToClient.writeBytes("Cartas do Dealer: "+Dealer.toString()+" | Pontos do Dealer: "+(pontos_do_dealer)+'\n');
                                //Pontos do Dealer, para tratamento interno
                                outToClient.writeBytes(pontos_do_dealer.toString()+'\n');
                                
                                if(pontos_do_dealer>21){
                                    connection.setAmount(identificador, amount.get(identificador)+bets.get(identificador));
                                }

                                Integer pontos_jogador=0;
                                for(Carta carta : cards.get(identificador)){
                                    pontos_jogador=pontos_jogador+carta.getPontos();
                                }

                                if(pontos_jogador>pontos_do_dealer){
                                    connection.setAmount(identificador, amount.get(identificador)+bets.get(identificador));
                                }

                                if(pontos_jogador<pontos_do_dealer){
                                    connection.setAmount(identificador, amount.get(identificador)-bets.get(identificador));
                                }
                            }
                        }catch (IOException e) {
                            //e.printStackTrace();

                        } 
            }





        }
    }
}
}
