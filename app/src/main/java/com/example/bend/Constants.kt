package com.example.bend

import com.example.bend.model.RegularUser
import java.util.UUID

object Constants {
    val regularUserDetailPlaceOrder = RegularUser(
        uuid = UUID(0L, 0L),
        username = "Cannot find User details",
        first_name = "",
        last_name = "",
        email = "",
        password = ""

    )

    const val NAVIGATION_MAIN_PAGE = "mainPage"
    const val NAVIGATION_REGULAR_USERS_LIST = "regularUsersList"
    const val NAVIGATION_REGULAR_USER_CREATE = "regularUserCreate"
    const val NAVIGATION_REGULAR_USERS_DETAIL = "regularUsersDetail/{regularUserUUID}"
    const val NAVIGATION_REGULAR_USERS_EDIT = "regularUsersEdit/{regularUserUUID}"
    const val NAVIGATION_REGULAR_UUID_ARGUMENT = "regularUserUUID"

    fun regularUsersList() = "regularUsersList"
    fun userDetailNavigation(userUUID : UUID) = "regularUsersDetail/$userUUID"
    fun userEditNavigation(userUUID : UUID) = "regularUsersEdit/$userUUID"

    fun List<RegularUser>?.orPlaceHolderList(): List<RegularUser> {
        fun placeHolderList(): List<RegularUser> {
            return listOf(RegularUser(
                uuid = UUID(0L, 0L),
                username = "No Regular Users Found",
                first_name = "Please create a Regular User.",
                last_name = "",
                email = "",
                password = ""
            ))
        }
        return if (!this.isNullOrEmpty()){
            this
        } else placeHolderList()
    }

    const val RegularUserTable = "regular_users"
    const val ArtistTable = "artist"
    const val EventArtistTable = "event_artist"
    const val EventTable = "event"
    const val EventOrganizerTable = "event_founder"
}