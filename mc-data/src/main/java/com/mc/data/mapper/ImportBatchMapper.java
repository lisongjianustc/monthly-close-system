package com.mc.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.data.entity.ImportBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导入批次Mapper
 */
@Mapper
public interface ImportBatchMapper extends BaseMapper<ImportBatch> {

    /**
     * 查询批次详情
     */
    ImportBatch selectByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 查询用户的导入记录
     */
    List<ImportBatch> selectByUserId(@Param("userId") Long userId);
}
