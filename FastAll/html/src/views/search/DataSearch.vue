<template>
  <div class="data-search">
    <div class="search-hero">
      <div class="hero-content">
        <h2 class="hero-title">
          <span class="gradient-text">全球药品文号检索引擎</span>
        </h2>
        <p class="hero-subtitle">Based on Elasticsearch / 万级并发 / 毫秒级响应 / 聚合搜索</p>
        
        <div class="search-box-wrapper">
          <el-input
            v-model="keyword"
            placeholder="输入药品名、成分、适应症或批次号进行全库检索..."
            class="mega-search-input"
            clearable
            @keyup.enter="handleSearch"
          >
            <template #prefix>
              <el-icon class="search-icon-prefix"><Search /></el-icon>
            </template>
            <template #append>
              <el-button class="search-submit-btn" type="primary" color="#4f46e5" @click="handleSearch">
                精准探索
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </div>

    <el-row :gutter="24" class="search-body" v-if="hasSearched">
      <!-- 侧边过滤面版 -->
      <el-col :span="6">
        <el-card class="filter-card main-card">
          <template #header>
            <div class="filter-header">
              <el-icon><Filter /></el-icon> 多维聚合分析
            </div>
          </template>
          
          <div class="filter-group">
            <h4 class="filter-title">药品剂型</h4>
            <el-checkbox-group v-model="filters.type" class="custom-checkbox-group">
              <el-checkbox label="胶囊" class="filter-checkbox">胶囊 (Capsule)</el-checkbox>
              <el-checkbox label="注射液" class="filter-checkbox">注射液 (Injection)</el-checkbox>
              <el-checkbox label="片剂" class="filter-checkbox">片剂 (Tablet)</el-checkbox>
            </el-checkbox-group>
          </div>
          
          <el-divider border-style="dashed" />
          
          <div class="filter-group">
            <h4 class="filter-title">是否处方药</h4>
            <el-radio-group v-model="filters.rx" class="custom-radio-group vertical">
              <el-radio label="ALL" class="filter-radio">不限范围</el-radio>
              <el-radio label="RX" class="filter-radio">处方药 (Rx)</el-radio>
              <el-radio label="OTC" class="filter-radio">非处方药 (OTC)</el-radio>
            </el-radio-group>
          </div>
        </el-card>
      </el-col>

      <!-- 搜索结果列表 -->
      <el-col :span="18">
        <div class="results-container" v-loading="loading">
          <div class="result-stats">
            <el-icon><DataAnalysis /></el-icon>
            找到 {{ results.length }} 个引擎匹配项 
            <span class="time-cost">（聚合耗时 {{ searchTime }} 毫秒）</span>
          </div>
          
          <div class="result-list">
            <el-card 
              v-for="item in results" 
              :key="item.id"
              class="result-card"
              shadow="hover"
            >
              <div class="result-content">
                <h3 class="item-title" v-html="highlight(item.title, keyword)"></h3>
                <p class="item-desc" v-html="highlight(item.desc, keyword)"></p>
                
                <div class="item-meta">
                  <div class="meta-tags">
                    <el-tag size="small" :type="item.type.includes('OTC') ? 'success' : 'warning'" effect="light" class="meta-tag">
                      {{ item.type }}
                    </el-tag>
                    <el-tag size="small" type="info" effect="plain" class="meta-tag approval-tag">
                      <el-icon><Stamp /></el-icon> {{ item.approval }}
                    </el-tag>
                  </div>
                  <el-button link type="primary" class="detail-btn">查看图谱 <el-icon><ArrowRight /></el-icon></el-button>
                </div>
              </div>
            </el-card>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const keyword = ref('')
const hasSearched = ref(false)
const loading = ref(false)
const searchTime = ref(0)
const filters = ref({ type: [], rx: 'ALL' })
const results = ref([])

const handleSearch = () => {
  if (!keyword.value) return
  hasSearched.value = true
  loading.value = true
  
  // TODO: Call backend Elasticsearch Search API
  setTimeout(() => {
    searchTime.value = Math.floor(Math.random() * 50) + 10 // mock tiny latency
    results.value = [
      { id: 1, title: '阿莫西林胶囊', desc: '用于敏感致病菌所致呼吸道感染、泌尿生殖道感染、急性单纯性淋病等。主要成分为阿莫西林。', type: '胶囊 / 处方药', approval: '国药准字H20030111' },
      { id: 2, title: '连花清瘟胶囊', desc: '清瘟解毒，宣肺泄热。用于治疗流行性感冒属热毒袭肺证。', type: '胶囊 / OTC', approval: '国药准字Z20040063' }
    ]
    loading.value = false
  }, 400)
}

// 简易前端模拟高亮 (实际开发由后端 ES Highlighting 返回)
const highlight = (text, key) => {
  if (!key) return text
  // 忽略特殊字符，仅做简单正则
  const safeStr = key.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&')
  const reg = new RegExp(safeStr, 'gi')
  return text.replace(reg, `<span class="highlight-text">$&</span>`)
}
</script>

<style scoped>
.data-search { 
  max-width: 1200px; 
  margin: 0 auto; 
  animation: fadeIn 0.4s ease;
}

/* Hero Section */
.search-hero {
  padding: 40px 0 60px 0;
  text-align: center;
  position: relative;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  margin: 0 0 16px 0;
  letter-spacing: -1px;
}

.gradient-text {
  background: linear-gradient(135deg, #1e293b, #4f46e5);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-subtitle {
  color: #64748b;
  font-size: 16px;
  margin: 0 0 40px 0;
  font-family: 'Monaco', monospace;
}

.search-box-wrapper {
  max-width: 760px;
  margin: 0 auto;
  position: relative;
  z-index: 10;
}

/* Ultra Search Input */
:deep(.mega-search-input .el-input__wrapper) {
  padding: 12px 24px;
  border-radius: 999px 0 0 999px;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.1) !important;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(226, 232, 240, 0.8);
  transition: all 0.3s ease;
}

:deep(.mega-search-input .el-input__wrapper.is-focus) {
  box-shadow: 0 20px 40px -10px rgba(79, 70, 229, 0.2) !important;
  border-color: #818cf8;
}

:deep(.mega-search-input .el-input__inner) {
  height: 36px;
  font-size: 16px;
  color: #1e293b;
}

:deep(.mega-search-input .el-input__inner::placeholder) {
  color: #94a3b8;
  font-weight: 400;
}

.search-icon-prefix {
  font-size: 20px;
  color: #64748b;
  margin-right: 8px;
}

:deep(.mega-search-input .el-input-group__append) {
  border-radius: 0 999px 999px 0;
  padding: 0;
  box-shadow: 0 20px 40px -10px rgba(0, 0, 0, 0.1);
  border: none;
  background: transparent;
}

.search-submit-btn {
  height: 62px;
  padding: 0 32px;
  border-radius: 0 999px 999px 0;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  box-shadow: 0 10px 20px -5px rgba(79, 70, 229, 0.3);
}

/* Filter Card */
.main-card {
  border: none;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(16px);
  box-shadow: 0 10px 30px -5px rgba(0, 0, 0, 0.05);
}

.filter-header {
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-size: 15px;
}

.filter-title {
  margin: 0 0 16px 0;
  color: #475569;
  font-size: 14px;
  font-weight: 600;
}

.filter-group {
  margin-bottom: 24px;
}

.custom-checkbox-group, .custom-radio-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

:deep(.filter-checkbox .el-checkbox__label),
:deep(.filter-radio .el-radio__label) {
  color: #64748b;
  font-weight: 500;
}

/* Results Area */
.results-container {
  padding: 0 10px;
}

.result-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #1e293b;
  font-weight: 600;
  font-size: 15px;
  margin-bottom: 24px;
}

.time-cost {
  color: #94a3b8;
  font-family: 'Monaco', monospace;
  font-size: 13px;
  font-weight: 400;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.result-card {
  border: none;
  border-radius: 16px;
  background: #ffffff;
  transition: all 0.3s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
}

.result-card:hover {
  transform: translateY(-2px) translateX(4px);
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), -4px 0 0 0 #4f46e5;
}

.result-content {
  padding: 8px 4px;
}

.item-title {
  margin: 0 0 12px 0;
  color: #1e293b;
  font-size: 18px;
  font-weight: 700;
  cursor: pointer;
  display: inline-block;
  transition: color 0.2s;
}

.item-title:hover {
  color: #4f46e5;
}

.item-desc {
  color: #475569;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 20px 0;
  max-width: 90%;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.meta-tags {
  display: flex;
  gap: 12px;
  align-items: center;
}

.meta-tag {
  border: none;
  font-weight: 600;
  border-radius: 6px;
}

.approval-tag {
  background: #f8fafc;
  border: 1px dashed #cbd5e1;
  color: #64748b;
  font-family: 'Monaco', monospace;
}

.detail-btn {
  font-weight: 600;
}

/* Highlighting returned by backend/mock */
:deep(.highlight-text) {
  color: #ef4444;
  font-weight: 700;
  background: rgba(239, 68, 68, 0.1);
  padding: 0 2px;
  border-radius: 2px;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
