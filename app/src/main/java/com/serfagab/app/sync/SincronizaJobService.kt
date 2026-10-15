package com.serfagab.app.sync

import android.app.job.JobParameters
import android.app.job.JobService
import androidx.core.content.edit
import com.serfagab.app.util.SessionStore

class SincronizaJobService : JobService() {

    private var hiloSincronizacion: Thread? = null

    override fun onStartJob(params: JobParameters): Boolean {
        hiloSincronizacion = Thread {
            val sesionActiva = SessionStore.restaurarSesion(applicationContext)

            val resultado = runCatching {
                sesionActiva && SyncManager.sincronizarCatalogos(applicationContext)
            }
            val exito = resultado.getOrDefault(false)

            if (exito) {
                getSharedPreferences(
                    SincronizaScheduler.PREFERENCIAS_SINCRONIZACION,
                    MODE_PRIVATE
                ).edit {
                    putLong(
                        SincronizaScheduler.CLAVE_ULTIMA_SINCRONIZACION,
                        System.currentTimeMillis()
                    )
                }
            }

            if (!Thread.currentThread().isInterrupted) {
                jobFinished(params, sesionActiva && !exito)
            }
        }.apply {
            name = "sincronizacion-catalogos"
            start()
        }
        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        hiloSincronizacion?.interrupt()
        return true
    }
}