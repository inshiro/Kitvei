package na.kephas.kitvei.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import na.kephas.kitvei.data.VerseRepository


/**
 * Factory for creating a [VerseListViewModel] with a constructor that takes a [VerseRepository].
 */
class VerseListViewModelFactory(
        private val repository: VerseRepository
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>) = VerseListViewModel(repository) as T

}
