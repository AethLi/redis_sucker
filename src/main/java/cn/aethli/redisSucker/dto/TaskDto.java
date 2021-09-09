package cn.aethli.redisSucker.dto;

import cn.aethli.redisSucker.model.Task;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TaskDto {
  private Task task;
  private LocalDateTime startTime;
  private String id;
}
