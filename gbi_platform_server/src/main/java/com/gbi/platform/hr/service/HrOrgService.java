package com.gbi.platform.hr.service;

import com.gbi.platform.hr.dto.*;
import com.gbi.platform.hr.vo.*;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrOrgService {
    PageVO<HrPostVO> pagePost(Long pageNum, Long pageSize, Integer status);
    void addPost(PostDTO dto);
    void updatePost(PostDTO dto);
    void deletePost(Long id);
}
