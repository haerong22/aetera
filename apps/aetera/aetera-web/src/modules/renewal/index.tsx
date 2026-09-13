import { ShieldCheck } from "lucide-react";
import type { FrontendModule } from "../types";
import { RenewalPage } from "./RenewalPage";
import { RENEWAL_MODULE_ID } from "./id";

export const renewalModule: FrontendModule = {
  id: RENEWAL_MODULE_ID,
  title: "만기 관리",
  icon: ShieldCheck,
  Page: RenewalPage,
  queryKeyPrefix: RENEWAL_MODULE_ID,
};
