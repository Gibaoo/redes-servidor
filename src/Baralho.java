
import java.util.*;

public class Baralho {

    private List<Integer> cartas = new ArrayList<>();

    public Baralho(){
        for(int i=0; i<52; i++){
            cartas.add(i);
        }
        Collections.shuffle(cartas, new Random());
    }

    public Integer retirarcarta() {
        return cartas.remove(cartas.size() - 1);
    }
}

