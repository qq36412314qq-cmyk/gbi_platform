package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.vo.PageVO;

public interface HrTransferService {
    PageVO<HrEntryApplyVO> pageEntry(Long pageNum, Long pageSize, Integer status);
    Long submitEntry(EntryApplyDTO dto);
    void revokeEntry(Long id);
    HrEntryApplyVO getEntry(Long id);
    PageVO<HrRegularApplyVO> pageRegular(Long pageNum, Long pageSize, Integer status);
    Long submitRegular(RegularApplyDTO dto);
    void revokeRegular(Long id);
    PageVO<HrTransferApplyVO> pageTransfer(Long pageNum, Long pageSize, Integer status);
    Long submitTransfer(TransferApplyDTO dto);
    void revokeTransfer(Long id);
    PageVO<HrResignApplyVO> pageResign(Long pageNum, Long pageSize, Integer status);
    Long submitResign(ResignApplyDTO dto);
    void revokeResign(Long id);
    void onEntryApproved(Long entryApplyId);
    void onEntryRejected(Long entryApplyId);
    void onRegularApproved(Long regularApplyId);
    void onTransferApproved(Long transferApplyId);
    void onResignApproved(Long resignApplyId);
}