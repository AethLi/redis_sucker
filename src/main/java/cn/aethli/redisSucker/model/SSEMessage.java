package cn.aethli.redisSucker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class SSEMessage {
  private String messageId;
  private String content;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime sendTime;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime releaseTime;

  public SSEMessage(String messageId, String content, LocalDateTime releaseTime) {
    this.messageId = messageId;
    this.content = content;
    this.releaseTime = releaseTime;
  }
}
