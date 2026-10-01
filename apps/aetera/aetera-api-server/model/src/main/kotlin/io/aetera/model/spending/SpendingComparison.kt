package io.aetera.model.spending

/**
 * 어느 달이 평소와 견줘 어떤가.
 *
 * **이 판단을 화면에 두지 않는다.** 타임라인(서버가 글을 짓는다)과 목록(브라우저가 그린다)이
 * 같은 데이터를 보는데, 문턱값을 양쪽에 두면 한쪽만 고쳐져 **타임라인은 "많음"이라 하고
 * 목록은 "비슷"이라 하는 달**이 생긴다. 그때 사용자는 둘 중 뭘 믿을지 모른다.
 *
 * 소득의 `continuesAfterLeaving`, 자산의 `signedAmount` 와 같은 자리다 — 규칙은 여기서
 * 정하고, 말과 색은 보여 주는 쪽이 정한다.
 */
enum class SpendingComparison {
    MORE,
    LESS,
    SIMILAR,
    ;

    companion object {
        /**
         * 이 아래 차이는 [SIMILAR] 로 본다.
         *
         * 몇천 원 차이에 "많음"을 붙이면 거의 매달 그 말이 붙어 아무 뜻이 없어지고,
         * 그러면 정말 많이 쓴 달이 눈에 들어오지 않는다.
         */
        const val NOISE_FLOOR: Long = 30_000

        fun of(difference: Long): SpendingComparison = when {
            difference > NOISE_FLOOR -> MORE
            difference < -NOISE_FLOOR -> LESS
            else -> SIMILAR
        }
    }
}
