package com.example.bend

import java.util.UUID

object Constants {

    const val NAVIGATION_LOGIN_PAGE = "loginPage"
    const val NAVIGATION_REGISTER_PAGE = "registerPage"
    const val NAVIGATION_FORGOT_PASS_PAGE = "forgotPassPage"
    const val NAVIGATION_HOME_PAGE = "homePage"
    const val NAVIGATION_PROFILE_PAGE = "profilePage/{userUUID}"
    const val NAVIGATION_SEARCH_PAGE = "searchPage"
    const val NAVIGATION_CREATE_EVENT_PAGE = "createEventPage"

    const val NAVIGATION_USER_UUID_ARGUMENT = "userUUID"


    fun userProfileNavigation(userUUID : String) = "profilePage/$userUUID"

}