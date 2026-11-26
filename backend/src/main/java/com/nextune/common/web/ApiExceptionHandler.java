package com.nextune.common.web;

import com.nextune.common.validation.ValidacaoException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Traduz as exceções em respostas JSON padronizadas (ver ErroResponse). */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<ErroResponse> validacao(ValidacaoException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", ex.getCampos());
    }

    // MethodArgumentNotValidException (@Valid em @RequestBody) é subclasse de BindException
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErroResponse> bind(BindException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(),
                    Objects.requireNonNullElse(erro.getDefaultMessage(), "Valor inválido"));
        }
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErroResponse> validacaoDeMetodo(HandlerMethodValidationException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos", Map.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> status(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String mensagem = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return montar(status, mensagem, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente, inválido ou mal formatado", Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoInvalido(MethodArgumentTypeMismatchException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos",
                Map.of(ex.getName(), "Valor inválido"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos",
                Map.of(ex.getParameterName(), "Campo obrigatório"));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErroResponse> arquivoAusente(MissingServletRequestPartException ex) {
        return montar(HttpStatus.BAD_REQUEST, "Dados inválidos",
                Map.of(ex.getRequestPartName(), "Arquivo obrigatório"));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroResponse> arquivoGrande(MaxUploadSizeExceededException ex) {
        return montar(HttpStatus.valueOf(413), "Arquivo muito grande", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> conflito(DataIntegrityViolationException ex) {
        return montar(HttpStatus.CONFLICT,
                "A operação conflita com dados já existentes. Tente novamente.", Map.of());
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, Map<String, String> campos) {
        ErroResponse corpo = new ErroResponse(
                Instant.now().toString(), status.value(), status.getReasonPhrase(), mensagem, campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
