import java.io.IOException;

public class Servidor{
    public static void main(String[] args) {
        System.out.println("Batata");

        Connection connection = new Connection();
        Thread connectionThread = connection;
        Thread blackJackThread= new BlackJack(connection);
        connectionThread.start();
        blackJackThread.start();

        try {
            connectionThread.join();
            blackJackThread.join();
        } catch (InterruptedException e) {
            System.err.println("Erro nas threads do servidor: " + e.getMessage());
        }
    }
    
}
