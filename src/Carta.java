import java.util.*;

public class Carta {
    private Valor rank;
    private String naipe;

    public Carta(Valor rank, String naipe){
        this.rank = rank;
        this.naipe = naipe;
    }

    public Valor getRank(){
        return rank;
    }

    @Override
    public String toString(){
        return rank.getNaipe()+naipe;
    }
}
