package com.mlinyun.onlinecourse.basics.baseClass;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;

/**
 * 基类服务层
 */
@Schema(description = "模板服务层")
@FunctionalInterface
public interface BaseService<E, ID extends Serializable> {

    BaseDao<E, ID> getRepository();

    @Operation(summary = "查询")
    default E get(ID id) {
        return getRepository().findById(id).orElse(null);
    }

    @Operation(summary = "查询")
    default List<E> getAll() {
        return getRepository().findAll();
    }

    @Operation(summary = "新增")
    default E save(E entity) {
        return getRepository().save(entity);
    }

    @Operation(summary = "编辑")
    default E update(E entity) {
        return getRepository().saveAndFlush(entity);
    }

    @Operation(summary = "批量保存")
    default Iterable<E> saveOrUpdateAll(Iterable<E> entities) {
        return getRepository().saveAll(entities);
    }

    @Operation(summary = "计数")
    default Long count() {
        return getRepository().count();
    }

    @Operation(summary = "删除")
    default void delete(E entity) {
        getRepository().delete(entity);
    }

    @Operation(summary = "删除")
    default void delete(ID id) {
        getRepository().deleteById(id);
    }

    @Operation(summary = "删除")
    default void delete(Iterable<E> entities) {
        getRepository().deleteAllInBatch(entities);
    }

    @Operation(summary = "查询")
    default Page<E> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

}
