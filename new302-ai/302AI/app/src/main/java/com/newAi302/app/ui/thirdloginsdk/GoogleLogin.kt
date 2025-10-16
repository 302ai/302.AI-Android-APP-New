package com.newAi302.app.ui.thirdloginsdk

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat.startIntentSenderForResult
import com.newAi302.app.utils.LogUtils
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.FileInputStream
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException


/**
 * author : lzh
 * e-mail : luozhanhang@adswave.com
 * time   : 2024/7/16
 * desc   :
 * version: 1.0
 */
class GoogleLogin : IXLogin {

    private val G_LOGIN_REQ = 0x0001
    private val TAG = "ceshi"
    private val GOOGLE_CLIENT_ID =
        //"683111604693-monol8mij37it7upgbj1s1ei11ng4b98.apps.googleusercontent.com"
        //"362024125499-1lftn9kfskim23pbkssovnd5im5mok1t.apps.googleusercontent.com"
        //"804653320775-6i6nk750c104iqriu7dtgrskot4hosca.apps.googleusercontent.com"
        //"804653320775-is8rp5akrvmc8oiuuuu9vd51898pvf20.apps.googleusercontent.com"
        "683111604693-79qhvg8ro93qd2t3eglp9ruq5vni88ov.apps.googleusercontent.com"
        //"683111604693-k83jp4n17oei07kuja7gd8t3m10rbrrf.apps.googleusercontent.com"//79qhvg8ro93qd2t3eglp9ruq5vni88ov






    private var mGoogleSignInClient: GoogleSignInClient? = null
    private var mLoginListener: ITLoginListener? = null

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private val REQ_ONE_TAP = 2
    private var id_token = ""

    private var keyHash = ""
    private var sha1 = ""
    private lateinit var context:Context

    override fun login(context: Activity, loginListener: ITLoginListener) {
        LogUtils.e("ceshi  第三方登录 login===444444444===")
        try {
            keyHash = Sha1Util.getKeyHash(context)
            sha1 = Sha1Util.getCertificateSHA1Fingerprint(context)
            LogUtils.e("ceshi  第三方登录 login===sha1===$sha1")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: CertificateException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        this.context = context
        this.mLoginListener = loginListener
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestId()
            .requestProfile()
            .requestIdToken(GOOGLE_CLIENT_ID)
            .requestServerAuthCode(GOOGLE_CLIENT_ID)
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso)


        //auth = Firebase.auth
        /*oneTapClient = Identity.getSignInClient(context)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder().setSupported(true).build())
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(GOOGLE_CLIENT_ID)
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            ).setAutoSelectEnabled(true).build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(context) { result ->
                try {
                    startIntentSenderForResult(
                        context,result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null,
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("tag", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(context) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d("tag", e.localizedMessage)

            }*/



        val signInIntent: Intent? = mGoogleSignInClient?.signInIntent
        LogUtils.e("ceshi 第三方登录 返回的req ============：",G_LOGIN_REQ)
        context.startActivityForResult(signInIntent, G_LOGIN_REQ)
    }

    override fun onActivityResult(reqCode: Int, resCode: Int, data: Intent) {
        LogUtils.e("ceshi 第三方登录=====onActivityResult=======：", resCode, resCode)
        try {
//            val googleCredential = oneTapClient.getSignInCredentialFromIntent(data)
//            googleCredential.googleIdToken.let {
//                id_token = it.toString()
//                Log.e("ceshi","id_token>>="+id_token)
////                val user = auth.currentUser?.getIdToken(true)?.result
////                Log.e("ceshi","user>>="+user)
//                successCallback1(id_token)
//            }

        }catch (e: ApiException) {
            LogUtils.e("ceshi 第三方登录=====666onActivityResult=======：", e.statusCode, e.message)
        }


        if (reqCode == G_LOGIN_REQ) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    override fun logout(context: Activity, logoutListener: ITLogoutListener) {
        if (mGoogleSignInClient == null) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestServerAuthCode(GOOGLE_CLIENT_ID)
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
        }
        mGoogleSignInClient?.signOut()
            ?.addOnCompleteListener(OnCompleteListener<Void?> {
                logoutListener.onLogout()
            })
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            id_token = completedTask.result.idToken.toString()

            Log.e("ceshi","status:${completedTask.isSuccessful}")

            //successCallback1(id_token)
            Log.e("ceshi","user>>${completedTask.result.id}")
            //successCallback(account)
            LogUtils.i(
                TAG,
                "signInResult:success code = 0, accout.token = " + account.serverAuthCode + "  id = " + account.id+"email="+account.email+"password="+account.requestExtraScopes().idToken
            )
            account.id?.let { account.email?.let { it1 -> successCallback2(it, it1) } }

            /*val user = Firebase.auth.currentUser
            if (user != null) {
                Log.e("ceshi","YES")
            } else {
                Log.e("ceshi","NO")
            }*/

            /*val mUser = auth.currentUser
            mUser!!.getIdToken(true)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        // Send token to your backend via HTTPS
                        // ...
                        Log.e("ceshi","user>>="+idToken.toString())
                        id_token = idToken.toString()
                        successCallback1(id_token)
                    } else {
                        // Handle error -> task.getException();
                        Log.e("ceshi","user>>=null")
                    }
                }*/
            // Signed in successfully, show authenticated UI.
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            LogUtils.e(TAG, "signInResult:failed code=" + e.getStatusCode() + "   " + e.message)
            Log.e(TAG, "signInResult:failed message="+e.message)
            if (e.statusCode == 12501) {
                if (mLoginListener != null) {
                    mLoginListener!!.onCancel()
                    mLoginListener = null
                }
            } else {
                failedCallback("Sign in Failed!")
                //                ReportManager.log("signInResult:failed code = " + e.getStatusCode() + "  sha1 " + sha1 + "  KeyHash = " + keyHash);
//                ReportManager.logError(e);
            }
        }
    }

    private fun successCallback(account: GoogleSignInAccount) {
        val bean = TLoginBean(
            id_token,
            account.id,
            "Google",
            TLoginMgr.LoginType.Google,
            account.email
        )
        if (mLoginListener != null) {
            mLoginListener?.onSuccess(bean)
            mLoginListener = null
        }
    }

    private fun successCallback1(id_token: String) {
        if (mLoginListener != null) {
            mLoginListener?.onSuccess1(id_token)
            mLoginListener = null
        }
    }

    private fun successCallback2(user_id: String,email:String) {
        if (mLoginListener != null) {
            mLoginListener?.onSuccess2(user_id,email)
            mLoginListener = null
        }
    }

    private fun failedCallback(errMsg: String) {
        if (mLoginListener != null) {
            mLoginListener?.onFailed(errMsg)
            mLoginListener = null
        }
    }

    override fun release() {
        mGoogleSignInClient = null
        mLoginListener = null
    }
}