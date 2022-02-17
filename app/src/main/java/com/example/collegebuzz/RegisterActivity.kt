package com.example.collegebuzz

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private var email: EditText? = null
    private var password: EditText? = null
    private var register: Button? = null
    private var name: EditText? = null
    private var signInButton: TextView? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val TAG = "RegisterActivity"
    private var mAuth: FirebaseAuth? = null
    private var auth: FirebaseAuth? = null
    private val RC_SIGN_IN = 1
    private var login_btn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        register = findViewById(R.id.register_button)
        name = findViewById(R.id.Name)
        auth = FirebaseAuth.getInstance()
        login_btn = findViewById(R.id.login_button)
        signInButton = findViewById(R.id.btn_google_signin)

        login_btn!!.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        register!!.setOnClickListener {
            firebaseRegistration()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mAuth = FirebaseAuth.getInstance()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        signInButton!!.setOnClickListener{signIn()}
    }

    private fun firebaseRegistration() {
        val email_txt = email!!.text.toString()
        val password_txt = password!!.text.toString()

        if (TextUtils.isEmpty(email_txt) || TextUtils.isEmpty(password_txt)) {
            Toast.makeText(this@RegisterActivity, "Empty Credentials", Toast.LENGTH_SHORT).show()
        } else if (password_txt.length < 6) {
            Toast.makeText(
                this@RegisterActivity,
                "Password must of atleast 6 characters",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            registerUser(email_txt, password_txt)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(
            this@RegisterActivity
        ) { task ->
            if (task.isSuccessful) {
                val documentReference: DocumentReference =
                    FirebaseFirestore.getInstance().collection("Users").document(
                        FirebaseAuth.getInstance()
                            .currentUser!!.uid
                    )
                val user: MutableMap<String, Any> =
                    HashMap()
                user["Name"] = name!!.text.toString()
                user["Email"] = email
                documentReference.set(user).addOnSuccessListener(OnSuccessListener<Void?> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "User Created Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG, "onSuccess")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
            }
        }.addOnFailureListener(
            this
        ) { e -> Toast.makeText(this, "" + e, Toast.LENGTH_SHORT).show() }
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
            firebaseAuth(acc)
        } catch (e: ApiException) {
            Toast.makeText(this, "Exception$e", Toast.LENGTH_SHORT).show()
            firebaseAuth(null)
        }
    }

    private fun firebaseAuth(acct: GoogleSignInAccount?) {
        val authCredential = GoogleAuthProvider.getCredential(acct!!.idToken, null)
        mAuth!!.signInWithCredential(authCredential).addOnCompleteListener(
            this
        ) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@RegisterActivity, "Successful", Toast.LENGTH_SHORT).show()
                val user = mAuth!!.currentUser
                val documentReference =
                    FirebaseFirestore.getInstance().collection("Users").document(
                        FirebaseAuth.getInstance()
                            .currentUser!!.uid
                    )
                val user1: MutableMap<String, Any?> =
                    java.util.HashMap()
                user1["Name"] = auth!!.currentUser!!.displayName
                user1["Email"] = auth!!.currentUser!!.email
                documentReference.set(user1).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(
                Intent(this, MainActivity::class.java).addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
        }
    }
}