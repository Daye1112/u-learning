package com.ky.ulearning.teacher.service;

import com.ky.ulearning.spi.common.dto.PageBean;
import com.ky.ulearning.spi.common.dto.PageParam;
import com.ky.ulearning.spi.common.vo.KeyLabelVo;
import com.ky.ulearning.spi.teacher.dto.ExaminationTaskDto;
import com.ky.ulearning.spi.teacher.entity.ExaminationTaskEntity;

import java.util.List;

/**
 * 测试任务service - 接口类
 *
 * @author luyuhao
 * @since 20/02/13 00:56
 */
public interface ExaminationTaskService {

    /**
     * 添加测试任务
     *
     * @param examinationTaskDto 待添加的测试任务
     */
    void save(ExaminationTaskDto examinationTaskDto);

    /**
     * 分页查询测试任务
     *
     * @param examinationTaskDto 查询参数
     * @param pageParam          分页参数
     * @return 封装测试任务的分页对象
     */
    PageBean<ExaminationTaskEntity> pageExaminationTaskList(ExaminationTaskDto examinationTaskDto, PageParam pageParam);

    /**
     * 根据id查询测试任务
     *
     * @param examinationId 测试任务id
     * @return 返回测试任务对象
     */
    ExaminationTaskEntity getById(Long examinationId);

    /**
     * 更新测试任务
     *
     * @param examinationTaskDto 待更新的测试任务
     */
    void update(ExaminationTaskDto examinationTaskDto);

    /**
     * 删除测试任务
     *
     * @param id       测试任务id
     * @param updateBy 更新者
     */
    void delete(Long id, String updateBy);

    /**
     * 根据教学任务id查询测试任务数组
     *
     * @param teachingTaskId 教学任务id
     * @return 测试任务数组
     */
    List<KeyLabelVo> getArrByTeachingTaskId(Long teachingTaskId);

    /**
     * 根据id查询组卷参数
     *
     * @param id id
     * @return 组卷参数
     */
    String getExaminationParameters(Long id);

    /**
     * 根据教师id查询进行中的测试任务数量
     *
     * @param teaId 教师id
     * @return 测试任务数量
     */
    Integer getExaminationNumber(Long teaId);
}
