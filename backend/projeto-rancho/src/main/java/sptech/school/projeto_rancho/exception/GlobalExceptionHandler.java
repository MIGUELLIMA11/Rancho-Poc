package sptech.school.projeto_rancho.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/exception/GlobalExceptionHandler.java
 *
 * Captura todas as exceções da aplicação e devolve um JSON padronizado.
 * O frontend usa o campo "message" para exibir o erro ao utilizador.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 — Recurso não encontrado ──────────────────────────
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNaoEncontrado(RecursoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // ── 400 — Erros de negócio (RuntimeException genérica) ───
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // ── 400 — Erros de validação (@Valid / @NotBlank etc.) ───
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        // Junta todas as mensagens de validação num só texto
        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            sb.append(err.getField()).append(": ").append(err.getDefaultMessage()).append(". ")
        );
        return buildResponse(HttpStatus.BAD_REQUEST, sb.toString().trim());
    }

    // ── 500 — Erro interno não esperado ──────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                             "Erro interno no servidor. Contate o suporte.");
    }

    // ── Helper: monta o JSON de resposta ─────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    status.value());
        body.put("message",   message);
        return ResponseEntity.status(status).body(body);
    }
}
