<template>
  <div class="dashboard">
    <header class="header">
      <div class="header-content">
        <div>
          <p class="kicker">Daily memory system</p>
          <h1>NoteHub</h1>
        </div>
        <button class="settings-btn" @click="isEditorOpen = true" title="设置日期">
          <SettingsIcon class="icon" />
          <span>设置日期</span>
        </button>
      </div>
    </header>

    <main v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>正在加载我们的记忆...</p>
    </main>

    <main v-else class="content-grid">
      <div class="events-section">
        <div class="section-heading">
          <div>
            <h2 class="section-title">重要日子</h2>
          </div>
          <div class="filters">
            <input v-model="keyword" class="search-input" type="search" placeholder="搜索名字、标签、故事" />
            <select v-model="activeCategory" class="category-select">
              <option value="all">全部分类</option>
              <option v-for="category in categories" :key="category.value" :value="category.value">
                {{ category.label }}
              </option>
            </select>
          </div>
        </div>

        <div class="quick-stats">
          <article v-for="stat in stats" :key="stat.label" class="stat-card">
            <strong>{{ stat.value }}</strong>
            <span>{{ stat.label }}</span>
          </article>
        </div>

        <div class="cards-grid">
          <EventCard
            v-for="(event, index) in filteredEvents"
            :key="event.id || index"
            :event="event"
            class="animate-in"
            :style="{ animationDelay: `${index * 0.1}s` }"
            @edit="openQuickEditor"
          />
        </div>

        <div v-if="filteredEvents.length === 0" class="empty-results">
          没有记录，点击右上角“设置日期”添加。
        </div>
      </div>

      <div class="calendar-section">
        <Calendar :marks="calendarMarks" class="animate-in" style="animationDelay: 0.3s" />
      </div>
    </main>

    <EventEditor
      :isOpen="isEditorOpen"
      @close="isEditorOpen = false"
      @saved="fetchDashboardData"
    />

    <EventQuickEditor
      :isOpen="isQuickEditorOpen"
      :event="quickEditEvent"
      @close="closeQuickEditor"
      @saved="fetchDashboardData"
    />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { getEvents, getCalendarMarks } from '../api';
import EventCard from './EventCard.vue';
import Calendar from './Calendar.vue';
import EventEditor from './EventEditor.vue';
import EventQuickEditor from './EventQuickEditor.vue';
import { Settings as SettingsIcon } from 'lucide-vue-next';

const events = ref([]);
const calendarMarks = ref([]);
const loading = ref(true);
const isEditorOpen = ref(false);
const isQuickEditorOpen = ref(false);
const quickEditEvent = ref(null);
const keyword = ref('');
const activeCategory = ref('all');

const categoryLabels = {
  anniversary: '纪念日',
  birthday: '生日',
  travel: '旅行',
  life: '生活',
  work: '工作',
  other: '其他'
};

const categories = computed(() => {
  const values = new Set(events.value.map(event => event.category || 'other'));
  return Array.from(values).map(value => ({
    value,
    label: categoryLabels[value] || value
  }));
});

const filteredEvents = computed(() => {
  const text = keyword.value.trim().toLowerCase();
  return events.value
    .filter(event => activeCategory.value === 'all' || (event.category || 'other') === activeCategory.value)
    .filter((event) => {
      if (!text) return true;
      return [
        event.name,
        event.story,
        event.location,
        event.mood,
        ...(event.tags || [])
      ].filter(Boolean).join(' ').toLowerCase().includes(text);
    })
    .sort((a, b) => Number(Boolean(b.pinned)) - Number(Boolean(a.pinned)));
});

const stats = computed(() => {
  const stories = events.value.filter(event => event.story).length;
  const tags = new Set(events.value.flatMap(event => event.tags || [])).size;
  const pinned = events.value.filter(event => event.pinned).length;

  return [
    { label: '重要日子', value: events.value.length },
    { label: '有故事的记录', value: stories },
    { label: '标签', value: tags },
    { label: '置顶', value: pinned }
  ];
});

const fetchDashboardData = async () => {
  loading.value = true;
  try {
    const year = new Date().getFullYear();
    const [eventsData, marksData] = await Promise.all([
      getEvents(),
      getCalendarMarks(year)
    ]);
    events.value = eventsData;
    calendarMarks.value = marksData;
  } catch (error) {
    console.error('Failed to load data:', error);
  } finally {
    loading.value = false;
  }
};

const openQuickEditor = (event) => {
  quickEditEvent.value = event;
  isQuickEditorOpen.value = true;
};

const closeQuickEditor = () => {
  isQuickEditorOpen.value = false;
  quickEditEvent.value = null;
};

onMounted(() => {
  fetchDashboardData();
});
</script>

<style scoped>
.dashboard {
  margin: 0 auto;
}

.header {
  margin: 10px 0 28px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 24px 28px;
  border-radius: 24px;
  background: var(--surface);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow: 0 12px 32px rgba(112, 87, 74, 0.05);
}

.kicker {
  margin: 0 0 8px;
  color: var(--secondary);
  font-size: 0.78rem;
  font-weight: 900;
  text-transform: uppercase;
}

.settings-btn {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.9);
  border-radius: 999px;
  min-width: 118px;
  height: 42px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-primary);
  font-weight: 700;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(112, 87, 74, 0.06);
}

.settings-btn:hover {
  background: #ffffff;
  color: var(--primary);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(112, 87, 74, 0.1);
}

.icon {
  width: 17px;
  height: 17px;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1rem;
  margin: 12px 0 0;
  max-width: 520px;
  line-height: 1.7;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.65fr) minmax(320px, 0.95fr);
  gap: 24px;
  align-items: start;
}

.events-section {
  min-width: 0;
  padding: 18px;
  border: 1px solid var(--line);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.58);
  box-shadow: 0 12px 34px rgba(31, 41, 55, 0.05);
}

.calendar-section {
  min-width: 0;
  position: sticky;
  top: 94px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.section-title {
  font-size: 1.4rem;
  color: var(--text-primary);
}

.section-subtitle {
  margin: 6px 0 0;
  color: var(--text-secondary);
}

.filters {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.search-input,
.category-select {
  height: 40px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--text-primary);
  font: inherit;
  outline: none;
  box-shadow: 0 1px 2px rgba(31, 41, 55, 0.03);
}

.search-input {
  width: 230px;
  padding: 0 14px;
}

.category-select {
  padding: 0 12px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 22px;
}

.stat-card {
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.78);
  box-shadow: 0 8px 20px rgba(31, 41, 55, 0.04);
}

.stat-card strong,
.stat-card span {
  display: block;
}

.stat-card strong {
  color: var(--primary);
  font-size: 1.38rem;
}

.stat-card span {
  color: var(--text-secondary);
  font-size: 0.86rem;
  margin-top: 4px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
}

.empty-results {
  color: var(--text-secondary);
  background: rgba(255, 255, 255, 0.72);
  border-radius: 16px;
  padding: 24px;
  text-align: center;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 40vh;
  color: var(--text-secondary);
}



@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .calendar-section {
    position: static;
  }

  .section-heading {
    align-items: stretch;
    flex-direction: column;
  }

  .filters {
    justify-content: stretch;
  }

  .search-input,
  .category-select {
    flex: 1;
    min-width: 160px;
  }

  .quick-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .header {
    margin-top: 4px;
  }

  .header-content {
    align-items: flex-start;
    flex-direction: column;
    padding: 20px;
  }

  .settings-btn {
    width: 100%;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>
