<template>
  <div class="calendar-container glass">
    <div class="calendar-header">
      <h3 class="title">动态日历</h3>
      <div class="year-selector">
        <span>{{ currentYear }} 年</span>
      </div>
    </div>
    
    <!-- 横向月份导航栏 -->
    <div class="month-nav">
      <button 
        v-for="m in 12" 
        :key="m" 
        class="month-tab"
        :class="{ active: currentMonth === m - 1 }"
        @click="currentMonth = m - 1"
      >
        {{ m }}月
        <div v-if="hasEventsInMonth(m)" class="month-dot"></div>
      </button>
    </div>

    <div class="calendar-grid">
      <div v-for="day in ['日', '一', '二', '三', '四', '五', '六']" :key="day" class="weekday">
        {{ day }}
      </div>
      
      <div v-for="blank in blankDays" :key="'blank-' + blank" class="day-cell blank"></div>
      
      <div 
        v-for="day in daysInMonth" 
        :key="'day-' + day" 
        class="day-cell"
        :class="{
          'today': isToday(day),
          'has-event': getEventsForDay(day).length > 0
        }"
      >
        <span class="day-number">{{ day }}</span>
        
        <!-- 悬停放大的 Tooltip -->
        <div class="tooltip" v-if="getEventsForDay(day).length > 0">
          <div v-for="(event, i) in getEventsForDay(day)" :key="i" class="tooltip-event">
            <span class="tooltip-icon">✨</span>
            <span class="tooltip-name">{{ event.name }}</span>
          </div>
        </div>

        <div class="event-dots" v-if="getEventsForDay(day).length > 0">
          <div 
            v-for="(event, index) in getEventsForDay(day)" 
            :key="index"
            class="dot dot-pink"
          ></div>
        </div>
      </div>
    </div>
    
    <div class="calendar-legend">
      <div class="legend-item">
        <div class="dot dot-pink"></div>
        <span>重要事件</span>
      </div>
      <div class="legend-item">
        <div class="month-dot-demo"></div>
        <span>包含事件的月份</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  marks: {
    type: Array,
    default: () => []
  }
});

const today = new Date();
const currentYear = ref(today.getFullYear());
const currentMonth = ref(today.getMonth()); // 0-11

const daysInMonth = computed(() => {
  return new Date(currentYear.value, currentMonth.value + 1, 0).getDate();
});

const blankDays = computed(() => {
  return new Date(currentYear.value, currentMonth.value, 1).getDay();
});

const isToday = (day) => {
  return day === today.getDate() && 
         currentMonth.value === today.getMonth() && 
         currentYear.value === today.getFullYear();
};

const hasEventsInMonth = (monthNum) => {
  return props.marks.some(mark => mark.month === monthNum);
};

const getEventsForDay = (day) => {
  const dateStr = `${currentYear.value}-${String(currentMonth.value + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  return props.marks.filter(mark => mark.date === dateStr);
};
</script>

<style scoped>
.calendar-container {
  padding: 20px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 250, 246, 0.88));
  border-radius: 18px;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.title {
  font-size: 1.15rem;
  color: var(--text-primary);
  margin: 0;
}

.year-selector {
  font-weight: 800;
  color: var(--secondary);
  background: linear-gradient(135deg, rgba(204, 251, 241, 0.82), rgba(255, 255, 255, 0.86));
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.88rem;
}

/* 月份导航栏 */
.month-nav {
  display: flex;
  gap: 6px;
  overflow-x: auto;
  padding-bottom: 12px;
  margin-bottom: 16px;
  scrollbar-width: none; /* Firefox */
}
.month-nav::-webkit-scrollbar {
  display: none; /* Safari and Chrome */
}

.month-tab {
  flex: 0 0 auto;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.96), rgba(255, 250, 246, 0.86));
  border: 1px solid #eef2f6;
  padding: 8px 10px;
  border-radius: 10px;
  font-size: 0.86rem;
  font-weight: 750;
  color: var(--text-secondary);
  cursor: pointer;
  position: relative;
  transition: all 0.2s;
}

.month-tab:hover {
  background: linear-gradient(135deg, #ffffff, rgba(240, 253, 250, 0.8));
}

.month-tab.active {
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.95), rgba(204, 251, 241, 0.82));
  color: #7c2d12;
  border-color: rgba(253, 186, 116, 0.38);
}

.month-dot {
  position: absolute;
  bottom: 2px;
  left: 50%;
  transform: translateX(-50%);
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(194, 65, 12, 0.68), rgba(15, 118, 110, 0.52));
}
.month-tab.active .month-dot {
  background: linear-gradient(135deg, rgba(194, 65, 12, 0.72), rgba(15, 118, 110, 0.6));
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 6px;
  margin-bottom: 20px;
}

.weekday {
  text-align: center;
  font-weight: 800;
  color: var(--text-secondary);
  font-size: 0.78rem;
  padding-bottom: 8px;
}

.day-cell {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 38px;
  border-radius: 10px;
  position: relative;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: default;
}

.day-cell:not(.blank):hover {
  background: linear-gradient(135deg, #ffffff, rgba(255, 250, 246, 0.82));
}

.day-number {
  color: var(--text-secondary);
  font-weight: 700;
  z-index: 2;
  transition: transform 0.2s;
}

.today {
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.82), rgba(255, 255, 255, 0.9)) !important;
  box-shadow: inset 0 0 0 1px rgba(194, 65, 12, 0.22);
}

.today .day-number {
  color: var(--primary);
  font-weight: 700;
}

.has-event {
  background: linear-gradient(145deg, #ffffff, rgba(255, 250, 246, 0.82));
  box-shadow: 0 5px 14px rgba(31, 41, 55, 0.08);
  cursor: pointer;
}

.has-event:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(31, 41, 55, 0.12);
  z-index: 10;
  background: linear-gradient(145deg, #ffffff, rgba(240, 253, 250, 0.78));
}

.event-dots {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 4px;
  margin-top: 4px;
  padding: 0 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
}

.dot-pink {
  background: linear-gradient(135deg, rgba(194, 65, 12, 0.72), rgba(15, 118, 110, 0.58));
}

/* Tooltip */
.tooltip {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  background: rgba(0, 0, 0, 0.85);
  color: white;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 0.85rem;
  white-space: nowrap;
  pointer-events: none;
  opacity: 0;
  transition: all 0.2s;
  z-index: 20;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.tooltip::after {
  content: '';
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  border-width: 5px;
  border-style: solid;
  border-color: rgba(0, 0, 0, 0.85) transparent transparent transparent;
}

.has-event:hover .tooltip {
  opacity: 1;
  transform: translateX(-50%) translateY(0);
}

.tooltip-event {
  display: flex;
  align-items: center;
  gap: 6px;
}

.calendar-legend {
  display: flex;
  gap: 16px;
  justify-content: flex-end;
  font-size: 0.85rem;
  color: var(--text-secondary);
  border-top: 1px solid var(--line);
  padding-top: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.month-dot-demo {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(194, 65, 12, 0.72), rgba(15, 118, 110, 0.58));
}
</style>
