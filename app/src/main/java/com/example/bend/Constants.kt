package com.example.bend

import java.util.UUID

object Constants {

    const val NAVIGATION_LOGIN_PAGE = "loginPage"
    const val NAVIGATION_REGISTER_PAGE = "registerPage"
    const val NAVIGATION_FORGOT_PASS_PAGE = "forgotPassPage"
    const val NAVIGATION_HOME_PAGE = "homePage"
    const val NAVIGATION_PROFILE_PAGE = "profilePage"
    const val NAVIGATION_SEARCH_PAGE = "searchPage"
    const val NAVIGATION_CREATE_EVENT_PAGE = "createEventPage"

    const val NAVIGATION_MAIN_PAGE = "mainPage"
    const val NAVIGATION_REGULAR_USERS_LIST = "regularUsersList"
    const val NAVIGATION_REGULAR_USER_CREATE = "regularUserCreate"
    const val NAVIGATION_REGULAR_USERS_DETAIL = "regularUsersDetail/{regularUserUUID}"
    const val NAVIGATION_REGULAR_USERS_EDIT = "regularUsersEdit/{regularUserUUID}"
    const val NAVIGATION_REGULAR_UUID_ARGUMENT = "regularUserUUID"

    fun userDetailNavigation(userUUID : UUID) = "regularUsersDetail/$userUUID"
    fun userEditNavigation(userUUID : UUID) = "regularUsersEdit/$userUUID"


    const val RegularUserTable = "regular_users"
    const val ArtistTable = "artist"
    const val EventArtistTable = "event_artist"
    const val EventTable = "event"
    const val EventOrganizerTable = "event_founder"
}