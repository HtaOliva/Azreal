package br.senac.fullstack.exception;

public class AiMatchmakingException extends RuntimeException {
    public AiMatchmakingException(String message) {
        super(message);
    }

    public AiMatchmakingException(String message, Throwable cause) {
        super(message, cause);
    }
}
