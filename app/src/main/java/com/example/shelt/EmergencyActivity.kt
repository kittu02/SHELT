package com.example.shelt

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.textfield.TextInputEditText

class EmergencyActivity : ComponentActivity() {
    private lateinit var adapter: ArrayAdapter<String>
    private val numbers = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_emergency)

        val prefs = getSharedPreferences("emergency", MODE_PRIVATE)
        numbers.addAll(prefs.getStringSet("numbers", emptySet()) ?: emptySet())
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, numbers)
        val list = findViewById<ListView>(R.id.listNumbers)
        list.adapter = adapter
        list.choiceMode = ListView.CHOICE_MODE_SINGLE

        findViewById<android.view.View>(R.id.btnAdd).setOnClickListener {
            val input = findViewById<TextInputEditText>(R.id.etNumber).text.toString()
            if (input.isNotBlank()) {
                numbers.add(input)
                adapter.notifyDataSetChanged()
                findViewById<TextInputEditText>(R.id.etNumber).setText("")
                prefs.edit().putStringSet("numbers", numbers.toSet()).apply()
            }
        }
        findViewById<android.view.View>(R.id.btnRemove).setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos != ListView.INVALID_POSITION) {
                numbers.removeAt(pos)
                adapter.notifyDataSetChanged()
                prefs.edit().putStringSet("numbers", numbers.toSet()).apply()
            }
        }
        findViewById<android.view.View>(R.id.btnSetPrimary).setOnClickListener {
            val pos = list.checkedItemPosition
            if (pos != ListView.INVALID_POSITION) {
                prefs.edit().putString("primary", numbers[pos]).apply()
            }
        }
    }
}


