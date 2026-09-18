package com.gbi.platform.service.hr;

import com.gbi.platform.dto.hr.*;
import com.gbi.platform.vo.hr.*;
import com.gbi.platform.vo.PageVO;
import java.util.List;

public interface HrOrgService {
    PageVO<HrPostVO> pagePost(Long pageNum, Long pageSize, Integer status);
    List<HrPostVO> getPostByDept(Long deptId);
    void addPost(PostDTO dto);
    void updatePost(PostDTO dto);
    void deletePost(Long id);
}
