package com.example.bend

object Constants {

    const val NAVIGATION_MY_EVENTS = "myEvents"
    const val NAVIGATION_LOGIN_PAGE = "loginPage"
    const val NAVIGATION_REGISTER_PAGE = "registerPage"
    const val NAVIGATION_FORGOT_PASS_PAGE = "forgotPassPage"
    const val NAVIGATION_HOME_PAGE = "homePage"
    const val NAVIGATION_SEARCH_PAGE = "searchPage"
    const val NAVIGATION_CREATE_EVENT_PAGE = "createEventPage"

    const val NAVIGATION_PROFILE_PAGE = "profilePage/{userUUID}"
    const val NAVIGATION_SINGLE_EVENT_PAGE = "singleEventPage/{eventUUID}"
    const val NAVIGATION_EDIT_EVENT_PAGE = "editEventPage/{eventUUID}"

    const val NAVIGATION_USER_UUID_ARGUMENT = "userUUID"
    const val NAVIGATION_EVENT_UUID_ARGUMENT = "eventUUID"


    fun userProfileNavigation(userUUID : String) = "profilePage/$userUUID"
    fun singleEventNavigation(eventUUID : String) = "singleEventPage/$eventUUID"
    fun editEventNavigation(eventUUID : String) = "editEventPage/$eventUUID"

}