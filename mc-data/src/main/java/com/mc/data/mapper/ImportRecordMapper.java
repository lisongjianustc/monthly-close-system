package com.mc.data.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mc.data.entity.ImportRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 导入记录Mapper
 */
@Mapper
public interface ImportRecordMapper extends BaseMapper<ImportRecord> {

    /**
     * 查询批次的所有记录
     */
    List<ImportRecord> selectByBatchId(@Param("batchId") Long batchId);

    /**
     * 查询失败记录
     */
    List<ImportRecord> selectFailRecords(@Param("batchId") Long batchId);

    /**
     * 按期间和单位查询记录数
     */
    Integer countByPeriodAndUnit(@Param("periodId") Long periodId, @Param("unitId") Long unitId);

    /**
     * 批量插入（MySQL批处理，1000条一批）
     */
    @Insert("<script>" +
            "INSERT INTO import_record (batch_id, row_no, account_code, account_name, amount, " +
            "dimension1, dimension2, dimension3, status, error_msg) VALUES " +
            "<foreach collection='records' item='r' separator=','>" +
            "(#{r.batchId}, #{r.rowNo}, #{r.accountCode}, #{r.accountName}, #{r.amount}, " +
            "#{r.dimension1}, #{r.dimension2}, #{r.dimension3}, #{r.status}, #{r.errorMsg})" +
            "</foreach>" +
            "</script>")
    void insertBatchForMySQL(@Param("records") List<ImportRecord> records);

    /**
     * 批量更新状态（用于validateData后的批量更新）
     */
    void updateStatusBatch(@Param("records") List<ImportRecord> records);
}
