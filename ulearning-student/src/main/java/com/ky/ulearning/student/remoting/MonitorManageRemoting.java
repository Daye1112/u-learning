package com.ky.ulearning.student.remoting;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author luyuhao
 * @date 19/12/17 02:58
 */
@FeignClient(value = "monitor-manage")
@RequestMapping(value = "/monitor-manage")
public interface MonitorManageRemoting {

    /**
     * 添加日志
     *
     * @param logEntity 待添加的日志对象
     */
    @Async
    @PostMapping("/log/add")
    void add(@RequestParam Map<String, Object> logEntity);

    /**
     * 添加文件记录
     *
     * @param fileRecordDto 待添加的文件对象
     */
    @Async
    @PostMapping("/fileRecord/add")
    void addFileRecord(@RequestParam Map<String, Object> fileRecordDto);
}
