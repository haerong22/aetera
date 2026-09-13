import { Receipt } from "lucide-react";
import { guideModule } from "../guide/module";
import { YEAR_END_TAX_MODULE_ID } from "./id";

export const yearEndTaxModule = guideModule({
  id: YEAR_END_TAX_MODULE_ID,
  title: "연말정산 준비",
  icon: Receipt,
});
