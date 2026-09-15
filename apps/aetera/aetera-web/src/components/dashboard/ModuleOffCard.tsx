import Link from "next/link";
import { Puzzle } from "lucide-react";
import { Card } from "@/components/ui/Card";
import { cn } from "@/components/ui/cn";

/**
 * 대시보드 카드가 기댈 모듈이 꺼져 있을 때 그 자리에 놓이는 안내.
 *
 * 네 카드가 같은 열두 줄을 각자 갖고 있다가 이미 갈리기 시작했다 — 한 곳은 아이콘에
 * `aria-hidden` 이 빠져 있었다. 문장만 다르고 나머지가 같으므로 문장만 받는다.
 *
 * 모듈 화면 전체가 403 일 때 쓰는 [ModuleDisabledNotice][@/modules/ModuleDisabledNotice] 와는
 * 다른 물건이다. 저쪽은 화면을 통째로 채우는 안내고, 이건 격자 한 칸을 메우는 카드다.
 */
export function ModuleOffCard({
  message,
  /** 옆 칸과 높이를 맞춰야 하는 자리(7/5 로 나뉜 줄)에서만 `h-full` 을 준다. */
  matchHeight = false,
}: {
  message: string;
  matchHeight?: boolean;
}) {
  return (
    <Card
      className={cn(
        "flex flex-col items-center justify-center gap-3 py-10 text-center",
        matchHeight && "h-full",
      )}
    >
      <span className="flex size-11 items-center justify-center rounded-2xl bg-primary-light text-primary">
        <Puzzle size={20} aria-hidden />
      </span>
      <p className="text-[15px] font-semibold text-grey-800">{message}</p>
      <Link href="/settings/modules" className="text-[14px] font-semibold text-primary hover:underline">
        모듈 살펴보기
      </Link>
    </Card>
  );
}
