package com.example.whatsappclone

import android.content.ComponentCallbacks
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER="phoneNumber"
class OtpActivity : AppCompatActivity() {
    lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String?=null
    var mVerificationId:String?=null
    var mResendToken:PhoneAuthProvider.ForceResendingToken?=null
    private var mCounterDown: CountDownTimer? = null
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        auth = FirebaseAuth.getInstance()
        initViews()

        startVerify()

    }

    private fun startVerify() {
        startPhoneNumberVerification(phoneNumber!!)
        showTimer(60000)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun showTimer(milliSecInFuture: Long) {

        resendBtn.isEnabled=false
     mCounterDown  = object:CountDownTimer(milliSecInFuture,1000){

            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible=true
                counterTv.text=getString(R.string.second_remaining,millisUntilFinished/1000)

            }

            override fun onFinish() {

                counterTv.isVisible=false
                resendBtn.isEnabled=true

            }


        }.start()



    }

    override fun onDestroy() {
        super.onDestroy()

        if(mCounterDown != null){
            mCounterDown!!.cancel()
        }

    }


    private fun initViews() {
        phoneNumber=intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text="Verify $phoneNumber"
        setSpannableString()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                val smsCode=credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    sentcodeEt.setText(smsCode)
                }



               // signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.


                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.


                // Save verification ID and resending token so we can use them later
               mVerificationId = verificationId
                mResendToken = token
            }
        }

    }

    private fun setSpannableString() {

        val span=SpannableString(getString(R.string.waiting_text,phoneNumber))
        val clickableSpan= object : ClickableSpan() {


            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText=false
                ds.color=ds.linkColor

            }

            override fun onClick(widget: View) {

                showLoginActivity()


            }
        }

        span.setSpan(clickableSpan,span.length-13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod=LinkMovementMethod.getInstance()
        waitingTv.text=span


    }

    private fun showLoginActivity() {
        startActivity(Intent(this,LoginActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))

    }


    override fun onBackPressed() {

    }


}