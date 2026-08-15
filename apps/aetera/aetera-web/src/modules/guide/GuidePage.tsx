"use client";

import { useState } from "react";
import { Info } from "lucide-react";
import { PageSpinner } from "@/components/ui/Spinner";
import { ErrorState } from "@/components/ui/ErrorState";
import { ModuleDisabledNotice, isModuleDisabled } from "../ModuleDisabledNotice";
import { useGuide, useResetJourney, useSetAnchorDate, useUpdateTask, type TaskPatch } from "./api";
import { JourneyHeader } from "./components/JourneyHeader";
import { PhaseSection } from "./components/PhaseSection";
import { StartJourneyCard } from "./components/StartJourneyCard";

/**
 * 가이드형 모듈의 공용 화면.
 *
 * 퇴사 준비·결혼 준비·이사가 전부 이 화면을 쓴다 — 화면이 아는 것은 "단계와 할 일과 기준일"이라는
 * 형태뿐이고, 내용은 서버가 내려주는 콘텐츠에서 온다. 그래서 가이드를 하나 더 붙일 때
 * 프론트엔드에서 새로 만드는 파일은 모듈 정의 하나뿐이다.
 */
export function GuidePage({ moduleTitle, guideId }: { moduleTitle: string; guideId: string }) {
  const { data: guide, error, refetch } = useGuide(guideId);
  const setAnchorDate = useSetAnchorDate(guideId);
  const resetJourney = useResetJourney(guideId);
  const updateTask = useUpdateTask(guideId);

  /**
   * 저장에 실패한 항목들. 뮤테이션 하나를 27개 항목이 공유하므로 `updateTask.isError` 만 보면
   * 다음 저장이 성공하는 순간 앞선 실패가 지워진다 — 저장되지 않은 항목이 저장된 얼굴을 하게 된다.
   * 그래서 실패를 항목별로 따로 기억한다.
   */
  const [failedTaskKeys, setFailedTaskKeys] = useState<ReadonlySet<string>>(new Set());

  function markTaskFailed(taskKey: string, failed: boolean) {
    setFailedTaskKeys((previous) => {
      if (previous.has(taskKey) === failed) return previous;
      const next = new Set(previous);
      if (failed) next.add(taskKey);
      else next.delete(taskKey);
      return next;
    });
  }

  /**
   * 실패 표시는 **여정에 속한다.** 여정을 버리면 표시도 함께 버려야 한다 —
   * "다시 눌러 주세요"라고 안내해 놓고 여정이 없어 체크박스가 잠긴 화면을 주면,
   * 사용자는 새로고침 말고는 그 표시를 지울 방법이 없다.
   */
  function clearFailedTasks() {
    setFailedTaskKeys((previous) => (previous.size === 0 ? previous : new Set()));
  }

  function changeTask(taskKey: string, patch: TaskPatch) {
    // 다시 누른 순간 지난 실패 표시는 지운다. 실패하면 아래에서 다시 붙는다.
    markTaskFailed(taskKey, false);
    updateTask.mutate({ taskKey, patch }, { onError: () => markTaskFailed(taskKey, true) });
  }

  if (error && isModuleDisabled(error)) return <ModuleDisabledNotice title={moduleTitle} />;

  // 한 번도 못 받았을 때만 화면을 오류로 덮는다. 이미 받아 둔 내용이 있는데 재조회가 잠깐
  // 실패했다고 멀쩡히 보고 있던 체크리스트를 통째로 에러 카드로 바꾸면 안 된다.
  // (지금은 TanStack 이 그 경우 error 를 올리지 않아 여기까지 오지도 않지만, 그건 라이브러리의
  //  사정이지 우리가 기대야 할 규칙은 아니다.)
  if (!guide) return error ? <ErrorState onRetry={() => void refetch()} /> : <PageSpinner />;

  // 서버가 null 필드를 빼고 보내므로 `!== null` 로는 판별되지 않는다.
  const started = Boolean(guide.journey);

  return (
    <div className="flex max-w-3xl flex-col gap-5">
      <div>
        <h1 className="text-2xl font-bold text-grey-900">{guide.title}</h1>
        <p className="mt-1 text-[15px] text-grey-500">{guide.summary}</p>
      </div>

      {guide.journey ? (
        <JourneyHeader
          anchorLabel={guide.anchorLabel}
          journey={guide.journey}
          progress={guide.progress}
          changePending={setAnchorDate.isPending}
          changeFailed={setAnchorDate.isError}
          resetPending={resetJourney.isPending}
          resetFailed={resetJourney.isError}
          onChangeAnchorDate={(anchorDate, onDone) => setAnchorDate.mutate(anchorDate, { onSuccess: onDone })}
          onReset={() => resetJourney.mutate(undefined, { onSuccess: clearFailedTasks })}
        />
      ) : (
        <StartJourneyCard
          anchorLabel={guide.anchorLabel}
          taskCount={guide.progress.total}
          pending={setAnchorDate.isPending}
          failed={setAnchorDate.isError}
          onStart={(anchorDate) => setAnchorDate.mutate(anchorDate)}
        />
      )}

      <div className="flex flex-col gap-4">
        {guide.phases.map((phase, index) => (
          <PhaseSection
            key={phase.key}
            phase={phase}
            order={index + 1}
            started={started}
            failedTaskKeys={failedTaskKeys}
            onChangeTask={changeTask}
          />
        ))}
      </div>

      <p className="flex items-start gap-2 rounded-(--radius-card) bg-grey-100 px-4 py-3 text-[13px] leading-relaxed text-grey-500">
        <Info size={15} aria-hidden className="mt-0.5 shrink-0" />
        {guide.disclaimer}
      </p>
    </div>
  );
}
