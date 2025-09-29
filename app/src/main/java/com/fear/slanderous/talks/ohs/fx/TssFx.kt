package com.fear.slanderous.talks.ohs.fx

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssShareBinding
import com.fear.slanderous.talks.ohs.model.NavigationEvent
import com.fear.slanderous.talks.ohs.model.UiState
import com.fear.slanderous.talks.viewmodel.TssFxViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TssFx : AppCompatActivity() {
    private val binding by lazy { TssShareBinding.inflate(layoutInflater) }
    private val viewModel: TssFxViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.share)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        observeUiState()
        observeNavigationEvents()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.atvPlo.setOnClickListener {
            //TODO
            viewModel.openInAppBrowser("https://www.google.com")
        }

        // 点击分享，分享应用商店链接
        binding.atvShare.setOnClickListener {
            viewModel.shareApp()
        }

        // 返回按钮
        binding.imgBack.setOnClickListener {
            viewModel.navigateBack()
        }
    }
    
    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 处理加载状态
                    }
                    is UiState.Success -> {
                        // 可以显示成功消息
                    }
                    is UiState.Error -> {
                        // 处理错误状态
                    }
                }
            }
        }
    }
    
    private fun observeNavigationEvents() {
        lifecycleScope.launch {
            viewModel.navigationEvent.collect { event ->
                when (event) {
                    is NavigationEvent.StartActivity -> {
                        if (event.intent.action == Intent.ACTION_SEND) {
                            startActivity(Intent.createChooser(event.intent, "Share via"))
                        } else {
                            startActivity(event.intent)
                        }
                    }
                    is NavigationEvent.MultipleJump -> {
                        performMultipleJumps(event.intents, event.delays)
                    }
                    NavigationEvent.Finish -> {
                        finish()
                    }
                }
            }
        }
    }
    
    private fun performMultipleJumps(intents: List<Intent>, delays: List<Long>) {
        lifecycleScope.launch {
            intents.forEachIndexed { index, intent ->
                if (index > 0) {
                    // 等待指定的延迟时间
                    delays.getOrNull(index - 1)?.let { delay(it) }
                }
                
                if (index == intents.size - 1) {
                    // 最后一个intent，finish当前activity
                    if (intent.action == Intent.ACTION_SEND) {
                        startActivity(Intent.createChooser(intent, "Share via"))
                    } else {
                        startActivity(intent)
                    }
                    finish()
                } else {
                    // 不是最后一个intent，使用startActivity
                    if (intent.action == Intent.ACTION_SEND) {
                        startActivity(Intent.createChooser(intent, "Share via"))
                    } else {
                        startActivity(intent)
                    }
                }
            }
        }
    }
}