<template>
  <div class="s-canvas" @click="drawPic">
    <canvas id="s-canvas" :width="contentWidth" :height="contentHeight"></canvas>
  </div>
</template>

<script setup>
import { onMounted, watch } from 'vue'

const props = defineProps({
  identifyCode: {
    type: String,
    default: '1234'
  },
  fontSizeMin: {
    type: Number,
    default: 25
  },
  fontSizeMax: {
    type: Number,
    default: 35
  },
  backgroundColorMin: {
    type: Number,
    default: 200
  },
  backgroundColorMax: {
    type: Number,
    default: 220
  },
  dotColorMin: {
    type: Number,
    default: 60
  },
  dotColorMax: {
    type: Number,
    default: 120
  },
  contentWidth: {
    type: Number,
    default: 116
  },
  contentHeight: {
    type: Number,
    default: 38
  }
})

// 生成随机数
const randomNum = (min, max) => {
  return Math.floor(Math.random() * (max - min) + min)
}

// 生成随机颜色
const randomColor = (min, max) => {
  const r = randomNum(min, max)
  const g = randomNum(min, max)
  const b = randomNum(min, max)
  return `rgb(${r},${g},${b})`
}

const drawPic = () => {
  const canvas = document.getElementById('s-canvas')
  const ctx = canvas.getContext('2d')
  ctx.textBaseline = 'bottom'
  // 绘制背景
  ctx.fillStyle = randomColor(props.backgroundColorMin, props.backgroundColorMax)
  ctx.fillRect(0, 0, props.contentWidth, props.contentHeight)
  // 绘制文字
  for (let i = 0; i < props.identifyCode.length; i++) {
    drawText(ctx, props.identifyCode[i], i)
  }
  drawLine(ctx)
  drawDot(ctx)
}

const drawText = (ctx, txt, i) => {
  ctx.fillStyle = randomColor(50, 160)
  ctx.font = randomNum(props.fontSizeMin, props.fontSizeMax) + 'px SimHei'
  const x = (i + 1) * (props.contentWidth / (props.identifyCode.length + 1))
  const y = randomNum(props.fontSizeMax, props.contentHeight - 5)
  const deg = randomNum(-30, 30)
  // 修改坐标原点和旋转角度
  ctx.translate(x, y)
  ctx.rotate(deg * Math.PI / 180)
  ctx.fillText(txt, 0, 0)
  // 恢复坐标原点和旋转角度
  ctx.rotate(-deg * Math.PI / 180)
  ctx.translate(-x, -y)
}

const drawLine = (ctx) => {
  for (let i = 0; i < 4; i++) {
    ctx.strokeStyle = randomColor(100, 200)
    ctx.beginPath()
    ctx.moveTo(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight))
    ctx.lineTo(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight))
    ctx.stroke()
  }
}

const drawDot = (ctx) => {
  for (let i = 0; i < 30; i++) {
    ctx.fillStyle = randomColor(0, 255)
    ctx.beginPath()
    ctx.arc(randomNum(0, props.contentWidth), randomNum(0, props.contentHeight), 1, 0, 2 * Math.PI)
    ctx.fill()
  }
}

watch(() => props.identifyCode, () => {
  drawPic()
})

onMounted(() => {
  drawPic()
})
</script>

<style scoped>
.s-canvas {
  height: 38px;
  cursor: pointer;
}
.s-canvas canvas {
  margin-top: 1px;
  margin-left: 8px;
}
</style>
