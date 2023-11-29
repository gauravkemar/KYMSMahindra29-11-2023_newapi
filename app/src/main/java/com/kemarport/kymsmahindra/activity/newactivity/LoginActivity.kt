package com.kemarport.kymsmahindra.activity.newactivity

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Gravity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kymsmahindra.R
import com.kemarport.kymsmahindra.databinding.ActivityLoginBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.helper.Utils
import com.kemarport.kymsmahindra.model.login.LoginRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModel
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModelFactory
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import android.provider.Settings
import com.kemarport.kymsmahindra.activity.MainActivity

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var progress: ProgressDialog
    private lateinit var session: SessionManager
    private lateinit var progressDialog: ProgressDialog
    var installedVersionCode = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.hide()

        Toasty.Config.getInstance()
            .setGravity(Gravity.CENTER)
            .apply()
         val androidId: String = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("android device id",androidId)
        progress = ProgressDialog(this)
        progress.setMessage("Please Wait...")

        val packageManager = applicationContext.packageManager

        try {
            // Get the package info for the app
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionCode = packageInfo.versionCode
            // Retrieve the version code and version name
            val versionName = packageInfo.versionName
            installedVersionCode = versionCode
            // binding.tvBuildNo.setText(installedVersionCode.toString())
            binding.tvAppVersion.setText("V${versionName}")
            // Log or display the version information
            Log.d("Version", "Version Name: $versionName")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        Log.d("Version", "Version Code: $installedVersionCode")
        val kymsRepository = KYMSRepository()
        val viewModelProviderFactory = LoginViewModelFactory(application, kymsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]
        session = SessionManager(this)
        binding.btnLogin.setOnClickListener {
            login()
            //startActivity(Intent(this@LoginActivity,VinRfidMappingActivity::class.java))
        }
        viewModel.loginMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let { resultResponse ->
                        try {

                            if (resultResponse.roleName.equals("supervisor") ||
                                resultResponse.roleName.equals("Super Admin") ||
                                resultResponse.roleName.equals("Driver")
                            ) {
                                session.createLoginSession(
                                    resultResponse.firstName,
                                    resultResponse.lastName,
                                    resultResponse.email,
                                    resultResponse.mobileNumber.toString(),
                                    resultResponse.isVerified.toString(),
                                    resultResponse.userName,
                                    resultResponse.jwtToken,
                                    resultResponse.refreshToken,
                                    resultResponse.roleName,
                                    resultResponse.id,
                                    resultResponse.coordinates,
                                    resultResponse.locationId.toString()
                                )
                                Utils.setSharedPrefsBoolean(
                                    this@LoginActivity,
                                    Constants.LOGGEDIN,
                                    true
                                )
                                startActivity()
                            } else {
                                Toasty.warning(
                                    this@LoginActivity,
                                    "Invalid User",
                                    Toasty.LENGTH_SHORT
                                ).show()
                            }

                        } catch (e: Exception) {
                            Toasty.warning(
                                this@LoginActivity,
                                e.printStackTrace().toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }

                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->
                        Toasty.error(
                            this@LoginActivity,
                            "Login failed - \nError Message: $errorMessage"
                        ).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        viewModel.getAppDetailsMutableLiveData.observe(this, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    when (response.data?.statusCode) {
                        Constants.HTTP_OK -> {
                            response.data.responseObject?.let { resultResponse ->
                                try {
                                    Log.e(
                                        "thisOutsideTry",
                                        installedVersionCode.toString() + " % " + resultResponse.apkVersion.toString()
                                    )
                                    if (resultResponse.apkVersion > installedVersionCode) {
                                        Log.e(
                                            "thisUpdate",
                                            installedVersionCode.toString() + " % " + resultResponse.apkVersion.toString()
                                        )
                                        showUpdateDialog(
                                            resultResponse.apkFileUrl,
                                            resultResponse.isMandatory
                                        )
                                    }
                                } catch (e: Exception) {
                                    Toasty.warning(
                                        this@LoginActivity,
                                        e.message.toString(),
                                        Toasty.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        Constants.HTTP_ERROR, Constants.HTTP_NOT_FOUND -> {
                            Toasty.warning(
                                this@LoginActivity,
                                response.data.errorMessage.toString(),
                                Toasty.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Toasty.error(
                                this,
                                "Submission Failed, statusCode - ${response.data?.statusCode}",
                                Toasty.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { errorMessage ->

                        Toasty.error(
                            this,
                            errorMessage,
                            Toasty.LENGTH_LONG
                        ).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })

    }

    private fun getApplicationVersionDetails() {
        try {
            viewModel.getAppDetails("http://192.168.1.50:5000/api/")
        } catch (e: Exception) {
            Toasty.error(
                this,
                e.message.toString(),
                Toasty.LENGTH_LONG
            ).show()
        }
    }
    override fun onResume() {
        super.onResume()
        //getApplicationVersionDetails()
    }
    private fun showUpdateDialog(appUrl: String, isMandatory: Boolean) {
        if (isMandatory) {
            AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("A new version of the app is available. Do you want to update?")
                .setCancelable(false)
                .setPositiveButton("Update") { dialog, _ ->
                    try {
                        dialog.dismiss()
                        val destinationFolder =
                            Environment.getExternalStorageDirectory().toString() + "/download/"

                        progressDialog = ProgressDialog(this)
                        progressDialog.setMessage("Downloading...")
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                        progressDialog.setCancelable(false)

                        GlobalScope.launch(Dispatchers.IO) {
                            Log.e("thisUpdateDownloadFile", installedVersionCode.toString())
                            downloadFileNew(appUrl, destinationFolder)
                        }
                    } catch (e: Exception) {
                        Toasty.error(
                            this@LoginActivity,
                            "This is exception " + e.toString()
                        ).show()
                    }
                }
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle("Update Available (Optional)")
                .setMessage("A new version of the app is available. Do you want to update?")
                .setCancelable(true)
                .setPositiveButton("Update") { dialog, _ ->
                    try {
                        dialog.dismiss()
                        val destinationFolder =
                            Environment.getExternalStorageDirectory().toString() + "/download/"

                        progressDialog = ProgressDialog(this)
                        progressDialog.setMessage("Downloading...")
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                        progressDialog.setCancelable(false)

                        GlobalScope.launch(Dispatchers.IO) {
                            downloadFileNew(appUrl, destinationFolder)
                        }
                    } catch (e: Exception) {
                        Toasty.error(
                            this@LoginActivity,
                            "This is exception " + e.toString()
                        ).show()
                    }

                }
                .setNegativeButton("Skip") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun downloadFileNew(url: String, destinationFolder: String) {
        try {
            val fileUrl = URL(url)
            val fileName = fileUrl.file.substring(fileUrl.file.lastIndexOf("/") + 1)
            val destinationFile = File(destinationFolder, fileName)
            val outputStream = FileOutputStream(destinationFile)
            val connection = fileUrl.openConnection()
            val contentLength = connection.contentLength
            val contentLengthMB = contentLength / (1000 * 1000) // Convert bytes to megabytes
            val buffer = ByteArray(1024)
            var totalBytesRead = 0
            var bytesRead = connection.getInputStream().read(buffer)
            runOnUiThread {
                progressDialog.max = contentLengthMB
                progressDialog.show()
            }
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead
                val totalBytesReadMB = totalBytesRead / (1000 * 1000) // Convert bytes to megabytes
                runOnUiThread { progressDialog.progress = totalBytesReadMB }
                bytesRead = connection.getInputStream().read(buffer)
            }
            outputStream.close()
            runOnUiThread {
                progressDialog.dismiss()
                showMessageDialog("Update downloaded successfully")
                installApkFile(destinationFile)
            }
        } catch (e: Exception) {
            runOnUiThread {
                progressDialog.dismiss()
                showMessageDialog("Error downloading file: ${e.message}")
            }
        }
    }

    private fun installApkFile(apkFile: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val authority = "${applicationContext.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(this, authority, apkFile)
            val installIntent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                data = apkUri
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(installIntent)
        } else {
            val apkUri = Uri.fromFile(apkFile)
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(installIntent)
        }
    }

    private fun showMessageDialog(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    fun startActivity() {
        startActivity(Intent(this@LoginActivity, HomeMenuActivity::class.java))
        finish()
    }

    fun login() {
        try {
            // Fetching Android ID and storing it into a constant

            val userId = binding.edUsername.text.toString()
            val pass = binding.edPass.text.toString()
            if (userId.isNotEmpty() && pass.isNotEmpty()) {
                val loginRequest = LoginRequest(pass, userId,Utils.getDeviceId(this@LoginActivity))
                viewModel.login(Constants.BASE_URL, loginRequest)
            } else {
                Toasty.warning(
                    this@LoginActivity,
                    "please fill the required credentials",
                    Toasty.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            Toasty.warning(
                this@LoginActivity,
                e.printStackTrace().toString(),
                Toasty.LENGTH_SHORT
            ).show()
        }
    }

    private fun showProgressBar() {
        progress.show()
    }

    private fun hideProgressBar() {
        progress.cancel()
    }
}