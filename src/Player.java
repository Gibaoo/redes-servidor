
import java.util.*;

public class Player {

    protected boolean online = true;
    protected String nick;
    protected List<Carta> hand = new ArrayList<>();
    protected boolean valid = false;

    public Player(String nick) {
        this.nick = nick;
    }

    public void getCard(Carta card) {
        hand.add(card);
    }

    public String getNick() {
        return nick;
    }

    public String showHand() {
        StringBuilder sb = new StringBuilder();
        for (Carta carta : hand) {
            sb.append(carta.toString()).append(" ");
        }
        return sb.toString().trim();
    }

    public List<Carta> getHand() {
        return hand;
    }

    public void clearHand() {
        hand.clear();
    }

    /**
     * Calcula os pontos da mão atual, tratando a lógica do Ás (1 ou 11).
     */
    public int calcularPontos() {
        int pontos = 0;
        int numAses = 0;

        for (Carta carta : hand) {
            int pontosCarta = carta.getPontos();
            if (pontosCarta == 11) {
                numAses++;
            }
            pontos += pontosCarta;
        }

        // Se estourar (pontos > 21) e tiver Ases,
        // transforma o valor de cada Ás de 11 para 1 até ficar <= 21
        while (pontos > 21 && numAses > 0) {
            pontos -= 10;
            numAses--;
        }

        return pontos;
    }

}
