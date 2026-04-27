package com.mc.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private long total;

    /** 当前页大小 */
    private long size;

    /** 当前页码 */
    private long current;

    /** 记录列表 */
    private List<T> records;

    /** 总页数 */
    private long pages;

    public PageResult() {
    }

    public PageResult(long total, long size, long current, List<T> records) {
        this.total = total;
        this.size = size;
        this.current = current;
        this.records = records;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
    }

    public static <T> PageResult<T> of(long total, long size, long current, List<T> records) {
        return new PageResult<>(total, size, current, records);
    }

    public boolean hasNext() {
        return current < pages;
    }

    public boolean hasPrevious() {
        return current > 1;
    }
}
