package com.example.bend.register_login

object Validator {
    fun validateFirstName(first_name:String):ValidationResult{
        return ValidationResult(
            (!first_name.isNullOrEmpty() && first_name.length >= 6)
        )
    }
    fun validateLastName(last_name:String):ValidationResult{
        return ValidationResult(
            (!last_name.isNullOrEmpty() && last_name.length >= 6)
        )
    }
    fun validateUsername(username:String):ValidationResult{
        return ValidationResult(
            (!username.isNullOrEmpty() && username.length >= 6)
        )
    }
    fun validatePassword(password:String):ValidationResult{
        return ValidationResult(
            (!password.isNullOrEmpty() && password.length >= 6)
        )
    }
    fun validateEmail(email:String):ValidationResult{
        return ValidationResult(
            (!email.isNullOrEmpty() && email.length >= 6)
        )
    }
    fun validateStageName(stage_name:String):ValidationResult{
        return ValidationResult(
            (!stage_name.isNullOrEmpty() && stage_name.length >= 6)
        )
    }

    fun validatePhone(phone:String):ValidationResult{
        return ValidationResult(
            (!phone.isNullOrEmpty() && phone.length >= 6)
        )
    }

}

data class ValidationResult(
    val status:Boolean = false
)