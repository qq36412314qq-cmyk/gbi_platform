package com.gbi.platform.config;

import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

/**
 * 增强版多租户拦截器：JsqlParser 解析失败时自动降级跳过
 * 解决 hr_attendance_record 等含 day/month/type 列名的表，被 JsqlParser 错误解析导致解析异常
 *
 * @author gbi
 */
@Slf4j
public class SafeTenantLineInnerInterceptor extends TenantLineInnerInterceptor {

    public SafeTenantLineInnerInterceptor(TenantLineHandler handler) {
        super(handler);
    }

    /**
     * 查询前：捕获 JsqlParser 解析异常后降级（不追加租户条件，直接执行原始 SQL）
     * 若原始 SQL 已手动写了 company_id 条件，说明业务代码已自行处理租户隔离，
     * 此时解析失败降级是预期行为，打 DEBUG 而非 WARN，避免噪音日志刷屏
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        try {
            super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        } catch (Exception e) {
            String rawSql = boundSql.getSql();
            // 检测 SQL 是否已手动包含 company_id 条件（兼容 #{companyId} / ? 两种占位符形态）
            boolean hasManualTenant = rawSql != null
                    && (rawSql.contains("company_id") || rawSql.contains("companyId"))
                    && rawSql.toLowerCase().contains("where");
            if (hasManualTenant) {
                log.debug("多租户拦截器跳过（SQL 已手动包含 company_id 条件，解析失败降级是预期行为）: {}",
                        rawSql.length() > 120 ? rawSql.substring(0, 120) + "..." : rawSql);
            } else {
                log.warn("多租户拦截器 beforeQuery 解析 SQL 失败，降级跳过拦截（可能存在数据越权风险）: {}", e.getMessage());
            }
        }
    }
}
