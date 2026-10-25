import SwiftUI
import CoreBluetooth
import Shared

/// Remembers devices and settings between launches.
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

    /// True once the user allowed Bluetooth before, so connecting on its own never brings up a prompt.
    static var wasAllowed: Bool { CBCentralManager.authorization == .allowedAlways }

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

/// The app-wide models. Devices, Live and Settings are shared by the whole app, so they are never closed.
extension AppGraph {
    func observedSettings() -> Observed<SettingsModel, SettingsUiState> {
        let model = settings(memory: UserDefaultsStore())
        return Observed(model, current: model.state.value as! SettingsUiState, watch: model.watch, close: { _ in })
    }

    func observedDevices() -> Observed<DevicesModel, DevicesUiState> {
        let model = devices(memory: UserDefaultsStore())
        return Observed(model, current: model.state.value as! DevicesUiState, watch: model.watch, close: { _ in })
    }

    func observedLive() -> Observed<LiveModel, LiveUiState> {
        let model = live(memory: UserDefaultsStore(), languageCode: Language.code)
        return Observed(model, current: model.state.value as! LiveUiState, watch: model.watch, close: { _ in })
    }
}

private struct UnitSystemKey: EnvironmentKey {
    static let defaultValue = UnitSystem.metric
}

extension EnvironmentValues {
    /// The units picked in Settings › General, for every screen that shows a distance or a speed.
    var unitSystem: UnitSystem {
        get { self[UnitSystemKey.self] }
        set { self[UnitSystemKey.self] = newValue }
    }
}

enum AppLanguage {
    /// iOS reads the app's language at launch, so a change shows the next time the app opens.
    static func apply(_ choice: LanguageChoice) {
        if let code = choice.code {
            UserDefaults.standard.set([code], forKey: "AppleLanguages")
        } else {
            UserDefaults.standard.removeObject(forKey: "AppleLanguages")
        }
    }
}

extension ThemeChoice {
    var colorScheme: ColorScheme? {
        switch self {
        case .light: return .light
        case .dark: return .dark
        default: return nil
        }
    }
}

extension Measure {
    /// "5.20 km", in the phone's number format.
    var text: String { "\(value.formatted(.number.precision(.fractionLength(Int(decimals))))) \(unit)" }
}
