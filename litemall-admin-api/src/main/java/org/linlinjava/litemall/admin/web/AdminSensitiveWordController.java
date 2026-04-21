package org.linlinjava.litemall.admin.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.linlinjava.litemall.admin.annotation.RequiresPermissionsDesc;
import org.linlinjava.litemall.core.sensitive.SensitiveWordCacheManager;
import org.linlinjava.litemall.core.util.ResponseUtil;
import org.linlinjava.litemall.core.validator.Order;
import org.linlinjava.litemall.core.validator.Sort;
import org.linlinjava.litemall.db.domain.LitemallSensitiveWord;
import org.linlinjava.litemall.db.service.LitemallSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 后台管理系统敏感词管理控制器
 * <p>
 * 功能说明：
 * 1. 提供敏感词的增删改查接口
 * 2. 提供敏感词的启用/禁用接口
 * 3. 所有修改操作后自动刷新本地缓存
 * <p>
 * 注意事项：
 * - 本控制器的接口路径包含 /admin/sensitive/，在Filter中被排除，不会进行敏感词过滤
 * - 这样设计是为了允许管理员添加/修改敏感词，否则无法添加新的敏感词
 */
@RestController
@RequestMapping("/admin/sensitive")
@Validated
public class AdminSensitiveWordController {
    private final Log logger = LogFactory.getLog(AdminSensitiveWordController.class);

    @Autowired
    private LitemallSensitiveWordService sensitiveWordService;

    /**
     * 敏感词缓存管理器
     * 用于在敏感词发生变化时刷新本地缓存
     */
    @Autowired
    private SensitiveWordCacheManager sensitiveWordCacheManager;

    /**
     * 查询敏感词列表
     * <p>
     * 权限：admin:sensitive:list
     *
     * @param word    敏感词关键字（模糊查询）
     * @param enabled 状态（启用/禁用）
     * @param page    页码
     * @param limit   每页数量
     * @param sort    排序字段
     * @param order   排序方式（asc/desc）
     * @return 分页列表
     */
    @RequiresPermissions("admin:sensitive:list")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "查询")
    @GetMapping("/list")
    public Object list(String word, Boolean enabled,
                       @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order) {
        List<LitemallSensitiveWord> sensitiveWordList = sensitiveWordService.querySelective(word, enabled, page, limit, sort, order);
        return ResponseUtil.okList(sensitiveWordList);
    }

    /**
     * 验证敏感词对象的必填字段
     *
     * @param sensitiveWord 敏感词对象
     * @return 验证失败返回错误响应，验证通过返回null
     */
    private Object validate(LitemallSensitiveWord sensitiveWord) {
        String word = sensitiveWord.getWord();
        if (StringUtils.isEmpty(word)) {
            return ResponseUtil.badArgument();
        }
        return null;
    }

    /**
     * 新增敏感词
     * <p>
     * 权限：admin:sensitive:create
     * <p>
     * 注意：
     * 1. 检查敏感词是否已存在
     * 2. 默认状态为启用
     * 3. 如果启用状态，添加后自动刷新缓存
     *
     * @param sensitiveWord 敏感词对象
     * @return 添加结果
     */
    @RequiresPermissions("admin:sensitive:create")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody LitemallSensitiveWord sensitiveWord) {
        Object error = validate(sensitiveWord);
        if (error != null) {
            return error;
        }
        if (sensitiveWordService.checkExist(sensitiveWord.getWord())) {
            return ResponseUtil.fail(502, "敏感词已存在");
        }
        if (sensitiveWord.getEnabled() == null) {
            sensitiveWord.setEnabled(true);
        }
        sensitiveWordService.add(sensitiveWord);
        if (Boolean.TRUE.equals(sensitiveWord.getEnabled())) {
            sensitiveWordCacheManager.refreshCache();
        }
        return ResponseUtil.ok(sensitiveWord);
    }

    /**
     * 获取敏感词详情
     * <p>
     * 权限：admin:sensitive:read
     *
     * @param id 敏感词ID
     * @return 敏感词详情
     */
    @RequiresPermissions("admin:sensitive:read")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "详情")
    @GetMapping("/read")
    public Object read(@NotNull Integer id) {
        LitemallSensitiveWord sensitiveWord = sensitiveWordService.findById(id);
        return ResponseUtil.ok(sensitiveWord);
    }

    /**
     * 更新敏感词
     * <p>
     * 权限：admin:sensitive:update
     * <p>
     * 注意：更新后自动刷新缓存，因为可能修改了敏感词内容或状态
     *
     * @param sensitiveWord 敏感词对象
     * @return 更新结果
     */
    @RequiresPermissions("admin:sensitive:update")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody LitemallSensitiveWord sensitiveWord) {
        Object error = validate(sensitiveWord);
        if (error != null) {
            return error;
        }
        if (sensitiveWordService.updateById(sensitiveWord) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        sensitiveWordCacheManager.refreshCache();
        return ResponseUtil.ok(sensitiveWord);
    }

    /**
     * 删除敏感词
     * <p>
     * 权限：admin:sensitive:delete
     * <p>
     * 注意：删除后自动刷新缓存
     *
     * @param sensitiveWord 敏感词对象（只需包含ID）
     * @return 删除结果
     */
    @RequiresPermissions("admin:sensitive:delete")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody LitemallSensitiveWord sensitiveWord) {
        Integer id = sensitiveWord.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        sensitiveWordService.deleteById(id);
        sensitiveWordCacheManager.refreshCache();
        return ResponseUtil.ok();
    }

    /**
     * 启用敏感词
     * <p>
     * 权限：admin:sensitive:enable
     * <p>
     * 注意：启用后自动刷新缓存，使该敏感词生效
     *
     * @param sensitiveWord 敏感词对象（只需包含ID）
     * @return 启用结果
     */
    @RequiresPermissions("admin:sensitive:enable")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "启用")
    @PostMapping("/enable")
    public Object enable(@RequestBody LitemallSensitiveWord sensitiveWord) {
        Integer id = sensitiveWord.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        LitemallSensitiveWord word = sensitiveWordService.findById(id);
        if (word == null) {
            return ResponseUtil.badArgumentValue();
        }
        word.setEnabled(true);
        if (sensitiveWordService.updateById(word) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        sensitiveWordCacheManager.refreshCache();
        return ResponseUtil.ok();
    }

    /**
     * 禁用敏感词
     * <p>
     * 权限：admin:sensitive:disable
     * <p>
     * 注意：禁用后自动刷新缓存，使该敏感词不再生效
     *
     * @param sensitiveWord 敏感词对象（只需包含ID）
     * @return 禁用结果
     */
    @RequiresPermissions("admin:sensitive:disable")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "禁用")
    @PostMapping("/disable")
    public Object disable(@RequestBody LitemallSensitiveWord sensitiveWord) {
        Integer id = sensitiveWord.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        LitemallSensitiveWord word = sensitiveWordService.findById(id);
        if (word == null) {
            return ResponseUtil.badArgumentValue();
        }
        word.setEnabled(false);
        if (sensitiveWordService.updateById(word) == 0) {
            return ResponseUtil.updatedDataFailed();
        }
        sensitiveWordCacheManager.refreshCache();
        return ResponseUtil.ok();
    }
}
