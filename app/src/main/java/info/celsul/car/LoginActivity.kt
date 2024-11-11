package info.celsul.car

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import info.celsul.car.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
//    private lateinit var oneTapClient: SignInClient
//    private lateinit var signInRequest: BeginSignInRequest
    private val RC_SIGN_IN = 100
    private lateinit var binding: ActivityLoginBinding
    private var loadingDialog: Dialog? = null
    private lateinit var auth: FirebaseAuth
//    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupGoogleLogin()
        setupView()
        verifyLoggedUser()
    }

    private fun verifyLoggedUser() {
        if (auth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun setupGoogleLogin() {
        auth = FirebaseAuth.getInstance()
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onCredentialCompleteListener(task, "Google")
            }
    }

    private fun onCredentialCompleteListener(
        task: Task<AuthResult>,
        loginType: String
    ) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            Log.d("LoginActivity", "LoginType: $loginType User: ${user?.uid}")
            navigateToMainActivity()
        } else {
            Toast.makeText(
                this,
                "${task.exception?.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    private fun setupView() {
        binding.btnSendSms.setOnClickListener {
            sendVerificationCode()
        }
        binding.btnVerifySms.setOnClickListener {
            verifyCode()
        }
    }
    private fun showLoadingDialog() {
        val view = layoutInflater.inflate(R.layout.loading_item, null)
        loadingDialog = AlertDialog.Builder(this)
            .setView(view)
            .setCancelable(false)
            .create()

        loadingDialog?.show()
    }
    private fun hideLoadingDialog() {
        loadingDialog?.dismiss()
    }

    private fun sendVerificationCode() {
        showLoadingDialog()
        val phoneNumber = binding.cellphone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    hideLoadingDialog()
                    Toast.makeText(
                        this@LoginActivity,
                        "${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@LoginActivity.verificationId = verificationId
                    Toast.makeText(
                        this@LoginActivity,
                        "Código de verificação enviado",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnVerifySms.visibility = View.VISIBLE
                    binding.veryfyCode.visibility = View.VISIBLE
                    hideLoadingDialog()
                }

            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode() {
        showLoadingDialog()
        val verificationCode = binding.veryfyCode.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                hideLoadingDialog()
                onCredentialCompleteListener(task, "Phone Number")
            }
    }

    companion object {

        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

//    private fun signIn() {
//        googleSignInLauncher.launch(googleSignInClient.signInIntent)
//    }
}
