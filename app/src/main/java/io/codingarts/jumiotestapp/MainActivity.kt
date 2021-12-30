package io.codingarts.jumiotestapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.core.app.ActivityCompat
import com.jumio.defaultui.JumioActivity
import com.jumio.defaultui.JumioActivity.Companion.PERMISSION_REQUEST_CODE
import com.jumio.sdk.JumioSDK
import com.jumio.sdk.enums.JumioDataCenter
import com.jumio.sdk.result.JumioResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val jumioLauncher = registerForActivityResult(StartActivityForResult()) { result: ActivityResult? -> this.onSuccessJumio(result) }
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.btnBeginVerification).setOnClickListener {
            requestToken()
        }
    }

    private fun requestToken() {
        val api = RestApiTest.Retrofit().create(RestApiTestInterface::class.java)
        api.jumioTest().enqueue(object : Callback<IdVerificationTokenResponse> {
            override fun onResponse(call: Call<IdVerificationTokenResponse>, response: Response<IdVerificationTokenResponse>) {
                token = response.body()?.result!!
                launchJumio()
            }

            override fun onFailure(call: Call<IdVerificationTokenResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, t.localizedMessage, Toast.LENGTH_LONG).show()
            }

        })

    }

    private fun launchJumio() {
        if(!checkPermissions()) return
        intent = Intent(this, JumioActivity::class.java)
                .putExtra(JumioActivity.EXTRA_TOKEN, token)
                .putExtra(JumioActivity.EXTRA_DATACENTER, JumioDataCenter.EU)
        jumioLauncher.launch(intent)
    }


    private fun onSuccessJumio(result: ActivityResult?) {
        if (result!!.data != null) {
            val jumioResult = result.data!!.getSerializableExtra(JumioActivity.EXTRA_RESULT) as JumioResult?
            if (jumioResult!!.isSuccess) {
                Toast.makeText(this, "Verification was successful!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, jumioResult.error!!.message, Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Unknown error", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermissions(requestCode: Int = PERMISSION_REQUEST_CODE) = if (!JumioSDK.hasAllRequiredPermissions(this)) {
            val mp = JumioSDK.getMissingPermissions(this)
            ActivityCompat.requestPermissions(this, mp, requestCode)
            false
    } else {
        true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        launchJumio()
    }
}