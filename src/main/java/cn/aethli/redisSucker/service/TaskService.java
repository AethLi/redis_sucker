package cn.aethli.redisSucker.service;

import cn.aethli.redisSucker.dto.TaskReleaseDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface TaskService {
    void releaseTask(TaskReleaseDto taskReleaseBo) throws JsonProcessingException;
}
