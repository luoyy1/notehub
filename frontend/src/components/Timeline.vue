<template>
  <main class="timeline-page">
    <section class="timeline-hero">
      <p class="eyebrow">时间轴</p>
      <h1>以今天为中心的时间线</h1>
      <p class="intro">过去收成左侧的年份刻度，未来展开在右侧。今天不是列表中的一项，而是整条时间线的锚点。</p>
    </section>

    <section v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>正在整理时间线...</p>
    </section>

    <section v-else class="timeline-shell">
      <div class="timeline-toolbar">
        <div>
          <span class="mode-title">今天 · {{ todayLabel }}</span>
          <span class="mode-subtitle">左侧压缩过去，右侧展开即将到来的日子</span>
        </div>
        <div class="summary">
          <span>{{ pastItems.length }} 个过去节点</span>
          <span>{{ futureItems.length }} 个未来提醒</span>
        </div>
      </div>

      <div
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
        <div class="centered-timeline">
          <section class="past-panel">
            <div class="panel-title">已经过去</div>
            <div class="past-ruler">
              <div
                v-for="year in pastYears"
                :key="year"
                class="year-tick"
                :class="{ current: year === today.getFullYear() }"
              >
                <span class="year-line"></span>
                <span class="year-label">{{ year }}</span>
              </div>

              <article
                v-for="item in pastItems"
                :key="item.key"
                class="past-event"
                :style="{ left: getPastLeft(item) }"
              >
                <span class="past-pin"></span>
                <div class="past-card">
                  <strong>{{ item.name }}</strong>
                  <span>{{ item.year }} · {{ item.monthDay }}</span>
                  <small>{{ item.note }}</small>
                  <p v-if="item.story">{{ item.story }}</p>
                  <div v-if="item.tags?.length" class="timeline-tags">
                    <em v-for="tag in item.tags" :key="tag"># {{ tag }}</em>
                  </div>
                </div>
              </article>
            </div>
          </section>

          <section class="today-anchor">
            <div class="today-card">
              <span class="today-kicker">TODAY</span>
              <strong>{{ todayLabel }}</strong>
              <span>{{ weekdayLabel }}</span>
            </div>
            <span class="today-line"></span>
          </section>

          <section class="future-panel">
            <div class="panel-title">即将发生</div>
            <div class="future-ruler">
              <div
                v-for="tick in futureTicks"
                :key="tick.key"
                class="future-tick"
                :class="{ major: tick.major }"
                :style="{ left: tick.left }"
              >
                <span class="future-line"></span>
                <span class="future-label">{{ tick.label }}</span>
              </div>

              <article
                v-for="(item, index) in futureItems"
                :key="item.key"
                class="future-event"
                :class="{ lower: index % 2 === 1 }"
                :style="{ left: getFutureLeft(item) }"
              >
                <span class="future-pin"></span>
                <div class="future-card">
                  <span class="date">{{ item.targetLabel }}</span>
                  <h2>{{ item.name }}</h2>
                  <p>{{ item.note }}</p>
                  <p v-if="item.story" class="story-note">{{ item.story }}</p>
                  <div class="place-row" v-if="item.location || item.mood">
                    <span v-if="item.location">{{ item.location }}</span>
                    <span v-if="item.mood">{{ item.mood }}</span>
                  </div>
                  <div class="metric-row" v-if="item.metrics.length">
                    <span v-for="metric in item.metrics" :key="metric">{{ metric }}</span>
                  </div>
                </div>
              </article>
            </div>
          </section>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup>
import { computed, nextTick, onMounted, ref } from 'vue';
import { getEvents } from '../api';

const events = ref([]);
const loading = ref(true);
const timelineViewportRef = ref(null);
const dragging = ref(false);
const startX = ref(0);
const startScrollLeft = ref(0);
const today = new Date();
today.setHours(0, 0, 0, 0);

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

const todayLabel = computed(() => formatFullDate(today));
const weekdayLabel = computed(() => today.toLocaleDateString('zh-CN', { weekday: 'long' }));

const normalizedItems = computed(() => {
  return events.value.map((event) => {
    const originDate = parseDate(event.date);
    const daysUntil = Number(event.daysUntil);
    const hasFuture = event.enableCountdown && Number.isFinite(daysUntil) && daysUntil >= 0;
    const targetDate = hasFuture ? addDays(today, daysUntil) : originDate;
    const metrics = [];

    if (event.enableCountUp && Number.isFinite(Number(event.daysPassed))) {
      metrics.push(`已过 ${event.daysPassed} 天`);
    }

    if (hasFuture) {
      metrics.push(daysUntil === 0 ? '今天' : `${daysUntil} 天后`);
    }

    if (event.daysToNextHundred) {
      metrics.push(`百日差 ${event.daysToNextHundred} 天`);
    }

    return {
      ...event,
      key: event.id || `${event.name}-${event.date}`,
      originDate,
      targetDate,
      year: originDate?.getFullYear() || '----',
      monthDay: originDate ? formatMonthDay(originDate) : event.date,
      targetLabel: targetDate ? formatFullDate(targetDate) : event.date,
      metrics
    };
  });
});

const pastItems = computed(() => {
  return normalizedItems.value
    .filter(item => item.originDate && item.originDate < today && item.enableCountUp)
    .map(item => ({
      ...item,
      note: item.story || (item.daysPassed ? `已经过去 ${item.daysPassed} 天` : '已经归入过去的年份里')
    }))
    .sort((a, b) => a.originDate - b.originDate);
});

const futureItems = computed(() => {
  return normalizedItems.value
    .filter(item => item.targetDate && item.targetDate >= today && item.enableCountdown)
    .map(item => ({
      ...item,
      note: Number(item.daysUntil) === 0 ? '今天就是这个日子。' : `距离到来还有 ${item.daysUntil} 天。`
    }))
    .sort((a, b) => a.targetDate - b.targetDate);
});

const minPastYear = computed(() => {
  const years = pastItems.value.map(item => item.originDate.getFullYear());
  return years.length ? Math.min(...years) : today.getFullYear() - 1;
});

const pastYears = computed(() => {
  const years = [];
  for (let year = minPastYear.value; year <= today.getFullYear(); year += 1) {
    years.push(year);
  }
  return years;
});

const futureEndDate = computed(() => {
  const latest = futureItems.value.at(-1)?.targetDate;
  const fallback = addDays(today, 365);
  return latest && latest > fallback ? latest : fallback;
});

const futureDays = computed(() => {
  return Math.max(30, Math.ceil((futureEndDate.value - today) / 86400000));
});

const futureTicks = computed(() => {
  const ticks = [];
  const cursor = new Date(today.getFullYear(), today.getMonth(), 1);

  while (cursor <= futureEndDate.value) {
    const daysFromToday = Math.round((cursor - today) / 86400000);
    if (daysFromToday >= 0) {
      ticks.push({
        key: `${cursor.getFullYear()}-${cursor.getMonth()}`,
        label: cursor.getMonth() === 0 ? `${cursor.getFullYear()}` : `${cursor.getMonth() + 1}月`,
        major: cursor.getMonth() === 0,
        left: `${Math.min(100, (daysFromToday / futureDays.value) * 100)}%`
      });
    }
    cursor.setMonth(cursor.getMonth() + 1);
  }

  return ticks;
});

const getPastLeft = (item) => {
  const yearSpan = Math.max(1, today.getFullYear() - minPastYear.value + 1);
  const yearOffset = item.originDate.getFullYear() - minPastYear.value;
  const monthOffset = item.originDate.getMonth() / 12;
  return `${Math.min(94, Math.max(5, ((yearOffset + monthOffset) / yearSpan) * 100))}%`;
};

const getFutureLeft = (item) => {
  const days = Math.max(0, Math.round((item.targetDate - today) / 86400000));
  return `${Math.min(96, Math.max(4, (days / futureDays.value) * 100))}%`;
};

const centerToday = async () => {
  await nextTick();
  const viewport = timelineViewportRef.value;
  if (!viewport) return;

  const todayOffset = 520 + 56;
  viewport.scrollLeft = Math.max(0, todayOffset - viewport.clientWidth / 2);
};

const startDrag = (event) => {
  const viewport = timelineViewportRef.value;
  if (!viewport) return;

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

onMounted(async () => {
  try {
    events.value = await getEvents();
  } catch (error) {
    console.error('Failed to load timeline:', error);
  } finally {
    loading.value = false;
    centerToday();
  }
});
</script>

<style scoped>
.timeline-page {
  max-width: 1120px;
  margin: 0 auto;
}

.timeline-hero {
  text-align: center;
  margin-bottom: 26px;
}

.eyebrow {
  margin: 0 0 8px;
  color: #9b6b70;
  font-weight: 800;
}

.timeline-hero h1 {
  font-size: 2.05rem;
  line-height: 1.2;
  background: linear-gradient(135deg, #8d666b 0%, #b08472 58%, #75877b 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.intro {
  max-width: 680px;
  margin: 12px auto 0;
  color: #736f6b;
  line-height: 1.75;
}

.timeline-shell {
  border-radius: 26px;
  padding: 24px;
  background: linear-gradient(180deg, rgba(255, 250, 248, 0.94), rgba(255, 255, 255, 0.98) 46%, #ffffff);
  border: 1px solid rgba(120, 98, 90, 0.1);
  box-shadow: 0 18px 48px rgba(112, 87, 74, 0.07);
}

.timeline-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  margin-bottom: 18px;
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

.mode-subtitle,
.summary {
  color: #817b76;
  font-size: 0.9rem;
}

.mode-subtitle {
  margin-top: 4px;
}

.summary {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.summary span {
  border: 1px solid rgba(126, 92, 82, 0.08);
  border-radius: 999px;
  padding: 7px 11px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.86), rgba(250, 244, 241, 0.55));
}

.timeline-viewport {
  overflow-x: auto;
  overflow-y: hidden;
  border-radius: 24px;
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

.centered-timeline {
  display: grid;
  grid-template-columns: 520px 112px 760px;
  width: 1392px;
  min-height: 620px;
  border-radius: 24px;
  border: 1px solid rgba(123, 101, 92, 0.1);
  overflow: hidden;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 252, 249, 0.72) 58%, #ffffff),
    repeating-linear-gradient(90deg, rgba(144, 121, 111, 0.04) 0, rgba(144, 121, 111, 0.04) 1px, transparent 1px, transparent 38px);
}

.past-panel,
.future-panel {
  position: relative;
  padding: 26px 24px;
}

.past-panel {
  background: linear-gradient(90deg, rgba(248, 242, 239, 0.58), rgba(255, 255, 255, 0.82));
}

.future-panel {
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.86), rgba(248, 251, 247, 0.58));
}

.panel-title {
  color: #807773;
  font-size: 0.9rem;
  font-weight: 850;
}

.past-ruler,
.future-ruler {
  position: absolute;
  left: 24px;
  right: 24px;
  top: 310px;
  height: 2px;
  background: linear-gradient(90deg, rgba(173, 125, 136, 0), rgba(173, 125, 136, 0.32), rgba(191, 162, 132, 0.22));
}

.future-ruler {
  background: linear-gradient(90deg, rgba(191, 162, 132, 0.24), rgba(128, 148, 134, 0.32), rgba(128, 148, 134, 0));
}

.year-tick,
.future-tick {
  position: absolute;
  top: -12px;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.year-tick {
  position: relative;
  top: -12px;
  transform: none;
  display: inline-flex;
  flex: 1 1 0;
}

.past-ruler {
  display: flex;
}

.year-line,
.future-line {
  width: 1px;
  height: 22px;
  background: rgba(130, 107, 99, 0.28);
}

.year-label,
.future-label {
  margin-top: 8px;
  color: #8d8580;
  font-size: 0.76rem;
  white-space: nowrap;
}

.year-tick.current .year-line,
.future-tick.major .future-line {
  height: 32px;
  background: rgba(141, 98, 108, 0.44);
}

.year-tick.current .year-label,
.future-tick.major .future-label {
  color: #74645f;
  font-weight: 850;
}

.past-event,
.future-event {
  position: absolute;
  transform: translateX(-50%);
  z-index: 2;
}

.past-event {
  top: -230px;
  width: 180px;
}

.past-pin,
.future-pin {
  position: absolute;
  left: 50%;
  border-radius: 50%;
  background: #ffffff;
  transform: translateX(-50%);
}

.past-pin {
  top: 218px;
  width: 11px;
  height: 11px;
  border: 3px solid #c99aa5;
  box-shadow: 0 0 0 7px rgba(201, 154, 165, 0.12);
}

.past-card,
.future-card {
  border-radius: 18px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(255, 250, 247, 0.78) 70%, #ffffff);
  border: 1px solid rgba(112, 88, 82, 0.1);
  box-shadow: 0 10px 30px rgba(116, 88, 80, 0.06);
}

.past-card {
  padding: 14px;
}

.past-card strong,
.past-card span,
.past-card small {
  display: block;
}

.past-card strong {
  color: #332f2f;
  font-size: 0.96rem;
}

.past-card span {
  margin-top: 6px;
  color: #96746d;
  font-size: 0.78rem;
  font-weight: 800;
}

.past-card small {
  margin-top: 8px;
  color: #746f6b;
  line-height: 1.5;
}

.past-card p,
.story-note {
  margin: 10px 0 0;
  color: #6f6663;
  line-height: 1.55;
  font-size: 0.86rem;
}

.timeline-tags,
.place-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
}

.timeline-tags em,
.place-row span {
  color: #8b6c67;
  background: rgba(248, 242, 239, 0.78);
  border: 1px solid rgba(129, 104, 98, 0.08);
  border-radius: 999px;
  padding: 5px 8px;
  font-size: 0.72rem;
  font-style: normal;
  font-weight: 750;
}

.today-anchor {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(252, 247, 244, 0.78), #ffffff);
  border-left: 1px solid rgba(126, 92, 82, 0.09);
  border-right: 1px solid rgba(126, 92, 82, 0.09);
}

.today-line {
  position: absolute;
  top: 28px;
  bottom: 28px;
  width: 2px;
  background: linear-gradient(180deg, rgba(151, 104, 113, 0), rgba(151, 104, 113, 0.42), rgba(151, 104, 113, 0));
}

.today-card {
  position: relative;
  z-index: 2;
  width: 96px;
  border-radius: 20px;
  padding: 16px 10px;
  text-align: center;
  background: linear-gradient(180deg, #ffffff, rgba(250, 244, 241, 0.86));
  border: 1px solid rgba(126, 92, 82, 0.12);
  box-shadow: 0 14px 32px rgba(116, 88, 80, 0.08);
}

.today-kicker,
.today-card span {
  display: block;
  color: #8f746f;
  font-size: 0.72rem;
  font-weight: 850;
}

.today-card strong {
  display: block;
  color: #3a3332;
  margin: 8px 0;
  font-size: 0.95rem;
  line-height: 1.45;
}

.future-event {
  top: -236px;
  width: 236px;
}

.future-event.lower {
  top: 54px;
}

.future-pin {
  top: 224px;
  width: 12px;
  height: 12px;
  border: 3px solid #b4a47c;
  box-shadow: 0 0 0 7px rgba(180, 164, 124, 0.13);
}

.future-event.lower .future-pin {
  top: -47px;
}

.future-card {
  padding: 16px;
}

.future-card .date {
  display: block;
  color: #90756f;
  font-size: 0.8rem;
  font-weight: 850;
  margin-bottom: 8px;
}

.future-card h2 {
  color: #302c2c;
  font-size: 1.05rem;
  line-height: 1.32;
  margin-bottom: 8px;
}

.future-card p {
  margin: 0;
  color: #6f6a66;
  font-size: 0.9rem;
  line-height: 1.58;
}

.metric-row {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 12px;
}

.metric-row span {
  color: #7d6a66;
  background: linear-gradient(135deg, rgba(247, 240, 238, 0.66), rgba(255, 255, 255, 0.94));
  border: 1px solid rgba(129, 104, 98, 0.08);
  border-radius: 999px;
  padding: 6px 9px;
  font-size: 0.76rem;
  font-weight: 750;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  color: var(--text-secondary);
}

.loader {
  border: 4px solid rgba(201, 164, 150, 0.24);
  border-top: 4px solid #b98491;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 860px) {
  .timeline-toolbar {
    align-items: flex-start;
    flex-direction: column;
  }

  .centered-timeline {
    grid-template-columns: 420px 112px 620px;
    width: 1152px;
  }

  .today-anchor {
    border: 1px solid rgba(126, 92, 82, 0.09);
  }

  .past-panel,
  .future-panel {
    min-height: 580px;
  }
}
</style>
