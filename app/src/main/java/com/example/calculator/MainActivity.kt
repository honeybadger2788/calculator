package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.calculator.Constantes.OPERATOR_SUB
import com.example.calculator.Constantes.POINT
import com.example.calculator.Operations.Companion.canReplaceOperator
import com.example.calculator.Operations.Companion.divideOperation
import com.example.calculator.Operations.Companion.getOperator
import com.example.calculator.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // muestra lo que está sucediendo en tiempo real en el tvOperation
        binding.tvOperation.run {
            addTextChangedListener { charSequence ->
                if(canReplaceOperator(charSequence.toString())){
                    val newStr = "${text.substring(0, text.length - 2)}${
                        text.substring(text.length - 1)
                    }"
                    text = newStr
                }
            }
        }
    }

    fun onClickButton( view:View){
        val valueStr = (view as Button).text.toString()
        val operation = binding.tvOperation.text.toString()

        when(view.id){
            R.id.btnDelete -> {
                binding.tvOperation.run {
                    // para que no se detenga la app una vez borrado lo que habia en pantalla
                    if(text.length > 0) text = operation.substring(0, text.length-1)
                }
            }
            R.id.btnClear -> {
                binding.tvOperation.text = ""
                binding.tvResult.text = ""
            }
            R.id.btnResolve -> checkOrResolve(operation, true)
            R.id.btnMulti,
            R.id.btnDiv,
            R.id.btnSub,
            R.id.btnSum -> {
                checkOrResolve(operation, false)
                addOperator(valueStr, operation)
            }
            R.id.btnPoint -> addPoint(valueStr,operation)
            else -> binding.tvOperation.append(valueStr) // para que agregue un caracter atrás de otro
        }
    }


    //valida las operaciones para evitar errores al añadir puntos
    private fun addPoint(pointStr: String, operation: String) {
        if(!operation.contains(POINT)){
            binding.tvOperation.append(pointStr)
        } else {
            val operator = getOperator(operation)
            val values = divideOperation(operator, operation)
            // permite agregar puntos en ambos parametros de la operacion
            if(values.size > 0) {
                val numberOne = values[0]!!
                if(values.size > 1) {
                    val numberTwo = values[1]!!
                    if(numberOne.contains(POINT) && !numberTwo.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                } else {
                    if(numberOne.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                }
            }
        }
    }

    private fun addOperator(operator: String, operation: String) {
        val lastElement = if(operation.isEmpty()) ""
        else operation.substring(operation.length-1)

        if (operator == OPERATOR_SUB) {
            if (operation.isEmpty() || lastElement != OPERATOR_SUB && lastElement != POINT){
                binding.tvOperation.append(operator)
            }
        } else {
            // evita operaciones incompletas
            if (!operation.isEmpty() && lastElement!= POINT){
                binding.tvOperation.append(operator)
            }
        }
    }

    private fun checkOrResolve (operation: String, isFromResolve: Boolean){
        Operations.tryResolve(operation, isFromResolve, object: OnResolveListener{
            override fun onShowResult(result: Double) {
                // para que muestre solo 4 decimales
                binding.tvResult.text = String.format(Locale.ROOT, "%.2f", result)
                // muestra el resultado parcial en el tvOperation
                if(binding.tvResult.text.isNotEmpty() && !isFromResolve){
                    binding.tvOperation.text = binding.tvResult.text
                }
            }

            override fun onShowMessage(errorRes: Int) {
                showMessage(errorRes)
            }

        })

    }

    private fun showMessage(errorRes: Int){
        Snackbar.make(binding.root,errorRes,Snackbar.LENGTH_SHORT)
            .setAnchorView(binding.llTop)
            .show()
    }
}