<template>
  <div class="app-shell">
    <div class="ambient-particles">
      <div id="stars"></div>
      <div id="stars2"></div>
      <div id="stars3"></div>
    </div>

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
  width: min(1220px, 100%);
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.top-nav {
  position: sticky;
  top: 16px;
  z-index: 100;
  width: fit-content;
  max-width: 100%;
  margin: 0 auto;
  padding: 6px;
  border-radius: 999px;
  display: flex;
  gap: 6px;
  overflow-x: auto;
  background: var(--surface);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(112, 87, 74, 0.05);
}

.nav-item {
  min-width: 96px;
  height: 38px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--text-secondary);
  font: inherit;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition: background 0.3s ease, color 0.3s ease, box-shadow 0.3s ease;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.5);
  color: var(--primary);
}

.nav-item.active {
  background: #ffffff;
  color: var(--primary);
  font-weight: 750;
  box-shadow: 0 4px 12px rgba(112, 87, 74, 0.08);
}

.nav-icon {
  width: 18px;
  height: 18px;
  flex: 0 0 auto;
}

@media (max-width: 640px) {
  .top-nav {
    top: 8px;
    width: 100%;
    justify-content: flex-start;
    border-radius: 18px;
  }

  .nav-item {
    min-width: 86px;
  }
}
</style>
