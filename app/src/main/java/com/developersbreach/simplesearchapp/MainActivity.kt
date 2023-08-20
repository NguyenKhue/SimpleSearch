package com.developersbreach.simplesearchapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity(), SearchAdapter.SportsAdapterListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var editText: AppCompatEditText
    private lateinit var noSearchResultsFoundText: TextView
    private lateinit var sportsList: List<Sports>
    private lateinit var clearQueryImageView: Button
    private lateinit var voiceSearchImageView: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.search_list)
        editText = findViewById(R.id.search_edit_text)
        noSearchResultsFoundText = findViewById(R.id.no_search_results_found_text)
        clearQueryImageView = findViewById(R.id.clear_search_query)
        voiceSearchImageView = findViewById(R.id.voice_search_query)

        sportsList = sportsList(applicationContext)
        attachAdapter(sportsList)

        editText.doOnTextChanged { text, _, _, _ ->
            val query = text.toString().lowercase(Locale.getDefault())
            filterWithQuery(query)
            toggleImageView(query)
        }

        voiceSearchImageView.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            launchVoice.launch(intent)
        }

        clearQueryImageView.setOnClickListener {
            editText.setText("")
        }
    }

    private fun attachAdapter(list: List<Sports>) {
        searchAdapter = SearchAdapter(list, this)
        recyclerView.adapter = searchAdapter
    }

    private fun filterWithQuery(query: String) {
        if (query.isNotEmpty()) {
            val filteredList: List<Sports> = onFilterChanged(query)
            attachAdapter(filteredList)
            toggleRecyclerView(filteredList)
        } else if (query.isEmpty()) {
            attachAdapter(sportsList)
        }
    }

    private fun onFilterChanged(filterQuery: String): List<Sports> {
        val filteredList = ArrayList<Sports>()
        for (currentSport in sportsList) {
            if (currentSport.title.lowercase(Locale.getDefault()).contains(filterQuery)) {
                filteredList.add(currentSport)
            }
        }
        return filteredList
    }

    private val launchVoice = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText: String? =
                result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).let { results ->
                    results?.get(0)
                }

            editText.setText(spokenText)
        }
    }

    private fun toggleRecyclerView(sportsList: List<Sports>) {
        if (sportsList.isEmpty()) {
            recyclerView.visibility = View.INVISIBLE
            noSearchResultsFoundText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noSearchResultsFoundText.visibility = View.INVISIBLE
        }
    }

    private fun toggleImageView(query: String) {
        if (query.isNotEmpty()) {
            clearQueryImageView.visibility = View.VISIBLE
            voiceSearchImageView.visibility = View.INVISIBLE
        } else if (query.isEmpty()) {
            clearQueryImageView.visibility = View.INVISIBLE
            voiceSearchImageView.visibility = View.VISIBLE
        }
    }

    override fun onSportSelected(sports: Sports?) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra("DETAIL_SPORTS_DATA", sports)
        startActivity(intent)
    }
}