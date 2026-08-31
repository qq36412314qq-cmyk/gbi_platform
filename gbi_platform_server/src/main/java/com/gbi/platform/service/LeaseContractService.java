package com.gbi.platform.service;

import com.gbi.platform.dto.ContractAddDTO;
import com.gbi.platform.dto.ContractQueryDTO;
import com.gbi.platform.dto.ContractTerminateDTO;
import com.gbi.platform.entity.StallContract;
import com.gbi.platform.vo.ContractVO;
import com.gbi.platform.vo.PageVO;

import java.util.Collection;
import java.util.Map;

/**
 * 租赁合同服务（租赁管理-合同，生效联动摊位状态与押金流水）
 *
 * @author gbi
 */
public interface LeaseContractService {

    /** 合同分页查询（含租户/摊位/分类名称） */
    PageVO<ContractVO> page(ContractQueryDTO dto);

    /** 新增合同：校验摊位空置，生效后摊位置为已租，押金写收入流水 */
    void add(ContractAddDTO dto);

    /** 退租（高危操作）：合同终止、摊位置空、押金退费支出流水、强制审计 */
    void terminate(ContractTerminateDTO dto);

    /** 租户是否存在生效合同（供租户删除前置校验） */
    boolean hasEffectiveContractByTenant(Long tenantId);

    /** 摊位是否存在合同（供摊位删除前置校验） */
    boolean hasAnyContractByStall(Long stallId);

    /** 激活合同：签约中 -> 生效中（缴费完成后调用） */
    void activateContract(Long contractId);
    /* ------------------------------ 跨模块只读能力（应收应付计划模块复用） ------------------------------ */

    /** 按 ID 取合同实体（供计划生成/红冲链读取合同数据） */
    StallContract getContractById(Long contractId);

    /** 批量组装摊位编号（供计划列表展示） */
    Map<Long, String> mapStallNumbers(Collection<Long> stallIds);

    /** 批量组装租户名称（供计划列表展示） */
    Map<Long, String> mapTenantNames(Collection<Long> tenantIds);
}
