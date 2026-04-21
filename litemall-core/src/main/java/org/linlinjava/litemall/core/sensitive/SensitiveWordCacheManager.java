package org.linlinjava.litemall.core.sensitive;

import org.linlinjava.litemall.db.domain.LitemallSensitiveWord;
import org.linlinjava.litemall.db.service.LitemallSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 敏感词本地缓存管理器
 * <p>
 * 功能说明：
 * 1. 使用ConcurrentSkipListSet作为本地缓存，存储所有启用状态的敏感词
 * 2. 应用启动时自动从数据库加载敏感词到缓存
 * 3. 当敏感词状态发生变化时（增删改、启用禁用），调用refreshCache()刷新缓存
 * 4. 提供敏感词检测方法，供Filter调用
 * <p>
 * 设计说明：
 * - 使用ConcurrentSkipListSet而非HashSet，因为它是线程安全的
 * - ConcurrentSkipListSet支持更高的并发读写性能
 * - 有序特性对于敏感词匹配算法（如DFA算法）有潜在优化空间
 */
@Component
public class SensitiveWordCacheManager {

    @Autowired
    private LitemallSensitiveWordService sensitiveWordService;

    /**
     * 本地缓存的敏感词集合
     * 使用ConcurrentSkipListSet确保线程安全，支持高并发读写
     */
    private final Set<String> sensitiveWords = new ConcurrentSkipListSet<>();

    /**
     * 初始化方法，在Bean创建后自动执行
     * 从数据库加载所有启用的敏感词到本地缓存
     */
    @PostConstruct
    public void init() {
        refreshCache();
    }

    /**
     * 刷新敏感词缓存
     * <p>
     * 调用时机：
     * 1. 应用启动时（@PostConstruct）
     * 2. 新增敏感词时（如果启用状态）
     * 3. 更新敏感词时
     * 4. 删除敏感词时
     * 5. 启用/禁用敏感词时
     */
    public void refreshCache() {
        List<LitemallSensitiveWord> enabledWords = sensitiveWordService.queryAllEnabled();
        sensitiveWords.clear();
        for (LitemallSensitiveWord word : enabledWords) {
            sensitiveWords.add(word.getWord());
        }
    }

    /**
     * 检测文本中是否包含敏感词
     *
     * @param text 待检测的文本
     * @return true-包含敏感词，false-不包含敏感词
     * <p>
     * 实现说明：
     * 1. 遍历缓存中的所有敏感词
     * 2. 检查文本是否包含任意一个敏感词
     * 3. 一旦找到敏感词立即返回true
     * <p>
     * 性能说明：
     * - 对于大量敏感词（>1000），当前实现可能存在性能问题
     * - 优化建议：可考虑使用DFA算法或AC自动机提高匹配效率
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (String sensitiveWord : sensitiveWords) {
            if (text.contains(sensitiveWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取当前缓存的所有敏感词
     * 主要用于调试和监控
     *
     * @return 敏感词集合的不可变视图
     */
    public Set<String> getSensitiveWords() {
        return sensitiveWords;
    }
}
