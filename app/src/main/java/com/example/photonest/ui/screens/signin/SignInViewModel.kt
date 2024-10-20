import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInViewModel : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState

    private var hasInteractedWithEmail = false
    private var hasInteractedWithPassword = false

    fun updateEmail(email: String) {
        hasInteractedWithEmail = true
        _uiState.update { it.copy(email = email) }
        validateInput()
    }

    fun updatePassword(password: String) {
        hasInteractedWithPassword = true
        _uiState.update { it.copy(password = password) }
        validateInput()
    }

    fun signIn() {
        val currentState = _uiState.value

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                auth.signInWithEmailAndPassword(currentState.email, currentState.password).await()

                _uiState.update { it.copy(isLoading = false, isSignInSuccessful = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred during sign in",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateInput() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val isEmailValid = if (hasInteractedWithEmail) android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() else true
        val isPasswordValid = if (hasInteractedWithPassword) password.isNotEmpty() else true

        _uiState.update {
            it.copy(
                isInputValid = isEmailValid && isPasswordValid,
                emailError = if (hasInteractedWithEmail && !isEmailValid) "Invalid email address" else null,
                passwordError = if (hasInteractedWithPassword && !isPasswordValid) "Password cannot be empty" else null
            )
        }
    }
}

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val showErrorDialog: Boolean = false
)