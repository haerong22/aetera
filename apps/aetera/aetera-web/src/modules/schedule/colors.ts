/**
 * 일정 색 팔레트. 첫 값은 브랜드 primary 와 같아야 한다 —
 * 색을 고르지 않은 일정의 기본값이자 다른 화면의 fallback 과 같은 값이기 때문이다.
 * (globals.css 의 --color-primary / --color-accent / success / danger / grey-500)
 *
 * 다이얼로그가 아니라 별도 파일에 둔다. 대시보드 카드가 이 상수 하나 때문에
 * EventDialog 와 그 뮤테이션 훅 전체를 번들에 끌어오지 않게 하기 위해서다.
 */
export const EVENT_COLORS = ["#5b5fef", "#ff6f0f", "#2ba471", "#f04452", "#7b8494"];

/** 색이 지정되지 않은 일정을 그릴 때 쓰는 값. */
export const DEFAULT_EVENT_COLOR = EVENT_COLORS[0];
