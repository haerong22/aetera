import { DoorOpen } from "lucide-react";
import { guideModule } from "../guide/module";
import { SeveranceTaxTool } from "./SeveranceTaxTool";

export const resignationModule = guideModule({
  id: "resignation",
  title: "퇴사 준비",
  icon: DoorOpen,
  // "IRP 계좌 확인하기" 항목 아래에 퇴직급여 세금 계산기를 접어서 붙인다.
  // 공용 가이드 화면에 넣으면 이사 준비 가이드까지 이 계산기를 짊어지므로, 붙일 자리를 여기서 정한다.
  taskTools: { "irp-account": SeveranceTaxTool },
});
