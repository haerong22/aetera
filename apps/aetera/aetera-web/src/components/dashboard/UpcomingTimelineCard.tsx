"use client";

import Link from "next/link";
import { ArrowRight, History, Puzzle } from "lucide-react";
import { Card, CardHeader } from "@/components/ui/Card";
import { Spinner } from "@/components/ui/Spinner";
import { daysUntil, localToday, toLocalDateIso } from "@/lib/date";
import { useTimeline } from "@/modules/timeline/api";
import { formatDay, untilLabel } from "@/modules/timeline/format";
import { TIMELINE_MODULE_ID } from "@/modules/timeline/id";
import { moduleIcon } from "@/modules/registry";
import { modulePath } from "@/modules/types";

/** 창의 길이. 만기·입사일·정산 기준일이 눈에 들어오기 시작하는 거리다. */
const WINDOW_MONTHS = 6;

/** 카드 한 장에 담을 줄 수. 더 보고 싶으면 타임라인 화면으로 간다. */
const MAX_ROWS = 5;

/**
 * 창의 끝 날짜. 말일 넘침(1월 31일 + 6개월)으로 며칠 어긋날 수 있지만
 * **창의 경계일 뿐**이라 그 며칠이 무엇을 보여줄지 바꾸지 않는다.
 */
function windowEnd(): string {
  const date = localToday();
  date.setMonth(date.getMonth() + WINDOW_MONTHS);
  return toLocalDateIso(date);
}

/**
 * 앞으로 올 이정표만 모은다.
 *
 * **지나간 것은 넣지 않는다.** 자산 모듈을 켠 사람은 달마다 순자산이 한 줄씩 쌓이는데,
 * 다섯 줄짜리 카드에 지난 기록을 넣으면 그 다섯 줄이 전부 "순자산 …원"이 되어
 * 정작 챙겨야 할 만기와 기준일을 밀어낸다. 지난 것은 타임라인 화면의 몫이다.
 *
 * 서버는 최신순으로 주지만 여기서는 **가까운 순**으로 뒤집는다 — 묻는 것이
 * "무엇을 지나왔나"가 아니라 "다음이 언제인가"이기 때문이다.
 */
export function UpcomingTimelineCard({ enabled }: { enabled: boolean }) {
  const today = toLocalDateIso(localToday());
  const { data: entries, isLoading, isError } = useTimeline(today, windowEnd(), { enabled });

  if (!enabled) {
    return (
      <Card className="flex flex-col items-center justify-center gap-3 py-10 text-center">
        <span className="flex size-11 items-center justify-center rounded-2xl bg-primary-light text-primary">
          <Puzzle size={20} aria-hidden />
        </span>
        <p className="text-[15px] font-semibold text-grey-800">
          타임라인 모듈을 켜면 다가오는 일이 여기 표시돼요
        </p>
        <Link href="/settings/modules" className="text-[14px] font-semibold text-primary hover:underline">
          모듈 살펴보기
        </Link>
      </Card>
    );
  }

  const upcoming = entries ? [...entries].reverse() : [];
  const shown = upcoming.slice(0, MAX_ROWS);
  const hidden = upcoming.length - shown.length;

  return (
    <Card>
      <CardHeader
        icon={<History size={16} />}
        // 옆 카드(오늘 할 일)와 같은 자리라 같은 뜻이어야 한다 — 여기 오는 숫자는 개수다.
        // 창의 길이는 목록이 비었을 때만 말하면 된다. 줄이 보이면 굳이 설명할 필요가 없다.
        title={isLoading || isError ? "다가오는 일" : `다가오는 일 · ${upcoming.length}`}
        action={
          <Link
            href={modulePath(TIMELINE_MODULE_ID)}
            className="flex items-center gap-0.5 text-[13px] font-semibold text-primary hover:underline"
          >
            타임라인 보기 <ArrowRight size={14} aria-hidden />
          </Link>
        }
      />

      {isLoading ? (
        <div className="flex justify-center py-8">
          <Spinner />
        </div>
      ) : isError ? (
        // 실패를 빈 목록으로 흘려보내면, 만기가 코앞인 사람에게 "예정된 일이 없다"고 말하게 된다.
        <p className="py-7 text-center text-[14px] text-grey-500">
          다가오는 일을 불러오지 못했어요.
        </p>
      ) : upcoming.length === 0 ? (
        <div className="py-7 text-center">
          <p className="text-[15px] font-medium text-grey-700">
            앞으로 {WINDOW_MONTHS}개월 안에 예정된 일이 없어요.
          </p>
          <p className="mt-1 text-[13px] text-grey-500">
            만기를 등록하거나 가이드를 시작하면 여기에 나타나요.
          </p>
        </div>
      ) : (
        <ul className="flex flex-col gap-3">
          {shown.map((entry, index) => {
            const Icon = moduleIcon(entry.moduleId);
            return (
              <li key={`${entry.moduleId}-${entry.on}-${index}`} className="flex items-center gap-3">
                <span className="flex size-8 shrink-0 items-center justify-center rounded-xl bg-primary-light text-primary">
                  <Icon size={15} aria-hidden />
                </span>

                <span className="min-w-0 flex-1">
                  <span className="block truncate text-[14.5px] font-semibold text-grey-900">
                    {entry.title}
                  </span>
                  <span className="block truncate text-[12.5px] text-grey-500">
                    {formatDay(entry.on)}
                    {entry.detail ? ` · ${entry.detail}` : ""}
                  </span>
                </span>

                <span className="shrink-0 text-[13px] font-semibold text-primary tabular-nums">
                  {untilLabel(daysUntil(entry.on))}
                </span>
              </li>
            );
          })}
        </ul>
      )}

      {/*
        잘라 낸 것을 말한다. 말하지 않으면 다섯 줄이 전부인 줄 안다.
        목록 밖에 두는 이유: 안에 넣으면 스크린리더가 "목록, 항목 6개" 라고 읽는다.
      */}
      {hidden > 0 && !isLoading && !isError && (
        <p className="mt-3 text-[12.5px] text-grey-500">
          외 {hidden}건은{" "}
          <Link href={modulePath(TIMELINE_MODULE_ID)} className="font-semibold text-primary hover:underline">
            타임라인
          </Link>
          에서 볼 수 있어요
        </p>
      )}
    </Card>
  );
}
