package com.kemarport.kymsmahindra.activity.newactivity

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.kemarport.kymsmahindra.R

import com.kemarport.kymsmahindra.databinding.ActivityChangePasswordBinding
import com.kemarport.kymsmahindra.helper.Constants
import com.kemarport.kymsmahindra.helper.Resource
import com.kemarport.kymsmahindra.helper.SessionManager
import com.kemarport.kymsmahindra.model.changepassword.ChangePasswordRequest
import com.kemarport.kymsmahindra.repository.KYMSRepository
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModel
import com.kemarport.kymsmahindra.viewmodel.login.LoginViewModelFactory

import es.dmoral.toasty.Toasty
import java.util.HashMap

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityChangePasswordBinding

    //get values from session
    private lateinit var userDetails: HashMap<String, String?>
    private lateinit var session: SessionManager
    private lateinit var viewModel: LoginViewModel
    private var userName: String? = ""
    private var token: String? = ""
    private lateinit var progress: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        //binding.changePasswordToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
        setSupportActionBar(binding.changePasswordToolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        progress = ProgressDialog(this)
        progress.setMessage("Loading...")
        session = SessionManager(this)
        userDetails = session.getUserDetails()
        userName = userDetails[Constants.KEY_USER_NAME].toString()
        token = userDetails[Constants.KEY_JWT_TOKEN].toString()
        val kdmsRepository = KYMSRepository()
        val viewModelProviderFactory =
            LoginViewModelFactory(application, kdmsRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[LoginViewModel::class.java]

        viewModel.changePasswordMutableLiveData.observe(this) { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    logout()
                    response.data?.let { resultResponse ->
                        /*   var errorMessage = resultResponse.errorMessage
                           var responseMessage = resultResponse.responseMessage
                           if (errorMessage.isNotEmpty()) {
                               Toasty.error(
                                   this@ChangePasswordActivity,
                                   "Error Message: $errorMessage"
                               ).show()
                           } else if (responseMessage.isNotEmpty()) {
                               Toasty.success(
                                   this@ChangePasswordActivity,
                                   "Success Message: $responseMessage"
                               ).show()
                           }*/
                    }
                }

                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { resultResponse ->
                        Toasty.error(
                            this@ChangePasswordActivity,
                            "Error Message: $resultResponse"
                        ).show()
                        if (resultResponse == "Session Expired ! Please relogin" || resultResponse == "Authentication token expired" ||
                            resultResponse == Constants.CONFIG_ERROR
                        ) {
                            showCustomDialog(
                                "Session Expired",
                                "Please re-login to continue"
                            )
                        }
                    }
                }

                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        }
        binding.btnSubmit.setOnClickListener {
            showConfirmDialog()
        }
        binding.edNewPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not needed for this example
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not needed for this example
            }

            override fun afterTextChanged(s: Editable?) {
                // Validate the entered text

                /*
                   val inputText = s.toString()
                if (isValidInput(inputText)) {
                    // The input is valid
                    binding.edNewPassword.error = null
                } else {
                    // The input is not valid, show an error
                    binding.edNewPassword.error = "Invalid input. Please follow the specified criteria."
                }*/
                val inputText = s.toString()
                updatePasswordCriteria(inputText)
            }
        })
    }

    private fun isValidInput(input: String): Boolean {
        val regex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{6,}$"
        return input.matches(regex.toRegex())
    }
    private fun updatePasswordCriteria(input: String) {

        val containsUpperCase = input.any { it.isUpperCase() }
        val containsLowerCase = input.any { it.isLowerCase() }
        val containsDigit = input.any { it.isDigit() }
        val containsSpecialCharacter = input.any { it in "@#$%^&+=" }
        val hasValidLength = input.length >= 6

        val criteriaStringBuilder = StringBuilder()

        if (!containsUpperCase) {
            criteriaStringBuilder.append("* should contain A-Z\n")
        }
        if (!containsLowerCase) {
            criteriaStringBuilder.append("* should contain a-z\n")
        }
        if (!containsDigit) {
            criteriaStringBuilder.append("* should contain 0-9\n")
        }
        if (!containsSpecialCharacter) {
            criteriaStringBuilder.append("* should contain special characters (@#$%^&+=)\n")
        }
        if (!hasValidLength) {
            criteriaStringBuilder.append("* should contain least 6 characters\n")
        }
        binding.tvPassRequirement.visibility = if (input.isNotEmpty() && criteriaStringBuilder.toString().trim().isNotEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        binding.tvPassRequirement.setText(criteriaStringBuilder.toString().trim())

    }

    private fun logout() {
        session.logoutUser()
        startActivity(Intent(this@ChangePasswordActivity, LoginActivity::class.java))
        finish()
    }

    fun showCustomDialog(title: String?, message: String?) {
        var alertDialog: AlertDialog? = null
        val builder: AlertDialog.Builder
        if (title.equals(""))
            builder = AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else if (message.equals(""))
            builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    alertDialog?.dismiss()
                }
        else
            builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Okay") { dialogInterface, which ->
                    if (title.equals("Session Expired")) {
                        logout()
                    } else {
                        alertDialog?.dismiss()
                    }
                }
        alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }

    private fun showConfirmDialog() {
        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setMessage("Are you sure? Would you like to Change the password?")
            .setCancelable(true)
            .setPositiveButton("Submit") { dialog, _ ->
                try {
                    changePassSubmit()
                } catch (e: Exception) {
                    Toasty.error(
                        this@ChangePasswordActivity,
                        "This is exception " + e.toString()
                    ).show()
                }

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    private fun changePassSubmit() {
        // Fetching user credentials from input fields
        val edOldPassword = binding.edOldPassword.text.toString().trim()
        val edNewPassword = binding.edNewPassword.text.toString().trim()
        val edConfirmPassword = binding.edConfirmPassword.text.toString().trim()

        // Validate user input
        val validationMessage = validateInput(edOldPassword, edNewPassword, edConfirmPassword)
        if (validationMessage == null) {
            userName
                ?.let {
                    ChangePasswordRequest(edConfirmPassword, edOldPassword, edNewPassword, it)
                }
                ?.let {
                    viewModel.changePassword(token!!, Constants.BASE_URL, it)
                }

        } else {
            Toasty.error(
                this@ChangePasswordActivity,
                validationMessage
            ).show()
        }
    }

    private fun validateInput(currentPass: String, newPass: String, confirmPass: String): String? {
        return when {
            !isValidInput(newPass)->"Invalid password format!!."
            currentPass.isEmpty() -> "Please enter the old password"
            newPass.isEmpty() -> "Please enter the new password"
            confirmPass.isEmpty() -> "Please confirm the new password"
            currentPass.length < 6 -> "Old password should have at least 6 characters"
            newPass.length < 6 -> "New password should have at least 6 characters"
            newPass != confirmPass -> "New password and confirm password do not match"
            else -> null
        }
    }
    fun showProgressBar() {
        progress.show()
    }

    fun hideProgressBar() {
        progress.cancel()
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}