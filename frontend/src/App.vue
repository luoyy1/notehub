<template>
  <div class="app-shell">
    <nav class="top-nav glass">
      <button
        v-for="route in visibleRoutes"
        :key="route.path"
        class="nav-item"
        :class="{ active: currentPath === route.path }"
        @click="go(route.path)"
        :title="route.title"
      >
        <component :is="iconMap[route.icon] || CircleIcon" class="nav-icon" />
        <span>{{ route.title }}</span>
      </button>
    </nav>

    <component :is="currentComponent" />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { Circle as CircleIcon, HeartHandshake, Home, Sparkles, Waypoints } from 'lucide-vue-next';
import { getRoutes } from './api';
import Dashboard from './components/Dashboard.vue';
import FeatureIdeas from './components/FeatureIdeas.vue';
import Timeline from './components/Timeline.vue';
import Wishes from './components/Wishes.vue';

const componentMap = {
  Dashboard,
  FeatureIdeas,
  Timeline,
  Wishes
};

const iconMap = {
  Home,
  HeartHandshake,
  Sparkles,
  Waypoints
};

const routes = ref([]);
const currentPath = ref(window.location.pathname || '/');

const normalizePath = (path) => {
  if (!path || path === '') return '/';
  return path.startsWith('/') ? path : `/${path}`;
};

const visibleRoutes = computed(() => {
  return routes.value
    .filter(route => route.visible !== false)
    .sort((a, b) => Number(a.sort || 0) - Number(b.sort || 0));
});

const matchedRoute = computed(() => {
  return routes.value.find(route => route.path === currentPath.value) || routes.value[0];
});

const currentComponent = computed(() => {
  return componentMap[matchedRoute.value?.component] || Dashboard;
});

const syncPath = () => {
  currentPath.value = normalizePath(window.location.pathname);
};

const go = (path) => {
  const nextPath = normalizePath(path);
  if (nextPath === currentPath.value) return;
  window.history.pushState({}, '', nextPath);
  currentPath.value = nextPath;
};

onMounted(async () => {
  routes.value = await getRoutes();
  syncPath();

  if (!matchedRoute.value) {
    go('/');
  }

  window.addEventListener('popstate', syncPath);
});

onBeforeUnmount(() => {
  window.removeEventListener('popstate', syncPath);
});
</script>

<style scoped>
.app-shell {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.top-nav {
  width: fit-content;
  max-width: 100%;
  margin: 0 auto;
  padding: 8px;
  border-radius: 999px;
  display: flex;
  gap: 6px;
  overflow-x: auto;
}

.nav-item {
  min-width: 92px;
  height: 40px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-secondary);
  font: inherit;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: background 0.2s, color 0.2s, box-shadow 0.2s;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.7);
  color: var(--primary);
}

.nav-item.active {
  background: #ffffff;
  color: var(--primary);
  box-shadow: 0 4px 14px rgba(236, 72, 153, 0.12);
}

.nav-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 auto;
}

@media (max-width: 640px) {
  .top-nav {
    width: 100%;
    justify-content: flex-start;
  }

  .nav-item {
    min-width: 84px;
  }
}
</style>
