package com.example.testapplication

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.testapplication.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }


        binding.rg.setOnCheckedChangeListener { _, checkedId ->
            val rb = binding.root.findViewById(checkedId) as RadioButton
            for (i in 0 until binding.rg.childCount) {
                val mView = binding.rg.getChildAt(i)
                if (mView is RadioButton) {
                    mView.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }
            }
            rb.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.pink_donut),
                null,
                null
            )
        }

        binding.rootView.setOnTouchListener(object : OnSwipeTouchListener(context) {
            override fun onSwipeTop() {
                Toast.makeText(context, "top", Toast.LENGTH_SHORT).show()
            }

            override fun onSwipeRight() {
                Toast.makeText(context, "right", Toast.LENGTH_SHORT).show()
                var lastCheckedId: RadioButton? = null
                var lastUncheckId: RadioButton? = null
                for (i in 0 until binding.rg.childCount) {
                    val mView = binding.rg.getChildAt(i)
                    if (mView is RadioButton) {
                        if (mView.isChecked) {
                            lastCheckedId = mView
                            if (binding.rg.childCount - 1 == i) {
                                lastUncheckId = mView
                            }
                        } else {
                            lastUncheckId = mView
                            if (lastCheckedId != null) {
                                break
                            }
                        }
                    }
                }
                lastUncheckId?.isChecked = true
            }

            override fun onSwipeLeft() {
                Toast.makeText(context, "left", Toast.LENGTH_SHORT).show()
                var lastUncheckId: RadioButton? = null
                for (i in 0 until binding.rg.childCount) {
                    val mView = binding.rg.getChildAt(i)
                    if (mView is RadioButton) {
                        if (mView.isChecked) {
                            break
                        } else {
                            lastUncheckId = mView
                        }
                    }
                }
                lastUncheckId?.isChecked = true
            }

            override fun onSwipeBottom() {
                Toast.makeText(context, "bottom", Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun setDrawable(isChecked: Boolean, radioButton: RadioButton) {
        if (isChecked) {
            radioButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.pink_donut),
                null,
                null
            )
        } else {
            radioButton.setCompoundDrawablesWithIntrinsicBounds(
                null,
                ContextCompat.getDrawable(requireContext(), R.drawable.pink_donut),
                null,
                null
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}