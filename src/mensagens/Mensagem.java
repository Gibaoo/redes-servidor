package mensagens;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Mensagem {
    public enum TipoMensagem {
        LOGIN,
        LOGIN_resposta,
        ACK,
        PING,
        PONG,

        LISTAR,
        JOIN,
        JOIN_ACK,
        LEAVE,

        MESA_UPDATE,
        MESA_STATE,
        ACAO_JOGADOR,
        APOSTA,
        APOSTA_ACK,
        RESULTADO_ROUND,

        ERRO;
        
    }

    private TipoMensagem tipo;
    private int seq;
    private String token;
    private JsonNode payload;

    public Mensagem() {}

    public Mensagem(TipoMensagem tipo, int seq, String token, JsonNode payload) {
        this.tipo = tipo;
        this.seq = seq;
        this.token = token;
        this.payload = payload;
    }

    public TipoMensagem getTipo() { return tipo; }
    public int getSeq() { return seq; }
    public String getToken() { return token; }
    public JsonNode getPayload() { return payload; }

    public static Mensagem criar(TipoMensagem tipo, int seq, JsonNode payload) {
        return new Mensagem(tipo, seq, null, payload);
    }

    public static Mensagem criarErro(int seq, String descricao) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode().put("erro", descricao);
            return new Mensagem(TipoMensagem.ERRO, seq, null, node);
        } catch (Exception e) {
            return new Mensagem(TipoMensagem.ERRO, seq, null, null);
        }
    }

    @Override
    public String toString() {
        return "Mensagem{" +
                "tipo=" + tipo +
                ", seq=" + seq +
                ", token='" + token + '\'' +
                ", payload=" + payload +
                '}';
    }
}
