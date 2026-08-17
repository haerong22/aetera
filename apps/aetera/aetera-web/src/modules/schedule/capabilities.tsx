"use client";

import { useMemo } from "react";
import { fromLocalDateIso } from "@/lib/date";
import type { CalendarDraftProps, ModuleCapabilities } from "../types";
import { EventDialog } from "./components/EventDialog";

function AddEventDialog({ open, onClose, draft }: CalendarDraftProps) {
  const initialDate = useMemo(() => fromLocalDateIso(draft.date), [draft.date]);
  const initial = useMemo(
    () => ({ title: draft.title, description: draft.description, allDay: true }),
    [draft.title, draft.description],
  );

  return <EventDialog open={open} onClose={onClose} initialDate={initialDate} initial={initial} />;
}

export const scheduleCapabilities: ModuleCapabilities = { AddEventDialog };
