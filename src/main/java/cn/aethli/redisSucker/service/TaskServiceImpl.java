package cn.aethli.redisSucker.service;

import cn.aethli.redisSucker.dto.ReleaseDto;
import cn.aethli.redisSucker.dto.TaskReleaseDto;
import cn.aethli.redisSucker.entity.TaskEntity;
import cn.aethli.redisSucker.enums.TaskType;
import cn.aethli.redisSucker.model.Task;
import cn.aethli.redisSucker.repository.TaskRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class TaskServiceImpl implements TaskService {
  @Resource private RedisTemplate<String, Object> redisTemplate;
  @Resource private ObjectMapper defaultMapper;
  @Resource private TaskRepository taskRepository;

  @Value("${redisSucker.redis.taskPrefix}")
  private String taskPrefix;

  @Override
  public void releaseTask(TaskReleaseDto taskReleaseBo) throws JsonProcessingException {
    ReleaseDto releaseDto = new ReleaseDto();
    BeanUtils.copyProperties(taskReleaseBo, releaseDto);
    releaseDto.setContent(taskReleaseBo.getMessageContent());
    Task task = new Task();
    task.setTaskId(taskReleaseBo.getTaskId());
    task.setTaskType(TaskType.RELEASE);
    task.setTaskContent(defaultMapper.writeValueAsString(releaseDto));
    task.setCreateTime(LocalDateTime.now());
    if (taskReleaseBo.getDelayTime() != null) {
      task.setExecuteTime(LocalDateTime.now().plusSeconds(taskReleaseBo.getDelayTime()));
      pushTask2Redis(task, Duration.ofSeconds(taskReleaseBo.getDelayTime()));
      saveTask2Database(task);
    } else if (taskReleaseBo.getStartTime() != null
        && taskReleaseBo.getStartTime().isAfter(LocalDateTime.now())) {
      task.setExecuteTime(taskReleaseBo.getStartTime());
      pushTask2Redis(task, Duration.between(LocalDateTime.now(), taskReleaseBo.getStartTime()));
      saveTask2Database(task);
    } else {
      throw new IllegalArgumentException("Cannot to determine the start time of the task");
    }
  }

  private void saveTask2Database(Task task) {
    TaskEntity taskEntity = new TaskEntity();
    BeanUtils.copyProperties(task, taskEntity);
    taskEntity.setCreateTime(LocalDateTime.now());
    taskEntity.setFirstExecuteTime(task.getExecuteTime());
    taskRepository.save(taskEntity);
  }

  private void pushTask2Redis(Task task, Duration timeout) {
    redisTemplate
        .opsForValue()
        .setIfAbsent(
            String.format("%s_Timer_%s_%s", taskPrefix, TaskType.RELEASE, task.getTaskId()),
            task.getTaskContent(),
            timeout);
    redisTemplate
        .opsForValue()
        .setIfAbsent(
            String.format("%s_Actual_%s_%s", taskPrefix, TaskType.RELEASE, task.getTaskId()), task);
  }
}
