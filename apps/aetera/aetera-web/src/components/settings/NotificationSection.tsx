"use client";

import { Card } from "@/components/ui/Card";
import { Select } from "@/components/ui/Select";
import { Switch } from "@/components/ui/Switch";
import { Spinner } from "@/components/ui/Spinner";
import {
  useChangeNotificationPreference,
  useNotificationPreference,
} from "@/lib/notifications";

/** 0~23시. 자정은 "0시"보다 "밤 12시"가 읽기 쉽다. */
const HOURS = Array.from({ length: 24 }, (_, hour) => ({
  value: String(hour),
  label: hour === 0 ? "밤 12시" : hour < 12 ? `오전 ${hour}시` : hour === 12 ? "낮 12시" : `오후 ${hour - 12}시`,
}));

/**
 * 알림 설정.
 *
 * 시각은 **내 시간대 기준**이다. 서버 잡이 매시 돌면서 각자에게 몇 시인지 보고 고르므로,
 * 어디에 있든 자기 아침에 받는다.
 *
 * 무엇이 오는지 함께 적는다. "알림"만 켜 두면 매일 뭔가 올 것 같은데, 실제로는
 * **보낼 게 있는 날에만** 간다 — 그 사실을 모르면 조용한 날마다 고장을 의심하게 된다.
 */
export function NotificationSection() {
  const { data: preference, isPending, isError } = useNotificationPreference();
  const change = useChangeNotificationPreference();

  return (
    <section className="flex flex-col gap-3">
      <div>
        <h2 className="text-[17px] font-bold text-grey-900">알림</h2>
        <p className="mt-1 text-[14px] text-grey-500">
          만기가 다가오면 메일로 알려드려요. 알릴 것이 있는 날에만 보내요.
        </p>
      </div>

      <Card>
        {isPending ? (
          <div className="flex justify-center py-4">
            <Spinner />
          </div>
        ) : isError || !preference ? (
          <p role="alert" className="text-[13px] text-danger">
            알림 설정을 불러오지 못했어요.
          </p>
        ) : (
          <div className="flex flex-col gap-4">
            <div className="flex items-center justify-between gap-3">
              <span className="text-[15px] font-semibold text-grey-900">메일로 받기</span>
              <Switch
                checked={preference.enabled}
                disabled={change.isPending}
                label="알림 메일 받기"
                onChange={(enabled) => change.mutate({ ...preference, enabled })}
              />
            </div>

            {/* 끈 사람에게 시각을 묻지 않는다 — 고를 수 있으면 뭔가 오는 줄 안다. */}
            {preference.enabled && (
              <Select
                label="받을 시각"
                value={String(preference.sendHour)}
                options={HOURS}
                disabled={change.isPending}
                onChange={(event) =>
                  change.mutate({ ...preference, sendHour: Number(event.target.value) })
                }
              />
            )}

            {change.isError && (
              <p role="alert" className="text-[13px] text-danger">
                바꾸지 못했어요. 잠시 후 다시 시도해 주세요.
              </p>
            )}
          </div>
        )}
      </Card>
    </section>
  );
}
