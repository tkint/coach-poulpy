import SwiftUI
import Shared

/// The screens Settings › Devices opens.
enum DeviceRoute: Hashable {
    case add, sources
    case device(String)
}

/// The You tab: You itself, and the pages it opens (Settings, Profile, Account and Devices). The account's screens open over
/// the tabs through `onAccount`.
struct SettingsView: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>
    @ObservedObject var account: Observed<AccountModel, AccountState>
    let onAccount: (String) -> Void
    @StateObject private var you: Observed<YouModel, YouState>
    @StateObject private var devices: Observed<DevicesModel, DevicesUiState>
    @StateObject private var profile: Observed<ProfileModel, ProfileUi>
    @StateObject private var weight: Observed<BodyModel, BodyUi>
    @StateObject private var height: Observed<BodyModel, BodyUi>
    @StateObject private var bluetooth = BluetoothGate()
    @Binding var path: NavigationPath

    init(
        graph: AppGraph,
        settings: Observed<SettingsModel, SettingsUiState>,
        account: Observed<AccountModel, AccountState>,
        path: Binding<NavigationPath>,
        onAccount: @escaping (String) -> Void
    ) {
        self.settings = settings
        self.account = account
        self.onAccount = onAccount
        _you = StateObject(wrappedValue: graph.observedYou())
        _devices = StateObject(wrappedValue: graph.observedDevices())
        _profile = StateObject(wrappedValue: graph.observedProfile())
        _weight = StateObject(wrappedValue: graph.observedBody(.weight))
        _height = StateObject(wrappedValue: graph.observedBody(.height))
        _path = path
    }

    var body: some View {
        NavigationStack(path: $path) {
            YouView(you: you, account: account, profile: profile)
                .navigationDestination(for: SettingsPage.self) { page in
                    Group {
                        switch page {
                        case .settings: SettingsPageView(settings: settings, devices: devices)
                        case .profile: ProfileView(profile: profile, account: account, weight: weight, height: height, units: settings.value.units)
                        case .account: AccountPage(account: account, onFlow: onAccount)
                        case .accountPassword: AccountPasswordPage(account: account)
                        case .accountDevices: AccountDevicesPage(account: account)
                        case .accountExport: AccountExportPage(account: account)
                        case .devices:
                            DevicesList(devices: devices, bluetooth: bluetooth, onAdd: { path.append(DeviceRoute.add) })
                                .navigationTitle("settings.devices")
                                .navigationBarTitleDisplayMode(.inline)
                                .toolbar {
                                    ToolbarItem(placement: .navigationBarTrailing) {
                                        NavigationLink(value: DeviceRoute.add) {
                                            LucideIcon(.plus)
                                        }
                                        .accessibilityLabel(Text("devices.add"))
                                    }
                                }
                        }
                    }
                    // These pages take the whole screen, as drawn.
                    .toolbar(.hidden, for: .tabBar)
                }
                .navigationDestination(for: DeviceRoute.self) { route in
                    Group {
                        switch route {
                        case .add: DeviceAddView(devices: devices)
                        case .sources: DataSourcesView(devices: devices)
                        case .device(let key): DeviceView(devices: devices, key: key)
                        }
                    }
                    .toolbar(.hidden, for: .tabBar)
                }
        }
    }
}

/// Settings, opened by the cog on You: Appearance, Region, Devices, App icon and About.
private struct SettingsPageView: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    @State private var appIcon = AppIcon.current
    @State private var pickingIcon = false

    var body: some View {
        let state = settings.value
        let model = settings.model
        List {
            Section {
                Picker("settings.theme", selection: Binding(get: { state.theme }, set: { model.setTheme(theme: $0) })) {
                    Text("theme.system").tag(ThemeChoice.system)
                    Text("theme.light").tag(ThemeChoice.light)
                    Text("theme.dark").tag(ThemeChoice.dark)
                }
                .pickerStyle(.menu)
            } header: {
                Text("settings.appearance")
            } footer: {
                Text("settings.themeHint")
            }
            Section {
                // Language names stay in their own language, so anyone can find theirs.
                Picker("settings.language", selection: Binding(get: { state.language }, set: {
                    model.setLanguage(language: $0)
                    AppLanguage.apply($0)
                })) {
                    Text("language.system").tag(LanguageChoice.system)
                    Text(verbatim: "English").tag(LanguageChoice.english)
                    Text(verbatim: "Français").tag(LanguageChoice.french)
                }
                .pickerStyle(.menu)
                Picker("settings.units", selection: Binding(get: { state.units }, set: { model.setUnits(units: $0) })) {
                    Text("units.metric").tag(UnitSystem.metric)
                    Text("units.imperial").tag(UnitSystem.imperial)
                }
                .pickerStyle(.menu)
            } header: {
                Text("settings.region")
            } footer: {
                Text("settings.languageNextLaunch") + Text(verbatim: " ") + Text("settings.unitsHint")
            }
            Section("settings.app") {
                NavigationLink(value: SettingsPage.devices) { row(.gauge, "settings.devices", devicesLine()) }
                Button { pickingIcon = true } label: {
                    HStack(spacing: 12) {
                        Image(appIcon.drawing).resizable().scaledToFit().frame(width: 30, height: 30).scaleEffect(1.6).frame(width: 26).padding(.bottom, 4).accessibilityHidden(true)
                        VStack(alignment: .leading, spacing: 0) {
                            Text("settings.appIcon").font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                            Text(appIcon.label).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                        Spacer()
                        LucideIcon(.chevronRight, size: 16).foregroundStyle(PoulpyColor.onSurfaceVariant.opacity(0.6))
                    }
                    .frame(maxWidth: .infinity, minHeight: 44, alignment: .leading)
                }
            }
            Section("settings.about") {
                row(.info, "settings.aboutApp", L("settings.aboutLine %@", Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "—"))
            }
        }
        .navigationTitle("tab.settings")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(isPresented: $pickingIcon) {
            AppIconSheet(selected: appIcon) { picked in
                appIcon = picked
                picked.apply()
            }
        }
    }

    /// "3 connected · treadmill, foot pod, heart rate band", or that nothing was added.
    private func devicesLine() -> String {
        let known = devices.value.known
        if known.isEmpty { return L("settings.devicesNone") }
        var kinds: [String] = []
        for device in known {
            let name = kindLabel(device.kind).lowercased()
            if !kinds.contains(name) { kinds.append(name) }
        }
        let connected = known.filter { $0.status == .connected }.count
        if connected > 0 { return L("settings.devicesLine %lld %@", Int64(connected), kinds.joined(separator: ", ")) }
        return L("settings.devicesIdle %@", kinds.joined(separator: ", "))
    }

    /// A leading icon, a bold title with a line under it.
    private func row(_ symbol: Lucide, _ title: LocalizedStringKey, _ subtitle: String?) -> some View {
        HStack(spacing: 12) {
            LucideIcon(symbol, size: 24).foregroundStyle(PoulpyColor.primary).frame(width: 26)
            VStack(alignment: .leading, spacing: 0) {
                Text(title).font(.poulpy(.headline))
                if let subtitle { Text(subtitle).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            }
        }
        .frame(minHeight: 44)
    }
}

private struct DevicesList: View {
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    @ObservedObject var bluetooth: BluetoothGate
    let onAdd: () -> Void

    var body: some View {
        let state = devices.value
        List {
            BluetoothProblems(bluetooth: bluetooth)
            if state.known.isEmpty {
                Section {
                    EmptyState(
                        title: Text("devices.emptyTitle"),
                        message: Text("devices.emptyBody") + Text(" ") + Text("devices.switchOn"),
                        button: Text("devices.addFirst"),
                        icon: .plus,
                        action: onAdd
                    )
                    .listRowBackground(Color.clear)
                    .listRowSeparator(.hidden)
                }
            } else {
                Section("devices.yours") {
                    ForEach(state.known, id: \.key) { device in
                        NavigationLink(value: DeviceRoute.device(device.key)) {
                            KnownRow(device: device) {
                                bluetooth.whenReady { devices.model.connect(key: device.key) }
                            }
                        }
                    }
                }
                if !state.sources.isEmpty {
                    Section {
                        NavigationLink(value: DeviceRoute.sources) {
                            Label {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("devices.sources").font(.poulpy(.headline))
                                    Text("devices.sourcesRowHint").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                                }
                            } icon: {
                                LucideIcon(.slidersHorizontal).foregroundStyle(PoulpyColor.primary)
                            }
                        }
                    }
                }
            }
        }
        .onAppear {
            if BluetoothGate.wasAllowed {
                bluetooth.whenReady { devices.model.connectAutomatically() }
            }
        }
    }
}

private struct KnownRow: View {
    let device: KnownDeviceUi
    let onConnect: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            DeviceAvatar(kind: device.kind, on: device.status == .connected)
            VStack(alignment: .leading, spacing: 2) {
                Text(device.name).font(.poulpy(.headline))
                StatusLine(device: device)
                Text(device.provides.map(quantityLabel).joined(separator: ", "))
                    .font(.poulpy(.footnote))
                    .foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Spacer(minLength: 0)
            if device.status == .idle || device.status == .failed {
                // Borderless, so a tap on it connects instead of opening the device.
                Button(LocalizedStringKey("devices.connect"), action: onConnect)
                    .buttonStyle(.borderless)
                    .font(.poulpy(.headline))
            }
        }
        .padding(.vertical, 4)
    }
}

private struct StatusLine: View {
    let device: KnownDeviceUi

    var body: some View {
        let connected = device.status == .connected
        HStack(spacing: 6) {
            if device.status == .connecting || device.status == .reconnecting {
                PoulpyLoader(size: 16)
            } else {
                Circle().fill(connected ? PoulpyColor.success : PoulpyColor.outline).frame(width: 8, height: 8)
            }
            Text([statusLabel(device.status), device.batteryPercent.map { L("devices.battery %lld", Int64($0.intValue)) }].compactMap { $0 }.joined(separator: " · "))
                .font(.custom(PoulpyFontFace.nunitoBold, size: 15, relativeTo: .subheadline))
                .foregroundStyle(connected ? PoulpyColor.success : PoulpyColor.onSurfaceVariant)
        }
    }
}

private struct DeviceAvatar: View {
    let kind: DeviceKind?
    let on: Bool
    var size: CGFloat = 44

    var body: some View {
        LucideIcon(kindIcon(kind))
            .foregroundStyle(on ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurfaceVariant)
            .frame(width: size, height: size)
            .background(on ? PoulpyColor.primaryContainer : PoulpyColor.surfaceVariant, in: Circle())
            .accessibilityHidden(true)
    }
}

private struct BluetoothProblems: View {
    @ObservedObject var bluetooth: BluetoothGate

    var body: some View {
        if bluetooth.isOff || bluetooth.isDenied {
            Section {
                if bluetooth.isOff {
                    Text("devices.bluetoothOff").foregroundStyle(PoulpyColor.error)
                }
                if bluetooth.isDenied {
                    Text("devices.permissionNeeded").foregroundStyle(PoulpyColor.error)
                    Button(LocalizedStringKey("devices.openSettings")) {
                        if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
                    }
                }
            }
        }
    }
}

/// Searches for devices to add, for as long as the screen is open.
private struct DeviceAddView: View {
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    @StateObject private var bluetooth = BluetoothGate()
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let state = devices.value
        List {
            BluetoothProblems(bluetooth: bluetooth)
            Section {
                VStack(spacing: 8) {
                    if state.scanning {
                        PoulpyLoader(size: 112)
                    }
                    Text("treadmill.scanning").font(PoulpyFont.headline).multilineTextAlignment(.center)
                    Text("devices.scanHint").multilineTextAlignment(.center)
                }
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .frame(maxWidth: .infinity)
                .padding(.vertical, PoulpySpace.md)
                .listRowBackground(PoulpyColor.primaryContainer)
                if let error = state.error {
                    Text(error).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.error)
                }
            }
            if !state.found.isEmpty {
                Section("devices.found") {
                    ForEach(state.found, id: \.key) { device in
                        HStack(spacing: 12) {
                            DeviceAvatar(kind: device.kind, on: device.kind != nil)
                            VStack(alignment: .leading, spacing: 2) {
                                Text(device.name).font(.poulpy(.headline))
                                Text(kindLabel(device.kind)).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                            }
                            Spacer(minLength: 0)
                            Button(LocalizedStringKey("devices.addOne")) {
                                devices.model.add(key: device.key)
                                dismiss()
                            }
                            .buttonStyle(PoulpyButtonStyle(.primary, size: .md))
                        }
                    }
                }
            }
            Section {
            } footer: {
                Text("devices.addedHidden")
            }
        }
        .navigationTitle("devices.add")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear { bluetooth.whenReady { devices.model.startScan() } }
        .onDisappear { devices.model.stopScan() }
    }
}

/// One added device: what it measures and which of those numbers Live uses, plus connect, auto-connect and forget.
private struct DeviceView: View {
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    let key: String
    @StateObject private var bluetooth = BluetoothGate()
    @State private var confirmForget = false
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        let state = devices.value
        let model = devices.model
        if let device = state.known.first(where: { $0.key == key }) {
            List {
                Section {
                    HStack(spacing: 14) {
                        DeviceAvatar(kind: device.kind, on: device.status == .connected, size: 56)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(kindLabel(device.kind)).font(.poulpy(.headline))
                            StatusLine(device: device)
                            if let error = device.error {
                                Text(error).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.error)
                            }
                        }
                    }
                    .padding(.vertical, 6)
                    .listRowBackground(PoulpyColor.primaryContainer)
                }
                Section("devices.measures") {
                    ForEach(device.provides, id: \.self) { quantity in
                        HStack {
                            Text(quantityLabel(quantity)).font(.poulpy(.headline))
                            Spacer()
                            if device.status == .connected {
                                usedPill(device, quantity, known: state.known)
                            }
                        }
                    }
                    if !state.sources.isEmpty {
                        NavigationLink(value: DeviceRoute.sources) {
                            Text("devices.changeSources").foregroundStyle(PoulpyColor.primary)
                        }
                    }
                }
                Section("settings.devices") {
                    Toggle(isOn: Binding(get: { device.autoConnect }, set: { model.setAutoConnect(key: device.key, on: $0) })) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("devices.autoConnect").font(.poulpy(.headline))
                            Text("devices.autoConnectHint").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                    }
                }
                BluetoothProblems(bluetooth: bluetooth)
                Section {
                    if device.status == .idle || device.status == .failed {
                        Button(LocalizedStringKey("devices.connect")) {
                            bluetooth.whenReady { model.connect(key: device.key) }
                        }
                    } else {
                        Button(LocalizedStringKey("devices.disconnect")) { model.disconnect(key: device.key) }
                    }
                    Button(LocalizedStringKey("devices.forget"), role: .destructive) { confirmForget = true }
                }
            }
            .navigationTitle(device.name)
            .navigationBarTitleDisplayMode(.inline)
            .alert(L("devices.forgetTitle %@", device.name), isPresented: $confirmForget) {
                Button(LocalizedStringKey("devices.forget"), role: .destructive) {
                    model.forget(key: device.key)
                    dismiss()
                }
                Button(LocalizedStringKey("action.cancel"), role: .cancel) {}
            } message: {
                Text("devices.forgetBody")
            }
        } else {
            Color.clear.onAppear { dismiss() }
        }
    }

    @ViewBuilder
    private func usedPill(_ device: KnownDeviceUi, _ quantity: Quantity, known: [KnownDeviceUi]) -> some View {
        let used = device.usedFor.contains(quantity)
        let instead = known.first { $0.key != device.key && $0.usedFor.contains(quantity) }
        if used || instead != nil {
            Text(used ? L("devices.used") : L("devices.usedInstead %@", instead?.name ?? ""))
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 13, relativeTo: .footnote))
                .foregroundStyle(used ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurfaceVariant)
                .padding(.horizontal, 10)
                .padding(.vertical, 4)
                .background(used ? PoulpyColor.primaryContainer : PoulpyColor.surfaceVariant, in: Capsule())
        }
    }
}

/// For each number two added devices measure: automatic, or one of them.
private struct DataSourcesView: View {
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    @State private var picking: SourceSheet?

    var body: some View {
        let state = devices.value
        let model = devices.model
        ScrollView {
            VStack(alignment: .leading, spacing: 12) {
                Text("devices.sourcesHint").font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                ForEach(state.sources, id: \.quantity) { source in
                    SelectField(label: LocalizedStringKey(quantityLabel(source.quantity)), value: source.options.first(where: { $0.key == source.choiceKey })?.name ?? L("devices.sourceAutomatic")) {
                        picking = SourceSheet(quantity: source.quantity)
                    }
                }
            }
            .padding(.horizontal, PoulpySpace.lg)
            .padding(.vertical, 16)
        }
        .background(PoulpyColor.background)
        .navigationTitle("devices.sources")
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $picking) { item in
            if let source = state.sources.first(where: { $0.quantity == item.quantity }) {
                OptionSheet(
                    title: LocalizedStringKey(quantityLabel(item.quantity)), why: nil, note: nil, selected: source.choiceKey,
                    options: [(String?.none, L("devices.sourceAutomatic"))] + source.options.map { (String?.some($0.key), $0.name) },
                    onSave: { model.setSource(quantity: item.quantity, key: $0) }
                )
            }
        }
    }
}

private struct SourceSheet: Identifiable {
    let quantity: Quantity
    var id: String { quantityLabel(quantity) }
}

private func statusLabel(_ status: ConnectionStatus) -> String {
    switch status {
    case .connected: return L("devices.statusConnected")
    case .connecting: return L("devices.statusConnecting")
    case .reconnecting: return L("devices.statusReconnecting")
    case .failed: return L("devices.statusFailed")
    default: return L("devices.statusIdle")
    }
}

private func quantityLabel(_ quantity: Quantity) -> String {
    switch quantity {
    case .speed: return L("quantity.speed")
    case .heartRate: return L("quantity.heartRate")
    case .cadence: return L("quantity.cadence")
    case .incline: return L("quantity.incline")
    case .energy: return L("quantity.energy")
    default: return L("quantity.power")
    }
}

private func kindLabel(_ kind: DeviceKind?) -> String {
    if kind == DeviceKind.treadmill { return L("kind.treadmill") }
    if kind == DeviceKind.footPod { return L("kind.footPod") }
    if kind == DeviceKind.heartRate { return L("kind.heartRate") }
    return L("kind.unknown")
}

private func kindIcon(_ kind: DeviceKind?) -> Lucide {
    guard let kind else { return .bluetooth }
    if kind == DeviceKind.heartRate { return .heartPulse }
    return kind == DeviceKind.footPod ? .footprints : .gauge
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
