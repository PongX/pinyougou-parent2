package com.pinyougou.entity;

import java.io.Serializable;
import java.util.List;

public class PageResult implements Serializable {


    private long total;//查询的总记录条数

    private List rows;//当前页显示的所有信息集合

    public PageResult(long total, List rows) {
        this.total = total;
        this.rows = rows;
    }

    public PageResult() {
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List getRows() {
        return rows;
    }

    public void setRows(List rows) {
        this.rows = rows;
    }
}
