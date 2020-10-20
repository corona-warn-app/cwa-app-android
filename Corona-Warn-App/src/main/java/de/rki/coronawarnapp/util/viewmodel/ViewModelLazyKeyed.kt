package de.rki.coronawarnapp.util.viewmodel

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlin.reflect.KClass

/**
 * This is a fork of the standard ViewModel code that allows to optionally provide
 * alternative keys for ViewModels, to allow for more freedom when using multiple viewModels.
 * Note the `keyProducer` argument.
 */

/**
 * An implementation of [Lazy] used by [androidx.fragment.app.Fragment.viewModels] and
 * [androidx.activity.ComponentActivity.viewmodels].
 *
 * [storeProducer] is a lambda that will be called during initialization, [VM] will be created
 * in the scope of returned [ViewModelStore].
 *
 * [factoryProducer] is a lambda that will be called during initialization,
 * returned [ViewModelProvider.Factory] will be used for creation of [VM]
 */
class ViewModelLazyKeyed<VM : ViewModel>(
    private val viewModelClass: KClass<VM>,
    private val keyProducer: (() -> String)? = null,
    private val storeProducer: () -> ViewModelStore,
    private val factoryProducer: () -> ViewModelProvider.Factory
) : Lazy<VM> {
    private var cached: VM? = null

    override val value: VM
        get() {
            val viewModel = cached
            return if (viewModel == null) {
                val factory = factoryProducer()
                val store = storeProducer()
                val key = keyProducer?.invoke() ?: "androidx.lifecycle.ViewModelProvider.DefaultKey"
                ViewModelProvider(store, factory).get(
                    key + ":" + viewModelClass.java.canonicalName,
                    viewModelClass.java
                ).also {
                    cached = it
                }
            } else {
                viewModel
            }
        }

    override fun isInitialized() = cached != null
}

/**
 * Creates a lazily instantiated ViewModel with Fragment scope
 */
@MainThread
fun <VM : ViewModel> Fragment.createViewModelLazyKeyed(
    viewModelClass: KClass<VM>,
    keyProducer: (() -> String)? = null,
    storeProducer: () -> ViewModelStore = { viewModelStore },
    factoryProducer: (() -> ViewModelProvider.Factory) = {
        val application = activity?.application ?: throw IllegalStateException(
            "ViewModel can be accessed only when Fragment is attached"
        )
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
): Lazy<VM> = ViewModelLazyKeyed(viewModelClass, keyProducer, storeProducer, factoryProducer)

/**
 * Creates a lazily instantiated ViewModel with Activity scope
 */
@MainThread
fun <VM : ViewModel> ComponentActivity.createViewModelLazyKeyed(
    viewModelClass: KClass<VM>,
    keyProducer: (() -> String)? = null,
    storeProducer: () -> ViewModelStore = { viewModelStore },
    factoryProducer: (() -> ViewModelProvider.Factory) = {
        val application = application ?: throw IllegalArgumentException(
            "ViewModel can be accessed only when Activity is attached"
        )
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
): Lazy<VM> = ViewModelLazyKeyed(viewModelClass, keyProducer, storeProducer, factoryProducer)
