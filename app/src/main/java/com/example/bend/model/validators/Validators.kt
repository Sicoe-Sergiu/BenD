package com.example.bend.model.validators

import android.net.Uri
import com.example.bend.model.Artist
import java.time.LocalDate
import java.time.LocalTime

object RegisterLoginValidator {
    fun validatePhoto(uri: Uri): ValidationResult {
        if (uri.toString().isEmpty()) {
            return ValidationResult(status = false, message = "Photo is required.")
        }
        return ValidationResult(status = true)
    }

    fun validateFirstName(firstName: String): ValidationResult {
//        if (firstName.isEmpty()) {
//            return ValidationResult(status = false, message = "First name is required.")
//        }
//        if (firstName.length < 4) {
//            return ValidationResult(status = false, message = "First name must be at least 4 characters long.")
//        }
        return ValidationResult(status = true)
    }

    fun validateLastName(lastName: String): ValidationResult {
//        if (lastName.isEmpty()) {
//            return ValidationResult(status = false, message = "Last name is required.")
//        }
//        if (lastName.length < 4) {
//            return ValidationResult(status = false, message = "Last name must be at least 4 characters long.")
//        }
        return ValidationResult(status = true)
    }

    fun validateUsername(username: String): ValidationResult {
        if (username.isEmpty()) {
            return ValidationResult(status = false, message = "Username is required.")
        }
        if (username.length < 4) {
            return ValidationResult(status = false, message = "Username must be at least 4 characters long.")
        }
        return ValidationResult(status = true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isEmpty()) {
            return ValidationResult(status = false, message = "Password is required.")
        }
        if (password.length < 6) {
            return ValidationResult(status = false, message = "Password must be at least 6 characters long.")
        }
        return ValidationResult(status = true)
    }

    fun validateEmail(email: String): ValidationResult {
        if (email.isEmpty()) {
            return ValidationResult(status = false, message = "Email address is required.")
        }
        if (!email.contains("@") || email.length < 5) {
            return ValidationResult(status = false, message = "Please enter a valid email address.")
        }
        return ValidationResult(status = true)
    }

    fun validateStageName(stageName: String): ValidationResult {
        if (stageName.isEmpty()) {
            return ValidationResult(status = false, message = "Stage name is required.")
        }
        if (stageName.length < 4) {
            return ValidationResult(status = false, message = "Stage name must be at least 4 characters long.")
        }
        return ValidationResult(status = true)
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isEmpty()) {
            return ValidationResult(status = false, message = "Phone number is required.")
        }
        if (phone.length < 10) {
            return ValidationResult(status = false, message = "Phone number must be at least 10 digits long.")
        }
        return ValidationResult(status = true)
    }
}


object CreateEventValidator {
    fun validatePoster(uri: Uri): ValidationResult {
        if (uri.toString().isEmpty()) {
            return ValidationResult(status = false, message = "Poster is not provided.")
        }
        return ValidationResult(status = true)
    }

    fun validateLocation(location: String): ValidationResult {
        if (location.isEmpty()) {
            return ValidationResult(status = false, message = "Location cannot be empty.")
        }
        return ValidationResult(status = true)
    }

    fun validateEntranceFee(entranceFee: Int): ValidationResult {
        if (entranceFee < 0) {
            return ValidationResult(status = false, message = "Entrance fee cannot be negative.")
        }
        return ValidationResult(status = true)
    }

    fun validateStartDate(startDate: LocalDate): ValidationResult {
        if (startDate.toString().isEmpty()) {
            return ValidationResult(status = false, message = "Start date cannot be empty.")
        }
        return ValidationResult(status = true)
    }

    fun validateEndDate(endDate: LocalDate): ValidationResult {
        if (endDate.toString().isEmpty()) {
            return ValidationResult(status = false, message = "End date cannot be empty.")
        }
        return ValidationResult(status = true)
    }

    fun validateStartTime(startTime: LocalTime): ValidationResult {
        if (startTime.toString().isEmpty()) {
            return ValidationResult(status = false, message = "Start time cannot be empty.")
        }
        return ValidationResult(status = true)
    }

    fun validateEndTime(endTime: LocalTime): ValidationResult {
        if (endTime.toString().isEmpty()) {
            return ValidationResult(status = false, message = "End time cannot be empty.")
        }
        return ValidationResult(status = true)
    }

    fun validateArtists(artistsUsernames: List<Artist>): ValidationResult {
        if (artistsUsernames.isEmpty()) {
            return ValidationResult(status = false, message = "At least one artist must be added.")
        }
        return ValidationResult(status = true)
    }
}

data class ValidationResult(
    val status: Boolean = false,
    val message: String = ""
)