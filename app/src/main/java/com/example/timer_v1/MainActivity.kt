package com.example.timer_v1

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timer_v1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),
    TimerService.TimerCallback{
        private lateinit var binding: ActivityMainBinding
        private lateinit var intent: Intent
        private var myTimer: TimerService? = null
        private var isBound = false
        private var serviceIsRunning = false
        private var time = 0
        private var timestamp = 0L
        private var notFirstOnStart = false

        private val serviceConnection = object : ServiceConnection{
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?
            ) {
                val binder = service as TimerService.LocalBinder
                myTimer = binder.getService()
                myTimer?.setCallback(this@MainActivity)
                isBound = true
                if (notFirstOnStart){
                    myTimer?.startTimer(time - timestamp.toInt())
                    binding.time.text = (time - timestamp).toString()
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                myTimer = null
                isBound = false
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            binding.btnStop1.isEnabled = false
        }

        override fun onStart() {
            super.onStart()

            this.timestamp = ((System.currentTimeMillis()/1000) - this.timestamp)
            serviceConfig(serviceIsRunning, isBound)
            Log.d("TimerService", "onStart -> Foreground = $isBound")

            setUI()
        }

        override fun onStop() {
            super.onStop()

//            this.timestamp = ((System.currentTimeMillis()/1000) - this.timestamp)

            notFirstOnStart = myTimer?.getIsRunning() == true
            stopBindNService()
        }

        // ===================================================================
        override fun onTimerUpdate(timer: Int) {
            binding.time.text = timer.toString()
        }

        override fun onFinished(finishTime: Int) {
            binding.time.text = finishTime.toString()
            this.timestamp = 0
            myTimer?.stopTimer()
            stopBindNService()
            setBtn(false)
            notFirstOnStart = false
        }

        // ===================================================================
        private fun setBtn(state: Boolean){
            when(state){
                true -> {
                    binding.btnStart1.isEnabled = false
                    binding.btnStop1.isEnabled = true
                }
                false -> {
                    binding.btnStart1.isEnabled = true
                    binding.btnStop1.isEnabled = false
                }
            }
        }

        private fun setUI(){
            binding.btnStart1.setOnClickListener {
                val inputTime = binding.eTnum.text.toString().toIntOrNull()

                if (inputTime != null){
                    time = inputTime
                    binding.time.text = time.toString()

                    serviceConfig(serviceIsRunning, isBound)

                    myTimer?.startTimer(time)

                    // mengubah text time

                    // bagian tambahan untuk set tv `time`, menghapus input et, serta menutup keyboard.
                    binding.eTnum.text.clear()
                    binding.eTnum.clearFocus()
                    val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.eTnum.windowToken, 0)

                    // atur button
                    setBtn(true)
                    this.timestamp = System.currentTimeMillis() / 1000
                }else{
                    Toast.makeText(this, "Enter a Number", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnStop1.setOnClickListener {
                myTimer?.stopTimer()
                stopBindNService()
                notFirstOnStart = false
                this.timestamp = 0
                setBtn(false)
            }
        }

        private fun serviceConfig(serviceIsRun: Boolean, isBind: Boolean){
            intent = Intent(this, TimerService::class.java)
            if (!serviceIsRun){
                startService(intent)
                this.serviceIsRunning = true
            }

            if (!isBind){
                bindService(intent, serviceConnection, BIND_AUTO_CREATE)
                this.isBound = true
            }
        }

        fun stopBindNService(){
            Log.d("TimerService", "STOP TIMER!! (MAIN)")
            if (isBound){
                unbindService(serviceConnection)
                stopService(intent)
                isBound = false
                serviceIsRunning = false
            }
        }
}