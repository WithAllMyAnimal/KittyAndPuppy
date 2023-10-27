package com.kittyandpuppy.withallmyanimal.util

import android.net.Uri

// 앱 초기에 UID 값과 Mypage Profile Images를 수정하기 전까지 한 번만 가져와서 쓰기 위해 만들었다.
// 리팩토링 시 프로필 전체 진행 할 예정!
object Constants {
    var currentUserUid : String? = null
    var currentUserProfileImg : Uri? = null
}