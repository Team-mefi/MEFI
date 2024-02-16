<template>
  <div>
    <div ref="editorContainer" id="editor"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { createFile } from '@/api/file'
import { useUserStore } from '@/stores/user'

// yjs 사용하기
import * as Y from 'yjs'
import { WebsocketProvider } from 'y-websocket'
import { QuillBinding } from 'y-quill'

// Quill Editor + Markdown 사용을 위한 최소한의 설정
import 'quill/dist/quill.snow.css'
import Quill from 'quill'
import QuillCursors from 'quill-cursors'
import MarkdownShortcuts from 'quill-markdown-shortcuts'
// import { quillEditor } from "vue-quill-editor";

const router = useRouter()
const props = defineProps({
  conferenceState: Boolean
})
const userStore = useUserStore()

const nowdate = ref(new Date())
const year = ref(nowdate.value.getFullYear())
const month = ref(nowdate.value.getMonth())
const date = ref(nowdate.value.getDate())

const route = useRoute()
const teamId = ref(route.params?.teamid)
const conferenceId = ref(route.params?.conferenceid)

Quill.register('modules/cursors', QuillCursors)
Quill.register('modules/markdownShortcuts', MarkdownShortcuts)

const ydoc = new Y.Doc()

const provider = new WebsocketProvider(
  // `ws${location.protocol.slice(4)}//${location.host}/ws`,
  // `ws://localhost:5460/ws`,
  'wss://demos.yjs.dev/ws', // use the public ws server
  `mefi123-${teamId.value}-${conferenceId.value}`,
  ydoc
)

// Yjs + Quill 연동
const ytext = ydoc.getText('quill')
const editorContainer = ref(null)

const binding = ref(null)

onMounted(() => {
  const editor = new Quill(editorContainer.value, {
    theme: 'snow', // or 'bubble'
    modules: {
      markdownShortcuts: {},
      toolbar: [
        ['bold', 'italic', 'underline', 'strike'],
        ['blockquote', 'code-block'],
        [{ header: 1 }, { header: 2 }],
        [{ list: 'ordered' }, { list: 'bullet' }],
        [{ script: 'sub' }, { script: 'super' }],
        [{ indent: '-1' }, { indent: '+1' }],
        [{ direction: 'rtl' }],
        [{ size: ['small', false, 'large', 'huge'] }],
        [{ header: [1, 2, 3, 4, 5, 6, false] }],
        [{ color: [] }, { background: [] }],
        [{ font: [] }],
        [{ align: [] }],
        ['clean'],
        ['link', 'image', 'video']
      ],
      cursors: {
        hideDelayMs: 400,
        hideSpeedMs: 400,
        transformOnTextChange: false
      },
      history: {
        userOnly: true
      }
    },
    placeholder: '회의 문서를 작성해 주세요...!'
  })

  // 문서 작성 중인 이름 및 색상 설정
  provider.awareness.setLocalStateField('user', {
    name: `${userStore.userInfo.name}`,
    // 색깔 랜덤 할당
    color: `#${Math.floor(Math.random() * 16777215).toString(16)}`
  })

  binding.value = new QuillBinding(ytext, editor, provider.awareness)
})

onBeforeUnmount(() => {
  binding.value.destroy()
})

// 공동 문서 작업 연결 종료
// 추후 사용할 가능성이 있으므로 주석처리 함
// const connectState = ref("Disconnect");

// const connect = () => {
//   if (provider.shouldConnect) {
//     provider.disconnect();
//     connectState.value = "Connect";
//   } else {
//     provider.connect();
//     connectState.value = "Disconnect";
//   }
// };

watch(
  () => props.conferenceState,
  () => {
    if (!props.conferenceState) {
      createPDF()
    }
  }
)

const deleteContent = () => {
  let target = editorContainer.value.firstChild

  while (target.firstChild) {
    target.removeChild(target.firstChild)
  }
}

// 공동 작업 문서 PDF 파일로 변환하여 저장
const createPDF = () => {
  // editorContainer을 canvas객체로 변환
  html2canvas(editorContainer.value).then((canvas) => {
    const filename =
      String(year.value) + '년 ' + String(month.value + 1).padStart(2, '0') + '월 ' + String(date.value).padStart(2, '0') + '일_회의록.pdf'
    const doc = new jsPDF('p', 'mm', 'a4') // jsPDF 객체 생성
    const imgData = canvas.toDataURL('image/png') // canvas를 .png 이미지로 변환
    const imgWidth = 210
    const pageHeight = 295
    const imgHeight = (canvas.height * imgWidth) / canvas.width

    let position = 0
    let heightLeft = imgHeight

    // 첫 페이지 생성
    doc.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
    heightLeft -= pageHeight

    // 저장해야할 남은 문서 내용이 없을 때까지 페이지 추가
    while (heightLeft >= 0) {
      position = heightLeft - imgHeight
      doc.addPage()
      doc.addImage(imgData, 'PNG', 0, position, imgWidth, imgHeight)
      heightLeft -= pageHeight
    }

    // 로컬에 문서 pdf 파일 저장
    // 추후 사용을 위해 주석 처리함
    // doc.save(filename)
    
    // 회의 문서 내용 모두 삭제
    deleteContent()

    // 서버로 axios 요청
    // 공동 작업 문서를 pdf(binary) 형식으로 보냄
    // 백엔드와 소통하여 axios 연결할 예정
    const formData = new FormData()
    const file = new File([doc.output('blob')], filename, { type: 'application/pdf' })

    const fileRequestDto = new Blob(
      [
        JSON.stringify({
          teamId: teamId.value,
          conferenceId: conferenceId.value,
          fileName: 'DOCUMENT.pdf',
          type: 'DOCUMENT'
        })
      ],
      { type: 'application/json' }
    )

    formData.append('file', file)
    formData.append('fileRequestDto', fileRequestDto)

    createFile(
      formData,
      (response) => {
        console.log(response.data?.dataBody)
      },
      (error) => {
        console.log(error)
      }
    ).then(() => {
      router.push({ name: 'team', params: { id: teamId.value } })
    })
  })
}
</script>

<style scoped>
#editor {
  background-color: white;
  padding: 10px 0;
  color: black;
}

/* quill 기본 css 오버라이딩 */
.ql-toolbar.ql-snow {
  border: 1px solid #ccc;
  box-sizing: border-box;
  font-family: 'Helvetica Neue', 'Helvetica', 'Arial', sans-serif;
  padding: 8px;
}

.ql-container {
  height: 83vh;
}
</style>
