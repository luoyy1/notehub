<template>
  <main class="wishes-page">
    <section class="page-header">
      <div>
        <p class="eyebrow">愿望与计划</p>
        <h1>把未来也放进我们的小网站</h1>
        <p class="intro">旅行、礼物、约会、想一起完成的小事，都可以先放在这里。</p>
      </div>
      <button class="primary-action" @click="addWish">新增愿望</button>
    </section>

    <section v-if="loading" class="loading-state">
      <div class="loader"></div>
      <p>正在翻开愿望清单...</p>
    </section>

    <section v-else class="board">
      <div v-for="column in columns" :key="column.status" class="column">
        <div class="column-header">
          <h2>{{ column.title }}</h2>
          <span>{{ wishesByStatus[column.status].length }}</span>
        </div>

        <article v-for="wish in wishesByStatus[column.status]" :key="wish.id" class="wish-card">
          <div class="wish-card-header">
            <select v-model="wish.type" class="type-select">
              <option value="wish">愿望</option>
              <option value="trip">旅行</option>
              <option value="gift">礼物</option>
              <option value="date">约会</option>
              <option value="life">生活</option>
            </select>
            <button class="ghost-btn" @click="removeWish(wish.id)">删除</button>
          </div>

          <input v-model="wish.title" class="title-input" placeholder="想一起做什么？" />
          <textarea v-model="wish.description" class="desc-input" placeholder="写一点计划、灵感或原因"></textarea>

          <div class="mini-grid">
            <label>
              <span>状态</span>
              <select v-model="wish.status">
                <option value="todo">想做</option>
                <option value="planning">计划中</option>
                <option value="done">已完成</option>
              </select>
            </label>
            <label>
              <span>优先级</span>
              <select v-model.number="wish.priority">
                <option :value="1">高</option>
                <option :value="2">中</option>
                <option :value="3">低</option>
              </select>
            </label>
            <label>
              <span>目标日期</span>
              <input v-model="wish.target_date" type="date" />
            </label>
            <label>
              <span>地点</span>
              <input v-model="wish.location" type="text" placeholder="可选" />
            </label>
          </div>

          <input v-model="wish.tagsText" class="tag-input" placeholder="标签，用逗号分隔" />
          <textarea v-model="wish.note" class="note-input" placeholder="补充备注、礼物线索、预约信息"></textarea>

          <div v-if="wish.tagsText" class="tag-row">
            <span v-for="tag in splitTags(wish.tagsText)" :key="tag"># {{ tag }}</span>
          </div>
        </article>

        <div v-if="wishesByStatus[column.status].length === 0" class="empty-column">
          这里还空着。
        </div>
      </div>
    </section>

    <footer v-if="!loading" class="save-bar">
      <span>{{ dirty ? '有修改还没保存' : '愿望清单已同步' }}</span>
      <button class="primary-action" :disabled="saving" @click="save">
        {{ saving ? '保存中...' : '保存愿望清单' }}
      </button>
    </footer>
  </main>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue';
import { getWishes, saveWishes } from '../api';

const wishes = ref([]);
const loading = ref(true);
const saving = ref(false);
const dirty = ref(false);

const columns = [
  { status: 'todo', title: '想做' },
  { status: 'planning', title: '计划中' },
  { status: 'done', title: '已完成' }
];

const splitTags = (text) => {
  return String(text || '')
    .split(/[,，]/)
    .map(tag => tag.trim())
    .filter(Boolean);
};

const normalizeWish = (wish) => ({
  id: wish.id || crypto.randomUUID(),
  title: wish.title || '新的愿望',
  description: wish.description || '',
  type: wish.type || 'wish',
  status: wish.status || 'todo',
  priority: wish.priority || 2,
  target_date: wish.target_date || '',
  completed_date: wish.completed_date || '',
  location: wish.location || '',
  tagsText: (wish.tags || []).join('，'),
  note: wish.note || ''
});

const toPayload = (wish) => {
  const { tagsText, ...payload } = wish;
  return {
    ...payload,
    tags: splitTags(tagsText),
    target_date: payload.target_date || null,
    completed_date: payload.status === 'done' ? (payload.completed_date || new Date().toISOString().split('T')[0]) : null
  };
};

const wishesByStatus = computed(() => {
  return columns.reduce((result, column) => {
    result[column.status] = wishes.value
      .filter(wish => wish.status === column.status)
      .sort((a, b) => Number(a.priority || 2) - Number(b.priority || 2));
    return result;
  }, {});
});

const addWish = () => {
  wishes.value.unshift(normalizeWish({
    title: '新的愿望',
    type: 'wish',
    status: 'todo',
    priority: 2
  }));
  dirty.value = true;
};

const removeWish = (id) => {
  wishes.value = wishes.value.filter(wish => wish.id !== id);
  dirty.value = true;
};

const save = async () => {
  saving.value = true;
  try {
    const saved = await saveWishes(wishes.value.map(toPayload));
    wishes.value = saved.map(normalizeWish);
    dirty.value = false;
  } catch (error) {
    console.error(error);
    alert('保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
};

watch(wishes, () => {
  if (!loading.value) dirty.value = true;
}, { deep: true });

onMounted(async () => {
  try {
    const data = await getWishes();
    wishes.value = data.map(normalizeWish);
    dirty.value = false;
  } catch (error) {
    console.error('Failed to load wishes:', error);
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>
.wishes-page {
  max-width: 1120px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 24px;
  margin-bottom: 28px;
}

.eyebrow {
  color: var(--primary);
  font-weight: 800;
  margin: 0 0 8px;
}

.page-header h1 {
  font-size: 2.05rem;
  line-height: 1.2;
}

.intro {
  color: var(--text-secondary);
  max-width: 620px;
  margin: 12px 0 0;
  line-height: 1.7;
}

.primary-action,
.ghost-btn {
  border: 0;
  border-radius: 12px;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.primary-action {
  color: #ffffff;
  background: linear-gradient(135deg, var(--primary), #f43f5e);
  padding: 12px 18px;
  box-shadow: 0 8px 22px rgba(236, 72, 153, 0.18);
  white-space: nowrap;
}

.primary-action:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.board {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 18px;
}

.column {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(236, 72, 153, 0.08);
  border-radius: 20px;
  padding: 16px;
  min-height: 420px;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.column-header h2 {
  font-size: 1.1rem;
}

.column-header span {
  color: var(--primary);
  background: var(--primary-light);
  border-radius: 999px;
  padding: 5px 10px;
  font-weight: 800;
}

.wish-card {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: #ffffff;
  border: 1px solid rgba(31, 41, 55, 0.06);
  border-radius: 16px;
  padding: 14px;
  box-shadow: 0 8px 22px rgba(236, 72, 153, 0.06);
  margin-bottom: 12px;
}

.wish-card-header {
  display: flex;
  justify-content: space-between;
  gap: 10px;
}

.type-select {
  width: 96px;
}

.ghost-btn {
  color: #be123c;
  background: #ffe4e6;
  padding: 7px 10px;
}

.title-input,
.desc-input,
.tag-input,
.note-input,
.mini-grid input,
.mini-grid select,
.type-select {
  border: 1px solid rgba(31, 41, 55, 0.1);
  border-radius: 10px;
  background: #ffffff;
  color: var(--text-primary);
  font: inherit;
  outline: none;
}

.title-input {
  border: 0;
  border-bottom: 2px solid rgba(236, 72, 153, 0.18);
  border-radius: 0;
  font-size: 1.05rem;
  font-weight: 800;
  padding: 6px 0;
}

.desc-input,
.note-input {
  min-height: 70px;
  resize: vertical;
  padding: 10px;
  line-height: 1.55;
}

.note-input {
  min-height: 56px;
}

.tag-input,
.mini-grid input,
.mini-grid select,
.type-select {
  height: 36px;
  padding: 0 10px;
}

.mini-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.mini-grid label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 0.78rem;
  font-weight: 800;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag-row span {
  color: #8b5cf6;
  background: #f5f3ff;
  border-radius: 999px;
  padding: 5px 8px;
  font-size: 0.76rem;
  font-weight: 800;
}

.empty-column {
  color: var(--text-secondary);
  border: 1px dashed rgba(31, 41, 55, 0.12);
  border-radius: 14px;
  padding: 18px;
  text-align: center;
}

.save-bar {
  position: sticky;
  bottom: 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 18px;
  margin-top: 22px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(236, 72, 153, 0.1);
  box-shadow: 0 12px 30px rgba(236, 72, 153, 0.12);
  color: var(--text-secondary);
  font-weight: 700;
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
  border: 4px solid var(--primary-light);
  border-top: 4px solid var(--primary);
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@media (max-width: 980px) {
  .board {
    grid-template-columns: 1fr;
  }

  .page-header,
  .save-bar {
    align-items: stretch;
    flex-direction: column;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
