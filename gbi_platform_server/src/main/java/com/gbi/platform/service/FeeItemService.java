package com.gbi.platform.service;

import com.gbi.platform.dto.FeeItemDTO;
import com.gbi.platform.dto.FeeItemQueryDTO;
import com.gbi.platform.vo.FeeItemVO;
import com.gbi.platform.vo.PageVO;

import java.util.List;

/**
 * 自定义收费类型服务（租金/物业费/水费/电费/押金/其他等，子公司可配置）
 *
 * @author gbi
 */
public interface FeeItemService {

    /** 全量列表（供收费规则页下拉选择，company_id 自动隔离） */
    List<FeeItemVO> list(FeeItemQueryDTO dto);

    /** 收费类型分页 */
    PageVO<FeeItemVO> page(FeeItemQueryDTO dto);

    /** 新增收费类型（同公司同名唯一） */
    void add(FeeItemDTO dto);

    /** 编辑收费类型 */
    void update(FeeItemDTO dto);

    /** 删除收费类型（被收费规则引用禁止删除，逻辑删除） */
    void delete(Long id);
}