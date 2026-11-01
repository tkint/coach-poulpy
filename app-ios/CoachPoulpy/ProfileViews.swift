import SwiftUI
import Charts
import Shared

/// The pages the Settings hub opens inside its tab. Device pages open through `DeviceRoute`.
enum SettingsPage: Hashable { case profile, weight, height, preferences, devices }

// MARK: - Hub

/// Settings: the profile card, Body, App and About.
struct SettingsHub: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>
    @ObservedObject var profile: Observed<ProfileModel, ProfileUi>
    @ObservedObject var weight: Observed<BodyModel, BodyUi>
    @ObservedObject var height: Observed<BodyModel, BodyUi>

    var body: some View {
        let units = settings.value.units
        let parts = [
            profile.value.age.map { L("profile.years %lld", Int64($0.intValue)) },
            height.value.latest.map { bodyText(.height, $0.value, units) },
            weight.value.latest.map { bodyText(.weight, $0.value, units) },
        ].compactMap { $0 }
        List {
            Section {
                NavigationLink(value: SettingsPage.profile) {
                    HStack(spacing: 14) {
                        Image("PoulpyMoodHappy").resizable().scaledToFit().frame(width: 64, height: 64).accessibilityHidden(true)
                        VStack(alignment: .leading, spacing: 2) {
                            if profile.value.displayName.isEmpty {
                                Text("settings.yourProfile").font(.poulpy(.title3))
                            } else {
                                Text(profile.value.displayName).font(.poulpy(.title3))
                            }
                            if !parts.isEmpty { Text(parts.joined(separator: " · ")) }
                            Text("settings.editProfile").font(.custom(PoulpyFontFace.nunitoExtraBold, size: 13, relativeTo: .footnote))
                        }
                        Spacer()
                    }
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .padding(.vertical, 6)
                }
                .listRowBackground(PoulpyColor.primaryContainer)
            }
            Section("settings.body") {
                NavigationLink(value: SettingsPage.height) {
                    row("arrow.up.and.down", "body.height", height.value.latest.map { bodyText(.height, $0.value, units) } ?? L("body.nothingLogged"))
                }
                NavigationLink(value: SettingsPage.weight) {
                    row("scalemass", "body.weight", weight.value.latest.map { bodyText(.weight, $0.value, units) } ?? L("body.nothingLogged"))
                }
            }
            Section("settings.app") {
                NavigationLink(value: SettingsPage.preferences) {
                    row("circle.lefthalf.filled", "settings.appearanceUnits", "\(themeName(settings.value.theme)) · \(languageName(settings.value.language)) · \(unitsName(units))")
                }
                NavigationLink(value: SettingsPage.devices) { row("antenna.radiowaves.left.and.right", "settings.devices", nil) }
            }
            Section("settings.about") {
                row("info.circle", "settings.aboutApp", L("settings.aboutLine %@", Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "—"))
            }
        }
    }

    /// A leading icon, a bold title with a line under it.
    private func row(_ symbol: String, _ title: LocalizedStringKey, _ subtitle: String?) -> some View {
        HStack(spacing: 12) {
            Image(systemName: symbol).font(.system(size: 20)).foregroundStyle(PoulpyColor.primary).frame(width: 26)
            VStack(alignment: .leading, spacing: 0) {
                Text(title).font(.poulpy(.headline))
                if let subtitle { Text(subtitle).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            }
        }
        .frame(minHeight: 44)
    }

    private func themeName(_ theme: ThemeChoice) -> String {
        switch theme {
        case .light: return L("theme.light")
        case .dark: return L("theme.dark")
        default: return L("theme.system")
        }
    }

    private func languageName(_ language: LanguageChoice) -> String {
        switch language {
        case .english: return "English"
        case .french: return "Français"
        default: return L("language.system")
        }
    }

    private func unitsName(_ units: UnitSystem) -> String {
        units == UnitSystem.imperial ? L("units.imperial") : L("units.metric")
    }
}

// MARK: - Appearance and units

/// Theme, language and units as selects.
struct PreferencesView: View {
    @ObservedObject var settings: Observed<SettingsModel, SettingsUiState>

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
        }
        .navigationTitle("settings.appearanceUnits")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Profile

private enum ProfileSheet: Identifiable {
    case birthdate, pronouns, sex, figure, goals, experience, pain
    var id: Self { self }
}

/// Your profile: every field is optional and saved as it is changed.
struct ProfileView: View {
    @ObservedObject var profile: Observed<ProfileModel, ProfileUi>
    @State private var sheet: ProfileSheet?
    @State private var name = ""
    @State private var custom = ""
    @State private var heartRate = ""

    var body: some View {
        let state = profile.value
        let model = profile.model
        let notSet = L("profile.notSet")
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                sectionLabel("profile.aboutYou")
                PoulpyTextField(label: "profile.name", text: $name)
                    .onChange(of: name) { model.setDisplayName(name: $0) }
                SelectField(label: "profile.birthdate", value: state.birthdate.map { birthdateText($0, age: state.age) } ?? notSet) { sheet = .birthdate }
                SelectField(label: "profile.pronouns", value: pronounsText(state, notSet)) { sheet = .pronouns }
                FieldNote("profile.pronounsNote")
                SelectField(label: "profile.sex", value: state.sexAssignedAtBirth.map(sexLabel) ?? notSet) { sheet = .sex }
                FieldNote("profile.sexNote")
                SelectField(label: "profile.bodyFigure", value: state.bodyFigure.map(figureLabel) ?? notSet) { sheet = .figure }
                FieldNote("profile.figureNote")

                sectionLabel("profile.training")
                SelectField(label: "profile.goal", value: state.goals.isEmpty ? notSet : state.goals.map(goalLabel).joined(separator: ", ")) { sheet = .goals }
                SelectField(label: "profile.experience", value: state.experience.map(experienceLabel) ?? notSet) { sheet = .experience }
                VStack(alignment: .leading, spacing: 6) {
                    Text("profile.restingHr").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant).padding(.horizontal, 4)
                    NumberField(text: $heartRate, description: "profile.restingHr", unit: L("profile.bpm"))
                        .onChange(of: heartRate) { model.setRestingHeartRateText(text: $0) }
                    FieldNote("profile.restingHrHint")
                }
                SelectField(label: "profile.pain", value: painText(state)) { sheet = .pain }
                FieldNote("profile.painNote")
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)
        }
        .scrollDismissesKeyboard(.interactively)
        .background(PoulpyColor.background)
        .navigationTitle("profile.title")
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            name = state.displayName
            custom = state.customPronouns
            heartRate = state.restingHeartRate
        }
        .sheet(item: $sheet) { item in
            switch item {
            case .birthdate:
                BirthdateSheet(initial: state.birthdate.map(date(from:)), onSave: { model.setBirthdateTo(date: planDate($0)) }, onClear: { model.clearBirthdate() })
            case .pronouns:
                OptionSheet(
                    title: "profile.pronouns", why: "profile.pronounsSheetNote", note: nil, selected: state.pronouns,
                    options: withNotSet([Pronouns.sheHer, .heHim, .theyThem, .justMyName, .custom, .preferNotToSay], pronounLabel),
                    onSave: { model.setPronouns(pronouns: $0, custom: custom) }
                ) { choice in
                    if choice == Pronouns.custom {
                        PoulpyTextField(label: "profile.customPronouns", text: $custom)
                    }
                }
            case .sex:
                OptionSheet(
                    title: "profile.sex", why: "profile.sexWhy", note: "profile.sexNotGender", selected: state.sexAssignedAtBirth,
                    options: withNotSet([SexAssignedAtBirth.female, .male, .intersex, .preferNotToSay], sexLabel),
                    onSave: { model.setSexAssignedAtBirth(sex: $0) }
                )
            case .figure:
                BodyFigureSheet(current: state.bodyFigure, onSave: { model.setBodyFigure(figure: $0) })
            case .pain:
                PainSheet(
                    figure: state.bodyFigure ?? .shoulders,
                    areas: state.painActive ? state.painAreas : [],
                    duration: state.painDuration,
                    onSave: { model.setPainTo(areas: $0, duration: $1) },
                    onClear: { model.clearPain() }
                )
            case .goals:
                MultiOptionSheet(
                    title: "profile.goal", why: "profile.goalsNote", selected: state.goals,
                    options: [TrainingGoal.stayActive, .buildStrength, .runFurther, .rehab, .somethingElse].map { ($0, goalLabel($0)) },
                    onSave: { model.setGoalsTo(goals: $0) }
                )
            case .experience:
                ScaleSheet(
                    title: "profile.experience", why: "profile.experienceNote", selected: state.experience,
                    steps: [Experience.justStarting, .gettingGoing, .regular, .experienced, .veryExperienced].map { ($0, experienceLabel($0)) },
                    onSave: { model.setExperience(experience: $0) }
                )
            }
        }
    }

    private func sectionLabel(_ key: LocalizedStringKey) -> some View {
        Text(key).font(.poulpy(.footnote).weight(.heavy)).foregroundStyle(PoulpyColor.onSurfaceVariant).padding(.horizontal, 4).padding(.top, 8)
    }

    private func pronounsText(_ state: ProfileUi, _ notSet: String) -> String {
        guard let pronouns = state.pronouns else { return notSet }
        if pronouns == Pronouns.custom && !state.customPronouns.isEmpty { return state.customPronouns }
        return pronounLabel(pronouns)
    }

    private func painText(_ state: ProfileUi) -> String {
        guard state.painActive else { return L("profile.painNone") }
        let areas = state.painAreas.map { BodyMap.shared.label(key: $0, languageCode: Language.code) }.joined(separator: ", ")
        guard let until = state.painUntil else { return L("profile.painUntilCleared %@", areas) }
        return L("profile.painUntil %@ %@", areas, shown(until))
    }

    private func birthdateText(_ born: PlanDate, age: KotlinInt?) -> String {
        guard let age else { return shown(born) }
        return L("profile.birthdateValue %lld %@", Int64(age.intValue), shown(born))
    }
}

private struct BirthdateSheet: View {
    let onSave: (Date) -> Void
    let onClear: () -> Void
    @State private var picked: Date
    @Environment(\.dismiss) private var dismiss

    init(initial: Date?, onSave: @escaping (Date) -> Void, onClear: @escaping () -> Void) {
        self.onSave = onSave
        self.onClear = onClear
        _picked = State(initialValue: initial ?? Calendar.current.date(byAdding: .year, value: -30, to: Date()) ?? Date())
    }

    var body: some View {
        NavigationStack {
            Form {
                DatePicker("profile.birthdate", selection: $picked, in: ...Date(), displayedComponents: .date)
                    .datePickerStyle(.graphical)
                Button(LocalizedStringKey("profile.notSet"), role: .destructive) {
                    onClear()
                    dismiss()
                }
            }
            .navigationTitle("profile.birthdate")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(LocalizedStringKey("action.cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(LocalizedStringKey("action.save")) {
                        onSave(picked)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.large])
    }
}

private func pronounLabel(_ value: Pronouns) -> String {
    switch value {
    case .sheHer: return L("profile.pronounSheHer")
    case .heHim: return L("profile.pronounHeHim")
    case .theyThem: return L("profile.pronounTheyThem")
    case .justMyName: return L("profile.pronounJustMyName")
    case .custom: return L("profile.pronounCustom")
    default: return L("profile.pronounPreferNotToSay")
    }
}

private func sexLabel(_ value: SexAssignedAtBirth) -> String {
    switch value {
    case .female: return L("profile.sexFemale")
    case .male: return L("profile.sexMale")
    case .intersex: return L("profile.sexIntersex")
    default: return L("profile.sexPreferNotToSay")
    }
}

private func figureLabel(_ value: BodyFigure) -> String {
    switch value {
    case .shoulders: return L("profile.figureShoulders")
    default: return L("profile.figureHips")
    }
}

private func goalLabel(_ value: TrainingGoal) -> String {
    switch value {
    case .stayActive: return L("profile.goalStayActive")
    case .buildStrength: return L("profile.goalBuildStrength")
    case .runFurther: return L("profile.goalRunFurther")
    case .rehab: return L("profile.goalRehab")
    default: return L("profile.goalSomethingElse")
    }
}

private func experienceLabel(_ value: Experience) -> String {
    switch value {
    case .justStarting: return L("profile.experienceJustStarting")
    case .gettingGoing: return L("profile.experienceGettingGoing")
    case .regular: return L("profile.experienceRegular")
    case .experienced: return L("profile.experienceExperienced")
    default: return L("profile.experienceVeryExperienced")
    }
}

/// The options of a select, with "Not set" first.
private func withNotSet<T>(_ values: [T], _ label: (T) -> String) -> [(T?, String)] {
    [(nil, L("profile.notSet"))] + values.map { (Optional($0), label($0)) }
}

// MARK: - Weight and height

/// Weight or height: the latest value, the chart and the log.
struct BodyView: View {
    let metric: BodyMetric
    @ObservedObject var log: Observed<BodyModel, BodyUi>
    let units: UnitSystem
    @State private var logging = false

    var body: some View {
        let state = log.value
        let isWeight = metric == BodyMetric.weight
        Group {
            if state.history.isEmpty {
                ScrollView {
                    PoulpyCard(
                        title: Text(LocalizedStringKey(isWeight ? "body.emptyWeightTitle" : "body.emptyHeightTitle")),
                        message: Text(LocalizedStringKey(isWeight ? "body.emptyWeightBody" : "body.emptyHeightBody")),
                        button: Text(LocalizedStringKey(isWeight ? "body.logWeight" : "body.logHeight")),
                        systemImage: "plus",
                        poulpySize: 120
                    ) { logging = true }
                    .padding(.horizontal, 20)
                    .padding(.top, 16)
                }
            } else {
                List {
            if let latest = state.latest {
                Section {
                    HStack(spacing: 14) {
                        Image("PoulpyMoodHappy").resizable().scaledToFit().frame(width: 56, height: 56).accessibilityHidden(true)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(bodyText(metric, latest.value, units)).font(.poulpy(.title2))
                            Text(L("body.entries %lld", Int64(state.history.count))).font(.poulpy(.subheadline))
                        }
                    }
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .padding(.vertical, 6)
                    .listRowBackground(PoulpyColor.primaryContainer)
                } footer: {
                    Text(L("body.lastLogged %@", shown(latest.date)))
                }
            } else {
                Section { Text("body.nothingLogged") }
            }
            Section {
                chart(state)
                HStack(spacing: 8) {
                    ForEach([(BodyRange.month, "body.range1m"), (.threeMonths, "body.range3m"), (.year, "body.range1y"), (.all, "body.rangeAll")], id: \.1) { range, key in
                        let on = state.range == range
                        Button { log.model.selectRange(selected: range) } label: {
                            Text(LocalizedStringKey(key))
                                .font(.poulpy(.subheadline).weight(.heavy))
                                .frame(maxWidth: .infinity, minHeight: 36)
                                .foregroundStyle(on ? PoulpyColor.onPrimary : PoulpyColor.onSurfaceVariant)
                                .background(on ? PoulpyColor.primary : Color.clear, in: Capsule())
                                .overlay(Capsule().stroke(on ? PoulpyColor.primary : PoulpyColor.outline, lineWidth: 1.5))
                        }
                        .buttonStyle(.plain)
                        .accessibilityAddTraits(on ? .isSelected : [])
                    }
                }
            }
            if isWeight, let change = state.change {
                Section {
                    HStack(alignment: .top, spacing: 8) {
                        stat("body.change", BodyDisplay.shared.weightChange(deltaKg: change.doubleValue, system: units, languageCode: Language.code), rangeNote(state.range))
                        if let low = state.lowest { stat("body.lowest", bodyText(metric, low.value, units), shown(low.date)) }
                        if let high = state.highest { stat("body.highest", bodyText(metric, high.value, units), shown(high.date)) }
                    }
                }
            }
            if !isWeight {
                Section { Text("body.heightNote").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant) }
            }
            if !state.history.isEmpty {
                Section("body.history") {
                    ForEach(state.history, id: \.epochDay) { point in
                        HStack {
                            Text(bodyText(metric, point.value, units)).font(.poulpy(.headline))
                            Spacer()
                            Text(shown(point.date)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                        }
                        .swipeActions {
                            Button(role: .destructive) {
                                log.model.deleteOn(date: point.date)
                            } label: {
                                Label("action.delete", systemImage: "trash")
                            }
                        }
                        .accessibilityAction(named: Text(L("body.deleteEntry %@", shown(point.date)))) { log.model.deleteOn(date: point.date) }
                    }
                }
            }
                }
            }
        }
        .navigationTitle(isWeight ? LocalizedStringKey("body.weight") : LocalizedStringKey("body.height"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button { logging = true } label: {
                    Text("body.log")
                        .font(.poulpy(.subheadline).weight(.heavy))
                        .foregroundStyle(PoulpyColor.onPrimary)
                        .padding(.horizontal, 18)
                        .frame(height: 40)
                        .background(PoulpyColor.primary, in: Capsule())
                }
            }
        }
        .sheet(isPresented: $logging) {
            LogSheet(metric: metric, units: units, start: state.latest?.value ?? BodyDisplay.shared.starting(metric: metric)) { value, day in
                log.model.logOn(value: value, date: planDate(day))
            }
        }
    }

    /// A line with a dot per entry over a grid of round values, with the unit above the axis. The grid is neutral:
    /// a weight going up is never red.
    private func chart(_ state: BodyUi) -> some View {
        let shared = BodyDisplay.shared
        let shownValues = state.points.map { shared.chartValue(metric: metric, stored: $0.value, system: units) }
        let span = shared.chartMinSpan(metric: metric, system: units)
        // Always drawn: with nothing in the range it is the grid alone, around the latest value.
        let centre = shared.chartValue(metric: metric, stored: state.latest?.value ?? shared.starting(metric: metric), system: units)
        let ticks = (state.points.isEmpty
            ? ChartAxis.shared.ticks(low: centre, high: centre, minSpan: span * 3, target: 4)
            : ChartAxis.shared.ticks(low: shownValues.min() ?? 0, high: shownValues.max() ?? 0, minSpan: span, target: 4)
        ).map { $0.doubleValue }
        let first = state.points.first
        let last = state.points.last
        let firstDay = Double(first?.epochDay ?? 0)
        let lastDay = Double(last?.epochDay ?? 1)
        let description = (first != nil && last != nil)
            ? L("body.chart %@ %@ %@ %@", bodyText(metric, first!.value, units), shown(first!.date), bodyText(metric, last!.value, units), shown(last!.date))
            : L("body.chartEmpty")
        return VStack(alignment: .leading, spacing: 4) {
            Text(verbatim: shared.axisUnit(metric: metric, system: units))
                .font(.poulpy(.caption2)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            Chart {
                ForEach(Array(state.points.enumerated()), id: \.offset) { index, point in
                    LineMark(x: .value("", Double(point.epochDay)), y: .value("", shownValues[index]))
                        .foregroundStyle(PoulpyColor.primary)
                    PointMark(x: .value("", Double(point.epochDay)), y: .value("", shownValues[index]))
                        .foregroundStyle(PoulpyColor.primary)
                }
            }
            .chartYScale(domain: (ticks.first ?? 0)...(ticks.last ?? 1))
            .chartXScale(domain: firstDay...max(lastDay, firstDay + 1), range: .plotDimension(padding: 8))
            .chartYAxis {
                AxisMarks(position: .leading, values: ticks) { value in
                    AxisGridLine().foregroundStyle(PoulpyColor.outline)
                    AxisValueLabel {
                        if let number = value.as(Double.self) {
                            Text(verbatim: shared.axisLabel(metric: metric, shown: number, system: units, languageCode: Language.code))
                        }
                    }
                }
            }
            .chartXAxis {
                AxisMarks(values: state.points.isEmpty ? [] : (first?.epochDay == last?.epochDay ? [firstDay] : [firstDay, lastDay])) { value in
                    AxisValueLabel {
                        if let day = value.as(Double.self), let first, let last {
                            Text(verbatim: shown(day == firstDay ? first.date : last.date))
                        }
                    }
                }
            }
            .frame(height: 190)
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(description))
        }
    }

    private func stat(_ name: LocalizedStringKey, _ value: String, _ note: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(name).font(.poulpy(.caption)).foregroundStyle(PoulpyColor.onSurfaceVariant)
            Text(value).font(.poulpy(.headline))
            Text(note).font(.poulpy(.caption2)).foregroundStyle(PoulpyColor.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func rangeNote(_ range: BodyRange) -> String {
        switch range {
        case .month: return L("body.overMonth")
        case .threeMonths: return L("body.overThreeMonths")
        case .year: return L("body.overYear")
        default: return L("body.overAll")
        }
    }
}

/// Log a weight or height: type the number (the keyboard opens with it selected), nudge with − and +, pick the date.
private struct LogSheet: View {
    let metric: BodyMetric
    let units: UnitSystem
    let start: Double
    let onSave: (Double, Date) -> Void
    @State private var text: String
    @State private var feet: String
    @State private var inches: String
    @State private var day = Date()
    @Environment(\.dismiss) private var dismiss

    init(metric: BodyMetric, units: UnitSystem, start: Double, onSave: @escaping (Double, Date) -> Void) {
        self.metric = metric
        self.units = units
        self.start = start
        self.onSave = onSave
        let shared = BodyDisplay.shared
        _text = State(initialValue: metric == BodyMetric.weight
            ? shared.weightText(kg: start, system: units, languageCode: Language.code)
            : shared.centimetresText(cm: start))
        _feet = State(initialValue: shared.feetText(cm: start))
        _inches = State(initialValue: shared.inchesText(cm: start, languageCode: Language.code))
    }

    private var isWeight: Bool { metric == BodyMetric.weight }
    private var feetAndInches: Bool { !isWeight && units == UnitSystem.imperial }

    /// What the fields hold now, in kilograms or centimetres; nil while they are empty or out of range.
    private var typed: Double? {
        let shared = BodyDisplay.shared
        if isWeight { return shared.weightFrom(text: text, system: units)?.doubleValue }
        if feetAndInches { return shared.centimetresFrom(feet: feet, inches: inches)?.doubleValue }
        return shared.centimetresFrom(text: text)?.doubleValue
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    Text(isWeight ? LocalizedStringKey("body.weightStored") : LocalizedStringKey("body.heightStored"))
                        .font(.poulpy(.subheadline)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    NumberEntry(onLess: { nudge(-1) }, onMore: { nudge(1) }) {
                        if feetAndInches {
                            HStack(spacing: 8) {
                                NumberField(text: $feet, description: "body.feet", unit: L("body.feet"), large: true)
                                NumberField(text: $inches, description: "body.inches", unit: L("body.inches"), decimal: true, large: true)
                            }
                        } else {
                            NumberField(
                                text: $text, description: isWeight ? "body.weight" : "body.height",
                                unit: BodyDisplay.shared.axisUnit(metric: metric, system: units), decimal: isWeight, large: true
                            )
                        }
                    }
                    FieldBox(height: 56) {
                        DatePicker("body.date", selection: $day, in: ...Date(), displayedComponents: .date)
                    }
                }
                .padding(20)
            }
            .scrollDismissesKeyboard(.interactively)
            .navigationTitle(isWeight ? LocalizedStringKey("body.logWeight") : LocalizedStringKey("body.logHeight"))
            .navigationBarTitleDisplayMode(.inline)
            .safeAreaInset(edge: .bottom) {
                SheetActions(canSave: typed != nil) {
                    if let typed { onSave(typed, day) }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }

    private func nudge(_ direction: Int32) {
        let shared = BodyDisplay.shared
        let next = shared.step(metric: metric, stored: typed ?? start, system: units, direction: direction)
        if isWeight {
            text = shared.weightText(kg: next, system: units, languageCode: Language.code)
        } else if feetAndInches {
            feet = shared.feetText(cm: next)
            inches = shared.inchesText(cm: next, languageCode: Language.code)
        } else {
            text = shared.centimetresText(cm: next)
        }
    }
}

// MARK: - Helpers

private func bodyText(_ metric: BodyMetric, _ value: Double, _ units: UnitSystem) -> String {
    BodyDisplay.shared.value(metric: metric, stored: value, system: units, languageCode: Language.code)
}

private func date(from day: PlanDate) -> Date {
    Calendar.current.date(from: DateComponents(year: Int(day.year), month: Int(day.month), day: Int(day.day))) ?? Date()
}

private func shown(_ day: PlanDate) -> String {
    date(from: day).formatted(date: .abbreviated, time: .omitted)
}

private func planDate(_ date: Date) -> PlanDate {
    let parts = Calendar.current.dateComponents([.year, .month, .day], from: date)
    return PlanDate(year: Int32(parts.year ?? 1970), month: Int32(parts.month ?? 1), day: Int32(parts.day ?? 1))
}

/// A localized string with printf-style arguments, as in Localizable.strings.
private func L(_ key: String, _ args: CVarArg...) -> String {
    String(format: NSLocalizedString(key, comment: ""), locale: Locale.current, arguments: args)
}
