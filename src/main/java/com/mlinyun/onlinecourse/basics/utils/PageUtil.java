package com.mlinyun.onlinecourse.basics.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mlinyun.onlinecourse.basics.baseVo.PageVo;
import com.mlinyun.onlinecourse.basics.exception.RuntimeExceptionHandler;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 封装的分页插件
 */
public class PageUtil {

    private final static String[] NO_CAN_USE_WORDS = {"drop", "select", "master", "insert", "truncate", "declare", "delete", "sleep", "update", "alter"};

    private static final String SORT_BY_ASC = "asc";

    private static final String SORT_BY_DESC = "desc";

    private static final String CAMEL_STEP_STR = "_";

    private static final String NULL_STR = "";

    @Operation(summary = "JPA分页方法")
    public static Pageable initPage(PageVo page) {
        Pageable able = null;
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        String sort = page.getSort();
        String order = page.getOrder();
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        pageSize = pageSize < 1 ? 1 : pageSize;
        pageSize = pageSize > 100 ? 100 : pageSize;
        if (!NullUtils.isNull(sort)) {
            Sort.Direction direction = NullUtils.isNull(order) ? Sort.Direction.DESC : Sort.Direction.valueOf(order.toUpperCase());
            Sort sortBy = Sort.by(direction, sort);
            able = PageRequest.of(pageNumber - 1, pageSize, sortBy);
        } else {
            able = PageRequest.of(pageNumber - 1, pageSize);
        }
        return able;
    }

    @Operation(summary = "MybatisPlus分页方法")
    public static Page initMpPage(PageVo page) {
        Page newPage = null;
        int pageNumber = page.getPageNumber();
        int pageSize = page.getPageSize();
        String sort = page.getSort();
        String order = page.getOrder();
        SQLInject(sort);
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        pageSize = pageSize < 1 ? 1 : pageSize;
        pageSize = pageSize > 100 ? 100 : pageSize;
        if (!NullUtils.isNull(sort)) {
            Boolean isAsc = false;
            if (NullUtils.isNull(order)) {
                isAsc = false;
            } else {
                if (Objects.equals(order.toLowerCase(), SORT_BY_DESC)) {
                    isAsc = false;
                } else if (Objects.equals(order.toLowerCase(), SORT_BY_ASC)) {
                    isAsc = true;
                }
            }
            newPage = new Page(pageNumber, pageSize);
            newPage.addOrder(isAsc ? OrderItem.asc(camel2Underline(sort)) : OrderItem.desc(camel2Underline(sort)));
        } else {
            newPage = new Page(pageNumber, pageSize);
        }
        return newPage;
    }

    @Operation(summary = "自定义分页方法")
    public static List listToPage(PageVo page, List list) {
        int pageNumber = page.getPageNumber() - 1;
        int pageSize = page.getPageSize();
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        pageSize = pageSize < 1 ? 1 : pageSize;
        pageSize = pageSize > 100 ? 100 : pageSize;
        int startIndex = pageNumber * pageSize;
        int endIndex = pageNumber * pageSize + pageSize;
        if (startIndex > list.size()) {
            return new ArrayList();
        } else if (endIndex > list.size() - 1) {
            return list.subList(startIndex, list.size());
        } else {
            return list.subList(startIndex, endIndex);
        }
    }

    @Operation(summary = "驼峰转下划线")
    public static String camel2Underline(String sqlStr) {
        if (NullUtils.isNull(sqlStr)) {
            return NULL_STR;
        }
        if (sqlStr.length() < 2) {
            return sqlStr.toLowerCase();
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int index = 1; index < sqlStr.length(); index++) {
            if (Character.isUpperCase(sqlStr.charAt(index))) {
                stringBuffer.append(CAMEL_STEP_STR + Character.toLowerCase(sqlStr.charAt(index)));
            } else {
                stringBuffer.append(sqlStr.charAt(index));
            }
        }
        return (sqlStr.charAt(0) + stringBuffer.toString()).toLowerCase();
    }

    @Operation(summary = "防MybatisPlus的SQL注入攻击")
    public static void SQLInject(String sqlStr) {
        if (NullUtils.isNull(sqlStr)) {
            return;
        }
        // 转小写
        sqlStr = sqlStr.toLowerCase();
        // 判断非法字符
        for (String word : NO_CAN_USE_WORDS) {
            if (sqlStr.contains(word)) {
                throw new RuntimeExceptionHandler(sqlStr + " 字符串中含有不能使用的单次");
            }
        }
    }
}
