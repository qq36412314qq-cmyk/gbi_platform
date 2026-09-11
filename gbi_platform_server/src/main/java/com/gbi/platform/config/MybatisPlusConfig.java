package com.gbi.platform.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.gbi.platform.common.security.LoginUser;
import com.gbi.platform.common.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * MyBatis-Plus 配置：分页插件 + 多租户 company_id 自动过滤 + 审计字段自动填充
 * 逻辑删除由 @TableLogic 注解 + application.yml 中 logic-delete-field 配置实现
 *
 * @author gbi
 */
@Slf4j
@Configuration
public class MybatisPlusConfig {

    /** 集团全局公共表：不做 company_id 自动过滤，由 service 层按需手动处理 */
    private static final Set<String> GLOBAL_TABLES = Set.of(
            "sys_dict_type", "sys_dict_data", "sys_menu",
            "sys_user_role_rel", "sys_role_menu_rel",
            "sys_audit_log", "sys_permission_audit",
            "sys_config",
            "sys_ding_sync_record", "biz_kingdee_push", "finance_pay_flow",
            "flow_definition", "biz_discount_policy",
            "hr_attendance_record", "hr_salary_archive", "hr_salary_month", "hr_social_security"
    );

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new MyTenantLineHandler()));
        return interceptor;
    }

    /** 多租户处理器实现类 */
    static class MyTenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {
        @Override
        public Expression getTenantId() {
            LoginUser user = UserContext.getLoginUser();
            return new LongValue(user.getCompanyId());
        }
        @Override
        public String getTenantIdColumn() { return "company_id"; }
        @Override
        public boolean ignoreTable(String tableName) {
            LoginUser user = UserContext.getLoginUserOrNull();
            if (user == null || user.isSuperAdmin()) { return true; }
            return GLOBAL_TABLES.contains(tableName.toLowerCase());
        }
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                Long userId = UserContext.getUserIdOrZero();
                LocalDateTime now = LocalDateTime.now();
                strictInsertFill(metaObject, "createBy", Long.class, userId);
                strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updateBy", Long.class, userId);
                strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
            }
            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updateBy", Long.class, UserContext.getUserIdOrZero());
                strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}