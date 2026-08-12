/**
 * ⚠️ UI 프리뷰용 mock 데이터.
 *
 * 우선순위·라이프 영역·목표·타임라인은 아직 백엔드 기능이 없다.
 * 홈 대시보드의 정보 구조를 먼저 보여주기 위한 정적 데이터이며,
 * 해당 모듈(목표/타임라인/AI 코치)이 구현되면 이 파일부터 제거한다.
 *
 * 원칙: 근거 없는 수치를 확정된 분석처럼 보여주지 않는다 —
 * 라이프 영역은 점수 대신 "기록 필요" 같은 상태 텍스트만 쓴다.
 */

export interface MockPriority {
  id: string;
  title: string;
  aiSuggested: boolean;
}

export const MOCK_PRIORITIES: MockPriority[] = [
  { id: "p1", title: "팀 회의 안건 정리", aiSuggested: true },
  { id: "p2", title: "진행 중인 목표 확인", aiSuggested: false },
  { id: "p3", title: "30분 산책하기", aiSuggested: true },
];

export type LifeAreaKey = "health" | "career" | "relationship" | "finance" | "growth";

export interface MockLifeArea {
  key: LifeAreaKey;
  label: string;
  /** 데이터가 없는 현재 상태를 있는 그대로 표현한다. */
  status: string;
  tone: "success" | "warning" | "muted";
}

export const MOCK_LIFE_AREAS: MockLifeArea[] = [
  { key: "health", label: "건강", status: "기록 필요", tone: "muted" },
  { key: "career", label: "커리어", status: "진행 중", tone: "success" },
  { key: "relationship", label: "관계", status: "기록 부족", tone: "warning" },
  { key: "finance", label: "재정", status: "연결 필요", tone: "muted" },
  { key: "growth", label: "성장", status: "목표 설정 필요", tone: "muted" },
];

export interface MockGoal {
  id: string;
  title: string;
  current: number;
  target: number;
  /** 진행률 오른쪽에 붙는 표기. 예: "2/3", "60/100p", "40%" */
  progressLabel: string;
}

export const MOCK_GOALS: MockGoal[] = [
  { id: "g1", title: "운동 3회", current: 2, target: 3, progressLabel: "2/3" },
  { id: "g2", title: "독서 100페이지", current: 60, target: 100, progressLabel: "60/100" },
  { id: "g3", title: "개인 프로젝트", current: 40, target: 100, progressLabel: "40%" },
];

export interface MockTimelineEntry {
  id: string;
  when: string;
  text: string;
}

export const MOCK_TIMELINE: MockTimelineEntry[] = [
  { id: "t1", when: "어제", text: "운동 목표 진행" },
  { id: "t2", when: "3일 전", text: "새로운 목표 생성" },
  { id: "t3", when: "지난주", text: "일정 모듈 사용 시작" },
];
