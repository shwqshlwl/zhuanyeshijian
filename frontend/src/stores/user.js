import { defineStore } from 'pinia'
import { login, getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo.username || '',
    realName: (state) => state.userInfo.realName || '',
    userType: (state) => state.userInfo.userType || 1,
    // userType: 1-学生，2-教师，3-管理员
    isStudent: (state) => state.userInfo.userType === 1,
    isTeacher: (state) => state.userInfo.userType === 2 || state.userInfo.userType === 3,
    isAdmin: (state) => state.userInfo.userType === 3,
    roleText: (state) => {
      const map = { 1: '学生', 2: '教师', 3: '管理员' }
      return map[state.userInfo.userType] || '未知'
    }
  },
  
  actions: {
    async login(loginForm) {
      try {
        const res = await login(loginForm)
        this.token = res.data.token
        this.userInfo = res.data.userInfo || {}
        localStorage.setItem('token', this.token)
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return res
      } catch (error) {
        throw error
      }
    },
    
    async fetchUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
        return res
      } catch (error) {
        throw error
      }
    },
    
    logout() {
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  }
})
