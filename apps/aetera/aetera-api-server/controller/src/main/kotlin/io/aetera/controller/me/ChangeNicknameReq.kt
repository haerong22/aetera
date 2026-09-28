package io.aetera.controller.me

import io.swagger.v3.oas.annotations.media.Schema

data class ChangeNicknameReq(
    @field:Schema(example = "홍길동")
    val nickname: String,
)
