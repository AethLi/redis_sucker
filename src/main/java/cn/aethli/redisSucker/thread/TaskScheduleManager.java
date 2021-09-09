package cn.aethli.redisSucker.thread;

import cn.aethli.redisSucker.dto.ReleaseDto;
import cn.aethli.redisSucker.entity.TaskEntity;
import cn.aethli.redisSucker.enums.TaskType;
import cn.aethli.redisSucker.model.SSEMessage;
import cn.aethli.redisSucker.model.Task;
import cn.aethli.redisSucker.repository.TaskRepository;
import cn.aethli.redisSucker.subscribe.SSEManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TaskScheduleManager {
  @Value("${redisSucker.redis.taskPrefix}")
  private String taskPrefix;

  @Resource private ObjectMapper defaultMapper;
  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource private TaskRepository taskRepository;
  private static final ThreadPoolExecutor THREAD_POOL_EXECUTOR =
      new ThreadPoolExecutor(2, 20, 20, TimeUnit.SECONDS, new LinkedBlockingDeque<>());

  public void pushTask(Task task) {
    TaskEntity taskEntity =
        Hibernate.unproxy(taskRepository.getById(task.getTaskId()), TaskEntity.class);
    switch (task.getTaskType()) {
      case RELEASE:
        THREAD_POOL_EXECUTOR.submit(
            () -> {
              try {
                ReleaseDto releaseDto =
                    defaultMapper.readValue(task.getTaskContent(), ReleaseDto.class);
                SSEManager.releaseMessageById(
                    releaseDto.getThemeName(),
                    releaseDto.getId(),
                    new SSEMessage(
                        UUID.randomUUID().toString(),
                        releaseDto.getContent(),
                        LocalDateTime.now()));
                stringRedisTemplate.delete(
                    String.format(
                        "%s_Actual_%s_%s", taskPrefix, TaskType.RELEASE, task.getTaskId()));
                taskEntity.setSuccess(true);
                taskEntity.setLastExecuteTime(LocalDateTime.now());
                taskEntity.setFirstExecuteTime(LocalDateTime.now());
                taskEntity.setRunTimes(taskEntity.getRunTimes() + 1);
                taskRepository.save(taskEntity);
              } catch (Exception e) {
                log.error(e.getMessage(), e);
              }
            });
        break;
      default:
    }
  }
}
