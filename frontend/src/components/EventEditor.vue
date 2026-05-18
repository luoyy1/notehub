<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content glass">
      <div class="modal-header">
        <h2>事件配置管家</h2>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      
      <div class="modal-body">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else class="events-list">
          <div v-for="(event, index) in localEvents" :key="event.id || index" class="event-item">
            <div class="event-header">
              <input type="text" v-model="event.name" class="name-input" placeholder="事件名称" />
              <button class="delete-btn" @click="removeEvent(index)" title="删除事件">删除</button>
            </div>
            
            <div class="form-grid">
              <label>
                <span>发生日期</span>
                <input type="date" v-model="event.date" class="field" />
              </label>
              <label>
                <span>分类</span>
                <select v-model="event.category" class="field">
                  <option value="anniversary">纪念日</option>
                  <option value="birthday">生日</option>
                  <option value="travel">旅行</option>
                  <option value="life">生活</option>
                  <option value="work">工作</option>
                  <option value="other">其他</option>
                </select>
              </label>
              <label>
                <span>主题色</span>
                <input type="color" v-model="event.color" class="color-field" />
              </label>
              <label>
                <span>地点</span>
                <input type="text" v-model="event.location" class="field" placeholder="例如 家、海边、餐厅" />
              </label>
              <label>
                <span>心情</span>
                <input type="text" v-model="event.mood" class="field" placeholder="例如 开心、期待、想念" />
              </label>
              <label>
                <span>标签</span>
                <input type="text" v-model="event.tagsText" class="field" placeholder="用逗号分隔" />
              </label>
            </div>

            <label class="story-field">
              <span>背后的故事</span>
              <textarea v-model="event.story" placeholder="写一点这一天为什么重要，时间轴会优先展示这些故事。"></textarea>
            </label>

            <div class="toggles-grid">
              <label class="toggle-label">
                <input type="checkbox" v-model="event.pinned" />
                <span class="toggle-text">首页置顶</span>
              </label>
              <label class="toggle-label">
                <input type="checkbox" v-model="event.is_annual" />
                <span class="toggle-text">每年重复</span>
              </label>
              <label class="toggle-label">
                <input type="checkbox" v-model="event.enable_count_up" />
                <span class="toggle-text">开启正计时</span>
              </label>
              <label class="toggle-label">
                <input type="checkbox" v-model="event.enable_countdown" />
                <span class="toggle-text">开启倒计时</span>
              </label>
              <label class="toggle-label">
                <input type="checkbox" v-model="event.enable_notification" />
                <span class="toggle-text">开启邮件提醒</span>
              </label>
            </div>

            <div v-if="event.enable_notification" class="notification-settings">
              <p>提前几天提醒？</p>
              <div class="checkbox-group">
                <label v-for="day in [7, 3, 1, 0]" :key="day">
                  <input type="checkbox" :value="day" v-model="event.notify_advance_days" />
                  {{ day === 0 ? '当天' : day + '天前' }}
                </label>
              </div>
            </div>
          </div>
          
          <button class="btn btn-outline" @click="addEvent">添加新事件</button>
        </div>
      </div>
      
      <div class="modal-footer">
        <button class="btn btn-secondary" @click="$emit('close')">取消</button>
        <button class="btn btn-primary" @click="save" :disabled="saving">
          {{ saving ? '保存中...' : '保存所有修改' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { getRawEvents, saveEvents } from '../api';

const props = defineProps({
  isOpen: Boolean
});

const emit = defineEmits(['close', 'saved']);

const loading = ref(true);
const saving = ref(false);
const localEvents = ref([]);

const toDateInput = (value) => {
  if (Array.isArray(value)) {
    const [year, month, day] = value;
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
  }
  return value || new Date().toISOString().split('T')[0];
};

const splitTags = (text) => {
  return String(text || '')
    .split(/[,，]/)
    .map(tag => tag.trim())
    .filter(Boolean);
};

const normalizeEvent = (event) => ({
  ...event,
  date: toDateInput(event.date),
  category: event.category || 'other',
  color: event.color || '#ec4899',
  pinned: Boolean(event.pinned),
  story: event.story || '',
  location: event.location || '',
  mood: event.mood || '',
  tags: event.tags || [],
  tagsText: (event.tags || []).join('，'),
  photos: event.photos || [],
  notify_advance_days: event.notify_advance_days || []
});

const toPayload = (event) => {
  const { tagsText, ...payload } = event;
  return {
    ...payload,
    name: String(payload.name || '').trim(),
    category: payload.category || 'other',
    color: payload.color || '#ec4899',
    pinned: Boolean(payload.pinned),
    story: payload.story || '',
    location: payload.location || '',
    mood: payload.mood || '',
    photos: payload.photos || [],
    notify_advance_days: payload.notify_advance_days || [],
    tags: splitTags(tagsText)
  };
};

const fetchData = async () => {
  if (!props.isOpen) return;
  loading.value = true;
  try {
    const data = await getRawEvents();
    localEvents.value = JSON.parse(JSON.stringify(data)).map(normalizeEvent);
  } catch (err) {
    console.error(err);
  } finally {
    loading.value = false;
  }
};

watch(() => props.isOpen, fetchData);

onMounted(() => {
  if (props.isOpen) fetchData();
});

const addEvent = () => {
  localEvents.value.push(normalizeEvent({
    id: '',
    name: '新建事件',
    date: new Date().toISOString().split('T')[0],
    is_annual: false,
    enable_count_up: false,
    enable_countdown: true,
    enable_notification: false,
    notify_advance_days: [],
    category: 'life',
    color: '#ec4899',
    pinned: false
  }));
};

const removeEvent = (index) => {
  localEvents.value.splice(index, 1);
};

const save = async () => {
  const payload = localEvents.value.map(toPayload);
  const invalidEvent = payload.find(event => !event.name || !event.date);
  if (invalidEvent) {
    alert('请先补全事件名称和发生日期');
    return;
  }

  saving.value = true;
  try {
    await saveEvents(payload);
    emit('saved');
    emit('close');
  } catch (err) {
    console.error(err);
    alert('保存失败，请稍后重试');
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(72, 61, 50, 0.28);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease-out;
  backdrop-filter: blur(8px);
}

.modal-content {
  width: 92%;
  max-width: 760px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(255, 250, 246, 0.92));
  padding: 24px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  animation: slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h2 {
  font-size: 1.5rem;
  color: var(--text-primary);
  margin: 0;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 1.5rem;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 4px;
  line-height: 1;
}

.modal-body {
  display: flex;
  flex-direction: column;
  max-height: 68vh;
  overflow-y: auto;
  padding-right: 8px;
}

.events-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.event-item {
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.96), rgba(255, 250, 246, 0.84));
  border: 1px solid rgba(0,0,0,0.05);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.event-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.name-input {
  flex: 1;
  font-size: 1.2rem;
  font-weight: bold;
  border: none;
  background: transparent;
  color: var(--text-primary);
  border-bottom: 2px solid transparent;
  padding: 4px 0;
  outline: none;
}

.name-input:focus {
  border-bottom-color: var(--primary);
}

.delete-btn {
  background: linear-gradient(135deg, rgba(254, 226, 226, 0.88), rgba(255, 247, 237, 0.78));
  color: #991b1b;
  border: 1px solid rgba(252, 165, 165, 0.36);
  border-radius: 10px;
  padding: 8px 12px;
  cursor: pointer;
  font-weight: 700;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.form-grid label,
.story-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 0.86rem;
  font-weight: 700;
}

.field,
.color-field,
.story-field textarea {
  border: 1px solid rgba(31, 41, 55, 0.1);
  border-radius: 10px;
  background: linear-gradient(135deg, #ffffff, rgba(255, 250, 246, 0.72));
  color: var(--text-primary);
  font: inherit;
  outline: none;
}

.field {
  height: 38px;
  padding: 0 10px;
}

.color-field {
  width: 58px;
  height: 38px;
  padding: 8px;
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.66), rgba(204, 251, 241, 0.58));
}

.color-field::-webkit-color-swatch-wrapper {
  padding: 0;
}

.color-field::-webkit-color-swatch {
  border: 0;
  border-radius: 8px;
  opacity: 0.64;
}

input[type="checkbox"] {
  accent-color: #d6a07a;
}

.story-field textarea {
  min-height: 84px;
  padding: 10px;
  resize: vertical;
}

.toggles-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(240, 253, 250, 0.58));
  padding: 12px;
  border-radius: 12px;
}

.toggle-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.notification-settings {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.68));
  padding: 12px;
  border-radius: 12px;
}

.notification-settings p {
  margin: 0 0 8px 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
}

.checkbox-group {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
  padding-top: 16px;
  border-top: 1px solid rgba(0,0,0,0.05);
}

.btn {
  padding: 12px 24px;
  border-radius: 12px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  font-family: inherit;
  font-size: 1rem;
  transition: all 0.2s;
}

.btn-outline {
  background: transparent;
  border: 2px dashed rgba(0,0,0,0.1);
  color: var(--text-secondary);
  width: 100%;
  padding: 16px;
}

.btn-outline:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.9), rgba(204, 251, 241, 0.68));
}

.btn-secondary {
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.96), rgba(255, 250, 246, 0.82));
  color: var(--text-secondary);
}

.btn-primary {
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.95), rgba(204, 251, 241, 0.82));
  color: #7c2d12;
  border: 1px solid rgba(253, 186, 116, 0.38);
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.modal-body::-webkit-scrollbar {
  width: 6px;
}
.modal-body::-webkit-scrollbar-track {
  background: transparent;
}
.modal-body::-webkit-scrollbar-thumb {
  background: rgba(0,0,0,0.1);
  border-radius: 4px;
}

@media (max-width: 720px) {
  .form-grid,
  .toggles-grid {
    grid-template-columns: 1fr;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px) scale(0.95); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
</style>
