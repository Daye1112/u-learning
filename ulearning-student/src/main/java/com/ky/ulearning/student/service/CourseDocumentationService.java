package com.ky.ulearning.student.service;

import com.ky.ulearning.spi.teacher.dto.CourseFileDocumentationDto;

import java.util.List;

/**
 * @author luyuhao
 * @since 20/02/22 20:35
 */
public interface CourseDocumentationService {

    /**
     * 根据教学任务id查询课程文件资料信息
     *
     * @param teachingTaskId 教学任务id
     * @return 课程文件资料对象
     */
    CourseFileDocumentationDto getByTeachingTaskId(Long teachingTaskId);

    /**
     * 查询文件资料集合
     *
     * @param courseFileDocumentationDto 筛选对象
     * @return 返回课程文件资料集合
     */
    List<CourseFileDocumentationDto> getList(CourseFileDocumentationDto courseFileDocumentationDto);

    /**
     * 根据id查询课程文件资料对象
     *
     * @param id 文件资料id
     * @return 课程文件资料对象
     */
    CourseFileDocumentationDto getById(Long id);
}
