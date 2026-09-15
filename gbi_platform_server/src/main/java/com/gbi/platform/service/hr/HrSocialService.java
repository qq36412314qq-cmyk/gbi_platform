package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.SocialDTO;
import com.gbi.platform.vo.hr.HrSocialVO;
import com.gbi.platform.vo.PageVO;

public interface HrSocialService {
    PageVO<HrSocialVO> pageSocial(Long pageNum, Long pageSize, Long employeeId, Integer status);
    void addSocial(SocialDTO dto);
    void updateSocial(SocialDTO dto);
    void deleteSocial(Long id);
}
