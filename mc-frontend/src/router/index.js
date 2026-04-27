import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: '/system/org',
        name: 'Org',
        component: () => import('@/views/system/Org.vue'),
        meta: { title: '组织管理' }
      },
      {
        path: '/system/user',
        name: 'User',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: '/system/role',
        name: 'Role',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: '/system/period',
        name: 'Period',
        component: () => import('@/views/system/Period.vue'),
        meta: { title: '期间管理' }
      },
      {
        path: '/workflow/template',
        name: 'Template',
        component: () => import('@/views/workflow/Template.vue'),
        meta: { title: '流程模板' }
      },
      {
        path: '/workflow/instance',
        name: 'Instance',
        component: () => import('@/views/workflow/Instance.vue'),
        meta: { title: '流程实例' }
      },
      {
        path: '/workflow/task',
        name: 'Task',
        component: () => import('@/views/workflow/Task.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: '/data/import',
        name: 'Import',
        component: () => import('@/views/data/Import.vue'),
        meta: { title: '数据导入' }
      },
      {
        path: '/rule',
        name: 'Rule',
        component: () => import('@/views/rule/Rule.vue'),
        meta: { title: '规则配置' }
      },
      {
        path: '/exception',
        name: 'Exception',
        component: () => import('@/views/exception/Exception.vue'),
        meta: { title: '异常处理' }
      },
      {
        path: '/notification',
        name: 'Notification',
        component: () => import('@/views/notification/Notification.vue'),
        meta: { title: '通知管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：检查登录状态
router.beforeEach((to, from, next) => {
  const token = Cookies.get('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
