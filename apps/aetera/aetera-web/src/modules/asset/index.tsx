import { PiggyBank } from "lucide-react";
import type { FrontendModule } from "../types";
import { AssetPage } from "./AssetPage";

export const assetModule: FrontendModule = {
  id: "asset",
  title: "자산",
  icon: PiggyBank,
  Page: AssetPage,
  queryKeyPrefix: "asset",
};
