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

@RestController
@RequestMapping("/admin/sensitive")
@Validated
public class AdminSensitiveWordController {
    private final Log logger = LogFactory.getLog(AdminSensitiveWordController.class);

    @Autowired
    private LitemallSensitiveWordService sensitiveWordService;

    @Autowired
    private SensitiveWordCacheManager sensitiveWordCacheManager;

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

    private Object validate(LitemallSensitiveWord sensitiveWord) {
        String word = sensitiveWord.getWord();
        if (StringUtils.isEmpty(word)) {
            return ResponseUtil.badArgument();
        }
        return null;
    }

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

    @RequiresPermissions("admin:sensitive:read")
    @RequiresPermissionsDesc(menu = {"系统管理", "敏感词管理"}, button = "详情")
    @GetMapping("/read")
    public Object read(@NotNull Integer id) {
        LitemallSensitiveWord sensitiveWord = sensitiveWordService.findById(id);
        return ResponseUtil.ok(sensitiveWord);
    }

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
