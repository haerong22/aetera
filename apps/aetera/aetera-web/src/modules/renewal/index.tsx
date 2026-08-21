import { ShieldCheck } from "lucide-react";
import type { FrontendModule } from "../types";
import { RenewalPage } from "./RenewalPage";

export const renewalModule: FrontendModule = {
  id: "renewal",
  title: "만기 관리",
  icon: ShieldCheck,
  Page: RenewalPage,
  queryKeyPrefix: "renewal",
};
