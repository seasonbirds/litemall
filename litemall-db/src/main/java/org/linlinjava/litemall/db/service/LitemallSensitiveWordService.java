package org.linlinjava.litemall.db.service;

import com.github.pagehelper.PageHelper;
import org.linlinjava.litemall.db.dao.LitemallSensitiveWordMapper;
import org.linlinjava.litemall.db.domain.LitemallSensitiveWord;
import org.linlinjava.litemall.db.domain.LitemallSensitiveWordExample;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LitemallSensitiveWordService {
    @Resource
    private LitemallSensitiveWordMapper sensitiveWordMapper;

    public List<LitemallSensitiveWord> querySelective(String word, Boolean enabled, Integer page, Integer limit, String sort, String order) {
        LitemallSensitiveWordExample example = new LitemallSensitiveWordExample();
        LitemallSensitiveWordExample.Criteria criteria = example.createCriteria();

        if (!StringUtils.isEmpty(word)) {
            criteria.andWordLike("%" + word + "%");
        }
        if (enabled != null) {
            criteria.andEnabledEqualTo(enabled);
        }
        criteria.andDeletedEqualTo(false);

        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, limit);
        return sensitiveWordMapper.selectByExample(example);
    }

    public LitemallSensitiveWord findById(Integer id) {
        return sensitiveWordMapper.selectByPrimaryKey(id);
    }

    public void add(LitemallSensitiveWord sensitiveWord) {
        sensitiveWord.setAddTime(LocalDateTime.now());
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        sensitiveWordMapper.insertSelective(sensitiveWord);
    }

    public int updateById(LitemallSensitiveWord sensitiveWord) {
        sensitiveWord.setUpdateTime(LocalDateTime.now());
        return sensitiveWordMapper.updateByPrimaryKeySelective(sensitiveWord);
    }

    public void deleteById(Integer id) {
        sensitiveWordMapper.logicalDeleteByPrimaryKey(id);
    }

    public boolean checkExist(String word) {
        LitemallSensitiveWordExample example = new LitemallSensitiveWordExample();
        example.or().andWordEqualTo(word).andDeletedEqualTo(false);
        return sensitiveWordMapper.countByExample(example) != 0;
    }

    public List<LitemallSensitiveWord> queryAllEnabled() {
        LitemallSensitiveWordExample example = new LitemallSensitiveWordExample();
        example.or().andEnabledEqualTo(true).andDeletedEqualTo(false);
        return sensitiveWordMapper.selectByExample(example);
    }
}
