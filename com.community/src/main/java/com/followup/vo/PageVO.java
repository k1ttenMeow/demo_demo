package com.followup.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> list;
    private Long total;
    private Long pageNum;
    private Long pageSize;
}