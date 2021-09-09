package cn.aethli.redisSucker.entity;

import cn.aethli.redisSucker.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "TASK")
@Entity
@Getter
@Setter
@Proxy(lazy = false)
public class TaskEntity {
  @Id private String taskId;
  @Column private String taskContent;
  @Column @Enumerated private TaskType taskType;
  @Column private Integer runTimes = 0;
  @Column private Boolean repeatTask = false;
  @Column private Boolean success = false;
  @Column private LocalDateTime createTime;
  @Column private LocalDateTime lastExecuteTime;
  @Column private LocalDateTime firstExecuteTime;
}
