package dev.code93.daviplata.presentation.auth.register

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.code93.daviplata.R
import dev.code93.daviplata.domain.validation.Validators
import dev.code93.daviplata.ui.common.AlertBanner
import dev.code93.daviplata.ui.common.AlertType
import dev.code93.daviplata.ui.common.AppBarVariant
import dev.code93.daviplata.ui.common.DaviAppBar
import dev.code93.daviplata.ui.common.DaviOutlinedInput
import dev.code93.daviplata.ui.common.PrimaryButton
import dev.code93.daviplata.ui.theme.*

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    initialStep: Int = 0,
    onRegister: (phone: String, name: String, document: String, email: String, username: String, password: String, confirmPassword: String) -> Unit,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    onResetState: () -> Unit,
) {
    var step by remember { mutableIntStateOf(initialStep) }

    // Step 1 fields
    var document by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // Step 2 fields
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        if (uiState is RegisterUiState.Success) onSuccess()
    }

    val isStep1Valid = Validators.firstError(
        Validators.document(document),
        Validators.name(name),
        Validators.email(email),
        Validators.phone(phone),
    ) == null
    val passwordStrength = remember(password) { evaluatePasswordStrength(password) }
    val isStep2Valid = Validators.firstError(
        Validators.username(username),
        Validators.strongPassword(password),
        Validators.passwordConfirmation(password, confirmPassword),
    ) == null

    Scaffold(
        topBar = {
            DaviAppBar(
                title = stringResource(R.string.register_title),
                onBack = { if (step == 0) onNavigateBack() else step = 0 },
                variant = AppBarVariant.Red,
            )
        },
        containerColor = White,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(White),
        ) {
            StepIndicator(
                currentStep = step,
                totalSteps = 2,
                labels = listOf(
                    stringResource(R.string.register_step_data),
                    stringResource(R.string.register_step_access)
                )
            )

            if (uiState is RegisterUiState.Error) {
                Spacer(Modifier.height(Spacing.s8))
                AlertBanner(
                    type = AlertType.Error,
                    text = uiState.message,
                    modifier = Modifier.padding(horizontal = Spacing.s24),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.s24, vertical = Spacing.s24),
            ) {
                if (step == 0) {
                    Step1Fields(
                        document = document, onDocumentChange = { if (it.length <= 10) document = it },
                        name = name, onNameChange = { name = it },
                        email = email, onEmailChange = { email = it },
                        phone = phone, onPhoneChange = { if (it.length <= 10) phone = it },
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.height(Spacing.s24))
                    PrimaryButton(
                        text = stringResource(R.string.register_button_continue),
                        onClick = { step = 1; onResetState() },
                        enabled = isStep1Valid,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Step2Fields(
                        username = username, onUsernameChange = { username = it },
                        password = password, onPasswordChange = { password = it },
                        confirmPassword = confirmPassword, onConfirmChange = { confirmPassword = it },
                        passwordStrength = passwordStrength,
                        passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword,
                    )
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.height(Spacing.s16))
                    Text(
                        text = stringResource(R.string.register_footer_terms),
                        style = DaviTextStyles.BodySmall,
                        color = TextSecondary,
                    )
                    Spacer(Modifier.height(Spacing.s16))
                    PrimaryButton(
                        text = stringResource(R.string.register_button_create),
                        onClick = {
                            onRegister(
                                phone,
                                name,
                                document,
                                email,
                                username,
                                password,
                                confirmPassword
                            )
                        },
                        loading = uiState is RegisterUiState.Loading,
                        enabled = isStep2Valid,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun Step1Fields(
    document: String, onDocumentChange: (String) -> Unit,
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    phone: String, onPhoneChange: (String) -> Unit,
) {
    DaviOutlinedInput(
        value = document, onValueChange = onDocumentChange,
        label = stringResource(R.string.register_document_label),
        leadingIcon = Icons.Outlined.Badge,
        keyboardType = KeyboardType.Number,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.s16))
    DaviOutlinedInput(
        value = name, onValueChange = onNameChange,
        label = stringResource(R.string.register_name_label),
        leadingIcon = Icons.Outlined.Person,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.s16))
    DaviOutlinedInput(
        value = email, onValueChange = onEmailChange,
        label = stringResource(R.string.register_email_label),
        leadingIcon = Icons.Outlined.Email,
        keyboardType = KeyboardType.Email,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.s16))
    DaviOutlinedInput(
        value = phone, onValueChange = onPhoneChange,
        label = stringResource(R.string.register_phone_label),
        leadingIcon = Icons.Outlined.PhoneAndroid,
        keyboardType = KeyboardType.Phone,
        maxLength = 10,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Step2Fields(
    username: String, onUsernameChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmChange: (String) -> Unit,
    passwordStrength: PasswordStrength,
    passwordMismatch: Boolean,
) {
    DaviOutlinedInput(
        value = username, onValueChange = onUsernameChange,
        label = stringResource(R.string.register_username_label),
        leadingIcon = Icons.Outlined.AlternateEmail,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(Spacing.s16))
    DaviOutlinedInput(
        value = password, onValueChange = onPasswordChange,
        label = stringResource(R.string.register_password_label),
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        helper = stringResource(R.string.register_password_helper),
        modifier = Modifier.fillMaxWidth(),
    )
    if (password.isNotEmpty()) {
        Spacer(Modifier.height(Spacing.s8))
        PasswordStrengthBar(strength = passwordStrength)
    }
    Spacer(Modifier.height(Spacing.s16))
    DaviOutlinedInput(
        value = confirmPassword, onValueChange = onConfirmChange,
        label = stringResource(R.string.register_confirm_password_label),
        leadingIcon = Icons.Outlined.Lock,
        isPassword = true,
        error = if (passwordMismatch) stringResource(R.string.register_password_mismatch_error) else null,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PasswordStrengthBar(strength: PasswordStrength) {
    val segments = 4
    val filledSegments = when (strength) {
        PasswordStrength.Weak -> 1
        PasswordStrength.Fair -> 2
        PasswordStrength.Good -> 3
        PasswordStrength.Strong -> 4
    }
    val label = when (strength) {
        PasswordStrength.Weak -> stringResource(R.string.register_strength_weak)
        PasswordStrength.Fair -> stringResource(R.string.register_strength_fair)
        PasswordStrength.Good -> stringResource(R.string.register_strength_good)
        PasswordStrength.Strong -> stringResource(R.string.register_strength_strong)
    }
    val color = when (strength) {
        PasswordStrength.Weak -> AppError
        PasswordStrength.Fair -> Warning
        PasswordStrength.Good -> Color(0xFF4CAF50)
        PasswordStrength.Strong -> Success
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(segments) { i ->
                val filled = i < filledSegments
                val segColor by animateColorAsState(
                    targetValue = if (filled) color else SurfaceMedium,
                    animationSpec = tween(300),
                    label = "seg$i",
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segColor),
                )
            }
        }
        Spacer(Modifier.width(Spacing.s8))
        Text(text = label, style = DaviTextStyles.BodySmall, color = color)
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int, labels: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrandRed)
            .padding(horizontal = Spacing.s24, vertical = Spacing.s12),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        labels.forEachIndexed { index, label ->
            val active = index <= currentStep
            val stepColor by animateColorAsState(
                targetValue = if (active) White else White.copy(alpha = 0.4f),
                animationSpec = tween(300),
                label = "step$index",
            )
            val lineColor by animateColorAsState(
                targetValue = if (index < currentStep) White else White.copy(alpha = 0.3f),
                animationSpec = tween(300),
                label = "line$index",
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(stepColor.copy(alpha = if (active) 1f else 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 1}",
                        style = DaviTextStyles.LabelStrong,
                        color = if (active) BrandRed else White.copy(alpha = 0.5f),
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = label,
                    style = DaviTextStyles.BodySmall,
                    color = stepColor,
                )
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(lineColor),
                )
            }
        }
    }
}
