import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
});

export const getEvents = () => {
  return api.get('/events').then(res => res.data).catch(() => {
    console.warn("Backend not reachable, using mock events data for preview");
    return [
      { id: "e1-mock", name: "在一起的日子", date: "2024-01-01", isAnnual: false, enableCountUp: true, enableCountdown: false, daysPassed: 862, daysToNextHundred: 38 },
      { id: "e2-mock", name: "老婆生日", date: "2000-06-15", isAnnual: true, enableCountUp: false, enableCountdown: true, daysUntil: 35, nextAge: 26 },
      { id: "e3-mock", name: "我的生日", date: "2000-03-20", isAnnual: true, enableCountUp: false, enableCountdown: true, daysUntil: 0, nextAge: 26 },
      { id: "e4-mock", name: "结婚纪念日", date: "2025-10-01", isAnnual: true, enableCountUp: true, enableCountdown: true, daysPassed: 222, daysToNextHundred: 78, daysUntil: 140 }
    ];
  });
};

export const getWishes = () => {
  return api.get('/wishes').then(res => res.data).catch(() => {
    console.warn("Backend not reachable, using mock wishes data for preview");
    return [
      {
        id: "w1-mock",
        title: "一起去海边看日出",
        description: "找一个不用赶路的周末，住在离海很近的地方。",
        type: "trip",
        status: "todo",
        priority: 2,
        target_date: "",
        completed_date: "",
        location: "海边",
        tags: ["旅行", "约会"],
        note: "可以顺手拍一组年度照片。"
      },
      {
        id: "w2-mock",
        title: "准备一个只属于她的小礼物",
        description: "不一定贵，但要和最近的生活细节有关。",
        type: "gift",
        status: "planning",
        priority: 1,
        target_date: "",
        completed_date: "",
        location: "",
        tags: ["礼物"],
        note: "提前记录灵感，别到节日前才开始慌。"
      }
    ];
  });
};

export const saveWishes = (wishes) => {
  return api.post('/wishes', wishes).then(res => res.data).catch(() => {
    console.warn("Backend not reachable, mocked saving wishes");
    return wishes;
  });
};

export const getRawEvents = () => {
  return api.get('/events/raw').then(res => res.data).catch(() => {
    return [
      { id: "e1-mock", name: "在一起的日子", date: "2024-01-01", is_annual: false, enable_count_up: true, enable_countdown: false, enable_notification: true, notify_advance_days: [] },
      { id: "e2-mock", name: "老婆生日", date: "2000-06-15", is_annual: true, enable_count_up: false, enable_countdown: true, enable_notification: true, notify_advance_days: [7, 3, 1, 0] },
      { id: "e3-mock", name: "我的生日", date: "2000-03-20", is_annual: true, enable_count_up: false, enable_countdown: true, enable_notification: true, notify_advance_days: [7, 3, 1, 0] },
      { id: "e4-mock", name: "结婚纪念日", date: "2025-10-01", is_annual: true, enable_count_up: true, enable_countdown: true, enable_notification: true, notify_advance_days: [7, 3, 1, 0] }
    ];
  });
};

export const saveEvents = (events) => {
  return api.post('/events', events).then(res => res.data).catch(() => {
    console.warn("Backend not reachable, mocked saving");
    return events;
  });
};

export const getCalendarMarks = (year) => {
  return api.get('/events/calendar', { params: { year } }).then(res => res.data).catch(() => {
    console.warn("Backend not reachable, using mock calendar marks for preview");
    const today = new Date();
    const y = today.getFullYear();
    const m = String(today.getMonth() + 1).padStart(2, '0');
    return [
      { id: "e1-mock_once", name: "在一起的日子", isAnnual: false, date: "2024-01-01", month: 1 },
      { id: "e1-mock_milestone_900", name: "在一起的日子 900天", isAnnual: false, date: `${y}-${m}-15`, month: parseInt(m) },
      { id: "e2-mock_annual", name: "老婆生日", isAnnual: true, date: `${y}-06-15`, month: 6 },
      { id: "e3-mock_annual", name: "我的生日", isAnnual: true, date: `${y}-${m}-${String(today.getDate()).padStart(2, '0')}`, month: parseInt(m) }
    ];
  });
};

export const getRoutes = () => {
  return api.get('/routes').then(res => res.data).catch(() => {
    console.warn("Backend not reachable, using mock routes for preview");
    return [
      { path: "/", name: "dashboard", title: "首页", component: "Dashboard", icon: "Home", sort: 1, visible: true },
      { path: "/timeline", name: "timeline", title: "时间轴", component: "Timeline", icon: "Waypoints", sort: 2, visible: true },
      { path: "/ideas", name: "ideas", title: "功能规划", component: "FeatureIdeas", icon: "Sparkles", sort: 3, visible: true },
      { path: "/wishes", name: "wishes", title: "愿望清单", component: "Wishes", icon: "HeartHandshake", sort: 4, visible: true }
    ];
  });
};
