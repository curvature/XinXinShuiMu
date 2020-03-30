package net.newsmth.dirac.fragment

import android.app.ProgressDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.format.Formatter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.facebook.drawee.backends.pipeline.Fresco
import net.newsmth.dirac.R
import net.newsmth.dirac.http.HttpHelper
import net.newsmth.dirac.provider.BoardCrawler
import net.newsmth.dirac.util.RetrofitUtils
import java.io.IOException

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(bundle: Bundle?, s: String?) {
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        listView.setPadding(0, 0, 0, 0)

        val eula = findPreference<Preference>("pref_eula")
        eula?.setOnPreferenceClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.newsmth.net/about/announce.html")))
            true
        }

        val update = findPreference<Preference>("pref_check_for_update")
        try {
            update?.summary = getString(R.string.current_version,
                    activity!!.packageManager
                            .getPackageInfo(activity!!.packageName, 0).versionName)
        } catch (e: PackageManager.NameNotFoundException) {
        }

        update?.setOnPreferenceClickListener {
            try {
                startActivity(
                        Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${activity?.packageName}")
                        )
                )
            } catch (e: Exception) {

            }
            true
        }

        val clearPostCache = findPreference<Preference>("pref_clear_post_cache")
        try {
            clearPostCache?.summary = getString(R.string.current_cache_size,
                    Formatter.formatFileSize(activity,
                            HttpHelper.getInstance().cache!!.size().coerceAtLeast(0)))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        clearPostCache?.setOnPreferenceClickListener {
            try {
                HttpHelper.getInstance().cache!!.evictAll()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            clearPostCache.setSummary(R.string.current_cache_size_zero)
            true
        }

        val clearImageCache = findPreference<Preference>("pref_clear_image_cache")
        clearImageCache?.summary = getString(R.string.current_cache_size,
                Formatter.formatFileSize(activity,
                        Fresco.getImagePipelineFactory().mainFileCache.size.coerceAtLeast(0)))

        clearImageCache?.setOnPreferenceClickListener {
            Fresco.getImagePipeline().clearDiskCaches()
            clearImageCache.setSummary(R.string.current_cache_size_zero)
            true
        }

        findPreference<Preference>("pref_update_board")
                ?.setOnPreferenceClickListener {

                    val progressDialog = ProgressDialog(activity)
                    progressDialog.setTitle(R.string.updating_boards)
                    progressDialog.setProgressNumberFormat(null)
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                    progressDialog.progress = 0
                    progressDialog.max = 963
                    progressDialog.isIndeterminate = false
                    progressDialog.setCancelable(false)
                    progressDialog.setCanceledOnTouchOutside(false)
                    progressDialog.show()

                    BoardCrawler().get(
                            { progress -> progressDialog.progress = progress },

                            {
                                progressDialog.dismiss()
                                AlertDialog.Builder(activity!!)
                                        .setTitle(R.string.update_board_success)
                                        .setPositiveButton(R.string.ok, null)
                                        .show()
                            },

                            {
                                progressDialog.dismiss()
                                AlertDialog.Builder(activity!!)
                                        .setTitle(R.string.update_board_failed)
                                        .setPositiveButton(R.string.ok, null)
                                        .show()
                            })

                    true

                }


    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "pref_use_https" -> RetrofitUtils.setUseHttps(sharedPreferences.getBoolean(key, true))
            "pref_night_mode" -> when (sharedPreferences.getString(key, null)) {
                "day" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    activity?.recreate()
                }
                "night" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    activity?.recreate()
                }
                "auto" -> {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME)
                    activity?.recreate()
                }
            }
        }
    }
}