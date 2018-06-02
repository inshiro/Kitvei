package na.kephas.kitvei

import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.flaviofaria.kenburnsview.KenBurnsView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.base_content.*
import kotlinx.android.synthetic.main.base_content_activity.*

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext
import na.kephas.kitvei.util.Fonts
import na.kephas.kitvei.util.toSpanned

class DedicatoryActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_content_activity)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val cTitle = R.string.dedicatory_title
        val cText = R.string.dedicatory_text
        val cDrawable = R.drawable.dedicatory

        toolbar_layout.title = getString(cTitle)
        toolbar_layout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar)
        toolbar_layout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar)

        showText(cText, cDrawable, true)

        /*
        val webView = findViewById<WebView>(R.id.webview)
        webView.isVerticalScrollBarEnabled = true
        webView.isHorizontalScrollBarEnabled = true
        webview.settings.builtInZoomControls = true
        @Suppress("DEPRECATION")
        webview.settings.setRenderPriority(WebSettings.RenderPriority.HIGH)
        webview.settings.cacheMode = WebSettings.LOAD_NO_CACHE
        webView.loadUrl("file:///android_asset/html/dedicatory.html")*/
    }

    override fun onPause() {
        super.onPause()
        if(isFinishing) overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun showText(cText: Int, cDrawable: Int, showText: Boolean) {
        //val tempImage = ContextCompat.getDrawable(context, R.drawable.image)
        /*val tempImage = resources.getDrawable(R.drawable.dedicatory)
        val kenBurnsView = findViewById(R.id.base_content_image) as KenBurnsView
        kenBurnsView.setImageDrawable(tempImage)*/
        //val context = this
        async(CommonPool) {
            val span = getString(cText).toSpanned()
            val kenBurnsView = findViewById<KenBurnsView>(R.id.base_content_image)
            //val image = ContextCompat.getDrawable(context, cDrawable)
            withContext(UI) {
                if (showText) {
                    baseTextView.text = span
                    baseTextView.typeface = Fonts.GentiumPlus_R
                }
                //Glide.with(context).load(cDrawable).into(kenBurnsView)
                //kenBurnsView.setImageDrawable(image)
                Picasso.get().load(cDrawable).into(kenBurnsView)
            }
        }
    }

}

