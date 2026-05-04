// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import OwnerHome from '@/layout/home.vue'
import NotFound from '@/views/NotFound.vue'
import Layout from '@/layout/Layout.vue'
import OwnerFee from '@/views/owner/OwnerFee.vue';
import AdminFee from '@/views/admin/FeeAdmin.vue';
import Payment from '@/views/owner/Payment.vue';
import Voucher from '@/views/owner/Voucher.vue';
import RepairList from '@/views/admin/RepairAdmin.vue'
import AnnouncementList from '@/views/admin/AnnouncementAdmin.vue'
import VisitorList from '@/views/admin/VisitorAdmin.vue'
import OwnerList from '@/views/admin/OwnerAdmin.vue'
import OwnerRepair from "@/views/owner/OwnerRepair.vue";
import OwnerAnnouncement from "@/views/owner/OwnerAnnouncement.vue";
import OwnerVisitor from "@/views/owner/OwnerVisitor.vue";
import PersonList from '@/views/admin/PersonAdmin.vue';
import IndexList from '@/views/admin/indexAdmin.vue';
import CarouselList from '@/views/admin/CarouselAdmin.vue';
import OwnerPerson from "@/views/owner/OwnerPerson.vue";
import OwnerDeepSeek from "@/views/owner/OwnerDeepSeek.vue";
import OwnerSuggestion from "@/views/owner/OwnerSuggestion.vue";
import OwnerMessage from "@/views/owner/OwnerMessage.vue";
import OwnerFloorPlan from "@/views/owner/OwnerFloorPlan.vue";
import SuggestionList from '@/views/admin/SuggestionAdmin.vue';
import MessageList from "@/views/admin/MessageAdmin.vue";
import StaffAdmin from '@/views/Manager/StaffAdmin.vue'
import EquipmentAdmin from '@/views/Manager/EquipmentAdmin.vue'
import RegulationAdmin from "@/views/Manager/RegulationAdmin.vue";
import PersonList2 from '@/views/Manager/PersonAdmin.vue';
import IndexList2 from '@/views/Manager/indexAdmin.vue'
import OwnerIndex from "@/views/owner/OwnerIndex.vue";
import FeeTypeAdmin from '@/views/Manager/FeeType.vue';
import OwnerFee02 from '@/views/owner/OwnerFee02.vue';
import OwnerRepair02 from "@/views/owner/OwnerRepair02.vue";
import OwnerVisitor02 from "@/views/owner/OwnerVisitor02.vue";
import Register02 from '@/views/Register02.vue';
import OwnerIndex02 from '@/layout/index02.vue';



const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/register',
      name: 'Register',
      component: Register02
    },
    {
      path: '/owner/OwnerIndex02',
      name: 'OwnerIndex02',
      component: OwnerIndex02
    },
    {
      path: '/owner',
      component: OwnerHome,
      meta: {
        requiresAuth: true,
        role: 'owner'
      },
      children: [
        {
          path: 'home',
          name: 'OwnerHome',
          component: () => import('@/layout/home.vue'), // 需要创建这个组件
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/OwnerIndex',
          name: 'OwnerIndex',
          component: OwnerIndex,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/Person',
          name: 'OwnerPerson',
          component: OwnerPerson,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/DeepSeek',
          name: 'OwnerDeepSeek',
          component: OwnerDeepSeek,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/Suggestion',
          name: 'OwnerSuggestion',
          component: OwnerSuggestion,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/Message',
          name: 'OwnerMessage',
          component: OwnerMessage,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/FloorPlan',
          name: 'OwnerFloorPlan',
          component: OwnerFloorPlan,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/owner/fee/list',
          name: 'OwnerFeeList',
          component: OwnerFee02,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: 'fee/payment',
          name: 'Payment',
          component: Payment,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: 'fee/voucher',
          name: 'Voucher',
          component: Voucher,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/repair/list',
          name: 'OwnerRepair',
          component: OwnerRepair02,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/announcement/list',
          name: 'OwnerAnnouncement',
          component: OwnerAnnouncement,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
        {
          path: '/visitor/list',
          name: 'OwnerVisitor',
          component: OwnerVisitor02,
          meta: {
            requiresAuth: true,
            role: 'owner'
          }
        },
      ]
    },
    {
      path: '/admin',
      component: Layout,
      redirect: '/admin/property-staff',
      meta: {
        requiresAuth: true,
        role: 'manager' // 仅物业管理员可访问admin布局
      },
      children: [
        {
          path: 'property-staff',
          name: 'PropertyStaff',
          component: () => import('@/views/admin/PropertyStaff.vue'),
          meta: { title: '物业人员管理', icon: 'User' }
        },
        // 确认路径为 OwnerManagement（首字母大写），匹配Login.vue跳转路径
        {
          path: 'OwnerList',
          name: 'OwnerList',
          component: OwnerList,
          meta: {
            title: '业主信息管理',
            icon: 'UserFilled',
            requiresAuth: true,
            role: 'manager'
          }
        },
        {
          path: '/admin/PersonList',
          name: 'PersonList',
          component: PersonList,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/PersonList2',
          name: 'PersonList2',
          component: PersonList2,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/IndexList',
          name: 'IndexList',
          component: IndexList,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/IndexList2',
          name: 'IndexList2',
          component: IndexList2,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/CarouselList',
          name: 'CarouselList',
          component: CarouselList,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/SuggestionList',
          name: 'SuggestionList',
          component: SuggestionList,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/MessageList',
          name: 'MessageList',
          component: MessageList,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/StaffAdmin',
          name: 'StaffAdmin',
          component: StaffAdmin,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/EquipmentAdmin',
          name: 'EquipmentAdmin',
          component: EquipmentAdmin,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/RegulationAdmin',
          name: 'RegulationAdmin',
          component: RegulationAdmin,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/FeeTypeAdmin',
          name: 'FeeTypeAdmin',
          component: FeeTypeAdmin,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/FeeAdmin',
          name: 'FeeAdminList',
          component: AdminFee,
          meta: {
            requiresAuth: true,
            role: 'manager' // 仅物业管理员可访问
          }
        },
        {
          path: '/admin/RepairAdmin',
          name: 'RepairList',
          component: RepairList,
          meta: {
            requiresAuth: true,
            role: 'manager'
          }
        },
        {
          path: '/admin/AnnouncementAdmin',
          name: 'AnnouncementList',
          component: AnnouncementList,
          meta: {
            requiresAuth: true,
            role: 'manager'
          }
        },
        {
          path: '/admin/VisitorAdmin',
          name: 'VisitorList',
          component: VisitorList,
          meta: {
            requiresAuth: true,
            role: 'manager'
          }
        },
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: NotFound
    }
  ]
})

// 路由守卫：核心修复userRole类型转换 + 优化提示方式
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  // 1. 判断是否需要登录
  if (to.meta.requiresAuth && !token) {
    ElMessage.warning('请先登录后再访问'); // 替换window.alert，体验更好
    next('/login');
    return;
  }

  // 2. 登录后校验角色权限
  if (to.meta.requiresAuth && token) {
    // 核心修复：将localStorage的字符串角色转为数字（关键！）
    const userRoleStr = localStorage.getItem('userRole');
    const userRole = userRoleStr ? Number(userRoleStr) : null; // 防止null/undefined转换出错
    const requiredRole = to.meta.role;

    // 调试日志（可选，方便排查问题）
    console.log('当前用户角色（数字）：', userRole);
    console.log('页面要求角色：', requiredRole);

    // 角色匹配校验（数字对比，避免字符串/数字类型不匹配）
    if (requiredRole === 'owner') {
      // 业主页面：仅允许role=2的用户访问
      if (userRole === 2) {
        next();
      } else {
        ElMessage.error('无业主权限，请切换账号');
        next('/login');
      }
    } else if (requiredRole === 'manager') {
      // 管理员页面：仅允许role=1/3的用户访问
      if (userRole === 1 || userRole === 3) {
        next();
      } else {
        ElMessage.error('无管理员权限，请切换账号');
        next('/login');
      }
    } else {
      // 无角色限制的页面，直接放行
      next();
    }
  } else {
    // 无需登录的页面，直接放行
    next();
  }
});

export default router