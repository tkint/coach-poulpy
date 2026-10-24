import SwiftUI
import UIKit
import CoreBluetooth
import Shared

/// Remembers the treadmill between launches.
final class UserDefaultsStore: NSObject, KeyValueStore {
    func getString(key: String) -> String? { UserDefaults.standard.string(forKey: key) }

    func putString(key: String, value: String?) {
        if let value { UserDefaults.standard.set(value, forKey: key) } else { UserDefaults.standard.removeObject(forKey: key) }
    }
}

/// Runs an action once Bluetooth is allowed and on. Kable turns off iOS's own "Turn on Bluetooth"
/// alert, so this manager keeps it: the system asks for access first, then to switch Bluetooth on.
final class BluetoothGate: NSObject, ObservableObject, CBCentralManagerDelegate {
    @Published private(set) var isOff = false
    @Published private(set) var isDenied = false
    private var manager: CBCentralManager?
    private var pending: (() -> Void)?

    func whenReady(_ action: @escaping () -> Void) {
        if let manager, manager.state == .poweredOn {
            action()
            return
        }
        pending = action
        if manager == nil {
            manager = CBCentralManager(delegate: self, queue: nil, options: [CBCentralManagerOptionShowPowerAlertKey: true])
        }
    }

    func centralManagerDidUpdateState(_ central: CBCentralManager) {
        isOff = central.state == .poweredOff
        isDenied = central.state == .unauthorized
        if central.state == .poweredOn, let action = pending {
            pending = nil
            action()
        }
    }
}

/// Bridges the Kotlin TreadmillModel to SwiftUI.
final class TreadmillViewModel: ObservableObject {
    @Published private(set) var state: TreadmillUiState
    let model: TreadmillModel
    private var watch: Cancellable?

    init(graph: AppGraph) {
        // Shared by the whole app: leaving the tab keeps the treadmill connected and the recording going.
        model = graph.treadmill(memory: UserDefaultsStore())
        state = model.state.value as! TreadmillUiState
        watch = model.watch { [weak self] state in
            DispatchQueue.main.async {
                self?.state = state
                // The phone stays awake while recording, as it sits on the treadmill's console.
                UIApplication.shared.isIdleTimerDisabled = state.recording != nil
            }
        }
    }

    deinit {
        watch?.cancel()
    }
}

struct TreadmillView: View {
    @StateObject private var viewModel: TreadmillViewModel
    @StateObject private var bluetooth = BluetoothGate()
    @State private var selectedPlan = "free-run"

    init(graph: AppGraph) {
        _viewModel = StateObject(wrappedValue: TreadmillViewModel(graph: graph))
    }

    private var state: TreadmillUiState { viewModel.state }
    private var model: TreadmillModel { viewModel.model }

    var body: some View {
        NavigationStack {
            List {
                connectionSection
                if state.connection == .idle || state.connection == .failed {
                    devicesSection
                }
                if state.connection == .connected || state.connection == .reconnecting {
                    liveSection
                }
                if let recording = state.recording {
                    recordingSection(recording)
                } else {
                    recordSection
                }
                if state.connection == .connected || state.connection == .reconnecting {
                    Section {
                        Toggle(isOn: Binding(get: { state.syncPause }, set: { model.setSyncPause(on: $0) })) {
                            Text("treadmill.sync.pause")
                        }
                    }
                }
                if let summary = state.summary {
                    summarySection(summary)
                }
                if !state.supportedMetrics.isEmpty || state.malformedPackets > 0 {
                    Section {
                        Text(L("treadmill.machine.reports", state.supportedMetrics.joined(separator: ", ")))
                        if state.malformedPackets > 0 {
                            Text(L("treadmill.malformed", Int(state.malformedPackets)))
                        }
                    }
                    .font(.poulpy(.footnote))
                    .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("tab.treadmill")
            .onAppear {
                // Opening the tab reconnects to the remembered treadmill, once Bluetooth was allowed before.
                if state.rememberedName != nil && CBCentralManager.authorization == .allowedAlways {
                    bluetooth.whenReady { model.connectRemembered() }
                }
            }
        }
    }

    private var connectionSection: some View {
        Section {
            Text(statusText).font(.poulpy(.headline))
            if state.connection == .connected {
                beltIndicator
            }
            if bluetooth.isDenied {
                Text("treadmill.permission.needed").foregroundStyle(.red)
            } else if bluetooth.isOff {
                Text("treadmill.bluetooth.off").foregroundStyle(.red)
            }
            if let error = state.error {
                Text(error).font(.poulpy(.footnote)).foregroundStyle(.red)
            }
            if isLinked {
                Button(LocalizedStringKey("treadmill.disconnect")) { model.disconnect() }
            } else if state.scanning {
                Button(LocalizedStringKey("treadmill.stop.scan")) { model.stopScan() }
            } else if let remembered = state.rememberedName {
                Button(L("treadmill.connect.to", remembered)) { bluetooth.whenReady { model.connectRemembered() } }
                Button(LocalizedStringKey("treadmill.scan.other")) { bluetooth.whenReady { model.startScan() } }
            } else {
                Button(LocalizedStringKey("treadmill.scan")) { bluetooth.whenReady { model.startScan() } }
            }
            if state.rememberedName != nil && !state.scanning {
                Button(LocalizedStringKey("treadmill.forget"), role: .destructive) { model.forget() }
            }
        }
    }

    private var isLinked: Bool {
        state.connection == .connecting || state.connection == .connected || state.connection == .reconnecting
    }

    /// Shown when the belt is not moving: paused if the treadmill says so, otherwise stopped.
    @ViewBuilder
    private var beltIndicator: some View {
        if state.belt == .paused {
            Label("treadmill.belt.paused", systemImage: "pause.circle.fill").font(.poulpy(.headline)).foregroundStyle(.orange)
        } else if state.belt == .stopped {
            Label("treadmill.belt.stopped", systemImage: "stop.circle.fill").font(.poulpy(.headline)).foregroundStyle(.orange)
        }
    }

    private var statusText: String {
        let name = state.deviceName ?? ""
        switch state.connection {
        case .connecting: return L("treadmill.status.connecting", name)
        case .connected: return L("treadmill.status.connected", name)
        case .reconnecting: return L("treadmill.status.reconnecting", name)
        case .failed: return L("treadmill.status.failed")
        default: return L("treadmill.status.idle")
        }
    }

    private var devicesSection: some View {
        Section {
            if state.scanning && state.devices.isEmpty {
                Text("treadmill.scanning")
            }
            ForEach(state.devices, id: \.key) { device in
                Button {
                    model.connect(deviceId: device.key)
                } label: {
                    HStack {
                        VStack(alignment: .leading) {
                            Text(device.name)
                            if device.advertisesFtms {
                                Text("treadmill.ftms.badge").font(.poulpy(.footnote)).foregroundStyle(.secondary)
                            }
                        }
                        Spacer()
                        Text("\(device.rssi) dBm").font(.poulpy(.footnote)).foregroundStyle(.secondary)
                    }
                }
            }
        }
    }

    private var liveSection: some View {
        Section {
            if state.tiles.isEmpty {
                Text("treadmill.waiting.data")
            } else {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], alignment: .leading, spacing: 12) {
                    ForEach(state.tiles, id: \.metric) { tile in
                        VStack(alignment: .leading, spacing: 2) {
                            Text(label(tile.metric)).font(.poulpy(.caption)).foregroundStyle(.secondary)
                            Text("\(formatted(tile)) \(unit(tile.metric))".trimmingCharacters(in: .whitespaces))
                                .font(.poulpy(.title2).monospacedDigit())
                        }
                    }
                }
                .padding(.vertical, 4)
            }
        }
    }

    private var recordSection: some View {
        Section("treadmill.record.title") {
            Picker(selection: $selectedPlan) {
                ForEach(state.plans, id: \.key) { plan in
                    Text(planName(plan)).tag(plan.key)
                }
            } label: {
                EmptyView()
            }
            .pickerStyle(.inline)
            Button(LocalizedStringKey("treadmill.start")) { model.startRecording(planKey: selectedPlan) }
        }
    }

    private func recordingSection(_ recording: RecordingUi) -> some View {
        Section {
            Text(L("treadmill.elapsed", clock(recording.elapsedSeconds))).font(.poulpy(.title2).monospacedDigit())
            if recording.paused {
                Text("treadmill.paused").foregroundStyle(.red)
            }
            beltIndicator
            if !recording.freeRun {
                Text(L("treadmill.phase", Int(recording.phaseNumber), Int(recording.phaseCount))).font(.poulpy(.headline))
                if recording.isRest {
                    Text("treadmill.rest")
                }
                if let speed = recording.targetSpeedKmh {
                    Text(L("treadmill.target.speed", decimal(speed.doubleValue, 1)))
                }
                if let distance = recording.targetDistanceMeters {
                    Text(L("treadmill.target.distance", Int(distance.doubleValue)))
                }
                if let remaining = recording.phaseRemainingSeconds {
                    Text(L("treadmill.remaining", clock(remaining.int64Value)))
                }
            }
            HStack {
                if recording.paused {
                    Button(LocalizedStringKey("treadmill.resume")) { model.resumeRecording() }
                } else {
                    Button(LocalizedStringKey("treadmill.pause")) { model.pauseRecording() }
                }
                if !recording.freeRun {
                    Button(LocalizedStringKey("treadmill.skip")) { model.skipPhase() }
                }
                Spacer()
                Button(LocalizedStringKey("treadmill.stop")) { model.stopRecording() }
                    .buttonStyle(.borderedProminent)
            }
            // Several buttons in one row: without this, a tap anywhere triggers them all.
            .buttonStyle(.bordered)
        }
    }

    private func summarySection(_ summary: SummaryUi) -> some View {
        Section("treadmill.summary.title") {
            LabeledContent(LocalizedStringKey("treadmill.summary.duration"), value: clock(summary.durationSeconds))
            if let distance = summary.distanceMeters {
                LabeledContent(LocalizedStringKey("metric.distance"), value: "\(decimal(distance.doubleValue / 1000, 2)) km")
            }
            if let speed = summary.averageSpeedKmh {
                LabeledContent(LocalizedStringKey("treadmill.summary.average.speed"), value: "\(decimal(speed.doubleValue, 1)) km/h")
            }
            if let hr = summary.averageHeartRateBpm {
                LabeledContent(LocalizedStringKey("treadmill.summary.average.hr"), value: "\(hr.intValue) bpm")
            }
            if let hr = summary.maxHeartRateBpm {
                LabeledContent(LocalizedStringKey("treadmill.summary.max.hr"), value: "\(hr.intValue) bpm")
            }
            if let kcal = summary.energyKcal {
                LabeledContent(LocalizedStringKey("metric.energy"), value: "\(kcal.intValue) kcal")
            }
            if let steps = summary.estimatedSteps {
                LabeledContent(LocalizedStringKey("metric.steps"), value: "≈ \(steps.intValue)")
            }
            if summary.phases.count > 1 {
                ForEach(summary.phases, id: \.phaseNumber) { phase in
                    Text(phaseLine(phase)).font(.poulpy(.footnote))
                }
            }
        }
    }

    private func phaseLine(_ phase: PhaseResultUi) -> String {
        var parts = [clock(phase.durationSeconds)]
        if let d = phase.distanceMeters { parts.append("\(Int(d.doubleValue)) m") }
        if let s = phase.averageSpeedKmh { parts.append("\(decimal(s.doubleValue, 1)) km/h") }
        if let t = phase.targetSpeedKmh { parts.append("→ \(decimal(t.doubleValue, 1))") }
        if let p = phase.onTargetPercent { parts.append(L("treadmill.summary.on.target", p.intValue)) }
        let title = phase.isRest ? L("treadmill.rest") : "#\(phase.phaseNumber)"
        return "\(title)  \(parts.joined(separator: " · "))"
    }

    private func planName(_ plan: PlanOption) -> String {
        switch plan.key {
        case "free-run": return L("treadmill.plan.free.run")
        case "demo-intervals": return L("treadmill.plan.demo")
        default: return plan.name
        }
    }

    private func label(_ metric: LiveMetric) -> String {
        switch metric {
        case .speed: return L("metric.speed")
        case .pace: return L("metric.pace")
        case .averageSpeed: return L("metric.average.speed")
        case .elevationGain: return L("metric.elevation.gain")
        case .met: return L("metric.met")
        case .remainingTime: return L("metric.remaining.time")
        case .incline: return L("metric.incline")
        case .distanceKm: return L("metric.distance")
        case .heartRate: return L("metric.heart.rate")
        case .energy: return L("metric.energy")
        case .machineTime: return L("metric.machine.time")
        case .stepsEstimated: return L("metric.steps")
        default: return L("metric.power")
        }
    }

    private func unit(_ metric: LiveMetric) -> String {
        switch metric {
        case .speed, .averageSpeed: return "km/h"
        case .pace: return "/km"
        case .elevationGain: return "m"
        case .met: return "MET"
        case .remainingTime: return ""
        case .incline: return "%"
        case .distanceKm: return "km"
        case .heartRate: return "bpm"
        case .energy: return "kcal"
        case .machineTime, .stepsEstimated: return ""
        default: return "W"
        }
    }

    private func formatted(_ tile: LiveTile) -> String {
        if tile.metric.isTime { return clock(Int64(tile.value)) }
        let value = decimal(tile.value, Int(tile.decimals))
        return tile.metric == .stepsEstimated ? "≈ \(value)" : value
    }
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}

private func decimal(_ value: Double, _ decimals: Int) -> String {
    value.formatted(.number.precision(.fractionLength(decimals)))
}

private func clock(_ seconds: Int64) -> String {
    let h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60
    return h > 0 ? String(format: "%lld:%02lld:%02lld", h, m, s) : String(format: "%lld:%02lld", m, s)
}
