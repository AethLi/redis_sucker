package cn.aethli.redisSucker.runner;

import cn.aethli.redisSucker.model.Task;
import cn.aethli.redisSucker.thread.TaskScheduleManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;

@Component
@Order(100)
@Slf4j
public class ReadTaskOnRedisRunner implements CommandLineRunner {
  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource private ObjectMapper defaultMapper;
  @Resource private TaskScheduleManager taskScheduleManager;

  @Value("${redisSucker.redis.taskPrefix}")
  private String taskPrefix;

  @Override
  public void run(String... args) throws Exception {
    Set<String> taskKeys =
        stringRedisTemplate.keys(String.format("%s_%s_%s", taskPrefix, "Actual", "*"));
    Optional.ofNullable(taskKeys)
        .ifPresent(
            keySet ->
                keySet.forEach(
                    keyString -> {
                      String taskString = stringRedisTemplate.opsForValue().get(keyString);
                      try {
                        Task task = defaultMapper.readValue(taskString, Task.class);
                        taskScheduleManager.pushTask(task);
                      } catch (JsonProcessingException e) {
                        log.error(e.getMessage(), e);
                      }
                    }));
  }
}
