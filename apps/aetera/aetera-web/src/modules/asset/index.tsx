import { PiggyBank } from "lucide-react";
import type { FrontendModule } from "../types";
import { AssetPage } from "./AssetPage";
import { CashOnHand } from "./CashOnHand";
import { ASSET_MODULE_ID } from "./id";

export const assetModule: FrontendModule = {
  id: ASSET_MODULE_ID,
  title: "자산",
  icon: PiggyBank,
  Page: AssetPage,
  queryKeyPrefix: ASSET_MODULE_ID,
  // 당장 쓸 수 있는 돈을 묻는 모듈이 있으면 여기서 답한다(퇴사 준비의 "버틸 개월 수").
  capabilities: { CashOnHand },
};
