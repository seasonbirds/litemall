package org.linlinjava.litemall.db.dao;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.linlinjava.litemall.db.domain.LitemallSensitiveWord;
import org.linlinjava.litemall.db.domain.LitemallSensitiveWordExample;

public interface LitemallSensitiveWordMapper {
    long countByExample(LitemallSensitiveWordExample example);

    int deleteByExample(LitemallSensitiveWordExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(LitemallSensitiveWord record);

    int insertSelective(LitemallSensitiveWord record);

    LitemallSensitiveWord selectOneByExample(LitemallSensitiveWordExample example);

    LitemallSensitiveWord selectOneByExampleSelective(@Param("example") LitemallSensitiveWordExample example, @Param("selective") LitemallSensitiveWord.Column ... selective);

    List<LitemallSensitiveWord> selectByExampleSelective(@Param("example") LitemallSensitiveWordExample example, @Param("selective") LitemallSensitiveWord.Column ... selective);

    List<LitemallSensitiveWord> selectByExample(LitemallSensitiveWordExample example);

    LitemallSensitiveWord selectByPrimaryKeySelective(@Param("id") Integer id, @Param("selective") LitemallSensitiveWord.Column ... selective);

    LitemallSensitiveWord selectByPrimaryKey(Integer id);

    LitemallSensitiveWord selectByPrimaryKeyWithLogicalDelete(@Param("id") Integer id, @Param("andLogicalDeleted") boolean andLogicalDeleted);

    int updateByExampleSelective(@Param("record") LitemallSensitiveWord record, @Param("example") LitemallSensitiveWordExample example);

    int updateByExample(@Param("record") LitemallSensitiveWord record, @Param("example") LitemallSensitiveWordExample example);

    int updateByPrimaryKeySelective(LitemallSensitiveWord record);

    int updateByPrimaryKey(LitemallSensitiveWord record);

    int logicalDeleteByExample(@Param("example") LitemallSensitiveWordExample example);

    int logicalDeleteByPrimaryKey(Integer id);
}
