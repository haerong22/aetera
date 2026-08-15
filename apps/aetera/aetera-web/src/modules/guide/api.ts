"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiFetch } from "@/lib/api-client";

/**
 * 백엔드 `GuideViewDto` 와 1:1. 날짜는 전부 `YYYY-MM-DD`(LocalDate) 문자열이다.
 *
 * 비어 있을 수 있는 값은 전부 **옵셔널(`?`)로 적는다**. 서버가
 * `spring.jackson.default-property-inclusion: non_null` 이라 null 인 필드는 응답에서 아예 빠지기 때문이다 —
 * `| null` 로 적어 두면 타입은 통과하는데 `x !== null` 이 항상 참이 되어 조용히 잘못 동작한다.
 */
export interface GuideLink {
  label: string;
  url: string;
}

export interface GuideTask {
  key: string;
  title: string;
  description: string;
  dueOffsetDays: number;
  /** 여정을 시작해야 정해진다. 시작 전에는 없다. */
  dueDate?: string;
  required: boolean;
  link?: GuideLink;
  done: boolean;
  /** 서버 응답에서는 없거나 문자열. 낙관적 반영은 "지움"을 null 로 표현한다. */
  note?: string | null;
}

export interface GuidePhase {
  key: string;
  title: string;
  summary: string;
  tasks: GuideTask[];
}

export interface GuideJourney {
  anchorDate: string;
}

export interface GuideProgress {
  total: number;
  done: number;
  requiredTotal: number;
  requiredDone: number;
}

export interface GuideView {
  guideId: string;
  title: string;
  summary: string;
  anchorLabel: string;
  disclaimer: string;
  /** 없으면 아직 시작 전 — 오류가 아니라 정상 상태다. */
  journey?: GuideJourney;
  phases: GuidePhase[];
  progress: GuideProgress;
}

const basePath = (guideId: string) => `/api/v1/modules/${guideId}/guide`;

/**
 * 캐시 키는 모듈 아이디로 시작한다 — 모듈 정의의 `queryKeyPrefix` 와 같은 값이라
 * 모듈을 껐다 켜면 코어가 이 가이드의 캐시만 정확히 비운다(다른 가이드는 건드리지 않는다).
 */
const guideKey = (guideId: string) => [guideId, "guide"] as const;

export function useGuide(guideId: string) {
  return useQuery({
    queryKey: guideKey(guideId),
    queryFn: () => apiFetch<GuideView>(basePath(guideId)),
  });
}

/**
 * 변경 API 는 전부 바뀐 화면 전체를 돌려준다. 응답을 그대로 캐시에 넣으면
 * 진행률·완료 개수 같은 파생값을 프론트가 따로 계산하지 않아도 된다.
 */
function useGuideMutation<TVariables>(
  guideId: string,
  mutationFn: (variables: TVariables) => Promise<GuideView>,
) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn,
    onSuccess: (view) => queryClient.setQueryData(guideKey(guideId), view),
    onError: () => void queryClient.invalidateQueries({ queryKey: guideKey(guideId) }),
  });
}

/** 여정 시작과 기준일 변경은 사용자에게 같은 행동이라 한 훅이다. */
export function useSetAnchorDate(guideId: string) {
  return useGuideMutation(guideId, (anchorDate: string) =>
    apiFetch<GuideView>(`${basePath(guideId)}/journey`, {
      method: "PUT",
      body: JSON.stringify({ anchorDate }),
    }),
  );
}

export function useResetJourney(guideId: string) {
  return useGuideMutation(guideId, () =>
    apiFetch<GuideView>(`${basePath(guideId)}/journey`, { method: "DELETE" }),
  );
}

export interface TaskPatch {
  done: boolean;
  note: string | null;
}

/**
 * 체크와 메모 저장. 체크박스는 눌린 즉시 반응해야 하므로 응답을 기다리지 않고 먼저 반영한다.
 * 실패하면 눌리기 전 상태로 정확히 되돌린다 — 성공한 척 남겨 두면 사용자가 저장됐다고 믿는다.
 */
export function useUpdateTask(guideId: string) {
  const queryClient = useQueryClient();
  const key = guideKey(guideId);

  return useMutation({
    mutationFn: ({ taskKey, patch }: { taskKey: string; patch: TaskPatch }) =>
      apiFetch<GuideView>(`${basePath(guideId)}/tasks/${taskKey}`, {
        method: "PUT",
        body: JSON.stringify(patch),
      }),
    onMutate: async ({ taskKey, patch }) => {
      // 진행 중인 조회가 나중에 도착해 낙관적 반영을 덮어쓰는 것을 막는다.
      await queryClient.cancelQueries({ queryKey: key });
      const previous = queryClient.getQueryData<GuideView>(key);
      if (previous) {
        queryClient.setQueryData(key, applyTaskPatch(previous, taskKey, patch));
      }
      return { previous };
    },
    onError: (_error, _variables, context) => {
      if (context?.previous) queryClient.setQueryData(key, context.previous);
      // 실패가 곧 "서버가 안 바뀌었다"는 뜻은 아니다 — 서버는 처리했는데 응답만 못 받았을 수
      // 있다(시간 초과). 되돌린 화면이 진짜 상태와 어긋날 수 있으므로 서버에 다시 물어본다.
      void queryClient.invalidateQueries({ queryKey: key });
    },
    onSuccess: (view) => queryClient.setQueryData(key, view),
  });
}

/**
 * 낙관적 반영. 항목 하나를 바꾸면 진행률도 함께 움직이므로 여기서 다시 센다 —
 * 체크만 바꾸고 진행률을 그대로 두면 서버 응답이 올 때 숫자가 튄다.
 */
function applyTaskPatch(view: GuideView, taskKey: string, patch: TaskPatch): GuideView {
  const phases = view.phases.map((phase) => ({
    ...phase,
    tasks: phase.tasks.map((task) => (task.key === taskKey ? { ...task, ...patch } : task)),
  }));
  const tasks = phases.flatMap((phase) => phase.tasks);
  const doneTasks = tasks.filter((task) => task.done);

  return {
    ...view,
    phases,
    progress: {
      total: tasks.length,
      done: doneTasks.length,
      requiredTotal: tasks.filter((task) => task.required).length,
      requiredDone: doneTasks.filter((task) => task.required).length,
    },
  };
}
