public class Carta {

    private final Valor valor;
    private final Naipe naipe;

    /**
     * Enum para o valor (rank) da carta.
     * A ordem é importante para o cálculo (A, 2, 3, ..., K).
     */
    public enum Valor {
        AS("A", 11),      // Para Blackjack, 0 é um valor especial para ser tratado depois
        DOIS("2", 2),
        TRES("3", 3),
        QUATRO("4", 4),
        CINCO("5", 5),
        SEIS("6", 6),
        SETE("7", 7),
        OITO("8", 8),
        NOVE("9", 9),
        DEZ("10", 10),
        VALETE("J", 10), // Jack
        DAMA("Q", 10),   // Queen
        REI("K", 10);    // King

        private final String simbolo;
        private final int pontos;

        Valor(String simbolo, int pontos) {
            this.simbolo = simbolo;
            this.pontos = pontos;
        }

        public int getPontos() {
            return pontos;
        }

        public String getSimbolo() {
            return simbolo;
        }
    }

    /**
     * Enum para o naipe da carta, usando os símbolos Unicode.
     * A ordem é definida para mapear o cálculo:
     * IDs 0-12   (div 13 = 0) -> ESPADAS
     * IDs 13-25  (div 13 = 1) -> PAUS
     * IDs 26-38  (div 13 = 2) -> COPAS
     * IDs 39-51  (div 13 = 3) -> DIAMANTES
     */
    public enum Naipe {
        ESPADAS("\u2660"),
        PAUS("\u2663"),
        COPAS("\u2665"),
        DIAMANTES("\u2666");

        private final String simbolo;

        Naipe(String simbolo) {
            this.simbolo = simbolo;
        }

        public String getSimbolo() {
            return simbolo;
        }
    }

    /**
     * Construtor da Carta.
     * @param id Um número de 0 a 51 que representa uma carta única no baralho.
     */
    public Carta(int id) {
        if (id < 0 || id > 51) {
            throw new IllegalArgumentException("O ID da carta deve estar entre 0 e 51.");
        }

        // Calcula o valor da carta. O valor se repete a cada 13 cartas.
        // (0-51) % 13 resulta em um índice de 0 a 12.
        this.valor = Valor.values()[id % 13];

        // Calcula o naipe da carta. O naipe muda a cada 13 cartas.
        // (0-51) / 13 (divisão inteira) resulta em um índice de 0 a 3.
        this.naipe = Naipe.values()[id / 13];
    }

    /**
     * Retorna o símbolo da carta (ex: "A", "2", "K").
     */
    public String getSimbolo() {
        return this.valor.getSimbolo();
    }

    /**
     * Retorna a pontuação da carta para Blackjack.
     */
    public int getPontos() {
        return this.valor.getPontos();
    }

    /**
     * Retorna o símbolo do naipe (ex: "♠", "♥").
     */
    public String getNaipe() {
        return this.naipe.getSimbolo();
    }

    /**
     * Retorna uma representação em texto da carta, por exemplo: "A♠".
     */
    @Override
    public String toString() {
        return getSimbolo() + getNaipe();
    }
}