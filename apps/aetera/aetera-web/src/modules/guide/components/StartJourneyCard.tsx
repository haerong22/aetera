"use client";

import { Flag } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { isoFromToday, nearestIsoOccurrence } from "@/lib/date";
import { AnchorDateForm } from "./AnchorDateForm";

/** 기준일을 정하기 전. 아래에 단계 미리보기가 이어지므로 여기서는 "왜 날짜부터인지"만 말한다. */
export function StartJourneyCard({
  anchorLabel,
  anchorMonthDay,
  taskCount,
  pending,
  failed,
  onStart,
}: {
  anchorLabel: string;
  /** 달력이 기준일을 정하는 가이드(연말정산의 12월 31일)라면 그 월·일. */
  anchorMonthDay?: string;
  taskCount: number;
  pending: boolean;
  failed: boolean;
  onStart: (anchorDate: string) => void;
}) {
  return (
    <Card className="flex flex-col gap-4 border-primary/20 bg-primary-light/40">
      <div className="flex items-start gap-3">
        <span className="flex size-11 shrink-0 items-center justify-center rounded-2xl bg-primary text-white">
          <Flag size={20} aria-hidden />
        </span>
        <div className="min-w-0">
          <h2 className="text-[17px] font-bold text-grey-900">{anchorLabel}을 정해주세요</h2>
          <p className="mt-1 text-[14px] leading-relaxed text-grey-600">
            날짜를 넣으면 <b className="font-semibold text-grey-800">남은 날짜</b>가 계산되고, {taskCount}개 할 일의
            체크와 메모를 저장할 수 있어요. 캘린더에 넣을 때도 알맞은 날짜가 자동으로 채워져요.
          </p>
          <p className="mt-1 text-[13px] leading-relaxed text-grey-500">
            나중에 날짜를 바꿔도 체크한 항목은 그대로 남아요.
          </p>
        </div>
      </div>

      <AnchorDateForm
        anchorLabel={anchorLabel}
        initialValue={anchorMonthDay ? nearestIsoOccurrence(anchorMonthDay) : isoFromToday(30)}
        submitLabel="시작하기"
        pending={pending}
        failed={failed}
        onSubmit={onStart}
      />
    </Card>
  );
}
