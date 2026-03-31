<template>
  <div class="slider-captcha" v-loading="loading">
    <div class="captcha-main">
      <div class="image-wrapper" :style="{ width: canvasWidth + 'px', height: canvasHeight + 'px' }">
        <img :src="backImg" class="back-img" alt="背景图">
        <img 
          :src="jigsawImg" 
          class="jigsaw-img" 
          :style="{ left: moveX + 'px' }" 
          alt="滑块图"
        >
        <div v-if="status === 'success'" class="status-overlay success">
          <el-icon><CircleCheck /></el-icon> 验证成功
        </div>
        <div v-if="status === 'fail'" class="status-overlay fail">
          <el-icon><CircleClose /></el-icon> 验证失败，请重试
        </div>
      </div>
      
      <div class="slider-control" ref="sliderRef">
        <div class="slider-track" :style="{ width: (moveX + handleWidth/2) + 'px', backgroundColor: trackColor }"></div>
        <div 
          class="slider-handle" 
          :style="{ left: moveX + 'px' }"
          @mousedown="onDragStart"
          @touchstart="onDragStart"
        >
          <el-icon v-if="status === 'idle'"><ArrowRightBold /></el-icon>
          <el-icon v-else-if="status === 'moving'"><DArrowRight /></el-icon>
          <el-icon v-else-if="status === 'success'"><Check /></el-icon>
          <el-icon v-else-if="status === 'fail'"><Close /></el-icon>
        </div>
        <div class="slider-text" v-show="status === 'idle'">向右滑动完成拼图</div>
      </div>
      
      <div class="captcha-footer">
        <el-button link icon="Refresh" @click="fetchData">刷新</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { getCaptcha, checkCaptcha } from '@/api/auth'
import { aesEncrypt } from '@/utils/aes'

const emit = defineEmits(['success', 'error'])

const loading = ref(false)
const status = ref('idle') // idle, moving, success, fail
const backImg = ref('')
const jigsawImg = ref('')
const token = ref('')
const secretKey = ref('')

const moveX = ref(0)
const startX = ref(0)
const isDragging = ref(false)

const canvasWidth = 310
const canvasHeight = 155
const handleWidth = 40

const trackColor = computed(() => {
  if (status.value === 'success') return '#f0f9eb'
  if (status.value === 'fail') return '#fef0f0'
  if (status.value === 'moving') return '#ecf5ff'
  return '#f5f7fa'
})

// 获取验证码数据
const fetchData = async () => {
  loading.value = true
  status.value = 'idle'
  moveX.value = 0
  try {
    const res = await getCaptcha({ captchaType: 'blockPuzzle' })
    if (res.repCode === '0000') {
      const data = res.repData
      backImg.value = data.originalImageBase64.startsWith('data:image') ? data.originalImageBase64 : `data:image/png;base64,${data.originalImageBase64}`
      jigsawImg.value = data.jigsawImageBase64.startsWith('data:image') ? data.jigsawImageBase64 : `data:image/png;base64,${data.jigsawImageBase64}`
      token.value = data.token
      secretKey.value = data.secretKey
    }
  } catch (err) {
    console.error('Fetch Captcha Error:', err)
  } finally {
    loading.value = false
  }
}

// 拖拽逻辑
const onDragStart = (e) => {
  if (status.value === 'success' || loading.value) return
  isDragging.value = true
  status.value = 'moving'
  const clientX = e.type === 'touchstart' ? e.touches[0].clientX : e.clientX
  startX.value = clientX - moveX.value
  
  window.addEventListener('mousemove', onDragging)
  window.addEventListener('mouseup', onDragEnd)
  window.addEventListener('touchmove', onDragging)
  window.addEventListener('touchend', onDragEnd)
}

const onDragging = (e) => {
  if (!isDragging.value) return
  const clientX = e.type === 'touchmove' ? e.touches[0].clientX : e.clientX
  let x = clientX - startX.value
  const maxX = canvasWidth - handleWidth
  if (x < 0) x = 0
  if (x > maxX) x = maxX
  moveX.value = x
}

const onDragEnd = async () => {
  if (!isDragging.value) return
  isDragging.value = false
  
  // 计算实际坐标
  const x = Math.round(moveX.value)
  const pointJson = JSON.stringify({ x, y: 5 })
  const encryptedPoint = aesEncrypt(pointJson, secretKey.value)
  
  console.log('[Captcha Debug] Pos:', { x, y: 5 })
  console.log('[Captcha Debug] Raw JSON:', pointJson)
  console.log('[Captcha Debug] Encrypted:', encryptedPoint)
  
  try {
    loading.value = true
    const res = await checkCaptcha({
      captchaType: 'blockPuzzle',
      pointJson: encryptedPoint,
      token: token.value
    })
    
    console.log('[Captcha Debug] Check API Response:', res)
    
    if (res.repCode === '0000') {
      status.value = 'success'
      // 容错处理：优先取 captchaVerification，没取到则按照官方标准手动生成
      let verification = res.repData ? res.repData.captchaVerification : null
      
      if (!verification) {
        console.warn('[Captcha Debug] Backend returned null verification, generating manually...')
        // 算法：AES(token + "---" + JSON.stringify(point), secretKey)
        verification = aesEncrypt(token.value + '---' + pointJson, secretKey.value)
        console.log('[Captcha Debug] Manually Generated Verification:', verification)
      } else {
        console.log('[Captcha Debug] Extracted Verification from Response:', verification)
      }
      
      emit('success', verification)
    } else {
      status.value = 'fail'
      setTimeout(() => {
        fetchData()
      }, 1000)
    }
  } catch (err) {
    status.value = 'fail'
    setTimeout(() => {
      fetchData()
    }, 1000)
  } finally {
    loading.value = false
    window.removeEventListener('mousemove', onDragging)
    window.removeEventListener('mouseup', onDragEnd)
    window.removeEventListener('touchmove', onDragging)
    window.removeEventListener('touchend', onDragEnd)
  }
}

onMounted(() => {
  fetchData()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onDragging)
  window.removeEventListener('mouseup', onDragEnd)
  window.removeEventListener('touchmove', onDragging)
  window.removeEventListener('touchend', onDragEnd)
})
</script>

<style scoped>
.slider-captcha {
  width: 310px;
  background: #fff;
  border-radius: 4px;
}

.image-wrapper {
  position: relative;
  overflow: hidden;
  border-radius: 4px;
  background: #f5f7fa;
}

.back-img {
  width: 100%;
  height: 100%;
  display: block;
}

.jigsaw-img {
  position: absolute;
  top: 0;
  height: 100%;
  z-index: 2;
}

.status-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 30px;
  line-height: 30px;
  text-align: center;
  font-size: 13px;
  color: #fff;
  z-index: 3;
}

.status-overlay.success {
  background: rgba(103, 194, 58, 0.8);
}

.status-overlay.fail {
  background: rgba(245, 108, 108, 0.8);
}

.slider-control {
  position: relative;
  height: 40px;
  background: #f5f7fa;
  margin-top: 15px;
  border-radius: 20px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

.slider-track {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  border-radius: 20px 0 0 20px;
  transition: background-color 0.3s;
}

.slider-handle {
  position: absolute;
  top: 0;
  width: 40px;
  height: 40px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  z-index: 4;
  transition: box-shadow 0.3s, transform 0.2s;
  color: #409eff;
}

.slider-handle:hover {
  box-shadow: 0 0 15px rgba(64, 158, 255, 0.3);
  transform: scale(1.05);
}

.slider-text {
  width: 100%;
  text-align: center;
  line-height: 40px;
  font-size: 12px;
  color: #909399;
  user-select: none;
}

.captcha-footer {
  margin-top: 10px;
  display: flex;
  justify-content: flex-end;
}
</style>
