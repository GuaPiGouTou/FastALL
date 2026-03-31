<template>
  <div class="evidence-verifier">
    <div class="page-header">
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Link /></el-icon> GxP 实验数据链上查验与追溯中心
        </h2>
        <p class="page-subtitle">区块链存证防篡改技术验证平台</p>
      </div>
    </div>

    <el-card class="box-card main-card">

      <el-alert
        title="GxP 完整性警告"
        type="warning"
        description="本系统产生的核心实验数据和批次报告均上链存证。您可以在此输入单据号进行全节点比对，以验证数据真实性。"
        show-icon
        class="custom-alert"
      />

      <div class="verify-action-area">
        <el-input 
          v-model="documentId" 
          placeholder="请输入实验单号或检验报告编号..." 
          class="verify-input"
        >
          <template #prefix>
            <el-icon><Document /></el-icon>
          </template>
        </el-input>
        <el-button color="#10b981" class="verify-btn" @click="verifyEvidence" :loading="loading">
          <template #icon><el-icon><Position /></el-icon></template>
          全网跨节点校验
        </el-button>
      </div>

      <div v-if="verifyResult" class="result-section">
        <el-divider border-style="dashed">
          <span class="divider-text"><el-icon><Cpu /></el-icon> 智能合约验证回执</span>
        </el-divider>
        
        <el-descriptions :column="1" border class="custom-descriptions">
          <el-descriptions-item label="单据编号">
            <span class="strong-text">{{ verifyResult.docId }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="本地数据指纹 (MD5)">
            <code class="hash-code local-hash">{{ verifyResult.localHash }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="区块存证指纹 (Hash)">
            <code class="hash-code chain-hash">{{ verifyResult.chainHash }}</code>
          </el-descriptions-item>
          <el-descriptions-item label="上链时间">
            <span class="time-text">{{ verifyResult.timestamp }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="最终裁定">
            <div class="verdict-box" :class="verifyResult.match ? 'success-verdict' : 'danger-verdict'">
              <el-icon size="24">{{ verifyResult.match ? 'SuccessFilled' : 'CircleCloseFilled' }}</el-icon>
              <span>{{ verifyResult.match ? '数据一致，未被篡改 (Matched)' : '警告：本地数据与链上存证不符！(Tampered)' }}</span>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const documentId = ref('EXP-2026-00928')
const loading = ref(false)
const verifyResult = ref(null)

const verifyEvidence = () => {
  if (!documentId.value) return
  loading.value = true
  
  // TODO: Call backend to get local data hash and smart contract to get chain hash
  setTimeout(() => {
    const isMockTampered = documentId.value.includes('error') // 制造错误数据的后门
    verifyResult.value = {
      docId: documentId.value,
      localHash: isMockTampered ? 'e10adc3949ba59abbe56e057f20f883f' : 'a7f3df210c4ba98b25e6e027f21f114e',
      chainHash: 'a7f3df210c4ba98b25e6e027f21f114e',
      timestamp: '2026-03-10 14:22:05',
      match: !isMockTampered
    }
    loading.value = false
  }, 1000) // 模拟链上查询延迟
}
</script>

<style scoped>
.evidence-verifier { 
  max-width: 1000px; 
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
  background: linear-gradient(135deg, #0f172a, #f59e0b);
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
  padding: 10px;
}

.custom-alert {
  border-radius: 12px;
  margin-bottom: 30px;
  border: 1px solid rgba(245, 158, 11, 0.2);
}

:deep(.custom-alert .el-alert__title) {
  font-size: 15px;
  font-weight: 700;
}

.verify-action-area {
  display: flex;
  gap: 16px;
  margin-bottom: 40px;
  background: #f8fafc;
  padding: 24px;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

:deep(.verify-input .el-input__wrapper) {
  padding: 8px 16px;
  border-radius: 12px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
}

:deep(.verify-input .el-input__inner) {
  font-size: 15px;
  color: #1e293b;
}

.verify-btn {
  padding: 0 32px;
  border-radius: 12px;
  font-weight: 600;
  letter-spacing: 0.5px;
  height: auto;
  min-width: 180px;
  font-size: 15px;
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
  transition: all 0.3s;
}

.verify-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(16, 185, 129, 0.4);
}

/* Result Section */
.result-section {
  animation: slideUp 0.4s ease;
}

.divider-text {
  color: #64748b;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
}

:deep(.custom-descriptions .el-descriptions__label) {
  width: 200px;
  background-color: #f8fafc;
  color: #475569;
  font-weight: 600;
  font-size: 14px;
}

.strong-text {
  font-weight: 700;
  color: #1e293b;
}

.hash-code { 
  display: block;
  background: #0f172a; 
  padding: 8px 12px; 
  font-family: 'Monaco', 'Consolas', monospace; 
  border-radius: 6px; 
  font-size: 13px;
  letter-spacing: 1px;
}

.local-hash { color: #38bdf8; }
.chain-hash { color: #f59e0b; }

.time-text {
  color: #64748b;
  font-family: 'Monaco', monospace;
  font-size: 14px;
}

.verdict-box {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 15px;
}

.success-verdict {
  background: rgba(16, 185, 129, 0.1);
  color: #059669;
  border: 1px solid rgba(16, 185, 129, 0.2);
}

.danger-verdict {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
  border: 1px solid rgba(239, 68, 68, 0.2);
  animation: pulse 2s infinite;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(239, 68, 68, 0); }
  100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0); }
}
</style>
