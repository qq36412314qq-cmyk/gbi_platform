package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.SocialDTO;
import com.gbi.platform.hr.vo.HrSocialVO;
import com.gbi.platform.vo.PageVO;

public interface HrSocialService {
    PageVO<HrSocialVO> pageSocial(Long pageNum, Long pageSize, Long employeeId, Integer status);
    void addSocial(SocialDTO dto);
    void updateSocial(SocialDTO dto);
    void deleteSocial(Long id);
}
