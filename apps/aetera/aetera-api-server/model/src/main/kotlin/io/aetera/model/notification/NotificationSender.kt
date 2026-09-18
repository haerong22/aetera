package io.aetera.model.notification

/**
 * 다이제스트를 실제로 내보내는 곳.
 *
 * 저장소와 같은 자리의 포트다 — 규약은 model 이 들고, 구현은 gateway 가 한다.
 * 무엇을 보낼지(모듈들이 모은 알림)와 어떻게 보낼지(메일·웹푸시)를 가르므로,
 * 웹푸시를 붙일 때 구현이 하나 더 생길 뿐 모으는 쪽은 한 줄도 바뀌지 않는다.
 *
 * 보내다 실패하면 던진다 — 삼키면 부르는 쪽이 "보냈다"고 표시해 버려 그날 알림이
 * 조용히 사라진다.
 */
interface NotificationSender {
    fun send(digest: NoticeDigest)
}
