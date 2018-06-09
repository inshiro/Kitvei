package na.kephas.kitvei.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import na.kephas.kitvei.data.SearchRepository
import na.kephas.kitvei.data.MiniSearchRepository


/**
 * Factory for creating a [SearchListViewModel] with a constructor that takes a [SearchRepository].
 */
class MiniSearchViewModelFactory(
        private val repository: MiniSearchRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = MiniSearchViewModel(repository) as T

}
