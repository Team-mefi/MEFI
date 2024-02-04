import { ref } from 'vue'
import { defineStore } from 'pinia'
import { useRouter } from 'vue-router'

// setting store
// 알림, 입출력 설정 저장 
export const useSettingStore = defineStore('setting', () => {
    const alarmPermission = ref(true)
    const alarmSound = ref(false)

  return { alarmPermission, alarmSound }
  }, { persist: true }
)
