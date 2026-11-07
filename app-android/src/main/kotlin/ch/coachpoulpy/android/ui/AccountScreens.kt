@file:OptIn(kotlin.time.ExperimentalTime::class)

package ch.coachpoulpy.android.ui

import android.icu.text.RelativeDateTimeFormatter
import android.icu.util.ULocale
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.coachpoulpy.android.CoachPoulpyApp
import ch.coachpoulpy.android.R
import ch.coachpoulpy.shared.AccountModel
import ch.coachpoulpy.shared.AccountState
import ch.coachpoulpy.shared.ConfirmResult
import ch.coachpoulpy.shared.DeleteResult
import ch.coachpoulpy.shared.NewPasswordResult
import ch.coachpoulpy.shared.SignInResult
import ch.coachpoulpy.shared.SignOutResult
import ch.coachpoulpy.shared.SignUpProblems
import ch.coachpoulpy.shared.SignUpResult
import kotlin.time.Instant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * The account (docs/accounts.md): Settings' Account row and page, and the screens that open over the tabs
 * to sign in, sign up, confirm the email and reset a password. Mockups: design/mockups/agreed/phone/account-*.
 * Every decision is AccountModel's; these screens only show its answers.
 */

/** The one account of the app, which syncs while the app is open (CoachPoulpyApp). */
@Composable
internal fun rememberAccount(): AccountModel {
    val context = LocalContext.current
    return remember { (context.applicationContext as CoachPoulpyApp).account }
}

/** Settings' Account section: one row that says where the phone stands and opens the next step. */
@Composable
internal fun AccountRow(onPage: () -> Unit, onFlow: (String) -> Unit, row: @Composable (title: String, subtitle: String, onClick: () -> Unit) -> Unit) {
    val account = rememberAccount()
    val state by account.state.collectAsStateWithLifecycle()
    when (val current = state) {
        AccountState.Guest -> row(stringResource(R.string.account_guestRow), stringResource(R.string.account_guestRowHint)) { onFlow("sign-in") }
        is AccountState.Pending -> row(stringResource(R.string.account_rowPending), stringResource(R.string.account_rowPendingHint, current.email), onPage)
        is AccountState.SignedIn -> row(current.email, syncSummary(current), onPage)
    }
}

/** "Up to date · Last synced 2 min ago", or that the training is on its way. */
@Composable
private fun syncSummary(state: AccountState.SignedIn): String = listOfNotNull(
    stringResource(if (state.upToDate) R.string.account_stateUpToDate else R.string.account_uploading),
    state.lastSyncedAt?.let { stringResource(R.string.account_stateLastSynced, since(it)) },
).joinToString(" · ")

/** How long ago [at] was, in the app's language, refreshed every half minute. */
@Composable
private fun since(at: Instant): String {
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(30_000)
            value = System.currentTimeMillis()
        }
    }
    val formatter = RelativeDateTimeFormatter.getInstance(ULocale.forLocale(LocalConfiguration.current.locales[0]))
    val minutes = ((now - at.toEpochMilliseconds()) / 60_000).coerceAtLeast(0)
    return when {
        minutes < 1 -> formatter.format(RelativeDateTimeFormatter.Direction.PLAIN, RelativeDateTimeFormatter.AbsoluteUnit.NOW)
        minutes < 60 -> formatter.format(minutes.toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.MINUTES)
        minutes < 24 * 60 -> formatter.format((minutes / 60).toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.HOURS)
        else -> formatter.format((minutes / (24 * 60)).toDouble(), RelativeDateTimeFormatter.Direction.LAST, RelativeDateTimeFormatter.RelativeUnit.DAYS)
    }
}

private enum class LeaveDialog { SIGN_OUT, UNSENT }

/** Settings › Account: the address, the sync state, what follows the account, Sign out and Delete account. */
@Composable
internal fun AccountPage(onBack: () -> Unit, onFlow: (String) -> Unit, modifier: Modifier = Modifier) {
    val account = rememberAccount()
    val state by account.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var dialog by remember { mutableStateOf<LeaveDialog?>(null) }
    var deleting by remember { mutableStateOf(false) }
    val current = state
    if (current == AccountState.Guest) {
        // Signed out, deleted or changed address: nothing is left to show here.
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val email = when (current) {
        is AccountState.Pending -> current.email
        is AccountState.SignedIn -> current.email
        AccountState.Guest -> ""
    }

    SubPage(stringResource(R.string.account_title), subtitle = null, onBack = onBack, modifier = modifier) {
        OutlinedSurface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(stringResource(R.string.account_email), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(email, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            }
        }
        when (current) {
            is AccountState.Pending -> {
                Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.md), color = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.Info, contentDescription = null)
                            Text(stringResource(R.string.account_pendingTitle), style = MaterialTheme.typography.titleMedium)
                        }
                        Text(stringResource(R.string.account_pendingBody, current.email), fontWeight = FontWeight.SemiBold)
                        HelpLine(R.string.account_noEmail, R.string.account_sendAgain) {
                            scope.launch { if (account.sendAgain() == SignUpResult.Sent) onFlow("check-email:resent") }
                        }
                        HelpLine(R.string.account_wrongAddress, R.string.account_changeAddress) {
                            account.changeAddress()
                            onFlow("sign-up")
                        }
                    }
                }
                SyncState(on = false, stringResource(R.string.account_stateNotSyncing), stringResource(R.string.account_stateNotSyncingHint))
            }
            is AccountState.SignedIn -> {
                SyncState(
                    on = current.upToDate,
                    title = stringResource(if (current.upToDate) R.string.account_stateUpToDate else R.string.account_uploading),
                    detail = current.lastSyncedAt?.let { stringResource(R.string.account_stateLastSynced, since(it)) },
                )
                OutlinedSurface {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(stringResource(R.string.account_followsTitle), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.account_followsBody), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                // CLEANUP: Export my data (account-details mockup) waits for an export of the whole phone (#304).
                Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { dialog = if (current.upToDate) LeaveDialog.SIGN_OUT else LeaveDialog.UNSENT },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                    ) { Text(stringResource(R.string.account_signOut), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
                    TextButton(onClick = { deleting = true }) {
                        Text(stringResource(R.string.account_deleteAccount), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            AccountState.Guest -> Unit
        }
    }

    val signOut = { evenWithChanges: Boolean ->
        scope.launch {
            dialog = when (account.signOut(evenWithChanges)) {
                SignOutResult.SIGNED_OUT -> null
                SignOutResult.CHANGES_WAITING -> LeaveDialog.UNSENT
            }
        }
    }
    when (dialog) {
        LeaveDialog.SIGN_OUT -> AlertDialog(
            onDismissRequest = { dialog = null },
            icon = { Image(painterResource(R.drawable.poulpy_pose_thinking), contentDescription = null, modifier = Modifier.size(96.dp)) },
            title = { Text(stringResource(R.string.account_signOutTitle), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.account_signOutBody), textAlign = TextAlign.Center) },
            confirmButton = { Button(onClick = { signOut(false) }) { Text(stringResource(R.string.account_signOut), fontWeight = FontWeight.ExtraBold) } },
            dismissButton = { OutlinedButton(onClick = { dialog = null }) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold) } },
        )
        LeaveDialog.UNSENT -> AlertDialog(
            onDismissRequest = { dialog = null },
            icon = { Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(stringResource(R.string.account_unsentTitle), textAlign = TextAlign.Center) },
            text = { Text(stringResource(R.string.account_unsentBody), textAlign = TextAlign.Center) },
            confirmButton = { Button(onClick = { dialog = null }) { Text(stringResource(R.string.account_waitForUpload), fontWeight = FontWeight.ExtraBold) } },
            dismissButton = {
                TextButton(onClick = { signOut(true) }) {
                    Text(stringResource(R.string.account_signOutAnyway), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                }
            },
        )
        null -> Unit
    }
    if (deleting) DeleteSheet(account, onDismiss = { deleting = false })
}

/** Delete account: the password confirms it, and a wrong one deletes nothing. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteSheet(account: AccountModel, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf<DeleteResult?>(null) }
    var busy by remember { mutableStateOf(false) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.account_deleteTitle), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.account_deleteBody), color = MaterialTheme.colorScheme.onSurfaceVariant)
            when (problem) {
                DeleteResult.WRONG_PASSWORD -> ErrorCard(stringResource(R.string.account_errorDeletePassword))
                DeleteResult.OFFLINE -> ErrorCard(stringResource(R.string.account_errorOffline))
                else -> Unit
            }
            PoulpyTextField(stringResource(R.string.account_deletePassword), password, { password = it }, password = true)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                ) { Text(stringResource(R.string.action_cancel), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface) }
                Button(
                    onClick = {
                        busy = true
                        scope.launch {
                            val result = account.deleteAccount(password)
                            busy = false
                            if (result == DeleteResult.DELETED) onDismiss() else problem = result
                        }
                    },
                    enabled = password.isNotEmpty() && !busy,
                    modifier = Modifier.weight(1.6f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                ) { Text(stringResource(R.string.account_deleteAccount), fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

/** A dot, green once everything is sent, with what the sync is doing. */
@Composable
private fun SyncState(on: Boolean, title: String, detail: String?) {
    Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(if (on) PoulpyTheme.extraColors.success else MaterialTheme.colorScheme.outline))
        Column {
            Text(title, fontWeight = FontWeight.ExtraBold)
            detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

/**
 * The screens that open over the tabs, named by [step] so they survive rotation: "sign-up", "sign-in",
 * "sign-in:reset-done", "forgot", "forgot-sent:<email>", "check-email", "check-email:resent", "welcome",
 * "confirmed", "confirm:<token>" and "reset:<token>" (the email links), "link-expired".
 */
@Composable
fun AccountFlow(step: String, onStep: (String) -> Unit, onClose: () -> Unit) {
    val account = rememberAccount()
    val (name, arg) = step.split(':', limit = 2).let { it[0] to it.getOrElse(1) { "" } }
    when (name) {
        "sign-up" -> SignUpScreen(account, onStep, onClose)
        "sign-in" -> SignInScreen(account, resetDone = arg == "reset-done", onStep, onClose)
        "forgot" -> ForgotScreen(account, onStep)
        "forgot-sent" -> ForgotSentScreen(account, arg, onStep)
        "check-email" -> CheckEmailScreen(account, resent = arg == "resent", onStep, onClose)
        "welcome" -> UploadScreen(account, R.drawable.poulpy_pose_standing_tall, poulpyLine = null, R.string.account_welcomeBackTitle, R.string.account_mergeBody, onClose)
        "confirmed" -> UploadScreen(account, R.drawable.poulpy_pose_champion, R.string.account_confirmedPoulpy, R.string.account_confirmedTitle, body = null, onClose)
        "confirm" -> ConfirmScreen(account, arg, onStep, onClose)
        "reset" -> ResetScreen(account, arg, onStep, onClose)
        "link-expired" -> MessageScreen(
            R.drawable.poulpy_pose_thinking, R.string.account_linkExpiredTitle, AnnotatedString(stringResource(R.string.account_linkExpiredBody)),
            action = R.string.account_backToSignUp, onAction = { onStep("sign-up") }, onClose = onClose,
        )
        else -> LaunchedEffect(step) { onClose() }
    }
}

@Composable
private fun SignUpScreen(account: AccountModel, onStep: (String) -> Unit, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf(account.knownEmail.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var consent by rememberSaveable { mutableStateOf(false) }
    var problems by remember { mutableStateOf(SignUpProblems()) }
    var offline by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var why by remember { mutableStateOf(false) }

    AccountFrame(onClose, R.string.account_createAccount, busy, onAction = {
        busy = true
        offline = false
        scope.launch {
            when (val result = account.signUp(email, password, consent)) {
                SignUpResult.Sent -> onStep("check-email")
                is SignUpResult.Invalid -> problems = result.problems
                SignUpResult.Offline -> offline = true
            }
            busy = false
        }
    }) {
        Intro(R.string.account_signUpTitle, R.string.account_signUpIntro)
        if (offline) ErrorCard(stringResource(R.string.account_errorOffline))
        PoulpyTextField(
            stringResource(R.string.account_email), email, { email = it; problems = problems.copy(email = false) },
            error = if (problems.email) stringResource(R.string.account_errorEmail) else null,
            keyboardType = KeyboardType.Email,
        )
        PoulpyTextField(
            stringResource(R.string.account_password), password, { password = it; problems = problems.copy(password = false) },
            note = stringResource(R.string.account_passwordHint),
            error = if (problems.password) stringResource(R.string.account_errorPasswordShort) else null,
            password = true,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = consent, onCheckedChange = { consent = it; problems = problems.copy(consent = false) })
                Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.account_consent), modifier = Modifier.clickable { consent = !consent; problems = problems.copy(consent = false) })
                    Link(R.string.account_consentLink) { why = true }
                }
            }
            if (problems.consent) FieldError(stringResource(R.string.account_errorConsent))
        }
        AltLine(R.string.account_haveAccount, R.string.account_signIn) { onStep("sign-in") }
    }
    if (why) ConsentSheet(onDismiss = { why = false })
}

/** What is stored and why, opened from the consent box. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsentSheet(onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(R.string.account_consentSheetTitle), style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.account_consentSheetIntro), color = MaterialTheme.colorScheme.onSurfaceVariant)
            listOf(R.string.account_consentItemTraining, R.string.account_consentItemBody, R.string.account_consentItemPrefs).forEach {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(it), fontWeight = FontWeight.Bold)
                }
            }
            Note(stringResource(R.string.account_consentStays))
            Note(stringResource(R.string.account_consentWhy))
            Text(
                stringResource(R.string.account_consentBefore),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(56.dp)) {
                Text(stringResource(R.string.account_gotIt), fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SignInScreen(account: AccountModel, resetDone: Boolean, onStep: (String) -> Unit, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf(account.knownEmail.orEmpty()) }
    var password by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf<SignInResult?>(null) }
    var busy by remember { mutableStateOf(false) }

    AccountFrame(onClose, R.string.account_signIn, busy || email.isBlank() || password.isEmpty(), onAction = {
        busy = true
        scope.launch {
            when (val result = account.signIn(email, password)) {
                SignInResult.SignedIn -> onClose()
                SignInResult.Joining -> onStep("welcome")
                else -> problem = result
            }
            busy = false
        }
    }) {
        Intro(R.string.account_signInTitle, R.string.account_signInIntro)
        when (problem) {
            SignInResult.WrongEmailOrPassword -> ErrorCard(stringResource(R.string.account_errorSignIn))
            is SignInResult.TooManyTries -> ErrorCard(stringResource(R.string.account_errorTooManyTries))
            SignInResult.Offline -> ErrorCard(stringResource(R.string.account_errorOffline))
            else -> if (resetDone) InfoCard(stringResource(R.string.account_passwordChanged))
        }
        PoulpyTextField(stringResource(R.string.account_email), email, { email = it }, keyboardType = KeyboardType.Email)
        PoulpyTextField(stringResource(R.string.account_password), password, { password = it }, password = true)
        Link(R.string.account_forgot) { onStep("forgot") }
        AltLine(R.string.account_noAccount, R.string.account_createOne) { onStep("sign-up") }
    }
}

@Composable
private fun CheckEmailScreen(account: AccountModel, resent: Boolean, onStep: (String) -> Unit, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state by account.state.collectAsStateWithLifecycle()
    val email = (state as? AccountState.Pending)?.email ?: account.knownEmail.orEmpty()
    var sentAgain by rememberSaveable { mutableStateOf(resent) }
    var offline by remember { mutableStateOf(false) }

    AccountFrame(onClose, R.string.account_ok, busy = false, onAction = onClose) {
        Poulpy(R.drawable.poulpy_pose_standing_tall, R.string.account_checkEmailPoulpy)
        Intro(stringResource(R.string.account_checkEmailTitle), bold(stringResource(R.string.account_checkEmailBody, email), email))
        InfoCard(stringResource(R.string.account_checkEmailGuest))
        if (offline) ErrorCard(stringResource(R.string.account_errorOffline)) else if (sentAgain) DoneLine(stringResource(R.string.account_resent))
        HelpLine(R.string.account_noEmail, R.string.account_sendAgain) {
            scope.launch {
                val result = account.sendAgain()
                offline = result == SignUpResult.Offline
                sentAgain = result == SignUpResult.Sent
            }
        }
        HelpLine(R.string.account_wrongAddress, R.string.account_changeAddress) {
            account.changeAddress()
            onStep("sign-up")
        }
    }
}

@Composable
private fun ForgotScreen(account: AccountModel, onStep: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var email by rememberSaveable { mutableStateOf(account.knownEmail.orEmpty()) }
    var offline by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }

    AccountFrame({ onStep("sign-in") }, R.string.account_sendLink, busy || email.isBlank(), onAction = {
        busy = true
        scope.launch {
            if (account.requestPasswordReset(email)) onStep("forgot-sent:${email.trim()}") else offline = true
            busy = false
        }
    }) {
        Intro(R.string.account_forgotTitle, R.string.account_forgotIntro)
        if (offline) ErrorCard(stringResource(R.string.account_errorOffline))
        PoulpyTextField(stringResource(R.string.account_email), email, { email = it }, keyboardType = KeyboardType.Email)
    }
}

@Composable
private fun ForgotSentScreen(account: AccountModel, email: String, onStep: (String) -> Unit) {
    val scope = rememberCoroutineScope()
    var sentAgain by rememberSaveable { mutableStateOf(false) }
    var offline by remember { mutableStateOf(false) }
    val back = { onStep("sign-in") }

    AccountFrame(back, R.string.account_ok, busy = false, onAction = back) {
        Poulpy(R.drawable.poulpy_pose_standing_tall, line = null)
        Intro(stringResource(R.string.account_checkEmailTitle), bold(stringResource(R.string.account_forgotSentBody, email), email))
        InfoCard(stringResource(R.string.account_forgotSentNote))
        if (offline) ErrorCard(stringResource(R.string.account_errorOffline)) else if (sentAgain) DoneLine(stringResource(R.string.account_resent))
        HelpLine(R.string.account_noEmail, R.string.account_sendAgain) {
            scope.launch {
                sentAgain = account.requestPasswordReset(email)
                offline = !sentAgain
            }
        }
    }
}

/** Welcome back and Email confirmed: the first upload, which carries on in the background. */
@Composable
private fun UploadScreen(account: AccountModel, @DrawableRes pose: Int, poulpyLine: Int?, title: Int, body: Int?, onClose: () -> Unit) {
    val upload by account.upload.collectAsStateWithLifecycle()
    AccountFrame(onClose = null, R.string.account_continue, busy = false, onAction = onClose) {
        Poulpy(pose, poulpyLine)
        Intro(stringResource(title), body?.let { AnnotatedString(stringResource(it)) })
        OutlinedSurface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.account_uploading), fontWeight = FontWeight.ExtraBold)
                val progress = upload?.let { if (it.total == 0L) 1f else it.sent.toFloat() / it.total } ?: 1f
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(PoulpyRadius.pill)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
                upload?.let {
                    Text(
                        stringResource(R.string.account_uploadProgress, it.sent.toInt(), it.total.toInt()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Note(stringResource(R.string.account_uploadOffline))
    }
}

/** The confirmation link, opened on this phone: confirms, signs in and moves on to what follows. */
@Composable
private fun ConfirmScreen(account: AccountModel, token: String, onStep: (String) -> Unit, onClose: () -> Unit) {
    var offline by remember { mutableStateOf(false) }
    LaunchedEffect(token) {
        when (account.confirmEmail(token)) {
            ConfirmResult.CONFIRMED -> onStep("confirmed")
            ConfirmResult.CONFIRMED_SIGN_IN -> onStep("sign-in")
            ConfirmResult.LINK_EXPIRED -> onStep("link-expired")
            ConfirmResult.OFFLINE -> offline = true
        }
    }
    if (!offline) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) { PoulpyLoader(size = 112.dp) }
        return
    }
    AccountFrame(onClose, R.string.account_ok, busy = false, onAction = onClose) {
        Intro(R.string.account_pendingTitle, null)
        ErrorCard(stringResource(R.string.account_errorOffline))
    }
}

/** The reset link: a new password, which signs every phone out. */
@Composable
private fun ResetScreen(account: AccountModel, token: String, onStep: (String) -> Unit, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf<NewPasswordResult?>(null) }
    var busy by remember { mutableStateOf(false) }

    AccountFrame(onClose, R.string.account_savePassword, busy || password.isEmpty(), onAction = {
        busy = true
        scope.launch {
            when (val result = account.setNewPassword(token, password)) {
                NewPasswordResult.SAVED -> onStep("sign-in:reset-done")
                NewPasswordResult.LINK_EXPIRED -> onStep("link-expired")
                else -> problem = result
            }
            busy = false
        }
    }) {
        Intro(R.string.account_resetTitle, R.string.account_resetIntro)
        if (problem == NewPasswordResult.OFFLINE) ErrorCard(stringResource(R.string.account_errorOffline))
        PoulpyTextField(
            stringResource(R.string.account_newPassword), password, { password = it; problem = null },
            note = stringResource(R.string.account_passwordHint),
            error = if (problem == NewPasswordResult.TOO_SHORT) stringResource(R.string.account_errorPasswordShort) else null,
            password = true,
        )
    }
}

@Composable
private fun MessageScreen(@DrawableRes pose: Int, title: Int, body: AnnotatedString, action: Int, onAction: () -> Unit, onClose: () -> Unit) {
    AccountFrame(onClose, action, busy = false, onAction = onAction) {
        Poulpy(pose, line = null)
        Intro(stringResource(title), body)
    }
}

/** Every account screen: a close button, the content scrolling, and one wide action at the bottom. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountFrame(onClose: (() -> Unit)?, action: Int, busy: Boolean, onAction: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background, scrolledContainerColor = colors.background),
                navigationIcon = {
                    onClose?.let { close ->
                        IconButton(onClick = close) { Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_close)) }
                    }
                },
            )
        },
        bottomBar = {
            Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)) {
                Button(onClick = onAction, enabled = !busy, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                    Text(stringResource(action), fontWeight = FontWeight.ExtraBold)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun Intro(title: Int, body: Int?) = Intro(stringResource(title), body?.let { AnnotatedString(stringResource(it)) })

@Composable
private fun Intro(title: String, body: AnnotatedString?) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontSize = 30.sp, lineHeight = 34.sp)
        body?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

/** Poulpy in the middle, with what he says under him. */
@Composable
private fun Poulpy(@DrawableRes pose: Int, line: Int?) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(pose), contentDescription = null, modifier = Modifier.size(140.dp))
        line?.let { Text(stringResource(it), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center) }
    }
}

/** [text] with [part] in bold, as the address in "Check your email". */
private fun bold(text: String, part: String): AnnotatedString = buildAnnotatedString {
    val at = if (part.isEmpty()) -1 else text.indexOf(part)
    if (at < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, at))
    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(part) }
    append(text.substring(at + part.length))
}

/** A problem: crimson, with an icon. */
@Composable
private fun ErrorCard(text: String) {
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(PoulpyRadius.md),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text(text, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun InfoCard(text: String) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(PoulpyRadius.md), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.Filled.Info, contentDescription = null)
            Text(text, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun DoneLine(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PoulpyTheme.extraColors.success, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = PoulpyTheme.extraColors.success, modifier = Modifier.padding(start = 6.dp))
    }
}

/** "Didn’t get the email? Send it again". */
@Composable
private fun HelpLine(question: Int, link: Int, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(question), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onClick) { Text(stringResource(link), fontWeight = FontWeight.ExtraBold) }
    }
}

/** "No account yet? Create one", under a form. */
@Composable
private fun AltLine(question: Int, link: Int, onClick: () -> Unit) = HelpLine(question, link, onClick)

@Composable
private fun Link(text: Int, onClick: () -> Unit) {
    Text(
        stringResource(text),
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 4.dp),
    )
}
