package com.gbi.platform.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gbi.platform.common.exception.BizException;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 统一审批流程节点配置工具：解析 / 序列化 node_config_json
 * 对齐《应收应付计划+统一审批引擎+优惠管理模块设计规范》3.3：
 * 前端表单生成 JSON，禁止自由文本公式；入库前后端双重过滤脚本字符
 *
 * @author gbi
 */
public final class FlowConfigUtil {

    private FlowConfigUtil() {
    }

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 禁止入库的恶意脚本关键字（后端二次过滤，前端已预过滤） */
    private static final String[] FORBIDDEN = {"<script", "</script>", "onload", "onclick", "onerror", "eval(", "javascript:", "prompt(", "alert("};

    /**
     * 过滤恶意脚本字符（SQL/XSS/JSON 注入防护）
     */
    public static String sanitize(String json) {
        if (json == null) {
            return null;
        }
        String s = json;
        for (String f : FORBIDDEN) {
            s = s.replace(f, "");
        }
        return s;
    }

    /**
     * 解析节点配置 JSON 为节点规范列表
     *
     * @param nodeConfigJson 前端表单生成的 JSON（已前端过滤，后端再次清洗）
     * @return 节点列表，非法配置抛出 BizException
     */
    public static List<NodeSpec> parseNodes(String nodeConfigJson) {
        if (!StringUtils.hasText(nodeConfigJson)) {
            return Collections.emptyList();
        }
        try {
            var root = MAPPER.readTree(sanitize(nodeConfigJson));
            // 兼容两种存储格式：{"nodes":[...]}（toJson 标准格式）与裸数组 [...]（v2.0 初始数据格式）
            JsonNode nodesArr = root.isArray() ? root : root.path("nodes");
            if (!nodesArr.isArray()) {
                throw new BizException("流程节点配置格式错误");
            }
            List<NodeSpec> list = new ArrayList<>();
            for (var n : nodesArr) {
                NodeSpec spec = new NodeSpec();
                spec.setNodeName(n.path("nodeName").asText("未命名节点"));
                spec.setNodeMode(n.path("nodeMode").asText("single"));
                spec.setHandlerType(n.path("handlerType").asText("role"));
                spec.setHandlerValue(n.path("handlerValue").asText());
                List<String> copyTo = new ArrayList<>();
                for (var c : n.path("copyTo")) {
                    copyTo.add(c.asText());
                }
                spec.setCopyTo(copyTo);
                list.add(spec);
            }
            if (list.isEmpty()) {
                throw new BizException("流程至少需要一个审批节点");
            }
            return list;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("流程节点配置解析失败");
        }
    }

    /**
     * 节点列表序列化为 JSON（前端回显）
     */
    public static String toJson(List<NodeSpec> nodes) {
        try {
            return MAPPER.writeValueAsString(Map.of("nodes", nodes));
        } catch (Exception e) {
            throw new BizException("流程节点配置序列化失败");
        }
    }

    /**
     * 节点规范（解析后的结构体）
     */
    @Data
    public static class NodeSpec {
        /** 节点名称 */
        private String nodeName;
        /** 节点模式 single单人 / multi会签 */
        private String nodeMode;
        /** 审批人类型 role/user/submitter */
        private String handlerType;
        /** 角色编码或用户ID */
        private String handlerValue;
        /** 抄送角色/用户集合（不阻塞流转） */
        private List<String> copyTo;
    }
}