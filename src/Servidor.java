// no external imports required here

public class Servidor{
    public static void main(String[] args) {
        System.out.println("Batata");
        // create the Connection thread instance so we can pass it to BlackJack
        Connection connection = new Connection();
        Thread connectionThread = connection;
        Thread blackJackThread= new BlackJack(connection);

        //start connection first so getPlayers() will be initialized while BlackJack runs
        connectionThread.start();
        blackJackThread.start();

    }
    
}
