package org.linlinjava.litemall.core.sensitive;

import org.linlinjava.litemall.db.domain.LitemallSensitiveWord;
import org.linlinjava.litemall.db.service.LitemallSensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class SensitiveWordCacheManager {

    @Autowired
    private LitemallSensitiveWordService sensitiveWordService;

    private final Set<String> sensitiveWords = new ConcurrentSkipListSet<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        List<LitemallSensitiveWord> enabledWords = sensitiveWordService.queryAllEnabled();
        sensitiveWords.clear();
        for (LitemallSensitiveWord word : enabledWords) {
            sensitiveWords.add(word.getWord());
        }
    }

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

    public Set<String> getSensitiveWords() {
        return sensitiveWords;
    }
}
