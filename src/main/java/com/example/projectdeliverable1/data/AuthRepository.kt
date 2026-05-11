package com.example.projectdeliverable1.data

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Assignment #04 - Firebase Authentication Repository
 *
 * Handles:
 *  - Email/Password sign-in and registration (F1)
 *  - Google Sign-In (F1)
 *  - Session persistence (Firebase handles this automatically)
 *  - Logout
 */
object AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Returns the currently signed-in user, or null if no session exists. */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    /** Returns true if a user is already logged in (session persists across restarts). */
    fun isLoggedIn(): Boolean = auth.currentUser != null

    /**
     * F1 - Sign in with Email and Password.
     * Returns Result.success(FirebaseUser) on success, Result.failure on error.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Sign-in failed: no user returned")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * F1 - Register a new user with Email and Password.
     * Returns Result.success(FirebaseUser) on success, Result.failure on error.
     */
    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Registration failed: no user returned")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * F1 - Sign in with Google.
     * Call this after the user has completed Google Sign-In and you have a GoogleSignInAccount.
     *
     * How to use:
     *  1. Launch GoogleSignInClient intent in your Activity/Fragment.
     *  2. In onActivityResult, get the GoogleSignInAccount from the intent.
     *  3. Pass that account here.
     *
     * Returns Result.success(FirebaseUser) on success, Result.failure on error.
     */
    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            // Exchange the Google ID token for a Firebase credential
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Google sign-in failed: no user returned")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs the current user out of Firebase Auth.
     */
    fun signOut() {
        auth.signOut()
    }
}
