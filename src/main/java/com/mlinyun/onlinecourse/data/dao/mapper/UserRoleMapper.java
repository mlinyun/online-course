package com.mlinyun.onlinecourse.data.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mlinyun.onlinecourse.data.entity.Role;
import com.mlinyun.onlinecourse.data.entity.UserRole;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author LingYun
 * @description 针对表【user_role】的数据库操作Mapper
 * @createDate 2024-02-23 23:15:44
 * @Entity com.mlinyun.onlinecourse.data.entity.UserRole
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 通过用户id获取
     *
     * @param userId
     * @return
     */
    List<Role> findByUserId(@Param("userId") String userId);

    /**
     * 通过用户id获取用户角色关联的部门数据
     *
     * @param userId
     * @return
     */
    List<String> findDepIdsByUserId(@Param("userId") String userId);

}
