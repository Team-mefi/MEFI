import { ref } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'
import { userLogin, userSignup } from '@/api/user.js'

// user store
// login, signup, user info, 토큰 관리
export const useUserStore = defineStore('user', () => {
  const router = useRouter()
  const isLogin = ref(false)
  const userInfo = ref(null)

  // 회원 가입 함수
  // user 정보 : email, password, name, position, department
  const signup = async (user) => {
    await userSignup(
      user,(response)=>{console.log(response)},
      (error)=>{console.log(error)}
    )
     .then(()=>{
      const loginUser = {
        email:user.email,
        password:user.password,
      }
      login(loginUser)
     })
  }

  // 로그인 함수
  // user 정보 : email, password
  const login = async (user) => {
    await userLogin(
      user,(response) => {
        userInfo.value = response.data.dataBody
        localStorage.setItem("accessToken", response.headers.accesstoken)
        localStorage.setItem("refreshToken", response.headers.refreshtoken)
        isLogin.value = true;
      },
      (error)=>{
        console.log(error)
      }
    ).then(()=>{
      router.push({name:'home'})
    })
  }

  // 로그아웃 함수
  // isLogin, token delete
  const logout = () => {
    isLogin.value = false
    userInfo.value = null
    localStorage.clear()
    router.push({name:'home'})
  }

  return { isLogin, signup, login, logout, userInfo }
},{ persist:true }
)
