package com.example.madicare.UI

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.madicare.R
import com.example.madicare.ui.theme.*

@Composable
fun LoginScreen() {
    var email by remember { mutableStateOf("bishal@example.com") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome Back",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,

        )
        Text(
            text = "Sign in to continue",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "bishal@example.com"
        )

        CustomTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "********",
            isPassword = true,
            isPasswordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Text(
            text = "Forgot Password?",
            color = PrimaryGreen,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { /* Handle click */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Handle Sign In */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(18.dp)
        ) {
            Text(text = "Sign In",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold)
        }

        // Divider with Text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = " or continue with ",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Divider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        // Social Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialIconButton(
                // Placeholder for Google Icon (Since we don't have vector assets)
                iconContent = {
                    Icon(
                        painter = painterResource(R.drawable.google),
                        contentDescription = "Phone",
                        modifier = Modifier.size(20.dp)
                        )
                },
                onClick = {}
            )
            Spacer(modifier = Modifier.width(24.dp))
            SocialIconButton(
                // Placeholder for Apple Icon
                iconContent = {
                    Icon(
                        Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = Color.Black,

                    )
                },
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Text
        Text(
            text = buildAnnotatedString {
                append("Don't have an account? ")
                withStyle(style = SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                    append("Sign Up")
                }
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { /* Handle Sign Up Navigation */ },
            fontSize = 18.sp,
            fontWeight = FontWeight.Light,
            color = Color.Black
        )
    }
}

@Composable
fun SignUpScreen() {
    var fullName by remember { mutableStateOf("Bishal Das") }
    var email by remember { mutableStateOf("bishal@example.com") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Account",
            fontSize = 30.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = "Join MediCare and take\ncontrol of your health.",
            fontSize = 14.sp,
            color = TextGray,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        CustomTextField(
            label = "Full Name",
            value = fullName,
            onValueChange = { fullName = it },
            placeholder = "Bishal Das"
        )

        CustomTextField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            placeholder = "bishal@example.com",
            keyboardType = KeyboardType.Email
        )

        CustomTextField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            placeholder = "********",
            isPassword = true,
            isPasswordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        CustomTextField(
            label = "Confirm Password",
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = "********",
            isPassword = true,
            isPasswordVisible = passwordVisible,
            onVisibilityToggle = { passwordVisible = !passwordVisible }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Terms and Conditions Checkbox
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                colors = CheckboxDefaults.colors(checkedColor = PrimaryGreen),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = buildAnnotatedString {
                    append("I agree to the ")
                    withStyle(style = SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Medium)) {
                        append("Terms & Privacy Policy")
                    }
                },
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { /* Handle Sign Up */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Sign Up", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Reusable Components ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityToggle: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextGray, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            singleLine = true,

            visualTransformation = if (isPassword && !isPasswordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onVisibilityToggle?.invoke() }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password Visibility",
                            tint = TextGray
                        )
                    }
                }
            } else null
        )
    }
}

@Composable
fun SocialIconButton(
    onClick: () -> Unit,
    iconContent: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 64.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                color = Color(0xFFE1E6EA),
                shape = RoundedCornerShape(20.dp)
            )
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        iconContent()
    }



}



@Preview
@Composable
fun Forgetpassword(
    onBackClick: () -> Unit = {},
    onSendResetLink: (String) -> Unit = {}
) {
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
            .padding(top = 50.dp, bottom = 30.dp)
    ) {

        // Back Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(Color(0xFFF4F7F5))
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = TextDark
            )
        }

        Spacer(modifier = Modifier.height(65.dp))

        // Illustration
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(30.dp))
                .fillMaxWidth()
                .background(Color(0xFFEAF7EF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LockReset,
                contentDescription = "Reset Password",
                tint = PrimaryGreen,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        // Heading
        Text(
            text = "Forgot Password?",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Don't worry! It happens. Enter your registered email address and we'll send you a link to reset your password.",
            fontSize = 16.sp,
            lineHeight = 25.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(45.dp))

        // Email Label
        Text(
            text = "Email Address",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextDark
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    "example@gmail.com",
                    color = TextGray
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = TextGray
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryGreen,
                unfocusedBorderColor = Color(0xFFDCE3E8),
                focusedContainerColor = Color(0xFFF8FAFA),
                unfocusedContainerColor = Color(0xFFF8FAFA)
            )
        )

        Spacer(modifier = Modifier.height(35.dp))

        // Send Reset Link
        Button(
            onClick = {
                if (email.isNotBlank()) {
                    onSendResetLink(email.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            )
        ) {
            Text(
                text = "Send Reset Link  →",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Remember your password? ",
                color = TextGray,
                fontSize = 14.sp
            )

            Text(
                "Sign In",
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    onBackClick()
                }
            )
        }
    }
}


// --- Previews ---
@Preview(showBackground = true, name = "Login Screen")
@Composable
fun PreviewLoginScreen() {
    MaterialTheme {
        LoginScreen()
    }
}

@Preview(showBackground = true, name = "Sign Up Screen")
@Composable
fun PreviewSignUpScreen() {
    MaterialTheme {
        SignUpScreen()
    }
}