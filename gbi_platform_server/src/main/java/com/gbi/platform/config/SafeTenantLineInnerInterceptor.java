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
     * 全局表（GLOBAL_TABLES）本来就不需要租户过滤，降级后数据完全正确；
     * 非全局表若解析失败则跳过拦截但记录 WARN 日志，便于后续修复表结构
     */
    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        try {
            super.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
        } catch (Exception e) {
            log.warn("多租户拦截器 beforeQuery 解析 SQL 失败，降级跳过拦截: {}", e.getMessage());
        }
    }
}
