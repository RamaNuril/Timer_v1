package com.example.timer_v1

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimerService: Service() {

    // 🔗 Interface callback ke UI
    interface TimerCallback{
        fun onTimerUpdate(timer: Int)
        fun onFinished(finishTime: Int)
    }

    // 🔒 Binder supaya Activity bisa akses service
    inner class LocalBinder: Binder(){
        fun getService(): TimerService = this@TimerService
    }

    private val binder = LocalBinder()
    private var callback: TimerCallback? = null

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var job: Job? = null


    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    fun setCallback(callback: TimerCallback){
        this.callback = callback
    }

    fun startTimer(inputTime: Int = 0){
        job =scope.launch {
            for (i in inputTime -1 downTo 0){
                delay(1000)
                Log.d("TimerService", "Timer: $i")
                withContext(Dispatchers.Main){
                    callback?.onTimerUpdate(i)
                }
            }

            withContext(Dispatchers.Main){
                callback?.onFinished(0)
            }

            stopSelf()
        }
    }

    fun stopTimer(){
        job?.cancel()
        job = null
    }

    override fun onDestroy() {
        super.onDestroy()

        job?.cancel()
        job = null
    }
}