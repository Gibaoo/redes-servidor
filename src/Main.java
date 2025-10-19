import java.io.*;
enum Valor{
    DOIS("2", 2),
    TRES("3", 3),
    QUATRO("4", 4),
    CINCO("5", 5),
    SEIS("6", 6),
    SETE("7", 7),
    OITO("8", 8),
    NOVE("9", 9),
    DEZ("10", 10),
    J("J", 10),
    Q("Q", 10),
    K("K", 10),
    A("A", 11);

    private final String naipe;
    private final int pontos;

    Valor(String simbolo, int pontos) {
        this.naipe = simbolo;
        this.pontos = pontos;
    }

    public int getPontos() { return pontos; }
    public String getNaipe() { return naipe; }
}

public class Main {
    
}