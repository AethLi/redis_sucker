package cn.aethli.redisSucker.config;

import cn.aethli.redisSucker.vo.ExceptionMessageVo;
import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(JsonParseException.class)
  public ResponseEntity<ExceptionMessageVo> jsonParseExceptionHandler(JsonParseException e) {
    return ResponseEntity.internalServerError().body(new ExceptionMessageVo(e.getMessage()));
  }
}
