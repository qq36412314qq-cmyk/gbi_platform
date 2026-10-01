package com.gbi.platform.service.impl.wecom;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gbi.platform.common.exception.BizException;
import com.gbi.platform.common.result.ResultCode;
import com.gbi.platform.dto.wecom.WecomMessageQueryDTO;
import com.gbi.platform.entity.wecom.WecomMessageRecord;
import com.gbi.platform.mapper.wecom.WecomMessageRecordMapper;
import com.gbi.platform.service.wecom.WecomMessageService;
import com.gbi.platform.vo.PageVO;
import com.gbi.platform.vo.wecom.WecomMessageVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企微消息记录服务实现
 *
 * @author gbi
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WecomMessageServiceImpl implements WecomMessageService {

    private final WecomMessageRecordMapper messageRecordMapper;

    @Override
    public PageVO<WecomMessageVO> pageMessages(WecomMessageQueryDTO dto) {
        Page<WecomMessageRecord> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<WecomMessageRecord> wrapper = new LambdaQueryWrapper<WecomMessageRecord>()
                .eq(StringUtils.hasText(dto.getMsgType()), WecomMessageRecord::getMsgType, dto.getMsgType())
                .eq(dto.getSendStatus() != null, WecomMessageRecord::getSendStatus, dto.getSendStatus())
                .like(StringUtils.hasText(dto.getToUser()), WecomMessageRecord::getToUser, dto.getToUser())
                .ge(dto.getStartTime() != null, WecomMessageRecord::getCreateTime, dto.getStartTime())
                .le(dto.getEndTime() != null, WecomMessageRecord::getCreateTime, dto.getEndTime())
                .orderByDesc(WecomMessageRecord::getCreateTime);
        Page<WecomMessageRecord> result = messageRecordMapper.selectPage(page, wrapper);
        List<WecomMessageVO> voList = result.getRecords().stream()
                .map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(voList, result.getTotal(), result.getCurrent(), result.getSize(), result.getPages());
    }

    @Override
    public WecomMessageVO getMessageDetail(Long id) {
        WecomMessageRecord record = messageRecordMapper.selectById(id);
        if (record == null) {
            throw new BizException(ResultCode.NOT_FOUND.getCode(), "消息记录不存在");
        }
        return toVO(record);
    }

    @Override
    public void exportMessages(WecomMessageQueryDTO dto, HttpServletResponse response) throws IOException {
        // TODO: Phase 7 实现 Excel 导出（使用 hutool ExcelUtil）
        log.info("企微消息导出请求，查询条件: msgType={}, sendStatus={}", dto.getMsgType(), dto.getSendStatus());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"message\":\"导出功能开发中，将在 Phase 7 实现\"}");
    }

    private WecomMessageVO toVO(WecomMessageRecord record) {
        WecomMessageVO vo = new WecomMessageVO();
        vo.setId(record.getId());
        vo.setCompanyId(record.getCompanyId());
        vo.setMsgId(record.getMsgId());
        vo.setMsgType(record.getMsgType());
        vo.setFromUser(record.getFromUser());
        vo.setToUser(record.getToUser());
        vo.setAgentId(record.getAgentId());
        vo.setContent(record.getContent());
        vo.setMediaId(record.getMediaId());
        vo.setSendStatus(record.getSendStatus());
        vo.setErrorMsg(record.getErrorMsg());
        vo.setCreateTime(record.getCreateTime());
        vo.setSendTime(record.getSendTime());
        return vo;
    }
}
