package com.fear.slanderous.talks.ohs

import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.fear.slanderous.talks.R
import com.fear.slanderous.talks.databinding.TssOneBinding
import com.fear.slanderous.talks.ohs.model.NavigationEvent
import com.fear.slanderous.talks.ohs.model.UiState
import com.fear.slanderous.talks.viewmodel.TssOneViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TssOne : AppCompatActivity() {
    private val binding by lazy { TssOneBinding.inflate(layoutInflater) }
    private val viewModel: TssOneViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.one)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback {
        }

        observeUiState()
        observeNavigationEvents()
        viewModel.startCountdown()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> {
                        // 处理加载状态
                    }
                    is UiState.Success -> {
                        binding.progressBar.progress = state.data
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
                        startActivity(event.intent)
                        finish()
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
                    startActivity(intent)
                    finish()
                } else {
                    // 不是最后一个intent，使用startActivity
                    startActivity(intent)
                }
            }
        }
    }
}