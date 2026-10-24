import SwiftUI
import UIKit
import Shared

/// The Live tab: every connected device's numbers together, the recorder under them, all on one screen.
struct LiveView: View {
    @StateObject private var live: Observed<LiveModel, LiveUiState>
    @StateObject private var devices: Observed<DevicesModel, DevicesUiState>
    @StateObject private var bluetooth = BluetoothGate()
    @State private var selectedPlan = "free-run"
    let onManageDevices: () -> Void

    init(graph: AppGraph, onManageDevices: @escaping () -> Void) {
        // Shared by the whole app: leaving the tab keeps the devices connected and the recording going.
        _live = StateObject(wrappedValue: graph.observedLive())
        _devices = StateObject(wrappedValue: graph.observedDevices())
        self.onManageDevices = onManageDevices
    }

    private var state: LiveUiState { live.value }
    private var model: LiveModel { live.model }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 10) {
                    if state.connectedCount == 0 && state.recording == nil {
                        nothingConnected
                    } else {
                        if state.belt == .paused || state.belt == .stopped {
                            beltBanner
                        }
                        if !state.tiles.isEmpty {
                            panel
                        } else {
                            Text("live.waiting").foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                        recorder
                        if state.hasTreadmill {
                            syncRow
                        }
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, PoulpySpace.md)
            }
            .background(PoulpyColor.background)
            .navigationTitle("tab.live")
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) { connectedChip }
            }
            .onAppear {
                UIApplication.shared.isIdleTimerDisabled = state.recording != nil
                // Opening the tab connects the added devices, once Bluetooth was allowed before.
                if BluetoothGate.wasAllowed {
                    bluetooth.whenReady { devices.model.connectAutomatically() }
                }
            }
            .onChange(of: state.recording != nil) { recording in
                // The phone stays awake while recording, as it sits on the treadmill's console.
                UIApplication.shared.isIdleTimerDisabled = recording
            }
            .sheet(isPresented: Binding(get: { state.summary != nil }, set: { if !$0 { model.clearSummary() } })) {
                if let summary = state.summary {
                    SummarySheet(summary: summary, units: state.units, onDone: { model.clearSummary() })
                }
            }
        }
    }

    private var connectedChip: some View {
        let count = Int(state.connectedCount)
        return Button(action: onManageDevices) {
            HStack(spacing: 6) {
                Circle().fill(count > 0 ? PoulpyColor.success : PoulpyColor.outline).frame(width: 8, height: 8)
                Text(count > 0 ? L("live.connected %lld", Int64(count)) : L("live.none.connected"))
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 14, relativeTo: .subheadline))
            }
            .foregroundStyle(PoulpyColor.onSurface)
            .padding(.horizontal, 12)
            .frame(height: 36)
            .background(PoulpyColor.surface, in: Capsule())
            .overlay(Capsule().stroke(PoulpyColor.outline))
        }
    }

    private var nothingConnected: some View {
        VStack(spacing: 10) {
            Image("PoulpyPoseStandingTall").resizable().scaledToFit().frame(width: 140, height: 140).accessibilityHidden(true)
            Text("live.empty.title").font(PoulpyFont.headline).multilineTextAlignment(.center)
            Text("live.empty.body").multilineTextAlignment(.center)
            Button(action: onManageDevices) {
                Text("live.manage.devices").font(.poulpy(.headline)).padding(.horizontal, 20).frame(height: 52)
            }
            .buttonStyle(.borderedProminent)
            .buttonBorderShape(.capsule)
            .padding(.top, PoulpySpace.sm)
        }
        .foregroundStyle(PoulpyColor.onPrimaryContainer)
        .padding(24)
        .frame(maxWidth: .infinity)
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
    }

    private var beltBanner: some View {
        HStack(spacing: 10) {
            Image(systemName: "pause.fill").foregroundStyle(PoulpyColor.grumpy)
            Text(LocalizedStringKey(state.belt == .paused ? "treadmill.belt.paused" : "treadmill.belt.stopped"))
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 16, relativeTo: .body))
                .foregroundStyle(PoulpyColor.onGrumpyContainer)
            Spacer(minLength: 0)
        }
        .padding(.horizontal, 12)
        .padding(.vertical, 8)
        .background(PoulpyColor.grumpyContainer, in: RoundedRectangle(cornerRadius: 14))
    }

    /// Speed and pace large, the rest two by two. Six small tiles at most, so the screen never scrolls.
    private var panel: some View {
        let big = state.tiles.filter { $0.metric == .speed || $0.metric == .pace }
        let small = Array(state.tiles.filter { $0.metric != .speed && $0.metric != .pace }.prefix(6))
        return VStack(spacing: 8) {
            if !big.isEmpty {
                HStack(spacing: 8) {
                    ForEach(big, id: \.metric) { bigTile($0) }
                }
            }
            LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                ForEach(small, id: \.metric) { smallTile($0) }
            }
        }
        .padding(10)
        .background(PoulpyColor.primaryContainer, in: RoundedRectangle(cornerRadius: PoulpyRadius.lg))
    }

    private func bigTile(_ tile: LiveTile) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label(tile.metric).uppercased()).font(PoulpyFont.label)
            Text(formatted(tile))
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 40, relativeTo: .largeTitle).monospacedDigit())
                .foregroundStyle(PoulpyColor.onPrimaryContainer)
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            Text(tile.unit).font(.poulpy(.subheadline))
            if let source = tile.source {
                Text(source).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant).lineLimit(1)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 12)
        .padding(.vertical, 10)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        .accessibilityElement(children: .combine)
    }

    private func smallTile(_ tile: LiveTile) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text([label(tile.metric), tile.source].compactMap { $0 }.joined(separator: " · "))
                .font(.poulpy(.caption))
                .foregroundStyle(PoulpyColor.onSurfaceVariant)
                .lineLimit(1)
            Text("\(formatted(tile)) \(tile.unit)".trimmingCharacters(in: .whitespaces))
                .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 22, relativeTo: .title2).monospacedDigit())
                .lineLimit(1)
                .minimumScaleFactor(0.7)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.horizontal, 10)
        .padding(.vertical, 8)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 14))
        .accessibilityElement(children: .combine)
    }

    private var recorder: some View {
        VStack(alignment: .leading, spacing: 10) {
            if let recording = state.recording {
                recordingControls(recording)
            } else {
                planPicker
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).stroke(PoulpyColor.outline))
    }

    private var planPicker: some View {
        VStack(alignment: .leading, spacing: 10) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(state.plans, id: \.key) { plan in
                        let on = plan.key == selectedPlan
                        Button { selectedPlan = plan.key } label: {
                            Text(planName(plan))
                                .font(.custom(PoulpyFontFace.nunitoBold, size: 15, relativeTo: .subheadline))
                                .foregroundStyle(on ? PoulpyColor.onPrimaryContainer : PoulpyColor.onSurfaceVariant)
                                .padding(.horizontal, 14)
                                .frame(height: 36)
                                .background(on ? PoulpyColor.primaryContainer : Color.clear, in: Capsule())
                                .overlay(Capsule().stroke(on ? Color.clear : PoulpyColor.outline))
                        }
                        .accessibilityAddTraits(on ? .isSelected : [])
                    }
                }
            }
            if selectedPlan == "demo-intervals" {
                Text("treadmill.plan.demo").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
            Button { model.startRecording(planKey: selectedPlan) } label: {
                Text("treadmill.start").font(.poulpy(.headline)).frame(maxWidth: .infinity).frame(height: 52)
            }
            .buttonStyle(.borderedProminent)
            .buttonBorderShape(.capsule)
        }
    }

    private func recordingControls(_ recording: RecordingUi) -> some View {
        let statusColor = recording.paused ? PoulpyColor.grumpy : PoulpyColor.error
        return VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 6) {
                Text(clock(recording.elapsedSeconds))
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 32, relativeTo: .title).monospacedDigit())
                Spacer()
                Circle().fill(statusColor).frame(width: 8, height: 8)
                Text(LocalizedStringKey(recording.paused ? "treadmill.paused" : "live.recording"))
                    .font(.custom(PoulpyFontFace.nunitoExtraBold, size: 15, relativeTo: .subheadline))
                    .foregroundStyle(statusColor)
            }
            if !recording.freeRun {
                HStack(alignment: .firstTextBaseline) {
                    Text(phaseLine(recording)).font(.poulpy(.subheadline))
                    Spacer()
                    if let remaining = recording.phaseRemainingSeconds {
                        Text(L("treadmill.remaining", clock(remaining.int64Value))).font(.poulpy(.subheadline).monospacedDigit())
                    }
                }
            }
            HStack(spacing: 8) {
                Button {
                    if recording.paused { model.resumeRecording() } else { model.pauseRecording() }
                } label: {
                    Image(systemName: recording.paused ? "play.fill" : "pause.fill").frame(width: 28, height: 40)
                }
                .buttonStyle(.bordered)
                .buttonBorderShape(.capsule)
                .accessibilityLabel(Text(LocalizedStringKey(recording.paused ? "treadmill.resume" : "treadmill.pause")))
                if !recording.freeRun {
                    Button { model.skipPhase() } label: {
                        Text("treadmill.skip").font(.poulpy(.headline)).frame(maxWidth: .infinity).frame(height: 40)
                    }
                    .buttonStyle(.bordered)
                    .buttonBorderShape(.capsule)
                }
                Button { model.stopRecording() } label: {
                    Text("treadmill.stop").font(.poulpy(.headline)).frame(maxWidth: .infinity).frame(height: 40)
                }
                .buttonStyle(.borderedProminent)
                .buttonBorderShape(.capsule)
            }
        }
    }

    private func phaseLine(_ recording: RecordingUi) -> String {
        var parts = [L("treadmill.phase", Int64(recording.phaseNumber), Int64(recording.phaseCount))]
        if recording.isRest { parts.append(L("treadmill.rest")) }
        if let speed = recording.targetSpeedKmh {
            parts.append(L("treadmill.target.speed", Units.shared.speed(kmh: speed.doubleValue, system: state.units).text))
        }
        if let distance = recording.targetDistanceMeters {
            parts.append(L("treadmill.target.distance", Int64(distance.doubleValue)))
        }
        return parts.joined(separator: " · ")
    }

    private var syncRow: some View {
        Toggle(isOn: Binding(get: { state.syncPause }, set: { model.setSyncPause(on: $0) })) {
            Label {
                Text("live.sync.belt").font(.custom(PoulpyFontFace.nunitoExtraBold, size: 16, relativeTo: .body))
            } icon: {
                Image(systemName: "arrow.triangle.2.circlepath").foregroundStyle(PoulpyColor.primary)
            }
        }
        .padding(.horizontal, 14)
        .padding(.vertical, 8)
        .background(PoulpyColor.surface, in: RoundedRectangle(cornerRadius: PoulpyRadius.md))
        .overlay(RoundedRectangle(cornerRadius: PoulpyRadius.md).stroke(PoulpyColor.outline))
    }

    private func planName(_ plan: PlanOption) -> String {
        switch plan.key {
        case "free-run": return L("treadmill.plan.free.run")
        case "demo-intervals": return L("treadmill.plan.demo").components(separatedBy: " (").first ?? plan.name
        default: return plan.name
        }
    }
}

/// The numbers of the session just finished, shown once.
private struct SummarySheet: View {
    let summary: SummaryUi
    let units: UnitSystem
    let onDone: () -> Void

    var body: some View {
        NavigationStack {
            List {
                Section {
                    LabeledContent(LocalizedStringKey("treadmill.summary.duration"), value: clock(summary.durationSeconds))
                    if let distance = summary.distanceMeters {
                        LabeledContent(LocalizedStringKey("metric.distance"), value: Units.shared.distance(meters: distance.doubleValue, system: units, decimals: 2).text)
                    }
                    if let speed = summary.averageSpeedKmh {
                        LabeledContent(LocalizedStringKey("treadmill.summary.average.speed"), value: Units.shared.speed(kmh: speed.doubleValue, system: units).text)
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
                }
                if summary.phases.count > 1 {
                    Section {
                        ForEach(summary.phases, id: \.phaseNumber) { phase in
                            Text(phaseLine(phase)).font(.poulpy(.footnote))
                        }
                    }
                }
            }
            .navigationTitle("treadmill.summary.title")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .confirmationAction) {
                    Button(LocalizedStringKey("action.done"), action: onDone)
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func phaseLine(_ phase: PhaseResultUi) -> String {
        var parts = [clock(phase.durationSeconds)]
        if let s = phase.averageSpeedKmh { parts.append(Units.shared.speed(kmh: s.doubleValue, system: units).text) }
        if let p = phase.onTargetPercent { parts.append(L("treadmill.summary.on.target", Int64(p.intValue))) }
        let title = phase.isRest ? L("treadmill.rest") : "#\(phase.phaseNumber)"
        return "\(title)  \(parts.joined(separator: " · "))"
    }
}

private func label(_ metric: LiveMetric) -> String {
    switch metric {
    case .speed: return L("metric.speed")
    case .pace: return L("metric.pace")
    case .distance: return L("metric.distance")
    case .heartRate: return L("metric.heart.rate")
    case .cadence: return L("metric.cadence")
    case .incline: return L("metric.incline")
    case .energy: return L("metric.energy")
    case .stepsEstimated: return L("metric.steps")
    default: return L("metric.power")
    }
}

/// A missing value keeps its tile with a dash; a pace dash keeps the shape of a time.
private func formatted(_ tile: LiveTile) -> String {
    guard let value = tile.value?.doubleValue else { return tile.metric.isTime ? "–:––" : "–" }
    let text = tile.metric.isTime ? clock(Int64(value)) : decimal(value, Int(tile.decimals))
    return tile.metric == .stepsEstimated ? "≈ \(text)" : text
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
