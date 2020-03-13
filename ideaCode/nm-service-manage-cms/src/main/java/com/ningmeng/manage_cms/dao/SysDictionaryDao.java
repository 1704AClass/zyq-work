package com.ningmeng.manage_cms.dao;

import com.ningmeng.framework.domain.system.SysDictionary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by 12699 on 2020/2/20.
 */
@Repository
public interface SysDictionaryDao extends MongoRepository<SysDictionary,String> {
    //根据字典分类查询字典信息
    SysDictionary findBydType(String dType);
}
