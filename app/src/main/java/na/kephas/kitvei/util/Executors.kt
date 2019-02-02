package na.kephas.kitvei.util

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.io.Closeable
import java.util.concurrent.*
import kotlin.coroutines.CoroutineContext


/**
 * Utility method to run blocks on a dedicated background thread, used for io/database work.
 */
@ExperimentalCoroutinesApi
val IO = ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L,
        TimeUnit.SECONDS, SynchronousQueue<Runnable>()
).asCoroutineDispatcher()

val IO_EXECUTOR by lazy { Executors.newSingleThreadExecutor() }
fun ioThread(f: () -> Unit) {
    IO_EXECUTOR.execute(f)
}

val UI by lazy { Dispatchers.Main }

class UiThreadExecutor : Executor {
    private val mHandler = Handler(Looper.getMainLooper())

    override fun execute(command: Runnable) {
        mHandler.post(command)
    }
}

val rootParent: Job by lazy { Job() }

@ExperimentalCoroutinesApi
val backgroundPool: CoroutineDispatcher by lazy(LazyThreadSafetyMode.PUBLICATION) {
    Executors.newCachedThreadPool().asCoroutineDispatcher()
}
@ExperimentalCoroutinesApi
val fixedThreadPool: CoroutineDispatcher by lazy(LazyThreadSafetyMode.PUBLICATION) {
    // Fix for dual or single core devices
    val numProcessors = Runtime.getRuntime().availableProcessors()
    //val numProcessors = ForkJoinPool.commonPool().parallelism
    when {
        numProcessors <= 2 -> Executors.newFixedThreadPool(2).asCoroutineDispatcher()
        else -> Dispatchers.Default // CommonPool
    }

}

fun launch(dispatcher: CoroutineDispatcher, block: suspend CoroutineScope.() -> Unit): Job =
        launchSilent(dispatcher) {
            block()
        }

fun <T> async(dispatcher: CoroutineDispatcher, block: suspend CoroutineScope.() -> T): Deferred<T> =
        GlobalScope.async(dispatcher) {
            block()
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

@ExperimentalCoroutinesApi
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
        block: suspend CoroutineScope.() -> Unit
): Job =
    GlobalScope.launch(context, start, block)
    // ${Thread.currentThread().name}



