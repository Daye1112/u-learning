package com.ky.ulearning.system.auth.controller;

import com.ky.ulearning.common.core.annotation.DeleteUserRedis;
import com.ky.ulearning.common.core.annotation.Log;
import com.ky.ulearning.common.core.annotation.PermissionName;
import com.ky.ulearning.common.core.api.controller.BaseController;
import com.ky.ulearning.common.core.component.component.FastDfsClientWrapper;
import com.ky.ulearning.common.core.component.constant.DefaultConfigParameters;
import com.ky.ulearning.common.core.constant.CommonErrorCodeEnum;
import com.ky.ulearning.common.core.constant.MicroConstant;
import com.ky.ulearning.common.core.constant.TableFileEnum;
import com.ky.ulearning.common.core.message.JsonResult;
import com.ky.ulearning.common.core.utils.*;
import com.ky.ulearning.common.core.validate.ValidatorBuilder;
import com.ky.ulearning.common.core.validate.handler.ValidateHandler;
import com.ky.ulearning.spi.common.dto.PageBean;
import com.ky.ulearning.spi.common.dto.PageParam;
import com.ky.ulearning.spi.common.dto.PasswordUpdateDto;
import com.ky.ulearning.spi.common.dto.UserContext;
import com.ky.ulearning.spi.common.excel.StudentExcel;
import com.ky.ulearning.spi.system.dto.StudentDto;
import com.ky.ulearning.spi.system.entity.StudentEntity;
import com.ky.ulearning.system.auth.service.StudentService;
import com.ky.ulearning.system.common.constants.SystemErrorCodeEnum;
import com.ky.ulearning.system.common.excel.StudentExcelListener;
import com.ky.ulearning.system.remoting.MonitorManageRemoting;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiOperationSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * @author luyuhao
 * @since 20/01/18 22:56
 */
@Slf4j
@RestController
@Api(tags = "学生管理", description = "学生管理接口")
@RequestMapping("/student")
public class StudentController extends BaseController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private DefaultConfigParameters defaultConfigParameters;

    @Autowired
    private FastDfsClientWrapper fastDfsClientWrapper;

    @Autowired
    private MonitorManageRemoting monitorManageRemoting;

    @Log("学生添加")
    @ApiOperationSupport(ignoreParameters = {"id", "stuPassword"})
    @ApiOperation(value = "学生添加", notes = "密码默认123456")
    @PermissionName(source = "student:save", name = "学生添加", group = "学生管理")
    @PostMapping("/save")
    public ResponseEntity<JsonResult> save(StudentDto studentDto) {
        ValidatorBuilder.build()
                .on(StringUtil.isEmpty(studentDto.getStuNumber()), SystemErrorCodeEnum.STU_NUMBER_CANNOT_BE_NULL)
                .on(StringUtil.isEmpty(studentDto.getStuName()), SystemErrorCodeEnum.STU_NAME_CANNOT_BE_NULL)
                .doValidate().checkResult();
        //获取操作者的编号
        String userNumber = RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class);
        //设置操作者编号
        studentDto.setCreateBy(userNumber);
        //设置更新者编号
        studentDto.setUpdateBy(userNumber);

        //密码加密
        studentDto.setStuPassword(EncryptUtil.encryptPassword("123456"));
        studentService.save(studentDto);
        return ResponseEntityUtil.ok(JsonResult.buildDataMsg(studentDto.getId(), "添加学生成功"));
    }

    @Log(value = "根据id查询学生信息", devModel = true)
    @ApiOperation(value = "根据id查询学生信息")
    @PermissionName(source = "student:getById", name = "根据id查询学生信息", group = "学生管理")
    @GetMapping("/getById")
    public ResponseEntity<JsonResult<StudentEntity>> getById(Long id) {
        ValidateHandler.checkParameter(StringUtil.isEmpty(id), SystemErrorCodeEnum.ID_CANNOT_BE_NULL);
        StudentEntity studentEntity = studentService.getById(id);
        return ResponseEntityUtil.ok(JsonResult.buildDataMsg(studentEntity, "查询成功"));
    }

    @Log(value = "分页查询学生信息", devModel = true)
    @ApiOperation(value = "分页查询学生信息")
    @ApiOperationSupport(ignoreParameters = {"id", "stuPassword"})
    @PermissionName(source = "student:pageList", name = "分页查询学生信息", group = "学生管理")
    @GetMapping("/pageList")
    public ResponseEntity<JsonResult<PageBean<StudentEntity>>> pageList(PageParam pageParam, StudentDto studentDto) {
        PageBean<StudentEntity> pageBean = studentService.pageStudentList(studentDto, setPageParam(pageParam));
        return ResponseEntityUtil.ok(JsonResult.buildDataMsg(pageBean, "查询成功"));
    }

    @Log("删除学生")
    @DeleteUserRedis
    @ApiOperation(value = "删除学生")
    @PermissionName(source = "student:delete", name = "删除学生", group = "学生管理")
    @GetMapping("/delete")
    public ResponseEntity<JsonResult> delete(Long id) {
        ValidateHandler.checkParameter(StringUtil.isEmpty(id), SystemErrorCodeEnum.ID_CANNOT_BE_NULL);
        studentService.delete(id, RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class));
        return ResponseEntityUtil.ok(JsonResult.buildMsg("删除学生成功"));
    }

    @Log("学生更新")
    @DeleteUserRedis
    @ApiOperation(value = "学生更新")
    @PermissionName(source = "student:update", name = "学生更新", group = "学生管理")
    @PostMapping("/update")
    public ResponseEntity<JsonResult> update(StudentDto studentDto) {
        ValidateHandler.checkParameter(StringUtil.isEmpty(studentDto.getId()), SystemErrorCodeEnum.ID_CANNOT_BE_NULL);
        if (!StringUtil.isEmpty(studentDto.getStuPassword())) {
            studentDto.setStuPassword(EncryptUtil.encryptPassword(studentDto.getStuPassword()));
            studentDto.setPwdUpdateTime(new Date());
        }
        //设置更新者编号
        studentDto.setUpdateBy(RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class));

        studentService.update(studentDto);
        return ResponseEntityUtil.ok(JsonResult.buildMsg("更新学生成功"));
    }

    @ApiOperation(value = "", hidden = true)
    @PostMapping("/login")
    public ResponseEntity<JsonResult<UserContext>> studentLogin(String stuNumber) {
        ValidateHandler.checkParameter(StringUtil.isEmpty(stuNumber), SystemErrorCodeEnum.STU_NUMBER_CANNOT_BE_NULL);
        //获取学生信息
        StudentEntity studentEntity = studentService.getByStuNumber(stuNumber);
        ValidateHandler.checkParameter(studentEntity == null, SystemErrorCodeEnum.STUDENT_NOT_EXISTS);
        //初始化用户context,无角色和权限信息
        UserContext userContext = new UserContext()
                .setId(studentEntity.getId())
                .setSysRole(MicroConstant.SYS_ROLE_STUDENT)
                .setUsername(studentEntity.getStuNumber())
                .setPassword(studentEntity.getStuPassword())
                .setPwdUpdateTime(studentEntity.getPwdUpdateTime())
                .setRoles(Collections.emptyList())
                .setPermissions(Collections.emptyList());
        return ResponseEntityUtil.ok(JsonResult.buildData(userContext));
    }

    @Log("更新密码")
    @DeleteUserRedis
    @ApiOperation("更新密码")
    @PermissionName(source = "student:updatePassword", name = "更新密码", group = "学生管理")
    @PostMapping("/updatePassword")
    public ResponseEntity<JsonResult> updatePassword(PasswordUpdateDto passwordUpdateDto) {
        ValidatorBuilder.build()
                .on(StringUtil.isEmpty(passwordUpdateDto.getId()), SystemErrorCodeEnum.ID_CANNOT_BE_NULL)
                .on(StringUtil.isEmpty(passwordUpdateDto.getOldPassword()), CommonErrorCodeEnum.OLD_PASSWORD_CANNOT_BE_NULL)
                .on(StringUtil.isEmpty(passwordUpdateDto.getNewPassword()), CommonErrorCodeEnum.NEW_PASSWORD_CANNOT_BE_NULL)
                .on(passwordUpdateDto.getNewPassword().equals(passwordUpdateDto.getOldPassword()), CommonErrorCodeEnum.PASSWORD_SAME)
                .doValidate().checkResult();
        //获取教师信息并校验
        StudentEntity studentEntity = studentService.getById(passwordUpdateDto.getId());
        ValidateHandler.checkParameter(studentEntity == null, SystemErrorCodeEnum.STUDENT_NOT_EXISTS);

        String oldPassword = EncryptUtil.encryptPassword(passwordUpdateDto.getOldPassword());
        String newPassword = EncryptUtil.encryptPassword(passwordUpdateDto.getNewPassword());
        //旧密码错误
        ValidateHandler.checkParameter(!oldPassword.equals(studentEntity.getStuPassword()), CommonErrorCodeEnum.OLD_PASSWORD_ERROR);
        StudentDto studentDto = new StudentDto();
        studentDto.setId(passwordUpdateDto.getId());
        studentDto.setUpdateBy(RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class));
        studentDto.setStuPassword(newPassword);
        studentDto.setPwdUpdateTime(new Date());
        studentService.update(studentDto);
        return ResponseEntityUtil.ok(JsonResult.buildMsg("修改成功"));
    }

    @Log("上传头像")
    @DeleteUserRedis
    @ApiOperation("上传头像")
    @PermissionName(source = "student:uploadPhoto", name = "上传头像", group = "学生管理")
    @PostMapping("/uploadPhoto")
    public ResponseEntity<JsonResult> uploadPhoto(@RequestParam("photo") MultipartFile photo, @RequestParam("id") Long id) throws IOException, InterruptedException {
        ValidatorBuilder.build()
                //参数非空校验
                .on(StringUtil.isEmpty(id), SystemErrorCodeEnum.ID_CANNOT_BE_NULL)
                .on(photo == null || photo.isEmpty(), CommonErrorCodeEnum.FILE_CANNOT_BE_NULL)
                //文件类型篡改校验
                .on(!FileUtil.fileTypeCheck(photo), CommonErrorCodeEnum.FILE_TYPE_TAMPER)
                //文件类型校验
                .on(!FileUtil.fileTypeRuleCheck(photo, FileUtil.IMAGE_TYPE), CommonErrorCodeEnum.FILE_TYPE_ERROR)
                //文件大小校验
                .on(photo.getSize() > defaultConfigParameters.getPhotoMaxSize(), CommonErrorCodeEnum.FILE_SIZE_ERROR)
                .doValidate().checkResult();
        //获取用户信息
        StudentEntity studentEntity = studentService.getById(id);
        //判断是否已有头像，有则先删除
        if (StringUtil.isNotEmpty(studentEntity.getStuPhoto())) {
            fastDfsClientWrapper.deleteFile(studentEntity.getStuPhoto());
        }
        //保存文件
        String url = fastDfsClientWrapper.uploadFile(photo);
        //更新url
        StudentDto studentDto = new StudentDto();
        studentDto.setId(id);
        studentDto.setStuPhoto(url);
        studentDto.setUpdateBy(RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class));
        studentService.update(studentDto);
        //记录文件
        monitorManageRemoting.addFileRecord(getFileRecordDto(url, photo,
                TableFileEnum.TEACHER_TABLE.getTableName(), id,
                RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class)));
        //返回信息
        return ResponseEntityUtil.ok(JsonResult.buildMsg("上传成功"));
    }

    @Log(value = "根据email查询学生", devModel = true)
    @ApiOperation("根据email查询学生")
    @PermissionName(source = "student:getByStuEmail", name = "根据email查询学生", group = "学生管理")
    @GetMapping("/getByStuEmail")
    public ResponseEntity<JsonResult<StudentEntity>> getByStuEmail(String stuEmail) {
        ValidateHandler.checkParameter(StringUtils.isEmpty(stuEmail), SystemErrorCodeEnum.EMAIL_CANNOT_BE_NULL);
        StudentEntity exists = studentService.getByStuEmail(stuEmail);
        return ResponseEntityUtil.ok(JsonResult.buildData(exists));
    }

    @Log("下载学生导入模板")
    @ApiOperation("下载学生导入模板")
    @PermissionName(source = "student:downloadTemplate", name = "下载学生导入模板", group = "学生管理")
    @GetMapping("/downloadTemplate")
    public ResponseEntity downloadTemplate() {
        byte[] courseFileBytes = ExcelUtil.createTemplate(StudentExcel.class);
        //设置head
        HttpHeaders headers = new HttpHeaders();
        //文件的属性名
        headers.setContentDispositionFormData("attachment", new String(("学生导入模板.xlsx").getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        //响应内容是字节流
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntityUtil.ok(headers, courseFileBytes);
    }

    @Log("导入学生信息")
    @ApiOperation("导入学生信息")
    @PermissionName(source = "student:importExcel", name = "导入学生信息", group = "学生管理")
    @PostMapping("/importExcel")
    public ResponseEntity<JsonResult<Map<Integer, StudentExcel>>> importExcel(MultipartFile file) throws IOException {
        ValidatorBuilder.build()
                .ofNull(file, CommonErrorCodeEnum.FILE_CANNOT_BE_NULL)
                //文件类型篡改校验
                .on(!FileUtil.fileTypeCheck(file), CommonErrorCodeEnum.FILE_TYPE_TAMPER)
                .on(!FileUtil.fileTypeRuleCheck(file, FileUtil.IMPORT_TYPE), CommonErrorCodeEnum.FILE_TYPE_ERROR)
                .doValidate().checkResult();
        log.info("管理员：{}，开始导入学生信息", RequestHolderUtil.getAttribute(MicroConstant.USERNAME, String.class));
        StudentExcelListener listener = new StudentExcelListener(studentService);
        ExcelUtil.readExcelToList(StudentExcel.class, file.getInputStream(), listener);
        return ResponseEntityUtil.ok(JsonResult.buildDataMsg(listener.getErrorMap(), CollectionUtils.isEmpty(listener.getErrorMap()) ? "导入成功" : "以下记录导入失败，请检查是否已存在或信息是否正确"));
    }
}
