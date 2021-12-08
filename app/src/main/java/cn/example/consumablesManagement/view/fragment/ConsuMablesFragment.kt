package cn.example.consumablesManagement.view.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.example.consumablesManagement.R

class ConsuMablesFragment : Fragment() {

    companion object {
        fun newInstance() = ConsuMablesFragment()
    }

    private lateinit var viewModel: ConsuMablesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_consumables, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ConsuMablesViewModel::class.java)
        // TODO: Use the ViewModel
    }

}