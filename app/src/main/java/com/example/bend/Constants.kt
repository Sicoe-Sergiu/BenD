package com.example.bend

object Constants {

    const val NAVIGATION_MY_EVENTS = "myEvents"
    const val NAVIGATION_LOGIN_PAGE = "loginPage"
    const val NAVIGATION_REGISTER_PAGE = "registerPage"
    const val NAVIGATION_SET_PROFILE_PHOTO_PAGE = "setProfilePhoto"
    const val NAVIGATION_FORGOT_PASS_PAGE = "forgotPassPage"
    const val NAVIGATION_HOME_PAGE = "homePage"
    const val NAVIGATION_SEARCH_PAGE = "searchPage"
    const val NAVIGATION_CREATE_EVENT_PAGE = "createEventPage"
    const val NAVIGATION_NOTIFICATIONS_PAGE = "notificationsPage"

    const val NAVIGATION_PROFILE_PAGE = "profilePage/{userUUID}"
    const val NAVIGATION_SINGLE_EVENT_PAGE = "singleEventPage/{eventUUID}"
    const val NAVIGATION_EDIT_EVENT_PAGE = "editEventPage/{eventUUID}"
    const val NAVIGATION_EDIT_USER_PAGE = "editUserPage/{userUUID}"
    const val NAVIGATION_ADD_REVIEW_PAGE = "addReviewPage/{eventUUID}"
    const val NAVIGATION_FOUNDER_REVIEWS_PAGE = "founderReviewsPage/{userUUID}"

    const val NAVIGATION_USER_UUID_ARGUMENT = "userUUID"
    const val NAVIGATION_EVENT_UUID_ARGUMENT = "eventUUID"


    fun userProfileNavigation(userUUID : String) = "profilePage/$userUUID"
    fun singleEventNavigation(eventUUID : String) = "singleEventPage/$eventUUID"
    fun editEventNavigation(eventUUID : String) = "editEventPage/$eventUUID"
    fun editUserNavigation(userUUID : String) = "editUserPage/$userUUID"
    fun addReviewNavigation(eventUUID : String) = "addReviewPage/$eventUUID"
    fun founderReviewNavigation(userUUID : String) = "founderReviewsPage/$userUUID"

    const val DEFAULT_PROFILE_PHOTO_URL = "https://firebasestorage.googleapis.com/v0/b/bend-c82c1.appspot.com/o/default_profile_photo%2Fdef_profile_photo.jpg?alt=media&token=5b7c625e-d504-4be2-b743-3b6184c0b07e"

    const val NEW_FOLLOWER = "started following you."
    const val FOLLOWED_USER_ATTEND = "will attend an event."

    const val NEW_EVENT = "added a new Event."
    const val EDITED_EVENT = "has modified an Event you are attending."
    const val EDITED_EVENT_FOR_ARTISTS = "has modified an Event you are performing at."
    const val DELETED_EVENT = "has canceled an Event you wanted to attend."
    const val DELETED_EVENT_FOR_ARTISTS = "has canceled an Event you were performing at."

    const val ARTIST_PERFORM = "will perform at a new Event."
    const val ARTIST_NO_MORE_PERFORM = "will no longer perform at this Event."
    const val ARTIST_NO_MORE_PERFORM_TO_ORGANIZER_EVENT = "will no longer perform at your Event."
    const val ARTIST_ADDED_TO_NEW_EVENT = "added you to an Event."
    const val ARTIST_REMOVED_FROM_EVENT = "removed you from an Event."


}