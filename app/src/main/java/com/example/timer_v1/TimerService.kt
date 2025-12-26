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
    private var isRunning = false

    fun setIsRunning(state: Boolean){
        this.isRunning = state
    }

    fun getIsRunning(): Boolean{
        return this.isRunning
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    fun setCallback(callback: TimerCallback){
        this.callback = callback
    }

    fun startTimer(inputTime: Int = 0){
        if(getIsRunning()) return
        setIsRunning(true)
        job = scope.launch {
            for (i in inputTime -1 downTo 0){
                delay(1000)
                Log.d("TimerService", "Timer: $i")
                Log.d("TimerService", "isRunning = ${isRunning}")
                withContext(Dispatchers.Main){
                    callback?.onTimerUpdate(i)
                }
            }

            withContext(Dispatchers.Main){
                callback?.onFinished(0)
            }

            setIsRunning(false)
            stopSelf()
        }
    }

    fun stopTimer() {
        Log.d("TimerService", "STOP TIMER!! (SERVICE) 1")
        if(isRunning){
            Log.d("TimerService", "STOP TIMER!! (SERVICE) 2")
            job?.cancel()
            job = null
            setIsRunning(false)
            stopSelf()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        if (job?.isActive == true){
            Log.d("TimerService", "onDestroy")
            job?.cancel()
            job = null
            setIsRunning(false)
            stopSelf()
        }
    }
}