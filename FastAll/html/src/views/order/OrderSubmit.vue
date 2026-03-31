<template>
  <div class="order-submit">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Connection /></el-icon> 采购需求提报网络
        </h2>
        <p class="page-subtitle">高并发场景下的业务幂等性与安全防重提交保障</p>
      </div>
    </div>

    <el-row :gutter="24">
      <el-col :span="14">
        <el-card class="box-card main-card">
          <template #header>
            <div class="card-header">
              <span class="header-icon"><el-icon><EditPen /></el-icon></span> 提报需求单
            </div>
          </template>

          <el-form :model="orderForm" label-position="top" class="order-form">
            <el-form-item label="选择集采药品">
              <el-select v-model="orderForm.drugId" placeholder="请选择测试药品" class="custom-select" style="width: 100%">
                <el-option label="阿司匹林肠溶片 (批次: A2026)" value="d_1001" />
                <el-option label="复方氨酚烷胺胶囊 (批次: F3011)" value="d_1002" />
                <el-option label="布洛芬缓释胶囊 (批次: B1052)" value="d_1003" />
              </el-select>
            </el-form-item>

            <el-form-item label="采购数量">
              <div class="amount-input-group">
                <el-input-number v-model="orderForm.amount" :min="100" :max="10000" :step="100" class="custom-input-number" />
                <span class="unit-tag">单位: 盒</span>
              </div>
            </el-form-item>

            <el-form-item label="目标收货引擎仓库">
              <el-radio-group v-model="orderForm.warehouse" class="custom-radio-group">
                <el-radio-button label="WH_SH" class="warehouse-radio">
                  <div class="radio-content">
                    <el-icon><Location /></el-icon>
                    <span>上海中转仓</span>
                  </div>
                </el-radio-button>
                <el-radio-button label="WH_BJ" class="warehouse-radio">
                  <div class="radio-content">
                    <el-icon><Location /></el-icon>
                    <span>北京集散仓</span>
                  </div>
                </el-radio-button>
                <el-radio-button label="WH_GZ" class="warehouse-radio">
                  <div class="radio-content">
                    <el-icon><Location /></el-icon>
                    <span>广州冷链仓</span>
                  </div>
                </el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="业务流水防重 Token">
              <div class="token-fetcher">
                <el-input 
                  v-model="orderForm.token" 
                  disabled 
                  placeholder="等待服务端下发防重令牌..." 
                  class="token-input"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                </el-input>
                <el-button color="#6366f1" class="fetch-btn" @click="fetchToken">申请 Token</el-button>
              </div>
            </el-form-item>

            <div class="action-divider"></div>

            <div class="btn-group">
              <el-button type="primary" size="large" class="submit-btn normal-btn" @click="submitNormal" :loading="loading">
                <el-icon><Check /></el-icon> 发起常规提报
              </el-button>
              <el-button type="danger" size="large" class="submit-btn attack-btn" @click="submitConcurrent" :loading="loading">
                <el-icon><WarnTriangleFilled /></el-icon> 并发压测 (连点攻击)
              </el-button>
            </div>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="10">
        <el-card class="box-card console-card">
          <template #header>
            <div class="console-header">
              <div class="dots"><span class="red"></span><span class="yellow"></span><span class="green"></span></div>
              <span>中台业务网关日志</span>
            </div>
          </template>
          <div class="result-box" ref="consoleBox">
            <div v-if="submitLogs.length === 0" class="empty-console">
              等待请求发起...
            </div>
            <p v-for="(log, index) in submitLogs" :key="index" :class="['log-line', log.type]">
              <span class="log-time">[{{ log.time }}]</span>
              <span class="log-content">{{ log.msg }}</span>
            </p>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const loading = ref(false)
const orderForm = ref({
  drugId: 'd_1001',
  amount: 500,
  warehouse: 'WH_SH',
  token: ''
})

const submitLogs = ref([])
const addLog = (msg, type = 'info') => {
  submitLogs.value.unshift({ 
    time: new Date().toLocaleTimeString(), 
    msg, 
    type 
  })
}

const fetchToken = () => {
  // TODO: Request backend for idempotent token
  setTimeout(() => {
    orderForm.value.token = 'idempotent_' + Math.random().toString(36).substr(2, 9)
    addLog(`成功获取防重 Token: ${orderForm.value.token}`, 'success')
  }, 300)
}

const sendSubmitRequest = async (reqId) => {
  return new Promise((resolve) => {
    // 模拟后端拦截
    setTimeout(() => {
      // 模拟第一发成功，后续相同的 token 失败
      if (Math.random() > 0.3 && reqId === 1) {
        addLog(`[请求${reqId}] 下单成功，订单号: ORD${Date.now()}`, 'success')
        resolve(true)
      } else {
        addLog(`[请求${reqId}] 操作太频繁或请求已处理，拦截成功！`, 'error')
        resolve(false)
      }
    }, 400 + Math.random() * 200)
  })
}

const submitNormal = async () => {
  if (!orderForm.value.token) return addLog('请先获取 Token！', 'error')
  loading.value = true
  addLog('发起单次下单请求...', 'info')
  await sendSubmitRequest(1)
  orderForm.value.token = '' // Token is burned
  loading.value = false
}

const submitConcurrent = async () => {
  if (!orderForm.value.token) return addLog('请先获取 Token！', 'error')
  loading.value = true
  addLog('模拟快速连点，同时发出 3 个相同的下单请求...', 'warning')
  
  // Fire multiple requests concurrently to test backend idempotency
  await Promise.all([
    sendSubmitRequest(1),
    sendSubmitRequest(2),
    sendSubmitRequest(3)
  ])
  
  orderForm.value.token = ''
  loading.value = false
}
</script>

<style scoped>
.order-submit { 
  max-width: 1200px; 
  margin: 0 auto; 
  animation: fadeIn 0.4s ease;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 24px;
  padding: 0 10px;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
  background: linear-gradient(135deg, #1e293b, #6366f1);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.page-subtitle {
  color: #64748b;
  margin: 0;
  font-size: 14px;
}

.main-card {
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05);
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

.header-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background: #e0e7ff;
  color: #4f46e5;
  border-radius: 8px;
}

/* Form Styles */
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #475569;
}

:deep(.custom-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

.amount-input-group {
  display: flex;
  align-items: center;
  gap: 16px;
}

.unit-tag {
  background: #f1f5f9;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* Radio Buttons */
:deep(.custom-radio-group .el-radio-button__inner) {
  border: 1px solid #cbd5e1;
  background: white;
  color: #64748b;
}

:deep(.custom-radio-group .el-radio-button:first-child .el-radio-button__inner) {
  border-radius: 8px 0 0 8px;
}

:deep(.custom-radio-group .el-radio-button:last-child .el-radio-button__inner) {
  border-radius: 0 8px 8px 0;
}

:deep(.custom-radio-group .el-radio-button__original-radio:checked + .el-radio-button__inner) {
  background: linear-gradient(135deg, #6366f1, #818cf8);
  border-color: #6366f1;
  box-shadow: -1px 0 0 0 #6366f1;
  color: white;
}

.radio-content {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
}

/* Token Fetcher */
.token-fetcher {
  display: flex;
  gap: 12px;
  width: 100%;
}

:deep(.token-input .el-input__wrapper) {
  border-radius: 8px;
  background: #f8fafc;
}

:deep(.token-input .el-input__wrapper.is-disabled) {
  background-color: #f1f5f9;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
  cursor: not-allowed;
}

:deep(.token-input .el-input__inner) {
  color: #10b981;
  font-family: 'Monaco', monospace;
  font-weight: 600;
  -webkit-text-fill-color: #10b981; /* Ensure color shows in disabled state */
}

.fetch-btn {
  border-radius: 8px;
  font-weight: 600;
  min-width: 120px;
}

.action-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 32px 0 24px 0;
}

/* Submit Buttons */
.btn-group { 
  display: flex; 
  gap: 16px; 
}

.submit-btn {
  flex: 1;
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s;
}

.submit-btn:hover {
  transform: translateY(-2px);
}

.normal-btn {
  background: linear-gradient(135deg, #3b82f6, #60a5fa);
  border: none;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
}

.normal-btn:hover {
  box-shadow: 0 6px 16px rgba(59, 130, 246, 0.4);
}

.attack-btn {
  background: linear-gradient(135deg, #ef4444, #f87171);
  border: none;
  box-shadow: 0 4px 12px rgba(239, 68, 68, 0.3);
}

.attack-btn:hover {
  box-shadow: 0 6px 16px rgba(239, 68, 68, 0.4);
}

/* Console View */
.console-card {
  border: none;
  border-radius: 16px;
  background: #0f172a;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.3);
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.console-card .el-card__header) {
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding: 16px 20px;
}

:deep(.console-card .el-card__body) {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

.console-header {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #94a3b8;
  font-family: 'Monaco', monospace;
  font-size: 14px;
}

.dots {
  display: flex;
  gap: 6px;
}

.dots span {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dots .red { background: #ef4444; }
.dots .yellow { background: #f59e0b; }
.dots .green { background: #10b981; }

.result-box {
  padding: 20px;
  height: 400px; 
  overflow-y: auto; 
  font-family: 'Monaco', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
}

/* Scrollbar for console */
.result-box::-webkit-scrollbar { width: 8px; }
.result-box::-webkit-scrollbar-track { background: rgba(0, 0, 0, 0.2); }
.result-box::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.2); border-radius: 4px; }
.result-box::-webkit-scrollbar-thumb:hover { background: rgba(255, 255, 255, 0.3); }

.empty-console {
  color: #475569;
  text-align: center;
  margin-top: 40%;
  font-style: italic;
}

.log-line {
  margin: 0 0 8px 0;
  display: flex;
  gap: 12px;
  animation: slideIn 0.3s ease;
}

.log-time {
  color: #64748b;
  flex-shrink: 0;
}

.log-content {
  word-break: break-all;
}

.success .log-content { color: #10b981; }
.error .log-content { color: #ef4444; }
.warning .log-content { color: #f59e0b; }
.info .log-content { color: #38bdf8; }

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideIn {
  from { opacity: 0; transform: translateX(-10px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
