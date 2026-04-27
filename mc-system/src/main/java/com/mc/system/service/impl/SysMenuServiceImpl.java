package com.mc.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mc.common.exception.BusinessException;
import com.mc.common.result.ResultCode;
import com.mc.system.entity.SysMenu;
import com.mc.system.mapper.SysMenuMapper;
import com.mc.system.service.ISysMenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单Service实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements ISysMenuService {

    @Override
    public List<SysMenu> getTree() {
        List<SysMenu> all = this.list();
        return buildTree(all, null);
    }

    @Override
    public List<SysMenu> getUserMenus(Long userId) {
        return this.baseMapper.selectByUserId(userId);
    }

    @Override
    public boolean addMenu(SysMenu menu) {
        if (this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getCode, menu.getCode())) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "菜单编码已存在");
        }
        return this.save(menu);
    }

    @Override
    public boolean updateMenu(SysMenu menu) {
        return this.updateById(menu);
    }

    @Override
    public boolean deleteMenu(Long id) {
        // 检查是否有子菜单
        if (this.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getParentId, id)) > 0) {
            throw new BusinessException(ResultCode.VALIDATE_FAIL.getCode(), "存在子菜单，无法删除");
        }
        return this.removeById(id);
    }

    private List<SysMenu> buildTree(List<SysMenu> all, Long parentId) {
        return all.stream()
                .filter(menu -> (parentId == null && menu.getParentId() == null) ||
                        (parentId != null && parentId.equals(menu.getParentId())))
                .peek(menu -> menu.setChildren(buildTree(all, menu.getId())))
                .collect(Collectors.toList());
    }
}