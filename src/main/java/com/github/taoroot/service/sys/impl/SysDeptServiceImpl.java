package com.github.taoroot.service.sys.impl;

import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.taoroot.common.core.utils.R;
import com.github.taoroot.common.data.CustomServiceImpl;
import com.github.taoroot.config.DBName;
import com.github.taoroot.entity.sys.SysDept;
import com.github.taoroot.mapper.sys.SysDeptMapper;
import com.github.taoroot.service.sys.SysDeptService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@DS(DBName.SYS)
@Service
@AllArgsConstructor
public class SysDeptServiceImpl extends CustomServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<Tree<Integer>> tree(Integer parentId) {
        List<SysDept> list = baseMapper.selectList(Wrappers.<SysDept>lambdaQuery()
                .eq(SysDept::getEnabled, true));
        return getTrees(parentId, list);
    }

    @Override
    public R tree(Integer parentId, String name) {
        LambdaQueryWrapper<SysDept> queryWrapper = Wrappers.lambdaQuery();

        if (!StringUtils.isEmpty(name)) {
            queryWrapper.like(SysDept::getName, name);
        }

        List<SysDept> list = baseMapper.selectList(queryWrapper);
        List<Tree<Integer>> tree = getTrees(parentId, list);
        if (tree.size() != 0) {
            return R.ok(tree);
        }
        return R.ok(list);
    }

    private List<Tree<Integer>> getTrees(Integer parentId, List<SysDept> list) {
        return TreeUtil.build(list, parentId, (treeNode, tree) -> {
            tree.setId(treeNode.getId());
            tree.setParentId(treeNode.getParentId());
            tree.setWeight(treeNode.getWeight());
            tree.setName(treeNode.getName());
            tree.putExtra("name", treeNode.getName());
            tree.putExtra("email", treeNode.getEmail());
            tree.putExtra("enabled", treeNode.getEnabled());
            tree.putExtra("leader", treeNode.getLeader());
            tree.putExtra("phone", treeNode.getPhone());
        });
    }
}
