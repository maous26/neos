package com.neostream.app.ui.sources

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.neostream.app.databinding.ActivitySourcesBinding

class SourcesActivity : AppCompatActivity() {
  private lateinit var vb: ActivitySourcesBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    vb = ActivitySourcesBinding.inflate(layoutInflater)
    setContentView(vb.root)

    vb.btnAddSource.setOnClickListener {
      startActivity(Intent().setClassName(this, "com.neostream.app.ui.imports.AddSourceActivity"))
    }
  }
}
