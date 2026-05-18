<template>
  <div class="dashboard">
    <header class="header">
      <div class="header-content">
        <h1>NoteHub</h1>
        <button class="settings-btn" @click="isEditorOpen = true" title="设置日期">
          <SettingsIcon class="icon" />
        </button>
      </div>
      <p class="subtitle">记录我们的小时光</p>
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
            <p class="section-subtitle">{{ filteredEvents.length }} 个记录正在陪我们往前走</p>
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
          没有匹配的记录，换个关键词试试。
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
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  margin-bottom: 48px;
  animation: fadeInDown 0.8s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.settings-btn {
  background: var(--surface);
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.settings-btn:hover {
  color: var(--primary);
  transform: rotate(30deg);
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
  margin-top: 8px;
  letter-spacing: 0.1em;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}

.section-heading {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  align-items: flex-end;
  margin-bottom: 18px;
}

.section-title {
  font-size: 1.5rem;
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
  border: 1px solid rgba(31, 41, 55, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.82);
  color: var(--text-primary);
  font: inherit;
  outline: none;
}

.search-input {
  width: 220px;
  padding: 0 14px;
}

.category-select {
  padding: 0 12px;
}

.quick-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 22px;
}

.stat-card {
  border: 1px solid rgba(236, 72, 153, 0.08);
  border-radius: 16px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.72);
}

.stat-card strong,
.stat-card span {
  display: block;
}

.stat-card strong {
  color: var(--primary);
  font-size: 1.45rem;
}

.stat-card span {
  color: var(--text-secondary);
  font-size: 0.86rem;
  margin-top: 4px;
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 24px;
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

.loader {
  border: 4px solid var(--primary-light);
  border-top: 4px solid var(--primary);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

.animate-in {
  opacity: 0;
  animation: fadeInUp 0.6s ease-out forwards;
}

@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: 1fr;
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

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
