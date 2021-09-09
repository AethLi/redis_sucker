package cn.aethli.redisSucker.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/** @author 93162 */
@Getter
@Setter
public class TaskReleaseDto {
  private String taskId;
  @NotNull private String themeName;
  @NotNull private String id;
  @NotNull private String messageContent;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime startTime;
  private Long delayTime;
}
