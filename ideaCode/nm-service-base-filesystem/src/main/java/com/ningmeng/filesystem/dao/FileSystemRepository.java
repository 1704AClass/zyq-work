package com.ningmeng.filesystem.dao;

import com.ningmeng.framework.domain.filesystem.FileSystem;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Created by 86181 on 2020/2/21.
 */
public interface FileSystemRepository extends MongoRepository<FileSystem,String>{
}
