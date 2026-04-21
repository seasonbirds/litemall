<template>
  <div class="app-container">

    <!-- 查询和其他操作 -->
    <div class="filter-container">
      <el-input v-model="listQuery.word" clearable class="filter-item" style="width: 200px;" placeholder="敏感词" />
      <el-select v-model="listQuery.enabled" clearable class="filter-item" placeholder="状态" style="width: 150px;">
        <el-option :value="true" label="启用" />
        <el-option :value="false" label="禁用" />
      </el-select>
      <el-button v-permission="['GET /admin/sensitive/list']" class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">{{ $t('app.button.search') }}</el-button>
      <el-button v-permission="['POST /admin/sensitive/create']" class="filter-item" type="primary" icon="el-icon-edit" @click="handleCreate">{{ $t('app.button.create') }}</el-button>
    </div>

    <!-- 查询结果 -->
    <el-table v-loading="listLoading" :data="list" :element-loading-text="$t('app.message.list_loading')" border fit highlight-current-row>
      <el-table-column align="center" label="ID" prop="id" sortable />

      <el-table-column align="center" label="敏感词" prop="word" />

      <el-table-column align="center" label="状态" prop="enabled">
        <template slot-scope="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
            {{ scope.row.enabled ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column align="center" label="操作" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button v-permission="['POST /admin/sensitive/update']" type="primary" size="mini" @click="handleUpdate(scope.row)">{{ $t('app.button.edit') }}</el-button>
          <el-button v-permission="['POST /admin/sensitive/enable']" v-if="!scope.row.enabled" type="primary" size="mini" @click="handleEnable(scope.row)">启用</el-button>
          <el-button v-permission="['POST /admin/sensitive/disable']" v-if="scope.row.enabled" type="warning" size="mini" @click="handleDisable(scope.row)">禁用</el-button>
          <el-button v-permission="['POST /admin/sensitive/delete']" type="danger" size="mini" @click="handleDelete(scope.row)">{{ $t('app.button.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.page" :limit.sync="listQuery.limit" @pagination="getList" />

    <!-- 添加或修改对话框 -->
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form ref="dataForm" :rules="rules" :model="dataForm" status-icon label-position="left" label-width="100px" style="width: 400px; margin-left:50px;">
        <el-form-item label="敏感词" prop="word">
          <el-input v-model="dataForm.word" />
        </el-form-item>
        <el-form-item label="状态" prop="enabled">
          <el-switch v-model="dataForm.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">{{ $t('app.button.cancel') }}</el-button>
        <el-button v-if="dialogStatus=='create'" type="primary" @click="createData">{{ $t('app.button.confirm') }}</el-button>
        <el-button v-else type="primary" @click="updateData">{{ $t('app.button.confirm') }}</el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script>
import { listSensitive, createSensitive, updateSensitive, deleteSensitive, enableSensitive, disableSensitive } from '@/api/sensitive'
import Pagination from '@/components/Pagination'

export default {
  name: 'Sensitive',
  components: { Pagination },
  data() {
    return {
      list: null,
      total: 0,
      listLoading: true,
      listQuery: {
        page: 1,
        limit: 20,
        word: undefined,
        enabled: undefined,
        sort: 'add_time',
        order: 'desc'
      },
      dataForm: {
        id: undefined,
        word: undefined,
        enabled: true
      },
      dialogFormVisible: false,
      dialogStatus: '',
      textMap: {
        update: '编辑',
        create: '创建'
      },
      rules: {
        word: [
          { required: true, message: '敏感词不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.listLoading = true
      listSensitive(this.listQuery)
        .then(response => {
          this.list = response.data.data.list
          this.total = response.data.data.total
          this.listLoading = false
        })
        .catch(() => {
          this.list = []
          this.total = 0
          this.listLoading = false
        })
    },
    handleFilter() {
      this.listQuery.page = 1
      this.getList()
    },
    resetForm() {
      this.dataForm = {
        id: undefined,
        word: undefined,
        enabled: true
      }
    },
    handleCreate() {
      this.resetForm()
      this.dialogStatus = 'create'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    createData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          createSensitive(this.dataForm)
            .then(response => {
              this.list.unshift(response.data.data)
              this.dialogFormVisible = false
              this.$notify.success({
                title: '成功',
                message: '添加敏感词成功'
              })
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    handleUpdate(row) {
      this.dataForm = Object.assign({}, row)
      this.dialogStatus = 'update'
      this.dialogFormVisible = true
      this.$nextTick(() => {
        this.$refs['dataForm'].clearValidate()
      })
    },
    updateData() {
      this.$refs['dataForm'].validate(valid => {
        if (valid) {
          updateSensitive(this.dataForm)
            .then(() => {
              for (const v of this.list) {
                if (v.id === this.dataForm.id) {
                  const index = this.list.indexOf(v)
                  this.list.splice(index, 1, this.dataForm)
                  break
                }
              }
              this.dialogFormVisible = false
              this.$notify.success({
                title: '成功',
                message: '更新敏感词成功'
              })
            })
            .catch(response => {
              this.$notify.error({
                title: '失败',
                message: response.data.errmsg
              })
            })
        }
      })
    },
    handleDelete(row) {
      this.$confirm('确认要删除该敏感词吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteSensitive(row)
          .then(() => {
            this.$notify.success({
              title: '成功',
              message: '删除敏感词成功'
            })
            this.getList()
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {})
    },
    handleEnable(row) {
      this.$confirm('确认要启用该敏感词吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(() => {
        enableSensitive(row)
          .then(() => {
            this.$notify.success({
              title: '成功',
              message: '启用敏感词成功'
            })
            this.getList()
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {})
    },
    handleDisable(row) {
      this.$confirm('确认要禁用该敏感词吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        disableSensitive(row)
          .then(() => {
            this.$notify.success({
              title: '成功',
              message: '禁用敏感词成功'
            })
            this.getList()
          })
          .catch(response => {
            this.$notify.error({
              title: '失败',
              message: response.data.errmsg
            })
          })
      }).catch(() => {})
    }
  }
}
</script>
