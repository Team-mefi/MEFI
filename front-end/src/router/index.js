import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

import ConferenceView from "@/views/conference/ConferenceView.vue"
import MainView from "@/views/main/MainView.vue"
import UserView from "@/views/user/UserView.vue"
import Login from "@/components/user/Login.vue"
import Signup from "@/components/user/Signup.vue"
import Email from "@/components/user/Email.vue"
import SearchPassword1 from "@/components/user/SearchPassword1.vue"
import SearchPassword2 from "@/components/user/SearchPassword2.vue"


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
      children: [
        {
          path: '/main',
          name: 'main',
          component: MainView
        },
      ]
    },
    {
      path: '/user',
      name: 'user',
      component: UserView,
      children:[
        {
          path:'/user/login',
          name:'login',
          component: Login,
        },
        {
          path:'/user/signup',
          name:'signup',
          component: Signup,
        },
        {
          path:'/user/email',
          name:'email',
          component: Email,
        },
        {
          path:'/user/signup/email',
          name:'search-password1',
          component: SearchPassword1,
        },
        {
          path:'/user/signup/search-password',
          name:'search-password2',
          component: SearchPassword2,
        }
      ]
    },
    {
      path: '/conference',
      name: 'conference',
      component: ConferenceView
    },
  ]
})

export default router
