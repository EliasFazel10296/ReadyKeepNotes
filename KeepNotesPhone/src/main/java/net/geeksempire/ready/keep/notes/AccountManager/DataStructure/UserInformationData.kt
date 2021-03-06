/*
 * Copyright © 2021 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 4/12/21 8:50 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.ready.keep.notes.AccountManager.DataStructure

import androidx.annotation.Keep
import com.google.firebase.firestore.FieldValue

@Keep
object UserInformationDataStructure {
    const val userIdentification = "userIdentification"
    const val userEmailAddress = "userEmailAddress"
    const val userDisplayName = "userDisplayName"
    const val userProfileImage = "userProfileImage"
    const val instagramAccount = "instagramAccount"
    const val twitterAccount = "twitterAccount"
    const val phoneNumber = "phoneNumber"
    const val phoneNumberVerified = "phoneNumberVerified"
    const val userLatitude = "userLatitude"
    const val userLongitude = "userLongitude"
    const val userState = "userState"
    const val userLastSignIn = "userLastSignIn"
    const val userJointDate = "userJointDate"
}

@Keep
data class UserInformationProfileData (var privacyAgreement: Boolean? = false,
                                       var userIdentification: String, var userEmailAddress: String, var userDisplayName: String, var userProfileImage: String,
                                       var instagramAccount: String?,
                                       var twitterAccount: String?,
                                       var phoneNumber: String?,
                                       var phoneNumberVerified: Boolean? = false,
                                       var userJointDate:  FieldValue = FieldValue.serverTimestamp(),
                                       var isBetaUser: Boolean = false)
