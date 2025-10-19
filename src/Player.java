
import java.util.*;

public class Player {

    protected boolean online = true;
    protected String nick;
    protected List<Carta> mao = new ArrayList<>();

    public Player(String nick){
        this.nick = nick;
    }

    public void receberCarta(Carta carta){
        mao.add(carta);
    }

    public String getNick(){
        return nick;
    }

    public int calculojogada(){
        int total = 0;
        int q_A = 0;

        for (Carta carta : mao) {
            if (carta.getRank() == Valor.A) {
                q_A++;
                total += 11;
            } else {
                total += carta.getRank().getPontos();
            }
        }
        while (total > 21 && q_A > 0) {
            total -= 10;
            q_A--;
        }

        return total;
    }

    public boolean estourou(){
        return calculojogada() > 21;
    }

    public String showhand(){
        StringBuilder sb = new StringBuilder();
        for (Carta carta : mao) {
            sb.append(carta.toString()).append(" ");
        }
        return sb.toString().trim();
    }
    public boolean esta_online() {
        return online;
    }

    public void stop(){
        online = false;
    }

    @Override
    public String toString() {
        return nick + " " + showhand() + " Total: "  + calculojogada();
    }
}


