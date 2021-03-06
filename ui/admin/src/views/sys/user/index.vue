<template>
  <div class="app-container">
    <el-card>
      <el-row :gutter="20">
        <!-- left dept -->
        <el-col :span="4">
          <div style="margin-top: 5px;">
            <el-input v-model="dept.filterText" size="small" clearable placeholder="输入关键字过滤部门" />
            <el-tree ref="dept" style="margin-top: 20px" :expand-on-click-node="false" highlight-current :data="dept.data" :props="{ children: 'children', label: 'name' }" default-expand-all :filter-node-method="deptFilter" @node-click="deptNodeClick" />
          </div>
        </el-col>
        <!-- right table -->
        <el-col :span="20">
          <div class="filter-container">
            <el-form :inline="true" :model="search" size="small">
              <el-form-item label="用户名称">
                <el-input v-model="search.username" placeholder="请输入菜单名称" clearable />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input v-model="search.phone" placeholder="请输入手机号码" clearable />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="search.enabled" placeholder="用户状态" clearable>
                  <el-option label="启用" :value="true" />
                  <el-option label="停用" :value="false" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" icon="el-icon-search" @click="tablePage">查询</el-button>
                <el-button icon="el-icon-refresh">重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="tools-container">
            <el-button v-permission="['sys:user:add']" type="primary" size="mini" icon="el-icon-plus" @click="tableCreate">新增</el-button>
            <el-button v-permission="['sys:user:update']" type="info" size="mini" icon="el-icon-edit" :disabled="table.selection.length !== 1" @click="tableEdit(table.selection[0])">编辑</el-button>
            <el-button v-permission="['sys:user:del']" type="danger" size="mini" icon="el-icon-delete" :disabled="table.selection.length === 0" @click="tableDelete(table.selection)">删除</el-button>
          </div>

          <div class="table-container">
            <el-table ref="table" v-loading="table.loading" size="medium" border :data="table.data" style="width: 100%" @selection-change="(val) => table.selection = val">
              <el-table-column type="selection" align="center" />
              <el-table-column label="用户名称" align="center" prop="username" :show-overflow-tooltip="true" />
              <el-table-column label="用户昵称" align="center" prop="nickname" :show-overflow-tooltip="true" />
              <el-table-column label="部门" align="center" prop="deptName" :show-overflow-tooltip="true" />
              <el-table-column label="手机号码" align="center" prop="phone" :show-overflow-tooltip="true" />
              <el-table-column label="状态" align="center">
                <template slot-scope="scope">
                  <el-switch v-model="scope.row.enabled" @change="tableEnabledChange(scope.row)" />
                </template>
              </el-table-column>
              <el-table-column label="操作" align="center" width="285">
                <template slot-scope="scope">
                  <el-button v-permission="['sys:user:update']" type="text" size="mini" icon="el-icon-edit" @click="tableEdit(scope.row)">编辑</el-button>
                  <el-button v-permission="['sys:user:del']" type="text" size="mini" icon="el-icon-delete" @click="tableDelete([scope.row.id])">删除</el-button>
                  <el-button v-permission="['sys:user:resetPass']" type="text" size="mini" icon="el-icon-refresh">重置</el-button>
                </template>
              </el-table-column>

            </el-table>
            <div class="pagination-container">
              <el-pagination :current-page.sync="table.current" :page-size="table.size" :total="table.total" layout="total, prev, pager, next" @current-change="tablePage()" />
            </div>
          </div>

          <el-dialog :append-to-body="true" :visible.sync="form.dialog" :title="form.data.id === undefined ? '新增' : '编辑'" width="600px" :close-on-click-modal="false">
            <el-form ref="form" :model="form.data" :rules="form.rules" size="small" label-width="100px">

              <el-row>
                <el-col :span="12">
                  <el-form-item label="用户昵称" prop="nickname">
                    <el-input v-model="form.data.nickname" placeholder="请输入用户昵称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="归属部门" prop="deptId">
                    <treeselect v-model="form.data.deptId" :options="dept.data" :normalizer="node => {if (node.children && !node.children.length) delete node.children; return { id: node.id, label: node.name, children: node.children }}" placeholder="请选择归属部门" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="手机号码" prop="phone">
                    <el-input v-model="form.data.phone" placeholder="请输入手机号码" maxlength="11" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="邮箱" prop="email">
                    <el-input v-model="form.data.email" placeholder="请输入邮箱" maxlength="50" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户名称" prop="username">
                    <el-input v-model="form.data.username" placeholder="请输入用户名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item v-if="form.data.id == undefined" label="用户密码" prop="password">
                    <el-input v-model="form.password" placeholder="请输入用户密码" type="password" />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="用户性别">
                    <el-select v-model="form.data.sex" placeholder="请选择">
                      <el-option v-for="item in dict.sexOptions" :key="item.dictValue" :label="item.dictLabel" :value="item.dictValue" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-radio-group v-model="form.data.enabled">
                      <el-radio v-for="item in dict.enabledOptions" :key="item.value" :label="item.value">{{ item.label }}</el-radio>
                    </el-radio-group>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="岗位">
                    <el-select v-model="form.data.postId" placeholder="请选择" @change="$forceUpdate()">
                      <el-option v-for="item in dict.postOptions" :key="item.postId" :label="item.postName" :value="item.postId" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="角色">
                    <el-select v-model="form.data.roles" placeholder="请选择" multiple @change="$forceUpdate()">
                      <el-option v-for="item in dict.roleOptions" :key="item.id" :label="item.name" :value="item.id" />
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="24">
                  <el-form-item label="备注">
                    <el-input v-model="form.data.remark" type="textarea" placeholder="请输入内容" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>

            <div slot="footer" class="dialog-footer">
              <el-button type="text" @click="form.dialog = false">取消</el-button>
              <el-button :loading="table.loading" type="primary" @click="tableSubmit">确认</el-button>
            </div>

          </el-dialog>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { userPage, userDel, userAdd, userUpdate, userStatusChange } from '@/api/sys/user'
import { rolePage } from '@/api/sys/role'
import { deptPage } from '@/api/sys/dept'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'

const _defaultRow = {
  id: undefined,
  role: '',
  description: ''
}

export default {
  components: { Treeselect },
  data() {
    return {
      search: {
        username: undefined,
        phone: undefined,
        enabled: undefined,
        deptId: undefined
      },
      table: {
        data: [],
        current: 1,
        size: 10,
        total: 0,
        loading: false,
        selection: []
      },
      form: {
        data: Object.assign({}, _defaultRow),
        rules: {},
        dialog: false
      },
      dept: {
        filterText: '',
        data: []
      },
      dict: {
        enabledOptions: [{ value: true, label: '启用' }, { value: false, label: '停用' }],
        sexOptions: [{ value: true, label: '男' }, { value: false, label: '女' }],
        roleOptions: []
      }
    }
  },
  watch: {
    'dept.filterText'(val, oldVal) {
      this.$refs.dept.filter(val)
      if (val === undefined || val === '' || (oldVal && (oldVal.length > val.length))) {
        this.search.deptId = undefined
      }
    }
  },
  mounted() {
    this.deptGetData()
    this.tablePage()
  },
  methods: {
    deptGetData() {
      deptPage().then(res => {
        this.dept.data = res.data
        this.dept.data.unshift({ id: undefined, name: '所有部门' })
      })
    },
    deptNodeClick(data) {
      this.search.deptId = data.id
      this.tablePage()
    },
    deptFilter(value, data) {
      if (!value) return true
      return data.name.indexOf(value) !== -1
    },
    tableCreate() {
      this.form.dialog = true
      this.form.data = Object.assign({}, _defaultRow)
    },
    tableDelete(ids) {
      this.$confirm('此操作将删除选中数据, 是否继续?', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(() => {
        userDel(ids).then(response => {
          this.tablePage()
        })
      })
    },
    tableEdit(row) {
      this.form.data = Object.assign({}, row)
      this.form.dialog = true
      rolePage({ size: -1 }).then(response => {
        this.dict.roleOptions = response.data.records
      })
    },
    tablePage() {
      this.table.loading = true
      const params = {
        current: this.table.current,
        size: this.table.size,
        username: this.search.username ? this.search.username : undefined,
        phone: this.search.phone ? this.search.phone : undefined,
        enabled: this.search.enabled,
        deptId: this.search.deptId
      }
      userPage(params).then(response => {
        this.table.loading = false
        this.table.data = response.data.records
        this.table.total = response.data.total
      })
    },
    tableSubmit() {
      this.$refs['form'].validate((valid) => {
        if (!valid) return
        if (this.form.data.id === undefined) {
          userAdd(this.form.data).then((res) => {
            this.form.dialog = false
            this.tablePage()
            this.$refs['form'].resetFields()
          })
        } else {
          userUpdate(this.form.data).then((res) => {
            this.form.dialog = false
            this.tablePage()
          })
        }
      })
    },
    tableEnabledChange(row) {
      const text = row.enabled ? '启用' : '停用'
      this.$confirm(`确认要 ${text} ${row.username} 用户吗?`, '警告', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }).then(function() {
        return userStatusChange(row.id, row.enabled)
      }).then(() => {
        this.$message({ message: text + '成功', type: 'success' })
      }).catch(() => {
        row.enabled = !row.enabled
      })
    }
  }
}
</script>

<style scoped>
.filter-container {
  margin-top: 6px;
}
.tools-container {
  margin-top: 5px;
  margin-bottom: 15px;
}
.pagination-container {
  position: relative;
  float: right;
  margin-top: 10px;
}
</style>
