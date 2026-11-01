import SwiftUI
import Shared

/// The screens Settings › Devices opens.
enum DeviceRoute: Hashable {
    case add, sources
    case device(String)
}

/// The Settings tab: a hub that opens the profile, weight, height, appearance and units, and devices.
struct SettingsView: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>
    @StateObject private var devices: Observed<DevicesModel, DevicesUiState>
    @StateObject private var profile: Observed<ProfileModel, ProfileUi>
    @StateObject private var weight: Observed<BodyModel, BodyUi>
    @StateObject private var height: Observed<BodyModel, BodyUi>
    @StateObject private var bluetooth = BluetoothGate()
    @Binding var path: NavigationPath

    init(graph: AppGraph, settings: Observed<SettingsModel, SettingsUiState>, path: Binding<NavigationPath>) {
        self.settings = settings
        _devices = StateObject(wrappedValue: graph.observedDevices())
        _profile = StateObject(wrappedValue: graph.observedProfile())
        _weight = StateObject(wrappedValue: graph.observedBody(.weight))
        _height = StateObject(wrappedValue: graph.observedBody(.height))
        _path = path
    }

    var body: some View {
        NavigationStack(path: $path) {
            SettingsHub(settings: settings, profile: profile, weight: weight, height: height)
                .navigationTitle("tab.settings")
                .navigationDestination(for: SettingsPage.self) { page in
                    switch page {
                    case .profile: ProfileView(profile: profile)
                    case .weight: BodyView(metric: .weight, log: weight, units: settings.value.units)
                    case .height: BodyView(metric: .height, log: height, units: settings.value.units)
                    case .preferences: PreferencesView(settings: settings)
                    case .devices:
                        DevicesList(devices: devices, bluetooth: bluetooth)
                            .navigationTitle("settings.devices")
                            .navigationBarTitleDisplayMode(.inline)
                            .toolbar {
                                ToolbarItem(placement: .navigationBarTrailing) {
                                    NavigationLink(value: DeviceRoute.add) {
                                        Image(systemName: "plus")
                                    }
                                    .accessibilityLabel(Text("devices.add"))
                                }
                            }
                    }
                }
                .navigationDestination(for: DeviceRoute.self) { route in
                    switch route {
                    case .add: DeviceAddView(devices: devices)
                    case .sources: DataSourcesView(devices: devices)
                    case .device(let key): DeviceView(devices: devices, key: key)
                    }
                }
        }
    }
}

private struct DevicesList: View {
    @ObservedObject var devices: Observed<DevicesModel, DevicesUiState>
    @ObservedObject var bluetooth: BluetoothGate

    var body: some View {
        let state = devices.value
        List {
            BluetoothProblems(bluetooth: bluetooth)
            if state.known.isEmpty {
                Section {
                    VStack(spacing: 10) {
                        Image("PoulpyPoseStandingTall").resizable().scaledToFit().frame(width: 140, height: 126).accessibilityHidden(true)
                        Text("devices.emptyTitle").font(PoulpyFont.headline).multilineTextAlignment(.center)
                        Text("devices.emptyBody").multilineTextAlignment(.center)
                        NavigationLink(value: DeviceRoute.add) {
                            Label("devices.addFirst", systemImage: "plus")
                                .font(.poulpy(.headline))
                                .foregroundStyle(PoulpyColor.onPrimary)
                                .padding(.horizontal, 24)
                                .frame(height: 56)
                                .background(PoulpyColor.primary, in: Capsule())
                        }
                        .buttonStyle(.plain)
                        .padding(.top, PoulpySpace.sm)
                        Text("devices.switchOn").font(.poulpy(.footnote))
                    }
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, PoulpySpace.lg)
                    .listRowBackground(PoulpyColor.primaryContainer)
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
                                Image(systemName: "slider.horizontal.3").foregroundStyle(PoulpyColor.primary)
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
        Image(systemName: kindIcon(kind))
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
                            .buttonStyle(.borderedProminent)
                            .buttonBorderShape(.capsule)
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
            .padding(.horizontal, 20)
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

private func kindIcon(_ kind: DeviceKind?) -> String {
    guard let kind else { return "dot.radiowaves.left.and.right" }
    return kind == DeviceKind.heartRate ? "heart.fill" : "figure.run"
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
