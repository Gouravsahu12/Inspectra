package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.UserRole
import com.example.ui.components.InspectraGovBadge
import com.example.ui.theme.Navy700
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.viewmodel.InspectraViewModel

enum class AuthMode {
    LOGIN,
    REGISTER
}

@Composable
fun LoginScreen(
    viewModel: InspectraViewModel
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(AuthMode.LOGIN) }

    val isLoading by viewModel.authLoading.collectAsState()
    val serverError by viewModel.authError.collectAsState()

    // Form states
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Registration specific states
    var fullName by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.CONSUMER) }

    // Controls
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    var localValidationError by remember { mutableStateOf<String?>(null) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color(0xFF0F172A),
        unfocusedTextColor = Color(0xFF0F172A),
        focusedBorderColor = Navy700,
        unfocusedBorderColor = Slate400,
        focusedContainerColor = Color(0xFFF8FAFC),
        unfocusedContainerColor = Color(0xFFF8FAFC),
        cursorColor = Navy900,
        focusedPlaceholderColor = Slate400,
        unfocusedPlaceholderColor = Slate500
    )

    val inputTextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF0F172A)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Government Badge / National Standard Header
            InspectraGovBadge()

            Spacer(modifier = Modifier.height(14.dp))

            // Logo & Title
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Navy900)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_app_icon),
                    contentDescription = "INSPECTRA Logo",
                    modifier = Modifier.size(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "INSPECTRA",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Navy900,
                    letterSpacing = 1.sp
                )
            )

            Text(
                text = "National Legal Metrology & Package Compliance Portal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Slate600,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Auth Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Login / Register Tabs
                    TabRow(
                        selectedTabIndex = if (currentMode == AuthMode.LOGIN) 0 else 1,
                        containerColor = Slate100,
                        contentColor = Navy900,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (currentMode == AuthMode.LOGIN) 0 else 1]),
                                color = Navy700,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                    ) {
                        Tab(
                            selected = currentMode == AuthMode.LOGIN,
                            onClick = {
                                currentMode = AuthMode.LOGIN
                                localValidationError = null
                                viewModel.clearAuthError()
                            },
                            text = {
                                Text(
                                    text = "Sign In",
                                    fontWeight = if (currentMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentMode == AuthMode.LOGIN) Navy900 else Slate600
                                )
                            }
                        )
                        Tab(
                            selected = currentMode == AuthMode.REGISTER,
                            onClick = {
                                currentMode = AuthMode.REGISTER
                                localValidationError = null
                                viewModel.clearAuthError()
                            },
                            text = {
                                Text(
                                    text = "Create Account",
                                    fontWeight = if (currentMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                                    color = if (currentMode == AuthMode.REGISTER) Navy900 else Slate600
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = if (currentMode == AuthMode.LOGIN) "Sign In" else "Create Account",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate900,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = if (currentMode == AuthMode.LOGIN)
                            "Enter your registered email and password"
                        else
                            "Register your citizen, retailer, or officer account",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Slate500,
                            fontSize = 12.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Full Name (Only for Registration)
                    if (currentMode == AuthMode.REGISTER) {
                        Text(
                            text = "FULL NAME",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                localValidationError = null
                            },
                            placeholder = { Text("Enter your full name (e.g. Amit Verma)", color = Slate400) },
                            singleLine = true,
                            textStyle = inputTextStyle,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Navy700,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // 2. Email
                    Text(
                        text = "EMAIL ADDRESS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate700,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            localValidationError = null
                            viewModel.clearAuthError()
                        },
                        placeholder = { Text("Enter your email address", color = Slate400) },
                        singleLine = true,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Password
                    Text(
                        text = "PASSWORD (MIN 6 CHARACTERS)",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate700,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            localValidationError = null
                            viewModel.clearAuthError()
                        },
                        placeholder = { Text("Enter password", color = Slate400) },
                        singleLine = true,
                        textStyle = inputTextStyle,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Navy700,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Slate600,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        colors = textFieldColors,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // 4. Confirm Password (Register mode only)
                    if (currentMode == AuthMode.REGISTER) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "CONFIRM PASSWORD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localValidationError = null
                            },
                            placeholder = { Text("Re-enter password", color = Slate400) },
                            singleLine = true,
                            textStyle = inputTextStyle,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Navy700,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = textFieldColors,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // 5. Select Role (Only in Register mode, placed at the last in column/list style)
                        Text(
                            text = "SELECT YOUR ROLE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate700,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UserRole.values().forEach { role ->
                                val isSelected = selectedRole == role
                                val roleIcon = when (role) {
                                    UserRole.CONSUMER -> Icons.Default.Person
                                    UserRole.RETAILER -> Icons.Default.Storefront
                                    UserRole.OFFICER -> Icons.Default.Shield
                                }
                                val title = when (role) {
                                    UserRole.CONSUMER -> "Consumer"
                                    UserRole.RETAILER -> "Retailer"
                                    UserRole.OFFICER -> "Legal Metrology Officer"
                                }
                                val subtitle = when (role) {
                                    UserRole.CONSUMER -> "General citizen scanning packages & verifying MRP"
                                    UserRole.RETAILER -> "Merchant / stockist checking package compliance"
                                    UserRole.OFFICER -> "Enforcement officer conducting field inspections"
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) Color(0xFFF0F4FF) else Color(0xFFF8FAFC),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Navy700 else Slate300
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRole = role }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Navy900 else Slate200),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = roleIcon,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Slate700,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Navy900 else Slate800,
                                                    fontSize = 13.sp
                                                )
                                            )
                                            Text(
                                                text = subtitle,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Slate600,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) Navy700 else Slate400,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Error banner (Local validation or Server error)
                    val activeError = localValidationError ?: serverError
                    if (activeError != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFFEF2F2))
                                .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = "Error",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeError,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF991B1B),
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Remember Me Checkbox & Reset Password
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = Navy700)
                            )
                            Text(
                                text = "Keep me signed in",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Slate800,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        if (currentMode == AuthMode.LOGIN) {
                            Text(
                                text = "Forgot password?",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Navy700,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                modifier = Modifier.clickable {
                                    if (email.isNotBlank()) {
                                        Toast.makeText(context, "Password reset instructions requested for $email", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Please enter your email address first", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Action Button (Sign In / Create Account)
                    Button(
                        onClick = {
                            if (email.isBlank()) {
                                localValidationError = "Please enter your email address"
                                return@Button
                            }
                            if (!email.contains("@") || !email.contains(".")) {
                                localValidationError = "Please enter a valid email format (e.g. name@domain.com)"
                                return@Button
                            }
                            if (password.isBlank() || password.length < 6) {
                                localValidationError = "Password must be at least 6 characters"
                                return@Button
                            }

                            if (currentMode == AuthMode.REGISTER) {
                                if (fullName.isBlank()) {
                                    localValidationError = "Please enter your full name"
                                    return@Button
                                }
                                if (password != confirmPassword) {
                                    localValidationError = "Passwords do not match"
                                    return@Button
                                }

                                viewModel.registerWithSupabase(
                                    email = email.trim(),
                                    password = password,
                                    role = selectedRole,
                                    fullName = fullName.trim(),
                                    rememberMe = rememberMe
                                ) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, "Account registered successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                viewModel.loginWithSupabase(
                                    email = email.trim(),
                                    password = password,
                                    selectedRole = UserRole.CONSUMER,
                                    rememberMe = rememberMe
                                ) { success, msg ->
                                    if (success) {
                                        Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (currentMode == AuthMode.LOGIN) "SIGNING IN..." else "CREATING ACCOUNT...",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        } else {
                            Icon(
                                imageVector = if (currentMode == AuthMode.LOGIN) Icons.Default.Security else Icons.Default.HowToReg,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (currentMode == AuthMode.LOGIN) "SIGN IN" else "CREATE ACCOUNT",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch between Login and Register prompt
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentMode == AuthMode.LOGIN) "New to Inspectra? " else "Already have an account? ",
                            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                        )
                        Text(
                            text = if (currentMode == AuthMode.LOGIN) "Create account" else "Sign in here",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Navy700,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable {
                                currentMode = if (currentMode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                                localValidationError = null
                                viewModel.clearAuthError()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Legal Metrology Act (2011) & Packaged Commodities Rules",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = Slate500
                    )
                )
            }
        }
    }
}
