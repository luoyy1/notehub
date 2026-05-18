<template>
  <div class="event-card glass" :style="cardStyle">
    <div class="card-header">
      <div>
        <div class="title-row">
          <span v-if="event.pinned" class="pin">置顶</span>
          <h3 class="event-name">{{ event.name }}</h3>
        </div>
        <div class="meta-row">
          <span>{{ categoryLabel }}</span>
          <span v-if="event.location">{{ event.location }}</span>
          <span v-if="event.mood">{{ event.mood }}</span>
        </div>
      </div>
      <div class="card-actions">
        <span class="event-date">{{ event.date }}</span>
        <button class="edit-btn" type="button" @click="$emit('edit', event)">编辑</button>
      </div>
    </div>

    <p v-if="event.story" class="story">{{ event.story }}</p>

    <div v-if="event.tags?.length" class="tags">
      <span v-for="tag in event.tags" :key="tag"># {{ tag }}</span>
    </div>
    
    <div class="card-body">
      <div v-if="!event.enableCountUp && !event.enableCountdown" class="empty-state">
        <p>只记录，不计时</p>
      </div>

      <div v-if="event.enableCountUp" class="timer-section count-up">
        <div class="timer-main">
          <span class="label">已经历</span>
          <div class="number-group">
            <span class="number">{{ event.daysPassed }}</span>
            <span class="unit">天</span>
          </div>
        </div>
        <div class="timer-sub" v-if="event.daysToNextHundred">
          距离 {{ Number(event.daysPassed) + Number(event.daysToNextHundred) }} 天纪念还有 <strong>{{ event.daysToNextHundred }}</strong> 天
        </div>
      </div>

      <div v-if="event.enableCountdown" class="timer-section countdown">
        <div class="timer-main">
          <span class="label">{{ event.daysUntil === 0 ? '今天' : '距离下次还有' }}</span>
          <div class="number-group">
            <span class="number highlight">{{ event.daysUntil }}</span>
            <span class="unit">天</span>
          </div>
        </div>
        <div class="timer-sub" v-if="event.isAnnual">
          {{ event.daysUntil === 0 ? `就是今天，${event.nextAge} 岁啦` : `即将在 ${event.nextAge} 岁触发` }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  event: {
    type: Object,
    required: true
  }
});

defineEmits(['edit']);

const categoryMap = {
  anniversary: '纪念日',
  birthday: '生日',
  travel: '旅行',
  life: '生活',
  work: '工作',
  other: '其他'
};

const categoryLabel = computed(() => categoryMap[props.event.category || 'other'] || props.event.category);
const cardStyle = computed(() => ({
  '--event-color': props.event.color || '#ec4899'
}));
</script>

<style scoped>
.event-card {
  position: relative;
  border-radius: 16px;
  padding: 20px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  gap: 14px;
  box-shadow: 0 10px 24px rgba(31, 41, 55, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  border: 1px solid var(--line);
  overflow: hidden;
}

.event-card::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 5px;
  background: var(--event-color);
}

.event-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 18px 40px rgba(31, 41, 55, 0.11);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pin {
  color: #9a3412;
  background: #ffedd5;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.72rem;
  font-weight: 800;
}

.event-name {
  margin: 0;
  font-size: 1.16rem;
  color: var(--text-primary);
  font-weight: 850;
}

.event-date {
  font-size: 0.85rem;
  color: var(--text-muted);
  font-family: monospace;
  white-space: nowrap;
}

.card-actions {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.edit-btn {
  border: 1px solid color-mix(in srgb, var(--event-color) 26%, white);
  border-radius: 999px;
  background: color-mix(in srgb, var(--event-color) 7%, white);
  color: var(--event-color);
  cursor: pointer;
  font: inherit;
  font-size: 0.78rem;
  font-weight: 800;
  padding: 6px 10px;
}

.edit-btn:hover {
  background: color-mix(in srgb, var(--event-color) 14%, white);
}

.meta-row,
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.meta-row {
  margin-top: 8px;
}

.meta-row span,
.tags span {
  color: var(--text-secondary);
  background: #f8fafc;
  border: 1px solid #edf0f3;
  border-radius: 999px;
  padding: 5px 9px;
  font-size: 0.78rem;
  font-weight: 700;
}

.story {
  margin: 0;
  color: #475467;
  line-height: 1.65;
  background: #fff7ed;
  border: 1px solid #fed7aa;
  border-radius: 12px;
  padding: 12px 14px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.timer-section {
  position: relative;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 15px 16px;
  border-radius: 14px;
  background: #ffffff;
  overflow: hidden;
  box-shadow: none;
  border: 1px solid #eef2f6;
}

.count-up::after,
.countdown::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 40px;
  pointer-events: none;
}

.count-up::after {
  background: linear-gradient(to top, color-mix(in srgb, var(--event-color) 12%, transparent), rgba(255, 255, 255, 0));
}

.countdown::after {
  background: linear-gradient(to top, rgba(249, 115, 22, 0.08), rgba(255, 255, 255, 0));
}

.timer-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
}

.label {
  color: var(--text-secondary);
  font-weight: 750;
  font-size: 0.95rem;
}

.number-group {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.number {
  font-size: 2.12rem;
  font-weight: 800;
  color: var(--event-color);
  line-height: 1;
}

.highlight {
  color: var(--secondary);
}

.unit {
  font-size: 0.9rem;
  color: var(--text-muted);
  font-weight: 600;
}

.timer-sub {
  font-size: 0.85rem;
  color: var(--text-secondary);
  position: relative;
  z-index: 2;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #999;
  font-style: italic;
  font-size: 0.9rem;
}

@media (max-width: 520px) {
  .card-header {
    flex-direction: column;
  }

  .card-actions {
    width: 100%;
    flex-direction: row;
    align-items: center;
    justify-content: space-between;
  }
}
</style>
