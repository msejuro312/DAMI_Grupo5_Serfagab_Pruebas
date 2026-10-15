package com.serfagab.app.sync

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

object SincronizaScheduler {

    fun  programar(context: Context): Int {
        val componente = ComponentName(context, SincronizaJobService::class.java)
        val tarea = JobInfo.Builder(ID_TRABAJO_SINCRONIZACION, componente)
            .setMinimumLatency(DEMORA_MINIMA_MS)
            .setOverrideDeadline(PLAZO_MAXIMO_MS)
            .build()

        val scheduler = context.getSystemService(JobScheduler::class.java)
        return scheduler.schedule(tarea)
    }

    const val PREFERENCIAS_SINCRONIZACION = "estado_sincronizacion"
    const val CLAVE_ULTIMA_SINCRONIZACION = "ultima_sincronizacion"
    private const val ID_TRABAJO_SINCRONIZACION = 6002
    private const val DEMORA_MINIMA_MS = 1_000L
    private const val PLAZO_MAXIMO_MS = 5_000L
}