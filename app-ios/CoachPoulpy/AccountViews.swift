import SwiftUI
import Shared

// The account (docs/accounts.md): Settings' Account row and page, and the screens that open over the tabs to sign
// in, sign up, confirm the email and reset a password. Mockups: design/mockups/agreed/phone/account-*. Every
// decision is AccountModel's; these views only show its answers. Mirrors AccountScreens.kt on Android.

/// An account screen opened over the tabs, from Settings or an email link.
struct AccountFlowStart: Identifiable {
    let id = UUID()
    let step: String
}

extension AppGraph {
    /// The one account of the app, made at launch so a signed-in phone syncs as soon as it opens.
    func makeAccount() -> AccountModel {
        account(
            baseUrl: "https://api.coach-poulpy.com",
            secrets: KeychainStore(),
            memory: UserDefaultsStore(),
            deviceName: UIDevice.current.name,
            languageCode: Language.code
        )
    }
}

extension AccountModel {
    func observedState() -> Observed<AccountModel, AccountState> {
        Observed(self, current: state.value as! AccountState, watch: watch, close: { _ in })
    }

    func observedUpload() -> Observed<AccountModel, UploadProgress?> {
        Observed(self, current: upload.value as? UploadProgress, watch: watchUpload, close: { _ in })
    }
}

/// The step an email link opens: `https://app.coach-poulpy.com/confirm-email#<token>` or `/reset-password#<token>`.
func accountStep(for url: URL) -> String? {
    guard url.scheme == "https", url.host == "app.coach-poulpy.com", let token = url.fragment, !token.isEmpty else { return nil }
    switch url.path {
    case "/confirm-email": return "confirm:\(token)"
    case "/reset-password": return "reset:\(token)"
    default: return nil
    }
}

// MARK: - Settings

/// Settings' Account section: one row that says where the phone stands and opens the next step.
struct AccountSection: View {
    @ObservedObject var account: Observed<AccountModel, AccountState>
    let onFlow: (String) -> Void

    var body: some View {
        Section("account.sectionLabel") {
            switch account.value {
            case let pending as AccountStatePending:
                NavigationLink(value: SettingsPage.account) { row(Text("account.rowPending"), L("account.rowPendingHint %@", pending.email)) }
            case let signedIn as AccountStateSignedIn:
                NavigationLink(value: SettingsPage.account) { row(Text(verbatim: signedIn.email), syncSummary(signedIn)) }
            default:
                Button { onFlow("sign-up") } label: {
                    HStack {
                        row(Text("account.guestRow"), L("account.guestRowHint"))
                        Spacer()
                        Image(systemName: "chevron.right").font(.system(size: 13, weight: .semibold)).foregroundStyle(PoulpyColor.onSurfaceVariant.opacity(0.6))
                    }
                }
                .foregroundStyle(PoulpyColor.onSurface)
            }
        }
    }

    private func row(_ title: Text, _ subtitle: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: "person.crop.circle").font(.system(size: 20)).foregroundStyle(PoulpyColor.primary).frame(width: 26)
            VStack(alignment: .leading, spacing: 0) {
                title.font(.poulpy(.headline))
                Text(subtitle).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
        .frame(minHeight: 44)
    }
}

/// "Up to date · Last synced 2 min ago", or that the training is on its way.
private func syncSummary(_ state: AccountStateSignedIn) -> String {
    [syncTitle(state), lastSynced(state)].compactMap { $0 }.joined(separator: " · ")
}

private func syncTitle(_ state: AccountStateSignedIn) -> String {
    state.upToDate ? L("account.stateUpToDate") : L("account.uploading")
}

private func lastSynced(_ state: AccountStateSignedIn) -> String? {
    guard let millis = state.lastSyncedEpochMillis else { return nil }
    let formatter = RelativeDateTimeFormatter()
    formatter.dateTimeStyle = .named
    let at = Date(timeIntervalSince1970: TimeInterval(millis.int64Value) / 1000)
    return L("account.stateLastSynced %@", formatter.localizedString(for: min(at, Date()), relativeTo: Date()))
}

private enum LeaveDialog { case signOut, unsent }

/// Settings › Account: the address, the sync state, what follows the account, Sign out and Delete account.
struct AccountPage: View {
    @ObservedObject var account: Observed<AccountModel, AccountState>
    let onFlow: (String) -> Void
    @Environment(\.dismiss) private var dismiss
    @State private var dialog: LeaveDialog?
    @State private var deleting = false
    // Refreshes "Last synced 2 min ago".
    @State private var now = Date()
    private let ticker = Timer.publish(every: 30, on: .main, in: .common).autoconnect()

    var body: some View {
        let state = account.value
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                if let email = email(state) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("account.email").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        Text(email).font(.poulpy(.headline).weight(.heavy))
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline, lineWidth: 1))
                }
                if let pending = state as? AccountStatePending {
                    VStack(alignment: .leading, spacing: 8) {
                        Label("account.pendingTitle", systemImage: "info.circle").font(.poulpy(.title3))
                        Text(L("account.pendingBody %@", pending.email)).font(.poulpy(.callout).weight(.semibold))
                        HelpLine(question: "account.noEmail", link: "account.sendAgain") {
                            Task { @MainActor in
                                if (try? await account.model.sendAgain()) is SignUpResultSent { onFlow("check-email:resent") }
                            }
                        }
                        HelpLine(question: "account.wrongAddress", link: "account.changeAddress") {
                            account.model.changeAddress()
                            onFlow("sign-up")
                        }
                    }
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .padding(EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14))
                    .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 16))
                    SyncState(on: false, title: L("account.stateNotSyncing"), detail: L("account.stateNotSyncingHint"))
                }
                if let signedIn = state as? AccountStateSignedIn {
                    SyncState(on: signedIn.upToDate, title: syncTitle(signedIn), detail: lastSynced(signedIn))
                        .id(now)
                    VStack(alignment: .leading, spacing: 6) {
                        Text("account.followsTitle").font(.poulpy(.title3))
                        Text("account.followsBody").font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline, lineWidth: 1))
                    // CLEANUP: Export my data (account-details mockup) waits for an export of the whole phone.
                    VStack(spacing: 4) {
                        Button { dialog = signedIn.upToDate ? .signOut : .unsent } label: {
                            Text("account.signOut")
                                .font(.poulpy(.headline).weight(.heavy))
                                .foregroundStyle(PoulpyColor.onSurface)
                                .frame(maxWidth: .infinity, minHeight: 52)
                                .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                        }
                        .buttonStyle(.plain)
                        Button { deleting = true } label: {
                            Text("account.deleteAccount").font(.poulpy(.headline).weight(.heavy)).foregroundStyle(PoulpyColor.error).frame(minHeight: 44)
                        }
                    }
                    .padding(.top, 8)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
        .background(PoulpyColor.background)
        .navigationTitle("account.title")
        .navigationBarTitleDisplayMode(.inline)
        .refreshable { _ = try? await account.model.refresh() }
        .onReceive(ticker) { now = $0 }
        .onChange(of: state is AccountStateGuest) { guest in
            // Signed out, deleted or changed address: nothing is left to show here.
            if guest { dismiss() }
        }
        .alert(L("account.signOutTitle"), isPresented: Binding(get: { dialog == .signOut }, set: { if !$0 { dialog = nil } })) {
            Button(LocalizedStringKey("action.cancel"), role: .cancel) {}
            Button(LocalizedStringKey("account.signOut")) { signOut(evenWithChanges: false) }
        } message: {
            Text("account.signOutBody")
        }
        .alert(L("account.unsentTitle"), isPresented: Binding(get: { dialog == .unsent }, set: { if !$0 { dialog = nil } })) {
            Button(LocalizedStringKey("account.waitForUpload"), role: .cancel) {}
            Button(LocalizedStringKey("account.signOutAnyway"), role: .destructive) { signOut(evenWithChanges: true) }
        } message: {
            Text("account.unsentBody")
        }
        .sheet(isPresented: $deleting) { DeleteSheet(account: account.model) }
    }

    private func email(_ state: AccountState) -> String? {
        (state as? AccountStatePending)?.email ?? (state as? AccountStateSignedIn)?.email
    }

    private func signOut(evenWithChanges: Bool) {
        Task { @MainActor in
            let result = try? await account.model.signOut(evenWithChanges: evenWithChanges)
            dialog = result == SignOutResult.changesWaiting ? .unsent : nil
        }
    }
}

/// Delete account: the password confirms it, and a wrong one deletes nothing.
private struct DeleteSheet: View {
    let account: AccountModel
    @Environment(\.dismiss) private var dismiss
    @State private var password = ""
    @State private var problem: DeleteResult?
    @State private var busy = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("account.deleteTitle").font(.poulpy(.title2))
                Text("account.deleteBody").font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                if problem == DeleteResult.wrongPassword { ErrorCard(L("account.errorDeletePassword")) }
                if problem == DeleteResult.offline { ErrorCard(L("account.errorOffline")) }
                PoulpyTextField(label: "account.deletePassword", text: $password, secure: true)
                HStack(spacing: 8) {
                    Button { dismiss() } label: {
                        Text("action.cancel")
                            .font(.poulpy(.headline).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onSurface)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .overlay(Capsule().stroke(PoulpyColor.outline, lineWidth: 2))
                    }
                    .buttonStyle(.plain)
                    Button {
                        busy = true
                        Task { @MainActor in
                            let result = (try? await account.deleteAccount(password: password)) ?? DeleteResult.offline
                            busy = false
                            if result == DeleteResult.deleted { dismiss() } else { problem = result }
                        }
                    } label: {
                        Text("account.deleteAccount")
                            .font(.poulpy(.headline).weight(.heavy))
                            .foregroundStyle(PoulpyColor.onError)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .background(PoulpyColor.error.opacity(password.isEmpty || busy ? 0.4 : 1), in: Capsule())
                    }
                    .buttonStyle(.plain)
                    .disabled(password.isEmpty || busy)
                }
                .padding(.top, 4)
            }
            .padding(20)
        }
        .presentationDetents([.medium, .large])
    }
}

/// A dot, green once everything is sent, with what the sync is doing.
private struct SyncState: View {
    let on: Bool
    let title: String
    let detail: String?

    var body: some View {
        HStack(spacing: 10) {
            Circle().fill(on ? PoulpyColor.success : PoulpyColor.outline).frame(width: 10, height: 10)
            VStack(alignment: .leading, spacing: 0) {
                Text(title).font(.poulpy(.headline).weight(.heavy))
                if let detail { Text(detail).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            }
        }
        .padding(.horizontal, 4)
    }
}

// MARK: - Screens over the tabs

/// The screens that open over the tabs, by step: "sign-up", "sign-up:<email>" (the address typed on sign-in), "sign-in", "sign-in:reset-done", "forgot",
/// "forgot-sent:<email>", "check-email", "check-email:resent", "welcome", "confirmed", "confirm:<token>" and
/// "reset:<token>" (the email links), "link-expired".
struct AccountFlowView: View {
    let account: AccountModel
    let onClose: () -> Void
    @State private var step: String

    init(account: AccountModel, start: String, onClose: @escaping () -> Void) {
        self.account = account
        self.onClose = onClose
        _step = State(initialValue: start)
    }

    var body: some View {
        let parts = step.split(separator: ":", maxSplits: 1).map(String.init)
        let name = parts.first ?? ""
        let arg = parts.count > 1 ? parts[1] : ""
        let go: (String) -> Void = { step = $0 }
        Group {
            switch name {
            case "sign-up": SignUpScreen(account: account, typedEmail: arg, onStep: go, onClose: onClose)
            case "sign-in": SignInScreen(account: account, resetDone: arg == "reset-done", onStep: go, onClose: onClose)
            case "forgot": ForgotScreen(account: account, onStep: go)
            case "forgot-sent": ForgotSentScreen(account: account, email: arg, onStep: go)
            case "check-email": CheckEmailScreen(account: account, resent: arg == "resent", onStep: go, onClose: onClose)
            case "welcome":
                UploadScreen(account: account, pose: "PoulpyPoseStandingTall", line: nil, title: "account.welcomeBackTitle", body: "account.mergeBody", onClose: onClose)
            case "confirmed":
                UploadScreen(account: account, pose: "PoulpyPoseChampion", line: "account.confirmedPoulpy", title: "account.confirmedTitle", body: nil, onClose: onClose)
            case "confirm": ConfirmScreen(account: account, token: arg, onStep: go, onClose: onClose)
            case "reset": ResetScreen(account: account, token: arg, onStep: go, onClose: onClose)
            default:
                AccountFrame(onClose: onClose, action: "account.backToSignUp", onAction: { go("sign-up") }) {
                    Poulpy(pose: "PoulpyPoseThinking", line: nil)
                    Intro(title: L("account.linkExpiredTitle"), body: Text("account.linkExpiredBody"))
                }
            }
        }
        // Each step starts afresh, as its own screen.
        .id(step)
    }
}

private struct SignUpScreen: View {
    let account: AccountModel
    let onStep: (String) -> Void
    let onClose: () -> Void
    @State private var email: String
    @State private var password = ""
    @State private var consent = false
    @State private var problems = SignUpProblems(email: false, password: false, consent: false)
    @State private var offline = false
    @State private var busy = false
    @State private var why = false

    init(account: AccountModel, typedEmail: String, onStep: @escaping (String) -> Void, onClose: @escaping () -> Void) {
        self.account = account
        self.onStep = onStep
        self.onClose = onClose
        _email = State(initialValue: typedEmail.isEmpty ? account.knownEmail ?? "" : typedEmail)
    }

    var body: some View {
        AccountFrame(onClose: onClose, action: "account.createAccount", busy: busy, onAction: send) {
            Intro(title: L("account.signUpTitle"), body: Text("account.signUpIntro"))
            if offline { ErrorCard(L("account.errorOffline")) }
            PoulpyTextField(label: "account.email", text: $email, error: problems.email ? L("account.errorEmail") : nil, keyboard: .emailAddress, contentType: .username)
            PoulpyTextField(
                label: "account.password", text: $password, note: "account.passwordHint",
                error: problems.password ? L("account.errorPasswordShort") : nil, secure: true, contentType: .newPassword
            )
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .top, spacing: 10) {
                    Button { consent.toggle() } label: {
                        Image(systemName: consent ? "checkmark.square.fill" : "square")
                            .font(.system(size: 24))
                            .foregroundStyle(consent ? PoulpyColor.primary : PoulpyColor.onSurfaceVariant)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text("account.consent"))
                    .accessibilityAddTraits(consent ? .isSelected : [])
                    VStack(alignment: .leading, spacing: 4) {
                        Text("account.consent").font(.poulpy(.callout)).onTapGesture { consent.toggle() }
                        Button { why = true } label: { Text("account.consentLink").font(.poulpy(.callout).weight(.heavy)) }
                    }
                }
                if problems.consent { FieldError(L("account.errorConsent")) }
            }
            HelpLine(question: "account.haveAccount", link: "account.signIn") { onStep("sign-in") }
        }
        .sheet(isPresented: $why) { ConsentSheet() }
    }

    private func send() {
        busy = true
        offline = false
        Task { @MainActor in
            let result = try? await account.signUp(email: email, password: password, healthDataConsent: consent)
            busy = false
            switch result {
            case is SignUpResultSent: onStep("check-email")
            case let invalid as SignUpResultInvalid: problems = invalid.problems
            default: offline = true
            }
        }
    }
}

/// What is stored and why, opened from the consent box.
private struct ConsentSheet: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                Text("account.consentSheetTitle").font(.poulpy(.title2))
                Text("account.consentSheetIntro").font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                ForEach(["account.consentItemTraining", "account.consentItemBody", "account.consentItemPrefs"], id: \.self) { key in
                    Label { Text(LocalizedStringKey(key)).font(.poulpy(.callout).weight(.bold)) } icon: {
                        Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary)
                    }
                }
                FieldNote("account.consentStays")
                FieldNote("account.consentWhy")
                Text("account.consentBefore").font(.poulpy(.footnote).weight(.heavy)).padding(.horizontal, 4)
                PrimaryButton(title: "account.gotIt") { dismiss() }.padding(.top, 4)
            }
            .padding(20)
        }
        .presentationDetents([.medium, .large])
    }
}

private struct SignInScreen: View {
    let account: AccountModel
    let resetDone: Bool
    let onStep: (String) -> Void
    let onClose: () -> Void
    @State private var email: String
    @State private var password = ""
    @State private var problem: String?
    @State private var busy = false

    init(account: AccountModel, resetDone: Bool, onStep: @escaping (String) -> Void, onClose: @escaping () -> Void) {
        self.account = account
        self.resetDone = resetDone
        self.onStep = onStep
        self.onClose = onClose
        _email = State(initialValue: account.knownEmail ?? "")
    }

    var body: some View {
        AccountFrame(onClose: onClose, action: "account.signIn", busy: busy || email.isEmpty || password.isEmpty, onAction: send) {
            Intro(title: L("account.signInTitle"), body: Text("account.signInIntro"))
            if let problem {
                ErrorCard(problem)
            } else if resetDone {
                InfoCard(L("account.passwordChanged"))
            }
            PoulpyTextField(label: "account.email", text: $email, keyboard: .emailAddress, contentType: .username)
            PoulpyTextField(label: "account.password", text: $password, secure: true, contentType: .password)
            Button { onStep("forgot") } label: { Text("account.forgot").font(.poulpy(.callout).weight(.heavy)) }
            HelpLine(question: "account.noAccount", link: "account.createOne") {
                let typed = email.trimmingCharacters(in: .whitespaces)
                onStep(typed.isEmpty ? "sign-up" : "sign-up:\(typed)")
            }
        }
    }

    private func send() {
        busy = true
        Task { @MainActor in
            let result = try? await account.signIn(email: email, password: password)
            busy = false
            switch result {
            case is SignInResultSignedIn: onClose()
            case is SignInResultJoining: onStep("welcome")
            case is SignInResultWrongEmailOrPassword: problem = L("account.errorSignIn")
            case is SignInResultTooManyTries: problem = L("account.errorTooManyTries")
            default: problem = L("account.errorOffline")
            }
        }
    }
}

private struct CheckEmailScreen: View {
    let account: AccountModel
    let onStep: (String) -> Void
    let onClose: () -> Void
    @State private var sentAgain: Bool
    @State private var offline = false

    init(account: AccountModel, resent: Bool, onStep: @escaping (String) -> Void, onClose: @escaping () -> Void) {
        self.account = account
        self.onStep = onStep
        self.onClose = onClose
        _sentAgain = State(initialValue: resent)
    }

    var body: some View {
        let email = (account.state.value as? AccountStatePending)?.email ?? account.knownEmail ?? ""
        AccountFrame(onClose: onClose, action: "account.ok", onAction: onClose) {
            Poulpy(pose: "PoulpyPoseStandingTall", line: "account.checkEmailPoulpy")
            Intro(title: L("account.checkEmailTitle"), body: emphasizing(L("account.checkEmailBody %@", email), email))
            InfoCard(L("account.checkEmailGuest"))
            if offline {
                ErrorCard(L("account.errorOffline"))
            } else if sentAgain {
                DoneLine(L("account.resent"))
            }
            HelpLine(question: "account.noEmail", link: "account.sendAgain") {
                Task { @MainActor in
                    let result = try? await account.sendAgain()
                    sentAgain = result is SignUpResultSent
                    offline = !sentAgain
                }
            }
            HelpLine(question: "account.wrongAddress", link: "account.changeAddress") {
                account.changeAddress()
                onStep("sign-up")
            }
        }
    }
}

private struct ForgotScreen: View {
    let account: AccountModel
    let onStep: (String) -> Void
    @State private var email: String
    @State private var offline = false
    @State private var busy = false

    init(account: AccountModel, onStep: @escaping (String) -> Void) {
        self.account = account
        self.onStep = onStep
        _email = State(initialValue: account.knownEmail ?? "")
    }

    var body: some View {
        AccountFrame(onClose: { onStep("sign-in") }, action: "account.sendLink", busy: busy || email.isEmpty, onAction: send) {
            Intro(title: L("account.forgotTitle"), body: Text("account.forgotIntro"))
            if offline { ErrorCard(L("account.errorOffline")) }
            PoulpyTextField(label: "account.email", text: $email, keyboard: .emailAddress, contentType: .username)
        }
    }

    private func send() {
        busy = true
        Task { @MainActor in
            let sent = (try? await account.requestPasswordReset(email: email))?.boolValue ?? false
            busy = false
            if sent {
                onStep("forgot-sent:\(email.trimmingCharacters(in: .whitespaces))")
            } else {
                offline = true
            }
        }
    }
}

private struct ForgotSentScreen: View {
    let account: AccountModel
    let email: String
    let onStep: (String) -> Void
    @State private var sentAgain = false
    @State private var offline = false

    var body: some View {
        AccountFrame(onClose: { onStep("sign-in") }, action: "account.ok", onAction: { onStep("sign-in") }) {
            Poulpy(pose: "PoulpyPoseStandingTall", line: nil)
            Intro(title: L("account.checkEmailTitle"), body: emphasizing(L("account.forgotSentBody %@", email), email))
            InfoCard(L("account.forgotSentNote"))
            if offline {
                ErrorCard(L("account.errorOffline"))
            } else if sentAgain {
                DoneLine(L("account.resent"))
            }
            HelpLine(question: "account.noEmail", link: "account.sendAgain") {
                Task { @MainActor in
                    sentAgain = (try? await account.requestPasswordReset(email: email))?.boolValue ?? false
                    offline = !sentAgain
                }
            }
        }
    }
}

/// Welcome back and Email confirmed: the first upload, which carries on in the background.
private struct UploadScreen: View {
    @StateObject private var upload: Observed<AccountModel, UploadProgress?>
    let pose: String
    let line: LocalizedStringKey?
    let title: String
    let bodyKey: LocalizedStringKey?
    let onClose: () -> Void

    init(account: AccountModel, pose: String, line: LocalizedStringKey?, title: String, body: LocalizedStringKey?, onClose: @escaping () -> Void) {
        _upload = StateObject(wrappedValue: account.observedUpload())
        self.pose = pose
        self.line = line
        self.title = title
        bodyKey = body
        self.onClose = onClose
    }

    var body: some View {
        let progress = upload.value
        AccountFrame(onClose: nil, action: "account.continue", onAction: onClose) {
            Poulpy(pose: pose, line: line)
            Intro(title: L(title), body: bodyKey.map { Text($0) })
            VStack(alignment: .leading, spacing: 10) {
                Text("account.uploading").font(.poulpy(.headline).weight(.heavy))
                ProgressView(value: progress.map { $0.total == 0 ? 1 : Double($0.sent) / Double($0.total) } ?? 1)
                    .tint(PoulpyColor.primary)
                if let progress {
                    Text(L("account.uploadProgress %lld %lld", progress.sent, progress.total))
                        .font(.poulpy(.footnote))
                        .foregroundStyle(PoulpyColor.onSurfaceVariant)
                }
            }
            .padding(16)
            .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.outline, lineWidth: 1))
            FieldNote("account.uploadOffline")
        }
    }
}

/// The confirmation link, opened on this phone: confirms, signs in and moves on to what follows.
private struct ConfirmScreen: View {
    let account: AccountModel
    let token: String
    let onStep: (String) -> Void
    let onClose: () -> Void
    @State private var offline = false

    var body: some View {
        Group {
            if offline {
                AccountFrame(onClose: onClose, action: "account.ok", onAction: onClose) {
                    Intro(title: L("account.pendingTitle"), body: nil)
                    ErrorCard(L("account.errorOffline"))
                }
            } else {
                PoulpyLoader(size: 112).frame(maxWidth: .infinity, maxHeight: .infinity).background(PoulpyColor.background)
            }
        }
        .task {
            let result = (try? await account.confirmEmail(token: token)) ?? ConfirmResult.offline
            switch result {
            case .confirmed: onStep("confirmed")
            case .confirmedSignIn: onStep("sign-in")
            case .linkExpired: onStep("link-expired")
            default: offline = true
            }
        }
    }
}

/// The reset link: a new password, which signs every phone out.
private struct ResetScreen: View {
    let account: AccountModel
    let token: String
    let onStep: (String) -> Void
    let onClose: () -> Void
    @State private var password = ""
    @State private var problem: NewPasswordResult?
    @State private var busy = false

    var body: some View {
        AccountFrame(onClose: onClose, action: "account.savePassword", busy: busy || password.isEmpty, onAction: save) {
            Intro(title: L("account.resetTitle"), body: Text("account.resetIntro"))
            if problem == NewPasswordResult.offline { ErrorCard(L("account.errorOffline")) }
            PoulpyTextField(
                label: "account.newPassword", text: $password, note: "account.passwordHint",
                error: problem == NewPasswordResult.tooShort ? L("account.errorPasswordShort") : nil, secure: true, contentType: .newPassword
            )
        }
    }

    private func save() {
        busy = true
        Task { @MainActor in
            let result = (try? await account.setNewPassword(token: token, password: password)) ?? NewPasswordResult.offline
            busy = false
            switch result {
            case .saved: onStep("sign-in:reset-done")
            case .linkExpired: onStep("link-expired")
            default: problem = result
            }
        }
    }
}

// MARK: - Parts

/// Every account screen: a close button, the content scrolling, and one wide action at the bottom.
private struct AccountFrame<Content: View>: View {
    let onClose: (() -> Void)?
    let action: LocalizedStringKey
    var busy = false
    let onAction: () -> Void
    @ViewBuilder var content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack {
                if let onClose {
                    Button(action: onClose) { Image(systemName: "xmark").font(.system(size: 18, weight: .semibold)) }
                        .accessibilityLabel(Text("action.close"))
                }
                Spacer()
            }
            .frame(minHeight: 44)
            .padding(.horizontal, 20)
            .padding(.vertical, 4)
            ScrollView {
                VStack(alignment: .leading, spacing: 16, content: content)
                    .padding(.horizontal, 20)
                    .padding(.vertical, 8)
            }
        }
        .background(PoulpyColor.background)
        .safeAreaInset(edge: .bottom) {
            PrimaryButton(title: action, enabled: !busy, action: onAction)
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(PoulpyColor.surface)
        }
    }
}

private struct PrimaryButton: View {
    let title: LocalizedStringKey
    var enabled = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.poulpy(.headline).weight(.heavy))
                .foregroundStyle(PoulpyColor.onPrimary)
                .frame(maxWidth: .infinity, minHeight: 56)
                .background(PoulpyColor.primary.opacity(enabled ? 1 : 0.4), in: Capsule())
        }
        .buttonStyle(.plain)
        .disabled(!enabled)
    }
}

private struct Intro: View {
    let title: String
    let detail: Text?

    init(title: String, body detail: Text?) {
        self.title = title
        self.detail = detail
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title).font(.custom(PoulpyFontFace.baloo2ExtraBold, size: 30, relativeTo: .title))
            if let detail { detail.font(.poulpy(.callout)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
        }
    }
}

/// Poulpy in the middle, with what he says under him.
private struct Poulpy: View {
    let pose: String
    let line: LocalizedStringKey?

    var body: some View {
        VStack(spacing: 8) {
            Image(pose).resizable().scaledToFit().frame(width: 140, height: 140).accessibilityHidden(true)
            if let line { Text(line).font(.poulpy(.title3)).multilineTextAlignment(.center) }
        }
        .frame(maxWidth: .infinity)
    }
}

/// A problem: crimson, with an icon.
private struct ErrorCard: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            Image(systemName: "exclamationmark.circle").font(.system(size: 18, weight: .semibold))
            Text(text).font(.poulpy(.callout).weight(.bold))
        }
        .foregroundStyle(PoulpyColor.error)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14))
        .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(PoulpyColor.error, lineWidth: 1.5))
    }
}

private struct InfoCard: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: "info.circle")
            Text(text).font(.poulpy(.callout).weight(.semibold))
        }
        .foregroundStyle(PoulpyColor.onPrimaryContainer)
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(EdgeInsets(top: 12, leading: 14, bottom: 12, trailing: 14))
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: 16))
    }
}

private struct DoneLine: View {
    let text: String
    init(_ text: String) { self.text = text }

    var body: some View {
        Label(text, systemImage: "checkmark.circle.fill")
            .font(.poulpy(.subheadline).weight(.heavy))
            .foregroundStyle(PoulpyColor.success)
            .frame(maxWidth: .infinity)
    }
}

/// "Didn’t get the email? Send it again".
private struct HelpLine: View {
    let question: LocalizedStringKey
    let link: LocalizedStringKey
    let action: () -> Void

    var body: some View {
        HStack(spacing: 4) {
            Text(question).font(.poulpy(.subheadline))
            Button(action: action) { Text(link).font(.poulpy(.subheadline).weight(.heavy)) }
        }
        .frame(maxWidth: .infinity)
    }
}

/// `text` with `part` in bold, as the address in "Check your email".
private func emphasizing(_ text: String, _ part: String) -> Text {
    guard !part.isEmpty, let range = text.range(of: part) else { return Text(verbatim: text) }
    return Text(verbatim: String(text[..<range.lowerBound])) + Text(verbatim: part).bold() + Text(verbatim: String(text[range.upperBound...]))
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
