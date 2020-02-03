package com.ky.ulearning.teacher.service;

import com.ky.ulearning.spi.common.dto.PageBean;
import com.ky.ulearning.spi.common.dto.PageParam;
import com.ky.ulearning.spi.teacher.dto.CourseQuestionDto;
import com.ky.ulearning.spi.teacher.dto.QuestionDto;

/**
 * 试题service - 接口类
 *
 * @author luyuhao
 * @since 20/02/03 19:52
 */
public interface CourseQuestionService {
    /**
     * 保存试题
     *
     * @param questionDto 待保存的试题对象
     */
    void save(QuestionDto questionDto);

    /**
     * 分页查询课程试题列表
     *
     * @param pageParam         分页参数
     * @param courseQuestionDto 筛选条件
     * @return 封装课程试题的分页对象
     */
    PageBean<CourseQuestionDto> pageList(PageParam pageParam, CourseQuestionDto courseQuestionDto);
}
