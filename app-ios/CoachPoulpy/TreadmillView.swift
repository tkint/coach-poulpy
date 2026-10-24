import SwiftUI
import UIKit
import Shared

/// Bridges the Kotlin TreadmillModel to SwiftUI.
final class TreadmillViewModel: ObservableObject {
    @Published private(set) var state: TreadmillUiState
    let model: TreadmillModel
    private var watch: Cancellable?

    init(graph: AppGraph) {
        model = graph.treadmill()
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
        model.close()
    }
}

struct TreadmillView: View {
    @StateObject private var viewModel: TreadmillViewModel
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
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                }
            }
            .navigationTitle("tab.treadmill")
        }
    }

    private var connectionSection: some View {
        Section {
            Text(statusText).font(.headline)
            if let error = state.error {
                Text(error).font(.footnote).foregroundStyle(.red)
            }
            if state.connection == .connecting || state.connection == .connected || state.connection == .reconnecting {
                Button(LocalizedStringKey("treadmill.disconnect")) { model.disconnect() }
            } else if state.scanning {
                Button(LocalizedStringKey("treadmill.stop.scan")) { model.stopScan() }
            } else {
                // iOS asks for Bluetooth access itself the first time the app scans.
                Button(LocalizedStringKey("treadmill.scan")) { model.startScan() }
            }
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
                                Text("treadmill.ftms.badge").font(.footnote).foregroundStyle(.secondary)
                            }
                        }
                        Spacer()
                        Text("\(device.rssi) dBm").font(.footnote).foregroundStyle(.secondary)
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
                            Text(label(tile.metric)).font(.caption).foregroundStyle(.secondary)
                            Text("\(formatted(tile)) \(unit(tile.metric))".trimmingCharacters(in: .whitespaces))
                                .font(.title2.monospacedDigit())
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
            Text(L("treadmill.elapsed", clock(recording.elapsedSeconds))).font(.title2.monospacedDigit())
            if recording.paused {
                Text("treadmill.paused").foregroundStyle(.red)
            }
            if !recording.freeRun {
                Text(L("treadmill.phase", Int(recording.phaseNumber), Int(recording.phaseCount))).font(.headline)
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
            if summary.phases.count > 1 {
                ForEach(summary.phases, id: \.phaseNumber) { phase in
                    Text(phaseLine(phase)).font(.footnote)
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
        case .incline: return L("metric.incline")
        case .distanceKm: return L("metric.distance")
        case .heartRate: return L("metric.heart.rate")
        case .energy: return L("metric.energy")
        case .machineTime: return L("metric.machine.time")
        default: return L("metric.power")
        }
    }

    private func unit(_ metric: LiveMetric) -> String {
        switch metric {
        case .speed: return "km/h"
        case .incline: return "%"
        case .distanceKm: return "km"
        case .heartRate: return "bpm"
        case .energy: return "kcal"
        case .machineTime: return ""
        default: return "W"
        }
    }

    private func formatted(_ tile: LiveTile) -> String {
        tile.metric == .machineTime ? clock(Int64(tile.value)) : decimal(tile.value, Int(tile.decimals))
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
