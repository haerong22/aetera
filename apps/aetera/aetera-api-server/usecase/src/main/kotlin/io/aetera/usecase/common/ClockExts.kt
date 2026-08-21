package io.aetera.usecase.common

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 서버가 보는 "오늘".
 *
 * 사용자의 타임존을 아직 쓰지 않으므로 UTC 기준이다 — 한국 시각 자정 직후 9시간 동안은
 * 서버의 오늘이 하루 이르다. 그 창을 없애려면 사용자 타임존을 여기 한 곳에 반영하면 된다.
 * 계산이 여러 서비스에 흩어져 있으면 그때 빠뜨리는 곳이 생긴다.
 */
fun Clock.today(): LocalDate = LocalDate.ofInstant(instant(), ZoneOffset.UTC)
