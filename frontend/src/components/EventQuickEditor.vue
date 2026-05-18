<template>
  <div v-if="isOpen" class="modal-overlay">
    <div class="modal-content glass">
      <div class="modal-header">
        <div>
          <p class="eyebrow">快速编辑</p>
          <h2>{{ form.name || '事件' }}</h2>
        </div>
        <button class="icon-btn" @click="$emit('close')" title="关闭">×</button>
      </div>

      <div v-if="loading" class="loading">正在打开事件...</div>

      <form v-else class="editor-form" @submit.prevent="save">
        <div class="form-grid">
          <label class="wide">
            <span>事件名称</span>
            <input v-model="form.name" class="field" type="text" placeholder="事件名称" />
          </label>
          <label>
            <span>发生日期</span>
            <input v-model="form.date" class="field" type="date" />
          </label>
          <label>
            <span>分类</span>
            <select v-model="form.category" class="field">
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
            <input v-model="form.color" class="color-field" type="color" />
          </label>
          <label>
            <span>地点</span>
            <input v-model="form.location" class="field" type="text" placeholder="例如 海边、家、餐厅" />
          </label>
          <label>
            <span>心情</span>
            <input v-model="form.mood" class="field" type="text" placeholder="例如 期待、珍惜" />
          </label>
          <label class="wide">
            <span>标签</span>
            <input v-model="form.tagsText" class="field" type="text" placeholder="用逗号分隔" />
          </label>
        </div>

        <label class="story-field">
          <span>背后的故事</span>
          <textarea v-model="form.story" placeholder="写一点这一天为什么重要"></textarea>
        </label>

        <div class="toggles-grid">
          <label class="toggle-label switch-toggle">
            <input v-model="form.show_in_timeline" type="checkbox" />
            <span>加入时间轴</span>
          </label>
          <label class="toggle-label">
            <input v-model="form.pinned" type="checkbox" />
            <span>首页置顶</span>
          </label>
          <label class="toggle-label">
            <input v-model="form.is_annual" type="checkbox" />
            <span>每年重复</span>
          </label>
          <label class="toggle-label">
            <input v-model="form.enable_count_up" type="checkbox" />
            <span>开启正计时</span>
          </label>
          <label class="toggle-label">
            <input v-model="form.enable_countdown" type="checkbox" />
            <span>开启倒计时</span>
          </label>
          <label class="toggle-label">
            <input v-model="form.enable_notification" type="checkbox" />
            <span>开启邮件提醒</span>
          </label>
        </div>

        <div v-if="form.enable_notification" class="notification-settings">
          <p>提前几天提醒？</p>
          <div class="checkbox-group">
            <label v-for="day in reminderOptions" :key="day">
              <input v-model="form.notify_advance_days" type="checkbox" :value="day" />
              {{ day === 0 ? '当天' : day + '天前' }}
            </label>
          </div>
        </div>

        <p v-if="errorMessage" class="error-message">{{ errorMessage }}</p>

        <div class="modal-footer">
          <button class="btn btn-secondary" type="button" @click="$emit('close')">取消</button>
          <button class="btn btn-primary" type="submit" :disabled="saving">
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, watch } from 'vue';
import { getRawEvents, saveEvents } from '../api';

const props = defineProps({
  isOpen: Boolean,
  event: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['close', 'saved']);

const reminderOptions = [7, 3, 1, 0];
const loading = ref(false);
const saving = ref(false);
const errorMessage = ref('');
const rawEvents = ref([]);
const form = reactive(createEmptyForm());

function createEmptyForm() {
  return {
    id: '',
    name: '',
    date: new Date().toISOString().split('T')[0],
    is_annual: false,
    enable_count_up: false,
    enable_countdown: true,
    enable_notification: false,
    notify_advance_days: [],
    category: 'other',
    tags: [],
    tagsText: '',
    color: '#ec4899',
    pinned: false,
    show_in_timeline: false,
    story: '',
    location: '',
    mood: '',
    photos: []
  };
}

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
  ...createEmptyForm(),
  ...event,
  date: toDateInput(event?.date),
  is_annual: Boolean(event?.is_annual ?? event?.isAnnual),
  enable_count_up: Boolean(event?.enable_count_up ?? event?.enableCountUp),
  enable_countdown: Boolean(event?.enable_countdown ?? event?.enableCountdown),
  enable_notification: Boolean(event?.enable_notification ?? event?.enableNotification),
  notify_advance_days: Array.isArray(event?.notify_advance_days) ? [...event.notify_advance_days] : [],
  category: event?.category || 'other',
  tags: Array.isArray(event?.tags) ? [...event.tags] : [],
  tagsText: Array.isArray(event?.tags) ? event.tags.join('，') : '',
  color: event?.color || '#ec4899',
  pinned: Boolean(event?.pinned),
  show_in_timeline: Boolean(event?.show_in_timeline ?? event?.showInTimeline),
  story: event?.story || '',
  location: event?.location || '',
  mood: event?.mood || '',
  photos: Array.isArray(event?.photos) ? [...event.photos] : []
});

const applyForm = (event) => {
  Object.assign(form, normalizeEvent(event));
};

const toPayload = () => ({
  id: form.id,
  name: String(form.name || '').trim(),
  date: form.date,
  is_annual: Boolean(form.is_annual),
  enable_count_up: Boolean(form.enable_count_up),
  enable_countdown: Boolean(form.enable_countdown),
  enable_notification: Boolean(form.enable_notification),
  notify_advance_days: form.enable_notification ? [...form.notify_advance_days].sort((a, b) => b - a) : [],
  category: form.category || 'other',
  tags: splitTags(form.tagsText),
  color: form.color || '#ec4899',
  pinned: Boolean(form.pinned),
  show_in_timeline: Boolean(form.show_in_timeline),
  story: form.story || '',
  location: form.location || '',
  mood: form.mood || '',
  photos: Array.isArray(form.photos) ? form.photos : []
});

const openEditor = async () => {
  if (!props.isOpen || !props.event) return;

  loading.value = true;
  errorMessage.value = '';
  try {
    const events = await getRawEvents();
    rawEvents.value = JSON.parse(JSON.stringify(events));
    const rawEvent = rawEvents.value.find(item => item.id === props.event.id) || props.event;
    applyForm(rawEvent);
  } catch (error) {
    console.error(error);
    errorMessage.value = '事件读取失败，请稍后重试。';
    applyForm(props.event);
  } finally {
    loading.value = false;
  }
};

watch(() => [props.isOpen, props.event?.id], openEditor, { immediate: true });

const save = async () => {
  const payload = toPayload();
  if (!payload.name || !payload.date) {
    errorMessage.value = '请先补全事件名称和发生日期。';
    return;
  }

  saving.value = true;
  errorMessage.value = '';
  try {
    const nextEvents = [...rawEvents.value];
    const index = nextEvents.findIndex(item => item.id === payload.id);
    if (index >= 0) {
      nextEvents[index] = {
        ...nextEvents[index],
        ...payload
      };
    } else {
      nextEvents.push(payload);
    }

    await saveEvents(nextEvents);
    emit('saved');
    emit('close');
  } catch (error) {
    console.error(error);
    errorMessage.value = '保存失败，请确认后端服务可用后重试。';
  } finally {
    saving.value = false;
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgba(72, 61, 50, 0.28);
  backdrop-filter: blur(8px);
}

.modal-content {
  width: min(760px, 100%);
  max-height: 88vh;
  overflow-y: auto;
  border-radius: 24px;
  padding: 24px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.98), rgba(255, 250, 246, 0.92));
}

.modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.eyebrow {
  margin: 0 0 6px;
  color: var(--primary);
  font-size: 0.8rem;
  font-weight: 850;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.35rem;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.95), rgba(255, 250, 246, 0.86));
  color: var(--text-secondary);
  cursor: pointer;
  font: inherit;
  font-size: 1.35rem;
  line-height: 1;
}

.loading,
.error-message {
  color: var(--text-secondary);
}

.editor-form,
.form-grid,
.toggles-grid {
  display: grid;
  gap: 14px;
}

.form-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.wide {
  grid-column: span 2;
}

.form-grid label,
.story-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 0.86rem;
  font-weight: 750;
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
  height: 40px;
  padding: 0 10px;
}

.color-field {
  width: 58px;
  height: 40px;
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
  min-height: 96px;
  padding: 10px;
  resize: vertical;
  line-height: 1.55;
}

.toggles-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  border-radius: 14px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(240, 253, 250, 0.58));
}

.toggle-label,
.checkbox-group label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 700;
}

.toggle-label {
  position: relative;
  min-height: 38px;
  border: 1px solid rgba(214, 199, 184, 0.74);
  border-radius: 999px;
  padding: 6px 12px 6px 42px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.92), rgba(255, 250, 246, 0.72));
  cursor: pointer;
}

.toggle-label input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.toggle-label::before {
  content: "";
  position: absolute;
  left: 10px;
  width: 24px;
  height: 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(226, 232, 240, 0.9), rgba(255, 250, 246, 0.82));
  border: 1px solid rgba(148, 163, 184, 0.32);
  transition: all 0.18s ease;
}

.toggle-label::after {
  content: "";
  position: absolute;
  left: 13px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(31, 41, 55, 0.16);
  transition: all 0.18s ease;
}

.toggle-label:has(input:checked) {
  color: #7c2d12;
  border-color: rgba(253, 186, 116, 0.42);
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.88), rgba(204, 251, 241, 0.72));
}

.toggle-label:has(input:checked)::before {
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.42), rgba(45, 212, 191, 0.42));
}

.toggle-label:has(input:checked)::after {
  transform: translateX(10px);
}

.notification-settings {
  border-radius: 14px;
  padding: 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.96), rgba(255, 247, 237, 0.68));
}

.notification-settings p {
  margin: 0 0 10px;
  color: var(--text-secondary);
  font-size: 0.9rem;
  font-weight: 750;
}

.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
}

.checkbox-group label {
  border: 1px solid rgba(214, 199, 184, 0.74);
  border-radius: 999px;
  padding: 7px 10px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(255, 250, 246, 0.72));
}

.error-message {
  margin: 0;
  color: #be123c;
  background: linear-gradient(135deg, rgba(255, 228, 230, 0.9), rgba(255, 247, 237, 0.78));
  border-radius: 12px;
  padding: 10px 12px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(31, 41, 55, 0.06);
}

.btn {
  border: 0;
  border-radius: 12px;
  padding: 11px 18px;
  font: inherit;
  font-weight: 800;
  cursor: pointer;
}

.btn-secondary {
  background: linear-gradient(135deg, rgba(248, 250, 252, 0.96), rgba(255, 250, 246, 0.82));
  color: var(--text-secondary);
}

.btn-primary {
  color: #7c2d12;
  border: 1px solid rgba(253, 186, 116, 0.38);
  background: linear-gradient(135deg, rgba(255, 237, 213, 0.95), rgba(204, 251, 241, 0.82));
  box-shadow: 0 8px 22px rgba(120, 76, 45, 0.1);
}

.btn-primary:disabled {
  opacity: 0.72;
  cursor: not-allowed;
}

@media (max-width: 720px) {
  .modal-overlay {
    align-items: stretch;
    padding: 12px;
  }

  .modal-content {
    max-height: 92vh;
    padding: 18px;
  }

  .form-grid,
  .toggles-grid {
    grid-template-columns: 1fr;
  }

  .wide {
    grid-column: auto;
  }

  .modal-footer {
    flex-direction: column-reverse;
  }
}
</style>
