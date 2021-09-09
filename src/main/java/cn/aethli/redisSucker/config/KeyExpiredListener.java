package cn.aethli.redisSucker.config;

import cn.aethli.redisSucker.model.Task;
import cn.aethli.redisSucker.thread.TaskScheduleManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Slf4j
@Component
public class KeyExpiredListener extends KeyExpirationEventMessageListener {
  @Value("${redisSucker.redis.taskPrefix}")
  private String taskPrefix;

  @Resource private TaskScheduleManager taskScheduleManager;
  @Resource private StringRedisTemplate stringRedisTemplate;
  @Resource private ObjectMapper defaultMapper;

  public KeyExpiredListener(RedisMessageListenerContainer listenerContainer) {
    super(listenerContainer);
  }

  @Override
  public void onMessage(Message message, byte[] pattern) {
    String regexString = taskPrefix + "_Timer_" + ".*?";
    String expiredKey = message.toString();
    log.info("expiredKey: {}", expiredKey);
    if (expiredKey.matches(regexString)) {
      String actualTaskKey = expiredKey.replace("Timer", "Actual");
      String value = stringRedisTemplate.opsForValue().get(actualTaskKey);
      try {
        taskScheduleManager.pushTask(defaultMapper.readValue(value, Task.class));
      } catch (JsonProcessingException e) {
        log.error(e.getMessage(), e);
      }
    }
  }
}
