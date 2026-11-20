package org.linketinder.spring.competencias.http

import org.linketinder.spring.competencias.service.DadosInvalidosException
import org.linketinder.spring.competencias.service.RecursoNaoEncontradoException
import org.linketinder.spring.competencias.service.RegraDeNegocioException
import org.linketinder.spring.competencias.service.ServicoIndisponivelException
import org.springframework.dao.DataAccessException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import tools.jackson.databind.exc.MismatchedInputException

/**
 *
 * @author Guilherme Lima Conte
 */

@RestControllerAdvice
class TratadorDeErros {

    static final List<HttpMethod> ORDEM_DOS_METODOS = [HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE]
    static final String CORPO_AUSENTE = "Required request body is missing"

    private final RegistroDoServidor registro

    TratadorDeErros(RegistroDoServidor registro) {
        this.registro = registro
    }

    @ExceptionHandler(DadosInvalidosException)
    ResponseEntity<Map<String, Object>> dadosInvalidos(DadosInvalidosException falha) {
        return erro(HttpStatus.BAD_REQUEST, falha.message)
    }

    @ExceptionHandler(RegraDeNegocioException)
    ResponseEntity<Map<String, Object>> regraDeNegocio(RegraDeNegocioException falha) {
        return erro(HttpStatus.BAD_REQUEST, falha.message)
    }

    @ExceptionHandler(RecursoNaoEncontradoException)
    ResponseEntity<Map<String, Object>> recursoNaoEncontrado(RecursoNaoEncontradoException falha) {
        return erro(HttpStatus.NOT_FOUND, falha.message)
    }

    @ExceptionHandler(ServicoIndisponivelException)
    ResponseEntity<Map<String, Object>> servicoIndisponivel(ServicoIndisponivelException falha) {
        return erro(HttpStatus.SERVICE_UNAVAILABLE, falha.message)
    }

    @ExceptionHandler([NoHandlerFoundException, NoResourceFoundException])
    ResponseEntity<Map<String, Object>> rotaNaoEncontrada() {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada")
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException)
    ResponseEntity<Map<String, Object>> metodoNaoAceito(HttpRequestMethodNotSupportedException falha) {
        String metodos = ORDEM_DOS_METODOS.findAll { HttpMethod metodo -> metodo in falha.supportedHttpMethods }*.name().join(", ")

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .header(HttpHeaders.ALLOW, metodos)
                .body([erro: "O método ${falha.method} não é suportado. Use ${metodos}".toString()] as Map<String, Object>)
    }

    @ExceptionHandler(HttpMessageNotReadableException)
    ResponseEntity<Map<String, Object>> corpoIlegivel(HttpMessageNotReadableException falha) {
        return erro(HttpStatus.BAD_REQUEST, descreverCorpoIlegivel(falha))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException)
    ResponseEntity<Map<String, Object>> conversaoInvalida(MethodArgumentTypeMismatchException falha) {
        if (falha.parameter.hasParameterAnnotation(PathVariable)) {
            return erro(HttpStatus.BAD_REQUEST, "O ID informado na URL não é um número")
        }
        return erro(HttpStatus.BAD_REQUEST, "O parâmetro '${falha.name}' deve ser um número")
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException)
    ResponseEntity<Map<String, Object>> tipoDeConteudoNaoAceito(HttpMediaTypeNotSupportedException falha) {
        return erro(falha.statusCode, "Requisição inválida")
    }

    @ExceptionHandler(DataAccessException)
    ResponseEntity<Map<String, Object>> falhaDeBanco(DataAccessException falha) {
        registro.registrarFalhaDeBanco(falha)
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao acessar o banco de dados")
    }

    @ExceptionHandler(Exception)
    ResponseEntity<Map<String, Object>> falhaInesperada(Exception falha) {
        registro.registrarFalhaInesperada(falha)
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno do servidor")
    }

    private static String descreverCorpoIlegivel(HttpMessageNotReadableException falha) {
        if (falha.message?.startsWith(CORPO_AUSENTE)) {
            return "O corpo da requisição não pode ser vazio"
        }
        if (falha.cause instanceof MismatchedInputException) {
            return "O corpo da requisição deve ser um objeto JSON"
        }
        return "O corpo da requisição deve ser um JSON válido"
    }

    private static ResponseEntity<Map<String, Object>> erro(HttpStatusCode status, String mensagem) {
        return ResponseEntity.status(status).body([erro: mensagem] as Map<String, Object>)
    }
}
