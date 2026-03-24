package com.followup.util;

import com.followup.vo.PageVO;

import java.util.List;

public final class PageUtil {

    private PageUtil() {
    }

    public static <T> PageVO<T> convertPage(List<T> list, Long total, Long pageNum, Long pageSize) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        return pageVO;
    }
}