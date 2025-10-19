
import java.util.*;

public class Baralho {

    private List<Carta> cartas = new ArrayList<>();
    private static final String[] NAIPES = {"♠", "♣", "♥", "♦"};

    public Baralho(){
        for(String naipes:NAIPES){
            for(Valor rank:Valor.values()){
                cartas.add(new Carta(rank, naipes));
            }
        }
        Collections.shuffle(cartas, new Random());
    }

    public Carta retirarcarta() {
        return cartas.remove(cartas.size() - 1);
    }
}

