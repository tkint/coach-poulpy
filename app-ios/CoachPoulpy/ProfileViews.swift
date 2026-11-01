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
                    }
                    .foregroundStyle(PoulpyColor.onPrimaryContainer)
                    .padding(.vertical, 6)
                }
                .listRowBackground(PoulpyColor.primaryContainer)
            }
            Section("settings.body") {
                NavigationLink(value: SettingsPage.weight) {
                    row("body.weight", weight.value.latest.map { bodyText(.weight, $0.value, units) })
                }
                NavigationLink(value: SettingsPage.height) {
                    row("body.height", height.value.latest.map { bodyText(.height, $0.value, units) })
                }
            }
            Section("settings.app") {
                NavigationLink(value: SettingsPage.preferences) { row("settings.appearanceUnits", nil) }
                NavigationLink(value: SettingsPage.devices) { row("settings.devices", nil) }
            }
            Section("settings.about") {
                Text(String(format: NSLocalizedString("settings.version %@", comment: ""), Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "—"))
            }
        }
    }

    private func row(_ title: LocalizedStringKey, _ value: String?) -> some View {
        HStack {
            Text(title).font(.poulpy(.headline))
            Spacer()
            if let value { Text(value).foregroundStyle(PoulpyColor.onSurfaceVariant) }
        }
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
    case birthdate, pronouns, sex, goal, experience
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
        List {
            Section {
                TextField("profile.name", text: $name)
                    .onChange(of: name) { model.setDisplayName(name: $0) }
                selectRow("profile.birthdate", state.birthdate.map { birthdateText($0, age: state.age) } ?? notSet) { sheet = .birthdate }
                selectRow("profile.pronouns", state.pronouns.map(pronounLabel) ?? notSet) { sheet = .pronouns }
                if state.pronouns == Pronouns.custom {
                    TextField("profile.customPronouns", text: $custom)
                        .onChange(of: custom) { model.setPronouns(pronouns: Pronouns.custom, custom: $0) }
                }
                selectRow("profile.sex", state.sexAssignedAtBirth.map(sexLabel) ?? notSet) { sheet = .sex }
            } header: {
                Text("profile.aboutYou")
            } footer: {
                VStack(alignment: .leading, spacing: 8) {
                    Text("profile.pronounsNote")
                    Text("profile.sexNote")
                }
            }
            Section {
                selectRow("profile.goal", state.mainGoal.map(goalLabel) ?? notSet) { sheet = .goal }
                selectRow("profile.experience", state.experience.map(experienceLabel) ?? notSet) { sheet = .experience }
                VStack(alignment: .leading, spacing: 4) {
                    Text("profile.restingHr").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    TextField("profile.restingHrValue", text: $heartRate)
                        .keyboardType(.numberPad)
                        .onChange(of: heartRate) { model.setRestingHeartRateText(text: $0) }
                }
            } header: {
                Text("profile.training")
            } footer: {
                Text("profile.restingHrHint")
            }
        }
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
                    title: "profile.pronouns", why: "profile.pronounsNote", note: nil, selected: state.pronouns,
                    options: withNotSet([Pronouns.sheHer, .heHim, .theyThem, .justMyName, .custom, .preferNotToSay], pronounLabel),
                    onSave: { model.setPronouns(pronouns: $0, custom: custom) }
                )
            case .sex:
                OptionSheet(
                    title: "profile.sex", why: "profile.sexWhy", note: "profile.sexNotGender", selected: state.sexAssignedAtBirth,
                    options: withNotSet([SexAssignedAtBirth.female, .male, .intersex, .preferNotToSay], sexLabel),
                    onSave: { model.setSexAssignedAtBirth(sex: $0) }
                )
            case .goal:
                OptionSheet(
                    title: "profile.goal", why: nil, note: nil, selected: state.mainGoal,
                    options: withNotSet([MainGoal.stayActive, .buildStrength, .runFurther, .rehab, .somethingElse], goalLabel),
                    onSave: { model.setMainGoal(goal: $0) }
                )
            case .experience:
                OptionSheet(
                    title: "profile.experience", why: nil, note: nil, selected: state.experience,
                    options: withNotSet([Experience.justStarting, .regular, .experienced], experienceLabel),
                    onSave: { model.setExperience(experience: $0) }
                )
            }
        }
    }

    private func selectRow(_ title: LocalizedStringKey, _ value: String, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                    Text(value).font(.poulpy(.headline)).foregroundStyle(PoulpyColor.onSurface)
                }
                Spacer()
                Image(systemName: "chevron.right").font(.footnote).foregroundStyle(PoulpyColor.onSurfaceVariant)
            }
        }
    }

    private func birthdateText(_ born: PlanDate, age: KotlinInt?) -> String {
        guard let age else { return shown(born) }
        return L("profile.birthdateValue %lld %@", Int64(age.intValue), shown(born))
    }
}

/// Pick one option and save it. A nil value is "not set".
private struct OptionSheet<T: Hashable>: View {
    let title: LocalizedStringKey
    let why: LocalizedStringKey?
    let note: LocalizedStringKey?
    let options: [(T?, String)]
    let onSave: (T?) -> Void
    @State private var choice: T?
    @Environment(\.dismiss) private var dismiss

    init(title: LocalizedStringKey, why: LocalizedStringKey?, note: LocalizedStringKey?, selected: T?, options: [(T?, String)], onSave: @escaping (T?) -> Void) {
        self.title = title
        self.why = why
        self.note = note
        self.options = options
        self.onSave = onSave
        _choice = State(initialValue: selected)
    }

    var body: some View {
        NavigationStack {
            List {
                if let why {
                    Section { Text(why) }
                }
                Section {
                    ForEach(options.indices, id: \.self) { index in
                        Button {
                            choice = options[index].0
                        } label: {
                            HStack {
                                Text(options[index].1).foregroundStyle(PoulpyColor.onSurface)
                                Spacer()
                                if choice == options[index].0 {
                                    Image(systemName: "checkmark").foregroundStyle(PoulpyColor.primary)
                                }
                            }
                        }
                    }
                } footer: {
                    if let note { Text(note) }
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(LocalizedStringKey("action.cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(LocalizedStringKey("action.save")) {
                        onSave(choice)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
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

private func goalLabel(_ value: MainGoal) -> String {
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
    case .regular: return L("profile.experienceRegular")
    default: return L("profile.experienceExperienced")
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
                Picker(selection: Binding(get: { state.range }, set: { log.model.selectRange(selected: $0) })) {
                    Text("body.range1m").tag(BodyRange.month)
                    Text("body.range3m").tag(BodyRange.threeMonths)
                    Text("body.range1y").tag(BodyRange.year)
                    Text("body.rangeAll").tag(BodyRange.all)
                } label: {
                    Text("body.range")
                }
                .pickerStyle(.segmented)
                if state.points.isEmpty {
                    Text("body.chartEmpty").font(.poulpy(.footnote)).foregroundStyle(PoulpyColor.onSurfaceVariant)
                } else {
                    chart(state)
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
        .navigationTitle(isWeight ? LocalizedStringKey("body.weight") : LocalizedStringKey("body.height"))
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .navigationBarTrailing) {
                Button(LocalizedStringKey("body.log")) { logging = true }
            }
        }
        .sheet(isPresented: $logging) {
            LogSheet(metric: metric, units: units, start: state.latest?.value ?? BodyDisplay.shared.starting(metric: metric)) { value, day in
                log.model.logOn(value: value, date: planDate(day))
            }
        }
    }

    private func chart(_ state: BodyUi) -> some View {
        let values = state.points.map { $0.value }
        let low = values.min() ?? 0
        let high = values.max() ?? 0
        let pad = max((high - low) * 0.2, 0.5)
        let first = state.points.first!
        let last = state.points.last!
        let description = L("body.chart %@ %@ %@ %@", bodyText(metric, first.value, units), shown(first.date), bodyText(metric, last.value, units), shown(last.date))
        return Chart(state.points, id: \.epochDay) { point in
            LineMark(x: .value("", Double(point.epochDay)), y: .value("", point.value))
                .foregroundStyle(PoulpyColor.primary)
            PointMark(x: .value("", Double(point.epochDay)), y: .value("", point.value))
                .foregroundStyle(PoulpyColor.primary)
        }
        .chartYScale(domain: (low - pad)...(high + pad))
        .chartXAxis(.hidden)
        .chartYAxis(.hidden)
        .frame(height: 160)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(description))
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

/// Log a weight or height: a stepper in the person's units and a date.
private struct LogSheet: View {
    let metric: BodyMetric
    let units: UnitSystem
    let onSave: (Double, Date) -> Void
    @State private var value: Double
    @State private var day = Date()
    @Environment(\.dismiss) private var dismiss

    init(metric: BodyMetric, units: UnitSystem, start: Double, onSave: @escaping (Double, Date) -> Void) {
        self.metric = metric
        self.units = units
        self.onSave = onSave
        _value = State(initialValue: start)
    }

    var body: some View {
        let isWeight = metric == BodyMetric.weight
        NavigationStack {
            Form {
                Section {
                    HStack {
                        stepButton("−", "body.less", -1)
                        Spacer()
                        Text(bodyText(metric, value, units)).font(.poulpy(.title2))
                        Spacer()
                        stepButton("+", "body.more", 1)
                    }
                } footer: {
                    Text(isWeight ? LocalizedStringKey("body.weightStored") : LocalizedStringKey("body.heightStored"))
                }
                Section {
                    DatePicker("body.date", selection: $day, in: ...Date(), displayedComponents: .date)
                }
            }
            .navigationTitle(isWeight ? LocalizedStringKey("body.logWeight") : LocalizedStringKey("body.logHeight"))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(LocalizedStringKey("action.cancel")) { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(LocalizedStringKey("action.save")) {
                        onSave(value, day)
                        dismiss()
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }

    private func stepButton(_ symbol: String, _ label: LocalizedStringKey, _ direction: Int32) -> some View {
        Button {
            value = BodyDisplay.shared.step(metric: metric, stored: value, system: units, direction: direction)
        } label: {
            Text(verbatim: symbol)
                .font(.poulpy(.title2))
                .frame(width: 56, height: 44)
                .background(PoulpyColor.surfaceVariant, in: RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.borderless)
        .accessibilityLabel(Text(label))
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
