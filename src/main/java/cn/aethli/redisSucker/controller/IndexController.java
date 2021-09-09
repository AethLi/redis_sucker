package cn.aethli.redisSucker.controller;

import cn.aethli.redisSucker.dto.ReleaseDto;
import cn.aethli.redisSucker.dto.TaskReleaseDto;
import cn.aethli.redisSucker.model.SSEMessage;
import cn.aethli.redisSucker.service.TaskService;
import cn.aethli.redisSucker.subscribe.SSEManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.UUID;

@RestControllerAdvice
@RequestMapping("sync/sse")
public class IndexController {

  @Resource private ObjectMapper defaultMapper;
  @Resource private TaskService taskService;

  @GetMapping(value = "subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<String>> sseSubscribe(
      @RequestParam String themeName, @RequestParam String id) {
    return Flux.interval(Duration.ofSeconds(1))
        .map(
            seq -> {
              Queue<SSEMessage> messageQueue = SSEManager.readMessageByThemeId(themeName, id);
              if (!messageQueue.isEmpty()) {
                String messageData;
                try {
                  SSEMessage sseMessage = messageQueue.remove();
                  sseMessage.setSendTime(LocalDateTime.now());
                  messageData = defaultMapper.writeValueAsString(sseMessage);
                } catch (JsonProcessingException e) {
                  messageData = e.getMessage();
                }
                return ServerSentEvent.<String>builder()
                    .event("message")
                    .id(String.valueOf(seq))
                    .data(messageData)
                    .build();
              } else {
                return ServerSentEvent.<String>builder().build();
              }
            });
  }

  @PostMapping(value = "release")
  public ResponseEntity<ObjectUtils.Null> release(@RequestBody ReleaseDto releaseDto) {
    SSEManager.releaseMessageById(
        releaseDto.getThemeName(),
        releaseDto.getId(),
        new SSEMessage(UUID.randomUUID().toString(), releaseDto.getContent(), LocalDateTime.now()));
    return ResponseEntity.ok(null);
  }

  @PostMapping(value = "releaseTask")
  public ResponseEntity<ObjectUtils.Null> releaseTask(
      @RequestBody @Validated TaskReleaseDto taskReleaseBo) throws JsonProcessingException {
    if (StringUtils.isEmpty(taskReleaseBo.getTaskId())) {
      taskReleaseBo.setTaskId(UUID.randomUUID().toString());
    }
    taskService.releaseTask(taskReleaseBo);
    return ResponseEntity.ok(null);
  }
}
