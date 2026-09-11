package com.gbi.platform.service;

import com.gbi.platform.dto.DiscountPolicyQueryDTO;
import com.gbi.platform.entity.BizDiscountPolicy;
import com.gbi.platform.vo.DiscountPolicyVO;
import com.gbi.platform.vo.PageVO;

/**
 * 优惠策略服务（集团模板 company_id=0 全子公司可见，子公司可自定义覆盖）
 * 优惠类型：免租期/折扣率/减免金额/组合/定额/阶梯
 *
 * @author gbi
 */
public interface DiscountPolicyService {

    PageVO<DiscountPolicyVO> page(DiscountPolicyQueryDTO dto);

    void add(BizDiscountPolicy policy);

    void update(BizDiscountPolicy policy);

    void delete(Long id);
}