package com.simple.notes

import android.app.Application
import com.simple.notes.data.VaultManager

class NotesApp : Application() {
    val vaults: VaultManager by lazy { VaultManager(this) }
}
