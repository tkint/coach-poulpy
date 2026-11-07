package ch.coachpoulpy.android

import android.app.Application
import android.os.Build
import android.provider.Settings
import ch.coachpoulpy.android.ui.AppPreferences
import ch.coachpoulpy.android.ui.KeystoreSecrets
import ch.coachpoulpy.data.local.DriverFactory
import ch.coachpoulpy.shared.AccountModel
import ch.coachpoulpy.shared.AppGraph
import java.util.Locale

class CoachPoulpyApp : Application() {
    lateinit var graph: AppGraph
        private set

    /** The account, made at start-up so that a signed-in phone syncs as soon as the app opens. */
    lateinit var account: AccountModel
        private set

    override fun onCreate() {
        super.onCreate()
        graph = AppGraph(DriverFactory(this))
        account = graph.account(
            baseUrl = BuildConfig.API_URL,
            secrets = KeystoreSecrets(this),
            memory = AppPreferences(this),
            // The name the phone's owner gave it, as in the server's device list; the model otherwise.
            deviceName = Settings.Global.getString(contentResolver, Settings.Global.DEVICE_NAME)?.takeIf { it.isNotBlank() } ?: Build.MODEL,
            languageCode = Locale.getDefault().language,
        )
    }
}
