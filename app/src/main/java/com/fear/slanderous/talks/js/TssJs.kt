package com.fear.slanderous.talks.js

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssJsBinding
import com.fear.slanderous.talks.dwj.TssDwj
import com.fear.slanderous.talks.sm.FileUtils
import com.fear.slanderous.talks.zp.TssZp


class TssJs : AppCompatActivity() {
    private val binding by lazy { TssJsBinding.inflate(layoutInflater) }

    private var countdownTimer: CountDownTimer? = null
    var jumpType = ""
    private var scanCountDownTimer: CountDownTimer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fl_end)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cleanedSize = intent.getLongExtra("CLEANED_SIZE", 0L)
        jumpType = intent.getStringExtra("jump_type") ?: ""

        binding.tvEndSize.text = "Saved ${FileUtils.formatFileSize(cleanedSize)} space for you"

        showCleaningDialog()
        binding.dialogType.tvBack.setOnClickListener {
            finish()
        }
        binding.title.setOnClickListener {
            finish()
        }
        binding.dialogType.conClean.setOnClickListener {

        }
        binding.llImage.setOnClickListener {
            startActivity(Intent(this@TssJs, TssZp::class.java))
            finish()

        }
        binding.llFile.setOnClickListener {
            startActivity(Intent(this@TssJs, TssDwj::class.java))
            finish()
        }

    }


    private fun showCleaningDialog() {
        val iamgeData = if (jumpType == "image") {
            R.drawable.pic_icon
        } else if (jumpType == "file") {
            R.drawable.dwj_icon_main
        } else {
            R.drawable.logo_clean
        }
        binding.dialogType.root.setOnClickListener {  }
        binding.dialogType.tvBack.setOnClickListener { finish() }
        binding.dialogType.tvTitle.text = "Cleaned"
        binding.dialogType.tvTip.text = "Cleanning..."

        binding.dialogType.imgLogo.setImageResource(iamgeData)
        binding.dialogType.conClean.visibility = View.VISIBLE

        val rotateAnimation = android.view.animation.RotateAnimation(
            0f, 360f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
            android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f
        )
        rotateAnimation.duration = 1500
        rotateAnimation.repeatCount = android.view.animation.Animation.INFINITE
        rotateAnimation.interpolator = android.view.animation.LinearInterpolator()

        binding.dialogType.imgBg1.startAnimation(rotateAnimation)

        binding.dialogType.conClean.postDelayed({
            binding.dialogType.conClean.visibility = View.GONE
            binding.dialogType.imgBg1.clearAnimation()
        }, 1500)
    }



    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}
