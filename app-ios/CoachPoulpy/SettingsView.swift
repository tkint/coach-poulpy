import SwiftUI
import Shared

enum SettingsSection: Hashable { case general, devices }

/// The screens Settings › Devices opens.
enum DeviceRoute: Hashable {
    case add, sources
    case device(String)
}

/// The Settings tab: General first, then Devices.
struct SettingsView: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>
    @StateObject private var devices: Observed<DevicesModel, DevicesUiState>
    @StateObject private var bluetooth = BluetoothGate()
    @Binding var section: SettingsSection

    init(graph: AppGraph, settings: Observed<SettingsModel, SettingsUiState>, section: Binding<SettingsSection>) {
        self.settings = settings
        _devices = StateObject(wrappedValue: graph.observedDevices())
        _section = section
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                Picker(selection: $section) {
                    Text("settings.general").tag(SettingsSection.general)
                    Text("settings.devices").tag(SettingsSection.devices)
                } label: {
                    EmptyView()
                }
                .pickerStyle(.segmented)
                .padding(.horizontal, 20)
                .padding(.vertical, PoulpySpace.sm)
                switch section {
                case .general: GeneralList(settings: settings)
                case .devices: DevicesList(devices: devices, bluetooth: bluetooth)
                }
            }
            .background(Color(UIColor.systemGroupedBackground))
            .navigationTitle("tab.settings")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    if section == .devices {
                        NavigationLink(value: DeviceRoute.add) {
                            Image(systemName: "plus")
                        }
                        .accessibilityLabel(Text("devices.add"))
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

private struct GeneralList: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>

    var body: some View {
        let state = settings.value
        let model = settings.model
        List {
            Section {
                choice("settings.theme", selection: Binding(get: { state.theme }, set: { model.setTheme(theme: $0) })) {
                    Text("theme.system").tag(ThemeChoice.system)
                    Text("theme.light").tag(ThemeChoice.light)
                    Text("theme.dark").tag(ThemeChoice.dark)
                }
            } header: {
                Text("settings.appearance")
            } footer: {
                Text("settings.theme.hint")
            }
            Section {
                // Language names stay in their own language, so anyone can find theirs.
                choice("settings.language", selection: Binding(get: { state.language }, set: {
                    model.setLanguage(language: $0)
                    AppLanguage.apply($0)
                })) {
                    Text("language.system").tag(LanguageChoice.system)
                    Text(verbatim: "English").tag(LanguageChoice.english)
                    Text(verbatim: "Français").tag(LanguageChoice.french)
                }
                choice("settings.units", selection: Binding(get: { state.units }, set: { model.setUnits(units: $0) })) {
                    Text("units.metric").tag(UnitSystem.metric)
                    Text("units.imperial").tag(UnitSystem.imperial)
                }
            } header: {
                Text("settings.region")
            } footer: {
                Text("settings.language.next.launch") + Text(verbatim: " ") + Text("settings.units.hint")
            }
        }
    }

    private func choice<T: Hashable, Options: View>(_ title: LocalizedStringKey, selection: Binding<T>, @ViewBuilder options: () -> Options) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title).font(.poulpy(.headline))
            Picker(selection: selection, content: options) { EmptyView() }.pickerStyle(.segmented)
        }
        .padding(.vertical, 4)
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
                        Text("devices.empty.title").font(PoulpyFont.headline).multilineTextAlignment(.center)
                        Text("devices.empty.body").multilineTextAlignment(.center)
                        NavigationLink(value: DeviceRoute.add) {
                            Label("devices.add.first", systemImage: "plus")
                                .font(.poulpy(.headline))
                                .foregroundStyle(PoulpyColor.onPrimary)
                                .padding(.horizontal, 24)
                                .frame(height: 56)
                                .background(PoulpyColor.primary, in: Capsule())
                        }
                        .buttonStyle(.plain)
                        .padding(.top, PoulpySpace.sm)
                        Text("devices.switch.on").font(.poulpy(.footnote))
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
                                    Text("devices.sources.row.hint").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
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
            Text([statusLabel(device.status), device.batteryPercent.map { L("devices.battery", Int64($0.intValue)) }].compactMap { $0 }.joined(separator: " · "))
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
                    Text("devices.bluetooth.off").foregroundStyle(PoulpyColor.error)
                }
                if bluetooth.isDenied {
                    Text("devices.permission.needed").foregroundStyle(PoulpyColor.error)
                    Button(LocalizedStringKey("devices.open.settings")) {
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
                    Text("devices.scan.hint").multilineTextAlignment(.center)
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
                            Button(LocalizedStringKey("devices.add.one")) {
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
                Text("devices.added.hidden")
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
                            Text("devices.change.sources").foregroundStyle(PoulpyColor.primary)
                        }
                    }
                }
                Section("settings.devices") {
                    Toggle(isOn: Binding(get: { device.autoConnect }, set: { model.setAutoConnect(key: device.key, on: $0) })) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("devices.auto.connect").font(.poulpy(.headline))
                            Text("devices.auto.connect.hint").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
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
            .alert(L("devices.forget.title", device.name), isPresented: $confirmForget) {
                Button(LocalizedStringKey("devices.forget"), role: .destructive) {
                    model.forget(key: device.key)
                    dismiss()
                }
                Button(LocalizedStringKey("action.cancel"), role: .cancel) {}
            } message: {
                Text("devices.forget.body")
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
            Text(used ? L("devices.used") : L("devices.used.instead", instead?.name ?? ""))
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

    var body: some View {
        let state = devices.value
        let model = devices.model
        List {
            Section {
            } footer: {
                Text("devices.sources.hint")
            }
            ForEach(state.sources, id: \.quantity) { source in
                Section(quantityLabel(source.quantity)) {
                    Picker(selection: Binding(get: { source.choiceKey }, set: { model.setSource(quantity: source.quantity, key: $0) })) {
                        Text("devices.source.automatic").tag(String?.none)
                        ForEach(source.options, id: \.key) { option in
                            Text(option.name).tag(String?.some(option.key))
                        }
                    } label: {
                        EmptyView()
                    }
                    .pickerStyle(.inline)
                    .labelsHidden()
                }
            }
        }
        .navigationTitle("devices.sources")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private func statusLabel(_ status: ConnectionStatus) -> String {
    switch status {
    case .connected: return L("devices.status.connected")
    case .connecting: return L("devices.status.connecting")
    case .reconnecting: return L("devices.status.reconnecting")
    case .failed: return L("devices.status.failed")
    default: return L("devices.status.idle")
    }
}

private func quantityLabel(_ quantity: Quantity) -> String {
    switch quantity {
    case .speed: return L("quantity.speed")
    case .heartRate: return L("quantity.heart.rate")
    case .cadence: return L("quantity.cadence")
    case .incline: return L("quantity.incline")
    case .energy: return L("quantity.energy")
    default: return L("quantity.power")
    }
}

private func kindLabel(_ kind: DeviceKind?) -> String {
    if kind == DeviceKind.treadmill { return L("kind.treadmill") }
    if kind == DeviceKind.footPod { return L("kind.foot.pod") }
    if kind == DeviceKind.heartRate { return L("kind.heart.rate") }
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
