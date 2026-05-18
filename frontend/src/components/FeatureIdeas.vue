<template>
  <main class="roadmap-page">
    <section class="roadmap-header">
      <p class="eyebrow">功能规划</p>
      <h1>把 NoteHub 慢慢养成我们的生活档案馆</h1>
      <p class="intro">
        当前版本已经先落地事件中心、回忆时间轴和愿望清单。后面的方向不追求一次做大，而是每次补一个能持续使用的小模块。
      </p>
    </section>

    <section class="release-strip">
      <article v-for="release in releases" :key="release.version" class="release-card">
        <span>{{ release.version }}</span>
        <h2>{{ release.name }}</h2>
        <p>{{ release.description }}</p>
      </article>
    </section>

    <section class="roadmap-grid">
      <article v-for="item in roadmap" :key="item.stage" class="roadmap-card glass">
        <component :is="item.icon" class="idea-icon" />
        <div>
          <span class="stage">{{ item.stage }}</span>
          <h2>{{ item.title }}</h2>
          <p>{{ item.description }}</p>
        </div>
      </article>
    </section>
  </main>
</template>

<script setup>
import {
  Album,
  Bell,
  BookOpenText,
  CalendarHeart,
  HeartHandshake,
  Image,
  Sparkles,
  Tags
} from 'lucide-vue-next';

const releases = [
  {
    version: '0.2',
    name: '事件中心',
    description: '分类、标签、颜色、置顶、搜索和故事字段已经进入事件管理。'
  },
  {
    version: '0.3',
    name: '回忆卡片',
    description: '时间轴开始展示故事、地点、心情和标签，让日期变成有内容的节点。'
  },
  {
    version: '0.4',
    name: '愿望清单',
    description: '新增愿望/计划页面，用想做、计划中、已完成来承接未来。'
  }
];

const roadmap = [
  {
    stage: 'V1',
    title: '事件中心增强',
    description: '把重要日子做成可筛选、可置顶、可讲故事的核心入口。',
    icon: Tags
  },
  {
    stage: 'V2',
    title: '回忆时间轴',
    description: '按年月归档共同经历，逐步加入照片、地点和更多回忆卡片。',
    icon: CalendarHeart
  },
  {
    stage: 'V3',
    title: '相册与瞬间',
    description: '做图片墙、精选照片和照片背后的故事。',
    icon: Image
  },
  {
    stage: 'V4',
    title: '愿望与计划',
    description: '旅行计划、礼物灵感和待完成约会清单都可以放进来。',
    icon: HeartHandshake
  },
  {
    stage: 'V5',
    title: '年度回顾',
    description: '自动整理一年里的重要节点、完成的愿望和精选回忆。',
    icon: Album
  },
  {
    stage: 'V6',
    title: '私密互动',
    description: '留言、小纸条、随机回忆和随机约会建议，让网站更像两个人的小世界。',
    icon: Bell
  },
  {
    stage: 'V7',
    title: '内容沉淀',
    description: '日记、菜谱、电影清单、音乐清单和共同收藏慢慢装进来。',
    icon: BookOpenText
  },
  {
    stage: 'V8',
    title: '智能辅助',
    description: '未来再加入纪念文案、年度总结草稿和照片说明建议。',
    icon: Sparkles
  }
];
</script>

<style scoped>
.roadmap-page {
  max-width: 1120px;
  margin: 0 auto;
}

.roadmap-header {
  text-align: center;
  margin-bottom: 28px;
}

.eyebrow {
  color: var(--primary);
  font-weight: 800;
  margin: 0 0 8px;
}

.roadmap-header h1 {
  font-size: 2.1rem;
}

.intro {
  color: var(--text-secondary);
  max-width: 720px;
  margin: 14px auto 0;
  line-height: 1.7;
}

.release-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-bottom: 24px;
}

.release-card {
  background: #ffffff;
  border: 1px solid rgba(236, 72, 153, 0.08);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 8px 24px rgba(236, 72, 153, 0.06);
}

.release-card span {
  color: var(--primary);
  font-weight: 900;
}

.release-card h2 {
  margin-top: 8px;
  font-size: 1.08rem;
}

.release-card p {
  margin: 8px 0 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

.roadmap-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 18px;
}

.roadmap-card {
  padding: 22px;
  display: grid;
  grid-template-columns: 42px 1fr;
  gap: 16px;
  align-items: start;
  background: rgba(255, 255, 255, 0.82);
}

.idea-icon {
  width: 28px;
  height: 28px;
  color: var(--primary);
}

.stage {
  color: #be185d;
  background: #fce7f3;
  border-radius: 999px;
  padding: 4px 8px;
  font-size: 0.74rem;
  font-weight: 900;
}

.roadmap-card h2 {
  font-size: 1.1rem;
  margin: 10px 0 8px;
}

.roadmap-card p {
  margin: 0;
  color: var(--text-secondary);
  line-height: 1.6;
}

@media (max-width: 760px) {
  .release-strip {
    grid-template-columns: 1fr;
  }
}
</style>
