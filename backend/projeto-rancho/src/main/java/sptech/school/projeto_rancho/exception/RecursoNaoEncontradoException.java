package sptech.school.projeto_rancho.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Caminho: src/main/java/sptech/school/projeto_rancho/exception/RecursoNaoEncontradoException.java
 *
 * Lançada quando um recurso não é encontrado no banco.
 * Resulta automaticamente em HTTP 404.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
