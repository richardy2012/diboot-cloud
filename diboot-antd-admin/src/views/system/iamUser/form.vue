<template>
  <a-drawer
    :title="title"
    :width="720"
    @close="close"
    :visible="state.visible"
    :body-style="{ paddingBottom: '80px' }"
  >
    <a-form layout="vertical" :form="form">
      <a-row :gutter="16">
        <a-col :span="24">
          <a-form-item label="所属部门">
            <template v-if="orgTreeList.length > 0">
              <a-tree-select
                placeholder="请选择所属部门"
                :dropdownStyle="{ maxHeight: '400px', overflow: 'auto' }"
                :treeData="orgTreeList"
                treeNodeFilterProp="title"
                showSearch
                treeDefaultExpandAll
                v-decorator="[
                  'orgId',
                  {
                    rules: [{ required: true, message: '请选择上级部门' }],
                    initialValue: model.orgId !== undefined ? `${model.orgId}` : currentNodeId
                  }
                ]"
              >
              </a-tree-select>
            </template>
            <template v-else>
              无
            </template>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="用户名">
            <a-input
              placeholder="请输入用户名"
              v-decorator="[
                'username',
                {
                  initialValue: model.username,
                  rules: [{ required: true, message: '用户名不能为空', whitespace: true }, { validator: this.checkUsernameDuplicate }]
                }
              ]"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="用户编号">
            <a-input
              placeholder="请输入用户编号"
              v-decorator="[
                'userNum',
                {
                  initialValue: model.userNum,
                  rules: [{ required: true, message: '用户编号不能为空', whitespace: true }, { validator: this.checkUserNumDuplicate }]
                }
              ]"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="姓名">
            <a-input
              placeholder="请输入姓名"
              v-decorator="[
                'realname',
                {
                  initialValue: model.realname,
                  rules: [{ required: true, message: '姓名不能为空', whitespace: true }]
                }
              ]"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="角色">
            <a-select
              v-if="more.iamRoleKvList"
              :getPopupContainer="getPopupContainer"
              mode="multiple"
              placeholder="请选择角色"
              v-decorator="[
                'roleIdList',
                {
                  initialValue: model.roleList && model.roleList.map(role => {return `${role.id}`}),
                  rules: [{ required: true, message: '角色不能为空' }]
                }
              ]"
            >
              <a-select-option
                v-for="(role, index) in more.iamRoleKvList"
                :key="index"
                :value="`${role.v}`"
              >
                {{ role.k }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="性别">
            <a-select
              v-if="more.genderKvList"
              :getPopupContainer="getPopupContainer"
              placeholder="请选择"
              v-decorator="[
                'gender',
                {
                  initialValue: model.gender,
                  rules: [{ required: true, message: '性别不能为空' }]
                }
              ]"
            >
              <a-select-option
                v-for="(gender, index) in more.genderKvList"
                :key="index"
                :value="gender.v"
              >
                {{ gender.k }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="状态">
            <a-select
              v-if="more.userStatusKvList"
              :getPopupContainer="getPopupContainer"
              placeholder="请选择"
              v-decorator="[
                'status',
                {
                  initialValue: model.status,
                  rules: [{ required: true, message: '用户状态不能为空' }]
                }
              ]"
            >
              <a-select-option
                v-for="(status, index) in more.userStatusKvList"
                :key="index"
                :value="status.v"
              >
                {{ status.k }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="电话">
            <a-input
              placeholder="请输入电话"
              v-decorator="[
                'mobilePhone',
                {
                  initialValue: model.mobilePhone
                }
              ]"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="邮箱">
            <a-input
              placeholder="xxx@xxx.com"
              v-decorator="[
                'email',
                {
                  initialValue: model.email
                }
              ]"
            />
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="密码">
            <a-button
              v-if="setPassword === false"
              @click="setPassword = true"
              size="small"
              type="primary">重设密码</a-button>
            <a-input
              v-if="setPassword === true"
              type="password"
              v-decorator="[
                'password',
                {
                  initialValue: '',
                  rules: [{ required: true, message: '密码不能为空', whitespace: true }]
                }
              ]"
            />
          </a-form-item>
        </a-col>
      </a-row>
    </a-form>
    <div class="drawer-footer">
      <a-button :style="{marginRight: '8px'}" @click="close">取消</a-button>
      <a-button @click="onSubmit" type="primary" :loading="state.confirmSubmit" :disabled="state.confirmSubmit">确定</a-button>
    </div>
  </a-drawer>
</template>

<script>
import form from '@/components/diboot/mixins/form'
import { dibootApi } from '@/utils/request'
import _ from 'lodash'
import { treeListFormatter } from '@/utils/treeDataUtil'

export default {
  name: 'IamUserForm',
  data () {
    return {
      baseApi: '/auth-server/iam/user',
      form: this.$form.createForm(this),
      orgList: [],
      setPassword: false,
      attachMoreList: [
        {
          type: 'D',
          target: 'GENDER'
        },
        {
          type: 'D',
          target: 'USER_STATUS'
        },
        {
          type: 'T',
          target: 'iamRole',
          key: 'name',
          value: 'id'
        }
      ]
    }
  },
  methods: {
    async afterOpen (id) {
      // 新建显示密码填写，更新隐藏密码填写
      if (id === undefined) {
        this.setPassword = true
      } else {
        this.setPassword = false
      }
      // 加载部门树数据
      this.loadOrgTree()
      // 获取account的username信息到表单中
      this.loadUsername()
    },
    async loadOrgTree () {
      const res = await dibootApi.get(`/auth-server/iam/org/tree`)
      if (res.code === 0) {
        this.orgList = res.data
      } else {
        this.$message.error(res.msg)
      }
    },
    async loadUsername () {
      const id = this.model.id
      if (id !== undefined) {
        const res = await dibootApi.get(`${this.baseApi}/getUsername/${id}`)
        if (res.code === 0 && res.data) {
          this.systemUser = true
          setTimeout(() => {
            this.form.setFieldsValue({ username: res.data })
          }, 200)
        }
      }
    },
    async checkUsernameDuplicate (rule, value, callback) {
      if (!value) {
        callback()
        return
      }
      const params = { id: this.model.id, username: value }
      const res = await dibootApi.get(`${this.baseApi}/checkUsernameDuplicate`, params)
      if (res.code === 0) {
        callback()
      } else {
        callback(res.msg.split(':')[1])
      }
    },
    async checkUserNumDuplicate (rule, value, callback) {
      if (!value) {
        callback()
        return
      }
      const params = { id: this.model.id, userNum: value }
      const res = await dibootApi.get(`${this.baseApi}/checkUserNumDuplicate`, params)
      if (res.code === 0) {
        callback()
      } else {
        callback(res.msg.split(':')[1])
      }
    },
    afterClose () {
      this.setPassword = false
    }
  },
  computed: {
    orgTreeList: function () {
      if (this.orgList === undefined || this.orgList.length === 0) {
        return []
      }
      const orgTreeList = treeListFormatter(_.cloneDeep(this.orgList), 'id', 'shortName', true)
      orgTreeList.unshift({ title: '无', value: '0', key: '0' })
      return orgTreeList
    }
  },
  mixins: [form],
  props: {
    currentNodeId: {
      type: String,
      default: () => {
        return '0'
      }
    }
  }
}
</script>

<style lang="less" scoped>
</style>
