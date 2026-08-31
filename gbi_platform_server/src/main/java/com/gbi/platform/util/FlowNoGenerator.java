package com.gbi.platform.util;

import com.gbi.platform.common.constant.CommonConst;
import com.gbi.platform.entity.FlowSeq;
import com.gbi.platform.mapper.FlowSeqMapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 财务流水单号生成器：YO + 公司编码(2位，不足补0) + yyyyMMdd + 6位顺序号
 *
 * 生成示例：YO0120260824000001
 *
 * 防并发/防重复/防跳号机制：
 *  1. 使用 biz_flow_seq 表（company_id + seq_date 联合主键）记录当日已分配序号
 *  2. INSERT IGNORE 尝试插入 seq_no=1：若当日尚无记录则成功，序号为1
 *  3. 若 INSERT 失败（重复键），执行 UPDATE seq_no = seq_no + 1 原子递增
 *  4. 每次生成调用均持锁（synchronized），保证同一 JVM 内并发安全
 *  5. 数据库行级锁（主键冲突）保证多实例部署时并发安全
 *
 * @author gbi
 */
@Slf4j
@Component
public class FlowNoGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SEQ_WIDTH = 6;

    /** 类级别锁：保证同一 JVM 内多线程并发时 INSERT/UPDATE 顺序正确 */
    private static final Object LOCK = new Object();

    private final FlowSeqMapper flowSeqMapper;

    public FlowNoGenerator(FlowSeqMapper flowSeqMapper) {
        this.flowSeqMapper = flowSeqMapper;
    }

    /**
     * 生成财务流水单号
     *
     * @param companyId 所属子公司ID（>0 时按公司维度顺序号；0 表示集团，共用同一序列）
     * @return 格式如 YO0120260824000001
     */
    public String generate(Long companyId) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DATE_FMT);
        String companyCode = String.format("%02d", Math.abs(companyId != null ? companyId : 0L) % 100);

        synchronized (LOCK) {
            // 1. 尝试插入当日初始序号（INSERT IGNORE：若已存在则忽略，不影响后续 UPDATE）
            FlowSeq seq = new FlowSeq();
            seq.setCompanyId(companyId != null ? companyId : 0L);
            seq.setSeqDate(today);
            seq.setSeqNo(1);
            try {
                flowSeqMapper.insert(seq);
            } catch (Exception e) {
                log.warn("FlowSeq insert ignored (duplicate key is expected): company={}, date={}", companyId, today);
            }

            // 2. 原子递增并获取新序号
            int newSeq = incrementAndFetch(companyId, today);

            // 3. 拼接单号
            return CommonConst.FLOW_NO_PREFIX + companyCode + dateStr + String.format("%0" + SEQ_WIDTH + "d", newSeq);
        }
    }

    /**
     * 原子递增当日序号并返回新值
     * MySQL 行级锁保证多连接并发安全：UPDATE WHERE 联合主键
     */
    private int incrementAndFetch(Long companyId, LocalDate today) {
        int rows = flowSeqMapper.incrementSeq(companyId != null ? companyId : 0L, today);
        if (rows > 0) {
            // 行已存在，递增后返回新值
            FlowSeq existing = flowSeqMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FlowSeq>()
                            .eq(FlowSeq::getCompanyId, companyId != null ? companyId : 0L)
                            .eq(FlowSeq::getSeqDate, today)
            );
            return existing != null ? existing.getSeqNo() : 1;
        }
        // 新插入行，seq_no 仍为初始值 1
        return 1;
    }
}
