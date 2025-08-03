package com.p9ph.integrator

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.*

@Serializable
data class AutomationScript(val id: String, val triggers: List<String>, val actions: List<String>)

class RealTimeAutomationScriptIntegrator(private val scriptRepository: ScriptRepository) {
    private val scriptCache = mutableMapOf<String, AutomationScript>()
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            scriptRepository.observeScripts().collect { scripts ->
                scripts.forEach { script ->
                    scriptCache[script.id] = script
                }
            }
        }
    }

    fun integrate(triggers: List<String>) {
        scope.launch {
            val scriptsToRun = scriptCache.filter { script ->
                triggers.any { trigger -> script.triggers.contains(trigger) }
            }
            scriptsToRun.forEach { (_, script) ->
                script.actions.forEach { action ->
                    // execute action
                    println("Executing action: $action")
                }
            }
        }
    }
}

interface ScriptRepository {
    fun observeScripts(): Flow<List<AutomationScript>>
}

class ScriptRepositoryImpl : ScriptRepository {
    override fun observeScripts(): Flow<List<AutomationScript>> {
        // implement script fetching logic here
        // for demo purposes, returning a static list
        return flow {
            emit(listOf(
                AutomationScript("script1", listOf("trigger1", "trigger2"), listOf("action1", "action2")),
                AutomationScript("script2", listOf("trigger2", "trigger3"), listOf("action3", "action4"))
            ))
        }
    }
}

fun main() {
    val scriptRepository = ScriptRepositoryImpl()
    val integrator = RealTimeAutomationScriptIntegrator(scriptRepository)
    integrator.integrate(listOf("trigger1", "trigger2"))
}