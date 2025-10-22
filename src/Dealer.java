public class Dealer extends Player {
    public Dealer(){
        super("Dealer");
    }

    public void jogar(Baralho baralho) {
        System.out.println("\nDealer revela a mão: " + this);
        while (calculojogada() < 17) {
            //receberCarta(baralho.retirarcarta());
            System.out.println("Dealer puxa carta: " + this);
        }
    }
}
