package na.kephas.kitvei.util

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.experimental.*
import java.io.Closeable
import java.util.concurrent.*
import kotlin.coroutines.experimental.CoroutineContext


/**
 * Utility method to run blocks on a dedicated background thread, used for io/database work.
 */
val IO = ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
        TimeUnit.SECONDS, SynchronousQueue<Runnable>()
).asCoroutineDispatcher()

val IO_EXECUTOR by lazy { Executors.newSingleThreadExecutor() }
fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}


class UiThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}

val rootParent: Job by lazy { Job() }
val backgroundPool: CoroutineDispatcher by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Executors.newCachedThreadPool().asCoroutineDispatcher()
}
val fixedThreadPool: CoroutineDispatcher by lazy(LazyThreadSafetyMode.PUBLICATION) {
    // Fix for dual or single core devices
    val numProcessors = Runtime.getRuntime().availableProcessors()
    //val numProcessors = ForkJoinPool.commonPool().parallelism
    when {
        numProcessors <= 2 -> newFixedThreadPoolContext(2, "background")
        else -> CommonPool
    }

}
fun <T> AppCompatActivity.waitFor(block: () -> T): T {
    val job = async(backgroundPool) {
        block()
    }
    return runBlocking {
        job.await()
    }

}
class BackgroundThreadExecutor : Executor {
    //val threadPool = Executors.newCachedThreadPool().asCoroutineDispatcher()
    //private val executorService = Executors.newFixedThreadPool(3)
    val executorService = Executors.newCachedThreadPool()

    override fun execute(command: Runnable) {
        //launchSilent(executorService.asCoroutineDispatcher()) {
        executorService.execute(command)
        //Log.d("BGThreadExecutor", "I'm working in thread: ${Thread.currentThread().name}")
        //}

    }
}

abstract class CloseableCoroutineDispatcher : CoroutineDispatcher(), Closeable

fun ExecutorService.asCoroutineDispatcher(): CloseableCoroutineDispatcher =
        object : CloseableCoroutineDispatcher() {
            val delegate = (this@asCoroutineDispatcher as Executor).asCoroutineDispatcher()
            override fun isDispatchNeeded(context: CoroutineContext): Boolean = delegate.isDispatchNeeded(context)
            override fun dispatch(context: CoroutineContext, block: Runnable) = delegate.dispatch(context, block)
            override fun close() = shutdown()
        }

/**
 * Equivalent to [launch] but return [Unit] instead of [Job].
 *
 * Mainly for usage when you want to lift [launch] to return. Example:
 *
 * ```
 * override fun loadData() = launchSilent {
 *     // code
 * }
 * ```
 */
fun launchSilent(
        context: CoroutineContext = backgroundPool,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        parent: Job? = rootParent,
        onCompletion: CompletionHandler? = null,
        block: suspend CoroutineScope.() -> Unit
) {
    launch(context, start, parent, onCompletion, block)
    // ${Thread.currentThread().name}
}


