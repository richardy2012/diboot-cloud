<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="18">
          <a-col :md="8" :sm="24">
            <a-form-item label="任务" labelAlign="right" :labelCol="{span: 6}" :wrapperCol="{span: 18}" style="width: 100%;">
              <a-select
                v-model="queryParam.jobName"
                :getPopupContainer="getPopupContainer"
                placeholder="请选择任务"
              >
                <a-select-option
                  v-for="(item, index) in jobList"
                  :key="index"
                  :value="item.jobName"
                >
                  {{ item.jobName }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-form-item label="状态" labelAlign="right" :labelCol="{span: 6}" :wrapperCol="{span: 18}" style="width: 100%;">
              <a-select v-model="queryParam.jobStatus" placeholder="请选择状态" style="width: 100%;">
                <a-select-option value="A">
                  启用
                </a-select-option>
                <a-select-option value="I">
                  停用
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <span class="table-page-search-submitButtons" >
              <a-button type="primary" @click="onSearch">查询</a-button>
              <a-button style="margin-left: 8px" @click="reset">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <div class="table-operator">
      <a-button v-action:create type="primary" icon="plus" @click="$refs.form.open()">新建</a-button>
      <a-button v-action:logList icon="profile" @click="$refs.logList.open()">日志记录</a-button>
    </div>
    <a-table
      ref="table"
      size="default"
      :columns="columns"
      :dataSource="data"
      :pagination="pagination"
      :scroll="tableScrollData"
      :loading="loadingData"
      @change="handleTableChange"
      rowKey="id"
    >
      <span slot="initStrategy" slot-scope="text, record">
        {{ initStrategyEnum[record.initStrategy] || '周期执行' }}
      </span>
      <span slot="jobStatus" slot-scope="text, record">
        <a-switch
          v-action:update
          :key="loadingData"
          checked-children="正常"
          un-checked-children="停用"
          :defaultChecked="record.jobStatus === 'A'"
          @change="handleSwitchChange(record)"/>
        <span v-permission-missing="['update']">
          <a-tag v-if="record.jobStatus === 'A'" color="#1095f9">{{ record.jobStatusLabel }}</a-tag>
          <a-tag v-else color="#bcbcbc">{{ record.jobStatusLabel }}</a-tag>
        </span>
      </span>
      <span slot="action" slot-scope="text, record">
        <a v-action:detail href="javascript:;" @click="$refs.detail.open(record.id)">详情</a>
        <a-divider v-action:detail v-permission="['executeOnce', 'update', 'delete']" type="vertical" />
        <a v-action:executeOnce href="javascript:;" @click="handleExecuteOnce(record.id)">运行一次</a>
        <a-divider v-action:executeOnce v-permission="['update', 'delete']" type="vertical" />
        <a-dropdown v-permission="['update', 'delete']">
          <a class="ant-dropdown-link">
            更多 <a-icon type="down"/>
          </a>
          <a-menu slot="overlay">
            <a-menu-item v-action:update>
              <a @click="$refs.form.open(record.id)">编辑</a>
            </a-menu-item>
            <a-menu-item v-action:delete>
              <a href="javascript:;" @click="remove(record.id)">删除</a>
            </a-menu-item>
          </a-menu>
        </a-dropdown>
      </span>
    </a-table>
    <diboot-form ref="form" @complete="getList"></diboot-form>
    <diboot-detail ref="detail"></diboot-detail>
    <diboot-log-list ref="logList"></diboot-log-list>
  </a-card>
</template>

<script>
import list from '@/components/diboot/mixins/list'
import dibootForm from './form'
import dibootDetail from './detail'
import dibootLogList from './log/list'

export default {
  name: 'ScheduleJobList',
  components: {
    dibootForm,
    dibootDetail,
    dibootLogList
  },
  mixins: [list],
  data () {
    return {
      baseApi: '/scheduler/scheduleJob',
      getListFromMixin: true,
      columns: [
        {
          title: '任务名称',
          dataIndex: 'jobName'
        },
        {
          title: '定时表达式',
          dataIndex: 'cron'
        },
        {
          title: '初始化策略',
          dataIndex: 'initStrategy',
          scopedSlots: { customRender: 'initStrategy' }

        },
        {
          title: '状态',
          dataIndex: 'jobStatus',
          scopedSlots: { customRender: 'jobStatus' }
        },
        {
          title: '备注',
          dataIndex: 'jobComment'
        },
        {
          title: '创建时间',
          dataIndex: 'createTime'
        },
        {
          title: '操作',
          width: '200px',
          fixed: 'right',
          dataIndex: 'action',
          scopedSlots: { customRender: 'action' }
        }
      ],
      initStrategyEnum: {
        DO_NOTHING: '周期执行',
        FIRE_AND_PROCEED: '立即执行一次，并周期执行',
        IGNORE_MISFIRES: '超期立即执行，并周期执行'
      },
      jobList: []
    }
  },
  created () {
    this.loadJobs()
  },
  methods: {

    /**
     * 加载job
     * @returns {Promise<void>}
     */
    async loadJobs () {
      const res = await this.$http.get('/scheduler/scheduleJob/allJobs')
      if (res.code === 0) {
        this.jobList = res.data || []
      } else {
        this.$message.error('无可执行定时任务！')
      }
    },
    /**
     * 改变状态
     * @param value
     * @returns {Promise<void>}
     */
    async handleSwitchChange (value) {
      const status = value.jobStatus === 'A' ? 'I' : 'A'
      try {
        const res = await this.$http.put(`/scheduler/scheduleJob/${value.id}/${status}`)
        if (res.code === 0) {
          this.$message.success('修改任务状态成功！')
        } else {
          this.$message.error(res.msg)
        }
      } catch (e) {
        this.$message.error('修改任务状态失败！')
      }
      this.getList()
    },
    /**
     * 执行一次任务
     * @param id
     * @returns {Promise<void>}
     */
    async handleExecuteOnce (id) {
      try {
        const res = await this.$http.put(`/scheduler/scheduleJob/executeOnce/${id}`)
        if (res.code === 0) {
          this.$message.success('发送执行任务成功！')
        } else {
          this.$message.error(res.msg)
        }
      } catch (e) {
        this.$message.error('发送执行任务失败！')
      }
      this.getList()
    }
  }

}
</script>
<style lang="less" scoped>
</style>
