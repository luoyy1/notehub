<template>
  <main class="timeline-page">
    <section class="timeline-hero">
      <h1>时间轴</h1>
    </section>

    <section v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>正在整理时间轴...</p>
    </section>

    <section v-else class="timeline-shell">
      <div class="timeline-toolbar">
        <div>
          <span class="mode-title">今天 · {{ todayLabel }}</span>
          <span class="mode-subtitle">{{ filteredTimelineEvents.length }} 个匹配事件，{{ pastItems.length }} 个过去节点，{{ futureItems.length }} 个未来提醒</span>
        </div>
        <div class="toolbar-actions">
          <button class="soft-btn" type="button" @click="centerToday">回到今天</button>
          <button class="soft-btn" type="button" @click="fitAll">适配全部</button>
        </div>
      </div>

      <div class="filters-panel">
        <input v-model="keyword" class="search-input" type="search" placeholder="搜索名称、故事、地点、心情、标签" />
        <select v-model="activeCategory" class="filter-select">
          <option value="all">全部分类</option>
          <option v-for="category in categoryOptions" :key="category.value" :value="category.value">
            {{ category.label }}
          </option>
        </select>
        <select v-model="activeTag" class="filter-select">
          <option value="all">全部标签</option>
          <option v-for="tag in tagOptions" :key="tag" :value="tag">{{ tag }}</option>
        </select>
      </div>

      <div class="segmented-row">
        <button
          v-for="option in directionOptions"
          :key="option.value"
          class="segment-btn"
          :class="{ active: directionFilter === option.value }"
          type="button"
          @click="directionFilter = option.value"
        >
          {{ option.label }}
        </button>
      </div>

      <div class="state-filters">
        <label v-for="option in stateOptions" :key="option.value" class="switch-pill">
          <input v-model="stateFilters" type="checkbox" :value="option.value" />
          <span>{{ option.label }}</span>
        </label>
      </div>

      <div v-if="timelineEvents.length === 0" class="timeline-empty">
        <strong>时间轴还没有事件</strong>
        <span>回到首页事件中心，把需要展示的事件打开“加入时间轴”。</span>
      </div>

      <div v-else-if="filteredTimelineEvents.length === 0" class="timeline-empty">
        <strong>没有匹配的时间轴事件</strong>
        <span>调整搜索、分类、标签或状态筛选后再试。</span>
      </div>

      <div
        v-else
        ref="timelineViewportRef"
        class="timeline-viewport"
        :class="{ dragging }"
        @pointerdown="startDrag"
        @pointermove="dragTimeline"
        @pointerup="stopDrag"
        @pointercancel="stopDrag"
        @pointerleave="stopDrag"
        @wheel.prevent="wheelTimeline"
      >
        <div class="scale-strip" :style="{ width: timelineWidthStyle }">
          <div class="scale-line"></div>
          <span class="range-label start">{{ rangeStartLabel }}</span>
          <span class="range-label today" :style="{ left: todayLeft }">今天</span>
          <span class="range-label end">{{ rangeEndLabel }}</span>
          <div
            v-for="tick in timelineTicks"
            :key="tick.key"
            class="scale-tick"
            :class="{ major: tick.major }"
            :style="{ left: tick.left }"
          >
            <span></span>
            <em>{{ tick.label }}</em>
          </div>

          <article
            v-for="item in axisItems"
            :key="item.key"
            class="timeline-event"
            :class="item.kind"
            :style="getItemStyle(item)"
            @click="openQuickEditor(item)"
          >
            <span class="event-pin"></span>
            <button class="event-summary" type="button" @click.stop="openQuickEditor(item)">
              <span class="event-date">{{ item.displayDate }}</span>
              <strong>{{ item.name }}</strong>
              <span class="event-note">{{ item.summary }}</span>
              <div class="event-meta">
                <em>{{ categoryLabel(item.category) }}</em>
                <em v-if="item.metric">{{ item.metric }}</em>
              </div>
            </button>
          </article>
        </div>
      </div>

      <div v-if="filteredTimelineEvents.length > 0" class="compact-timeline">
        <section class="compact-section">
          <div class="compact-section-title">过去</div>
          <article v-for="item in pastItems" :key="item.key" class="compact-card" @click="openQuickEditor(item)">
            <strong>{{ item.name }}</strong>
            <span>{{ item.displayDate }} · {{ item.metric }}</span>
            <p>{{ item.summary }}</p>
          </article>
        </section>

        <section class="compact-today">
          <span>TODAY</span>
          <strong>{{ todayLabel }}</strong>
          <small>{{ weekdayLabel }}</small>
        </section>

        <section class="compact-section">
          <div class="compact-section-title">未来</div>
          <article v-for="item in futureItems" :key="item.key" class="compact-card" @click="openQuickEditor(item)">
            <strong>{{ item.name }}</strong>
            <span>{{ item.displayDate }} · {{ item.metric }}</span>
            <p>{{ item.summary }}</p>
          </article>
        </section>
      </div>
    </section>

    <EventQuickEditor
      :isOpen="isQuickEditorOpen"
      :event="quickEditEvent"
      @close="closeQuickEditor"
      @saved="fetchTimelineData"
    />
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue';
import { getEvents } from '../api';
import EventQuickEditor from './EventQuickEditor.vue';

const events = ref([]);
const loading = ref(true);
const timelineViewportRef = ref(null);
const dragging = ref(false);
const startX = ref(0);
const startScrollLeft = ref(0);
const keyword = ref('');
const activeCategory = ref('all');
const activeTag = ref('all');
const directionFilter = ref('all');
const stateFilters = ref([]);
const isQuickEditorOpen = ref(false);
const quickEditEvent = ref(null);
const today = new Date();
today.setHours(0, 0, 0, 0);

const laneGap = 118;
const axisBaseTop = 220;
const timelineWidth = 1680;
const axisInset = 140;
const laneCount = 4;
const dayMs = 86400000;

const categoryLabels = {
  anniversary: '纪念日',
  birthday: '生日',
  travel: '旅行',
  life: '生活',
  work: '工作',
  other: '其他'
};

const directionOptions = [
  { label: '全部', value: 'all' },
  { label: '过去', value: 'past' },
  { label: '未来', value: 'future' }
];

const stateOptions = [
  { label: '置顶', value: 'pinned' },
  { label: '正计时', value: 'countUp' },
  { label: '倒计时', value: 'countdown' },
  { label: '每年重复', value: 'annual' }
];

const addDays = (date, days) => {
  const next = new Date(date);
  next.setDate(next.getDate() + Number(days || 0));
  return next;
};

const parseDate = (dateText) => {
  const date = new Date(`${dateText}T00:00:00`);
  if (Number.isNaN(date.getTime())) return null;
  date.setHours(0, 0, 0, 0);
  return date;
};

const formatMonthDay = (date) => `${date.getMonth() + 1}月${date.getDate()}日`;
const formatFullDate = (date) => `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`;
const formatShortDate = (date) => `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;

const todayLabel = computed(() => formatFullDate(today));
const weekdayLabel = computed(() => today.toLocaleDateString('zh-CN', { weekday: 'long' }));
const timelineWidthStyle = computed(() => `${timelineWidth}px`);

const timelineEvents = computed(() => {
  return events.value.filter(event => Boolean(event.showInTimeline ?? event.show_in_timeline));
});

const categoryOptions = computed(() => {
  const values = new Set(timelineEvents.value.map(event => event.category || 'other'));
  return Array.from(values).map(value => ({
    value,
    label: categoryLabel(value)
  }));
});

const tagOptions = computed(() => {
  return Array.from(new Set(timelineEvents.value.flatMap(event => event.tags || []))).sort();
});

const normalizedItems = computed(() => {
  return timelineEvents.value.map((event) => {
    const originDate = parseDate(event.date);
    const daysUntil = Number(event.daysUntil);
    const hasFuture = Boolean(event.enableCountdown) && Number.isFinite(daysUntil) && daysUntil >= 0;
    const targetDate = hasFuture ? addDays(today, daysUntil) : originDate;
    const isPast = Boolean(event.enableCountUp) && originDate && originDate < today;
    const isFuture = hasFuture && targetDate && targetDate >= today;

    return {
      ...event,
      key: event.id || `${event.name}-${event.date}`,
      originDate,
      targetDate,
      isPast,
      isFuture,
      searchableText: [
        event.name,
        event.story,
        event.location,
        event.mood,
        ...(event.tags || [])
      ].filter(Boolean).join(' ').toLowerCase()
    };
  });
});

const filteredTimelineEvents = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  const states = new Set(stateFilters.value);

  return normalizedItems.value
    .filter(item => !text || item.searchableText.includes(text))
    .filter(item => activeCategory.value === 'all' || (item.category || 'other') === activeCategory.value)
    .filter(item => activeTag.value === 'all' || (item.tags || []).includes(activeTag.value))
    .filter(item => directionFilter.value === 'all' || (directionFilter.value === 'past' ? item.isPast : item.isFuture))
    .filter(item => !states.has('pinned') || item.pinned)
    .filter(item => !states.has('countUp') || item.enableCountUp)
    .filter(item => !states.has('countdown') || item.enableCountdown)
    .filter(item => !states.has('annual') || item.isAnnual);
});

const pastItems = computed(() => {
  return filteredTimelineEvents.value
    .filter(item => item.isPast)
    .sort((a, b) => a.originDate - b.originDate)
    .map((item, index) => toAxisItem(item, index, 'past'));
});

const futureItems = computed(() => {
  return filteredTimelineEvents.value
    .filter(item => item.isFuture)
    .sort((a, b) => a.targetDate - b.targetDate)
    .map((item, index) => toAxisItem(item, index, 'future'));
});

const axisItems = computed(() => [...pastItems.value, ...futureItems.value]);

const rangeStart = computed(() => {
  const dates = axisItems.value.map(item => item.axisDate).filter(Boolean);
  if (!dates.length) return addDays(today, -180);
  const earliest = new Date(Math.min(...dates.map(date => date.getTime())));
  return addDays(earliest < today ? earliest : today, -30);
});

const rangeEnd = computed(() => {
  const dates = axisItems.value.map(item => item.axisDate).filter(Boolean);
  if (!dates.length) return addDays(today, 365);
  const latest = new Date(Math.max(...dates.map(date => date.getTime())));
  return addDays(latest > today ? latest : today, 30);
});

const rangeDays = computed(() => Math.max(30, Math.ceil((rangeEnd.value - rangeStart.value) / dayMs)));
const drawableWidth = computed(() => timelineWidth - axisInset * 2);
const todayLeft = computed(() => `${dateToPixel(today)}px`);
const rangeStartLabel = computed(() => formatShortDate(rangeStart.value));
const rangeEndLabel = computed(() => formatShortDate(rangeEnd.value));

const timelineTicks = computed(() => {
  const ticks = [];
  const cursor = new Date(rangeStart.value.getFullYear(), rangeStart.value.getMonth(), 1);
  cursor.setMonth(cursor.getMonth() + 1);

  while (cursor < rangeEnd.value) {
    ticks.push({
      key: `${cursor.getFullYear()}-${cursor.getMonth()}`,
      label: cursor.getMonth() === 0 ? `${cursor.getFullYear()}` : `${cursor.getMonth() + 1}月`,
      major: cursor.getMonth() === 0,
      left: `${dateToPixel(cursor)}px`
    });
    cursor.setMonth(cursor.getMonth() + 1);
  }

  return ticks;
});

function categoryLabel(value) {
  return categoryLabels[value || 'other'] || value || '其他';
}

function dateToPercent(date) {
  return Math.min(100, Math.max(0, ((date - rangeStart.value) / dayMs / rangeDays.value) * 100));
}

function dateToPixel(date) {
  return axisInset + (drawableWidth.value * dateToPercent(date)) / 100;
}

function toAxisItem(item, index, kind) {
  const axisDate = kind === 'past' ? item.originDate : item.targetDate;
  const metric = kind === 'past'
    ? `已过 ${item.daysPassed || Math.max(0, Math.round((today - item.originDate) / dayMs))} 天`
    : (Number(item.daysUntil) === 0 ? '今天' : `${item.daysUntil} 天后`);

  return {
    ...item,
    kind,
    lane: index % laneCount,
    axisDate,
    displayDate: axisDate ? formatShortDate(axisDate) : item.date,
    metric,
    summary: item.story || item.location || item.mood || categoryLabel(item.category)
  };
}

const getItemStyle = (item) => ({
  left: `${dateToPixel(item.axisDate)}px`,
  top: `${axisBaseTop + item.lane * laneGap}px`
});

const centerToday = async () => {
  await nextTick();
  const viewport = timelineViewportRef.value;
  if (!viewport) return;
  viewport.scrollLeft = Math.max(0, dateToPixel(today) - viewport.clientWidth / 2);
};

const fitAll = async () => {
  await nextTick();
  const viewport = timelineViewportRef.value;
  if (!viewport) return;
  viewport.scrollLeft = 0;
};

const fetchTimelineData = async () => {
  loading.value = true;
  try {
    events.value = await getEvents();
  } catch (error) {
    console.error('Failed to load timeline:', error);
  } finally {
    loading.value = false;
    centerToday();
  }
};

const openQuickEditor = (item) => {
  quickEditEvent.value = item;
  isQuickEditorOpen.value = true;
};

const closeQuickEditor = () => {
  isQuickEditorOpen.value = false;
  quickEditEvent.value = null;
};

const startDrag = (event) => {
  const viewport = timelineViewportRef.value;
  const target = event.target instanceof Element ? event.target : null;
  if (!viewport || target?.closest('button')) return;
  dragging.value = true;
  startX.value = event.clientX;
  startScrollLeft.value = viewport.scrollLeft;
  viewport.setPointerCapture?.(event.pointerId);
};

const dragTimeline = (event) => {
  const viewport = timelineViewportRef.value;
  if (!dragging.value || !viewport) return;
  viewport.scrollLeft = startScrollLeft.value - (event.clientX - startX.value);
};

const stopDrag = (event) => {
  const viewport = timelineViewportRef.value;
  if (!dragging.value || !viewport) return;
  dragging.value = false;
  viewport.releasePointerCapture?.(event.pointerId);
};

const wheelTimeline = (event) => {
  const viewport = timelineViewportRef.value;
  if (!viewport) return;
  viewport.scrollLeft += event.deltaY || event.deltaX;
};

watch([filteredTimelineEvents, directionFilter, activeCategory, activeTag, stateFilters], () => {
  centerToday();
});

onMounted(fetchTimelineData);
</script>

<style scoped>
.timeline-page {
  max-width: 1120px;
  margin: 0 auto;
}

.timeline-hero {
  text-align: center;
  margin-bottom: 24px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #9b6b70;
  font-weight: 800;
}

.timeline-hero h1 {
  font-size: 2.05rem;
  line-height: 1.2;
  color: var(--primary);
}

.intro {
  max-width: 680px;
  margin: 12px auto 0;
  color: #736f6b;
  line-height: 1.75;
}

.timeline-shell {
  border-radius: 24px;
  padding: 22px;
  background: var(--surface);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 12px 32px rgba(112, 87, 74, 0.05);
}

.timeline-toolbar,
.filters-panel,
.segmented-row,
.state-filters,
.toolbar-actions,
.event-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.timeline-toolbar {
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.mode-title,
.mode-subtitle {
  display: block;
}

.mode-title {
  color: #383433;
  font-size: 1.08rem;
  font-weight: 850;
}

.mode-subtitle {
  margin-top: 4px;
  color: #817b76;
  font-size: 0.9rem;
}

.soft-btn,
.segment-btn,
.filter-select,
.search-input {
  min-height: 40px;
  border: 1px solid rgba(214, 199, 184, 0.74);
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(255, 250, 246, 0.78));
  color: #5f5753;
  font: inherit;
}

.soft-btn,
.segment-btn {
  padding: 0 14px;
  font-weight: 800;
  cursor: pointer;
}

.soft-btn:hover,
.segment-btn.active {
  color: #7c2d12;
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.88), rgba(204, 251, 241, 0.72));
}

.filters-panel {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) 160px 160px;
  margin-bottom: 12px;
}

.search-input,
.filter-select {
  padding: 0 14px;
  outline: none;
}

.segmented-row,
.state-filters {
  margin-bottom: 12px;
}

.switch-pill {
  position: relative;
  display: inline-flex;
  align-items: center;
  min-height: 38px;
  border: 1px solid rgba(214, 199, 184, 0.74);
  border-radius: 999px;
  padding: 0 12px 0 40px;
  color: #6f6663;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 250, 246, 0.72));
  cursor: pointer;
  font-weight: 800;
}

.switch-pill input {
  position: absolute;
  opacity: 0;
}

.switch-pill::before {
  content: "";
  position: absolute;
  left: 11px;
  width: 20px;
  height: 12px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(226, 232, 240, 0.9), rgba(255, 250, 246, 0.82));
  border: 1px solid rgba(148, 163, 184, 0.32);
}

.switch-pill::after {
  content: "";
  position: absolute;
  left: 14px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(31, 41, 55, 0.16);
  transition: transform 0.18s ease;
}

.switch-pill:has(input:checked) {
  color: #7c2d12;
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.88), rgba(204, 251, 241, 0.72));
}

.switch-pill:has(input:checked)::after {
  transform: translateX(8px);
}

.timeline-empty {
  display: grid;
  gap: 6px;
  border: 1px solid rgba(214, 199, 184, 0.74);
  border-radius: 18px;
  padding: 24px;
  color: #746f6b;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.88), rgba(255, 247, 237, 0.62));
}

.timeline-empty strong {
  color: #3a3332;
  font-size: 1.05rem;
}

.timeline-viewport {
  overflow-x: auto;
  overflow-y: hidden;
  border-radius: 22px;
  cursor: grab;
  scrollbar-width: thin;
  scrollbar-color: rgba(160, 125, 116, 0.3) transparent;
}

.timeline-viewport.dragging {
  cursor: grabbing;
}

.timeline-viewport::-webkit-scrollbar {
  height: 10px;
}

.timeline-viewport::-webkit-scrollbar-track {
  background: transparent;
}

.timeline-viewport::-webkit-scrollbar-thumb {
  background: linear-gradient(90deg, rgba(179, 137, 144, 0.28), rgba(193, 169, 139, 0.25));
  border-radius: 999px;
}

.scale-strip {
  position: relative;
  height: 710px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.4);
}

.scale-line {
  position: absolute;
  left: 140px;
  right: 140px;
  top: 188px;
  height: 1px;
  background: var(--line);
}

.range-label {
  position: absolute;
  top: 28px;
  color: #8d8580;
  font-size: 0.78rem;
  font-weight: 850;
}

.range-label.start {
  left: 140px;
}

.range-label.end {
  right: 140px;
}

.range-label.today {
  top: 54px;
  transform: translateX(-50%);
  color: #7c2d12;
}

.scale-tick {
  position: absolute;
  top: 172px;
  transform: translateX(-50%);
  display: grid;
  justify-items: center;
  gap: 8px;
}

.scale-tick span {
  width: 1px;
  height: 26px;
  background: rgba(130, 107, 99, 0.24);
}

.scale-tick.major span {
  height: 36px;
  background: rgba(141, 98, 108, 0.38);
}

.scale-tick em {
  color: #8d8580;
  font-size: 0.74rem;
  font-style: normal;
  white-space: nowrap;
}

.timeline-event {
  position: absolute;
  width: 218px;
  transform: translateX(-50%);
}

.event-pin {
  position: absolute;
  left: 50%;
  top: -42px;
  width: 11px;
  height: 11px;
  border: 3px solid #c99aa5;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 0 7px rgba(201, 154, 165, 0.12);
  transform: translateX(-50%);
}

.timeline-event.future .event-pin {
  border-color: #b4a47c;
  box-shadow: 0 0 0 7px rgba(180, 164, 124, 0.13);
}

.event-summary {
  width: 100%;
  min-height: 106px;
  max-height: 106px;
  border: 1px solid rgba(112, 88, 82, 0.1);
  border-radius: 16px;
  padding: 13px;
  text-align: left;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(255, 250, 247, 0.78) 70%, #ffffff);
  box-shadow: 0 10px 28px rgba(116, 88, 80, 0.06);
  cursor: pointer;
}

.event-summary:hover {
  transform: translateY(-2px);
  box-shadow: 0 16px 34px rgba(116, 88, 80, 0.1);
}

.event-date,
.event-note,
.event-meta em {
  color: #746f6b;
  font-size: 0.78rem;
}

.event-summary strong {
  display: block;
  margin: 6px 0;
  color: #302c2c;
  font-size: 1rem;
  line-height: 1.35;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.event-note {
  display: block;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.event-meta {
  margin-top: 10px;
}

.event-meta em {
  border: 1px solid rgba(129, 104, 98, 0.08);
  border-radius: 999px;
  padding: 5px 8px;
  background: linear-gradient(135deg, rgba(247, 240, 238, 0.66), rgba(255, 255, 255, 0.94));
  font-style: normal;
  font-weight: 750;
}

.compact-timeline {
  display: none;
}

.compact-section {
  display: grid;
  gap: 12px;
}

.compact-section-title {
  color: #807773;
  font-size: 0.9rem;
  font-weight: 850;
}

.compact-card,
.compact-today {
  border: 1px solid rgba(112, 88, 82, 0.1);
  border-radius: 16px;
  padding: 14px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 250, 246, 0.82));
  box-shadow: 0 8px 22px rgba(116, 88, 80, 0.05);
}

.compact-card {
  cursor: pointer;
}

.compact-card strong,
.compact-card span {
  display: block;
}

.compact-card strong {
  color: #302c2c;
}

.compact-card span,
.compact-card p {
  color: #746f6b;
  font-size: 0.88rem;
}

.compact-card p {
  margin: 8px 0 0;
}

.compact-today {
  margin: 16px 0;
  text-align: center;
}

.compact-today span,
.compact-today small {
  display: block;
  color: #8f746f;
  font-size: 0.74rem;
  font-weight: 850;
}

.compact-today strong {
  display: block;
  margin: 6px 0;
  color: #3a3332;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: var(--text-secondary);
}



@media (max-width: 860px) {
  .timeline-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .filters-panel {
    grid-template-columns: 1fr;
  }

  .timeline-viewport {
    display: none;
  }

  .compact-timeline {
    display: grid;
    gap: 12px;
  }
}

@media (max-width: 640px) {
  .timeline-shell {
    padding: 16px;
    border-radius: 20px;
  }

  .timeline-hero {
    text-align: left;
  }

  .timeline-hero h1 {
    font-size: 1.6rem;
  }
}
</style>
