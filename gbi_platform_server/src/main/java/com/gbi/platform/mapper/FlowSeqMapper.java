package com.gbi.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gbi.platform.entity.FlowSeq;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 财务流水单号顺序号计数器 Mapper
 *
 * @author gbi
 */
@Mapper
public interface FlowSeqMapper extends BaseMapper<FlowSeq> {

    /**
     * 原子递增当日序号：先尝试插入 seq_no=1（忽略重复键），再更新已有行 seq_no+1
     * 由 FlowNoGenerator 通过 INSERT IGNORE + UPDATE 组合调用，此处提供便捷方法
     */
    @Update("UPDATE biz_flow_seq SET seq_no = seq_no + 1, update_time = NOW() " +
            "WHERE company_id = #{companyId} AND seq_date = #{seqDate}")
    int incrementSeq(@Param("companyId") Long companyId, @Param("seqDate") java.time.LocalDate seqDate);
}
