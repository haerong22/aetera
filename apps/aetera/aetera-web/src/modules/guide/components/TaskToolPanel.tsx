import type { ReactNode } from "react";
import { Info } from "lucide-react";

/**
 * 할 일 아래에 펼쳐지는 도구의 겉모습.
 *
 * 상자·제목·각주는 도구의 내용이 아니라 **자리의 생김새**다. 도구마다 다시 적으면
 * 세 번째 도구에서 여백이나 글자 크기가 슬쩍 어긋나고, 그건 아무도 리뷰에서 못 잡는다.
 *
 * 각주는 선택이 아니다 — 이 자리에 붙는 도구는 대체로 추정이나 계산이라
 * "무엇을 빼고 셈했는지"를 말하지 않으면 사용자가 숫자를 그대로 믿는다.
 */
export function TaskToolPanel({
  title,
  description,
  footnote,
  children,
}: {
  title: string;
  description?: ReactNode;
  footnote: ReactNode;
  children: ReactNode;
}) {
  return (
    <div className="rounded-(--radius-card) border border-grey-200 bg-grey-50 p-4 sm:p-5">
      <h3 className="text-[15px] font-bold text-grey-900">{title}</h3>
      {description && <p className="mt-1 text-[13px] leading-relaxed text-grey-600">{description}</p>}

      {children}

      <p className="mt-4 flex items-start gap-2 text-[12px] leading-relaxed text-grey-500">
        <Info size={13} aria-hidden className="mt-0.5 shrink-0" />
        {footnote}
      </p>
    </div>
  );
}
