package com.example.collegebuzz

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*

class LoginActivity : AppCompatActivity() {

    private var email: EditText? = null
    private var password: EditText? = null
    private var login: Button? = null
    private var signInButton: TextView? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "LoginActivity"
    private var mAuth: FirebaseAuth? = null
    private var auth: FirebaseAuth? = null
    private val RC_SIGN_IN = 1
    private var register_txt: Button? = null
    private var forgot_password: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        register_txt = findViewById(R.id.register_txt)
        register_txt!!.setOnClickListener(View.OnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
        })
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        login = findViewById(R.id.login_button)
        forgot_password = findViewById(R.id.forgot_password)

        auth = FirebaseAuth.getInstance()

        login!!.setOnClickListener(View.OnClickListener {
            if (TextUtils.isEmpty(
                    email!!.getText().toString()
                ) || TextUtils.isEmpty(password!!.getText().toString())
            ) {
                Toast.makeText(this@LoginActivity, "Credentials Can't be Empty", Toast.LENGTH_SHORT)
                    .show()
            } else {
                val email_txt = email!!.getText().toString()
                val password_txt = password!!.getText().toString()
                loginUser(email_txt, password_txt)
            }
        })

        signInButton = findViewById(R.id.sign_in_button)
        mAuth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton!!.setOnClickListener(View.OnClickListener { signIn() })

        forgot_password!!.setOnClickListener(View.OnClickListener { v ->
            val resetMail = EditText(v.context)
            val passwordResetDialog = AlertDialog.Builder(v.context)
            passwordResetDialog.setTitle("Reset Password ?")
            passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link.")
            passwordResetDialog.setView(resetMail)
            passwordResetDialog.setPositiveButton(
                "Yes"
            ) { dialog, which -> // extract the email and send reset link
                val mail = resetMail.text.toString()
                auth!!.sendPasswordResetEmail(mail).addOnSuccessListener {
                    Toast.makeText(
                        this@LoginActivity,
                        "Reset Link Sent To Your Email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }.addOnFailureListener { e ->
                    Toast.makeText(
                        this@LoginActivity,
                        "Error ! Reset Link is Not Sent " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            passwordResetDialog.setNegativeButton(
                "No"
            ) { dialog, which ->
                // close the dialog
            }
            passwordResetDialog.create().show()
        })
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val acc = completedTask.getResult(ApiException::class.java)
            Toast.makeText(this, "Signed In Successfully", Toast.LENGTH_SHORT).show()
            FirebaseGoogleAuth(acc)
        } catch (e: ApiException) {
            Toast.makeText(this, "Exception$e", Toast.LENGTH_SHORT).show()
            //            FirebaseGoogleAuth(null);
        }
    }

    private fun FirebaseGoogleAuth(acct: GoogleSignInAccount?) {
        val authCredential = GoogleAuthProvider.getCredential(acct!!.idToken, null)
        mAuth!!.signInWithCredential(authCredential).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@LoginActivity, "Successful", Toast.LENGTH_SHORT).show()
                val user = mAuth!!.currentUser
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()
            } else {
                Toast.makeText(this@LoginActivity, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth!!.signInWithEmailAndPassword(email, password).addOnSuccessListener {
            Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
            finish()
        }
    }
}


