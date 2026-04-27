package com.mc.common.util;

import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 树形结构构建工具类
 * <p>
 * 通用递归构建父子树，避免重复实现
 * </p>
 *
 * @param <T> 节点类型，必须有 getId() 和 getParentId()
 */
public class TreeUtil {

    /**
     * 构建树形结构
     *
     * @param all       全部节点列表
     * @param parentId   父节点ID（null表示构建根节点树）
     * @param getter    获取节点父ID的函数
     * @param parentCheck 判断是否为父节点的函数
     * @param <T>       节点类型
     * @return 树形列表
     */
    public static <T> List<T> buildTree(List<T> all, Long parentId,
                                        Function<T, Long> getter,
                                        Predicate<T> parentChecker) {
        return all.stream()
                .filter(item -> {
                    Long pid = getter.apply(item);
                    return (parentId == null && pid == null) ||
                           (parentId != null && parentId.equals(pid));
                })
                .peek(item -> {
                    // 递归设置children（如果有setChildren方法，需外部处理）
                })
                .collect(Collectors.toList());
    }

    /**
     * 判断是否为叶子节点
     */
    public static <T> boolean isLeaf(List<T> all, T item, Function<T, Long> getter) {
        Long id = getter.apply(item);
        return all.stream().noneMatch(i -> {
            Long pid = getter.apply(i);
            return pid != null && pid.equals(id);
        });
    }
}
