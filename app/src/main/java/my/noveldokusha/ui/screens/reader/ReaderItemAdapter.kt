package my.noveldokusha.ui.screens.reader

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnNextLayout
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import my.noveldokusha.AppPreferences
import my.noveldokusha.R
import my.noveldokusha.databinding.*
import my.noveldokusha.resolvedBookImagePath
import my.noveldokusha.tools.TextSynthesisState
import my.noveldokusha.ui.screens.reader.tools.FontsLoader
import my.noveldokusha.ui.screens.reader.tools.ReaderSpeaker
import my.noveldokusha.utils.inflater

class ReaderItemAdapter(
    private val ctx: Context,
    list: List<ReaderItem>,
    private val bookUrl: String,
    private val fontsLoader: FontsLoader,
    private val appPreferences: AppPreferences,
    private val readerSpeaker: ReaderSpeaker,
    private val onChapterStartVisible: (chapterUrl: String) -> Unit,
    private val onChapterEndVisible: (chapterUrl: String) -> Unit,
    private val onReloadReader: () -> Unit,
    private val onClick: () -> Unit,
) : ArrayAdapter<ReaderItem>(ctx, 0, list) {
    override fun getCount() = super.getCount() + 2
    override fun getItem(position: Int): ReaderItem = when (position) {
        0 -> topPadding
        count - 1 -> bottomPadding
        else -> super.getItem(position - 1)!!
    }

    // Get list index from current position
    fun fromPositionToIndex(position: Int): Int = when (position) {
        in 1 until (count - 1) -> position - 1
        else -> -1
    }

    fun fromIndexToPosition(index: Int): Int = when (index) {
        in 0 until super.getCount() -> index + 1
        else -> -1
    }

    private val topPadding = ReaderItem.Padding(chapterUrl = "", chapterIndex = Int.MIN_VALUE)
    private val bottomPadding = ReaderItem.Padding(chapterUrl = "", chapterIndex = Int.MAX_VALUE)

    override fun getViewTypeCount(): Int = 11
    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ReaderItem.Body -> 0
        is ReaderItem.Image -> 1
        is ReaderItem.BookEnd -> 2
        is ReaderItem.BookStart -> 3
        is ReaderItem.Divider -> 4
        is ReaderItem.Error -> 5
        is ReaderItem.Padding -> 6
        is ReaderItem.Progressbar -> 7
        is ReaderItem.Title -> 8
        is ReaderItem.Translating -> 9
        is ReaderItem.GoogleTranslateAttribution -> 10
    }

    private fun viewTranslateAttribution(
        item: ReaderItem.GoogleTranslateAttribution,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemGoogleTranslateAttributionBinding.inflate(
                parent.inflater,
                parent,
                false
            ).also { it.root.tag = it }
            else -> ActivityReaderListItemGoogleTranslateAttributionBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewBody(item: ReaderItem.Body, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemBodyBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemBodyBinding.bind(convertView)
        }

        bind.body.updateTextSelectability()
        bind.root.background = getItemReadingStateBackground(item)
        val paragraph = item.textToDisplay + "\n"
        bind.body.text = paragraph
        bind.body.textSize = appPreferences.READER_FONT_SIZE.value
        bind.body.typeface = fontsLoader.getTypeFaceNORMAL(appPreferences.READER_FONT_FAMILY.value)

        when (item.location) {
            ReaderItem.LOCATION.FIRST -> onChapterStartVisible(item.chapterUrl)
            ReaderItem.LOCATION.LAST -> onChapterEndVisible(item.chapterUrl)
            else -> run {}
        }
        return bind.root
    }

    private fun viewImage(
        item: ReaderItem.Image,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemImageBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemImageBinding.bind(convertView)
        }

        bind.image.updateLayoutParams<ConstraintLayout.LayoutParams> {
            dimensionRatio = "1:${item.image.yrel}"
        }

        val imageModel = resolvedBookImagePath(ctx, bookUrl = bookUrl, imagePath = item.image.path)

        // Glide uses current imageView size to load the bitmap best optimized for it, but current
        // size corresponds to the last image (different size) and the view layout only updates to
        // the new values on next redraw. Execute Glide loading call in the next (parent) layout
        // update to let it get the correct values.
        // (Avoids getting "blurry" images)
        bind.imageContainer.doOnNextLayout {
            Glide.with(ctx)
                .load(imageModel)
                .fitCenter()
                .error(R.drawable.ic_baseline_error_outline_24)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(bind.image)
        }

        when (item.location) {
            ReaderItem.LOCATION.FIRST -> onChapterStartVisible(item.chapterUrl)
            ReaderItem.LOCATION.LAST -> onChapterEndVisible(item.chapterUrl)
            else -> run {}
        }

        return bind.root
    }

    private fun viewBookEnd(
        item: ReaderItem.BookEnd,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemSpecialTitleBinding.inflate(
                parent.inflater,
                parent,
                false
            ).also { it.root.tag = it }
            else -> ActivityReaderListItemSpecialTitleBinding.bind(convertView)
        }

        bind.specialTitle.updateTextSelectability()
        bind.specialTitle.text = ctx.getString(R.string.reader_no_more_chapters)
        bind.specialTitle.typeface =
            fontsLoader.getTypeFaceBOLD(appPreferences.READER_FONT_FAMILY.value)
        return bind.root
    }

    private fun viewBookStart(
        item: ReaderItem.BookStart,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {

            null -> ActivityReaderListItemSpecialTitleBinding.inflate(
                parent.inflater,
                parent,
                false
            ).also { it.root.tag = it }
            else -> ActivityReaderListItemSpecialTitleBinding.bind(convertView)
        }

        bind.specialTitle.updateTextSelectability()
        bind.specialTitle.text = ctx.getString(R.string.reader_first_chapter)
        bind.specialTitle.typeface =
            fontsLoader.getTypeFaceBOLD(appPreferences.READER_FONT_FAMILY.value)
        return bind.root
    }

    private fun viewProgressbar(
        item: ReaderItem.Progressbar,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemProgressBarBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemProgressBarBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewTranslating(
        item: ReaderItem.Translating,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemTranslatingBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemTranslatingBinding.bind(convertView)
        }
        bind.text.text = context.getString(
            R.string.translating_from_lang_a_to_lang_b,
            item.sourceLang,
            item.targetLang
        )
        return bind.root
    }

    private fun viewDivider(item: ReaderItem.Divider, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemDividerBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemDividerBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewError(item: ReaderItem.Error, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemErrorBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemErrorBinding.bind(convertView)
        }

        bind.error.updateTextSelectability()
        bind.reloadButton.setOnClickListener { onReloadReader() }
        bind.error.text = item.text
        return bind.root
    }

    private fun viewPadding(item: ReaderItem.Padding, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemPaddingBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemPaddingBinding.bind(convertView)
        }
        return bind.root
    }

    private fun viewTitle(item: ReaderItem.Title, convertView: View?, parent: ViewGroup): View {
        val bind = when (convertView) {
            null -> ActivityReaderListItemTitleBinding.inflate(parent.inflater, parent, false)
                .also { it.root.tag = it }
            else -> ActivityReaderListItemTitleBinding.bind(convertView)
        }

        bind.title.updateTextSelectability()
        bind.root.background = getItemReadingStateBackground(item)
        bind.title.text = item.textToDisplay
        bind.title.typeface = fontsLoader.getTypeFaceBOLD(appPreferences.READER_FONT_FAMILY.value)
        return bind.root
    }

    private val currentReadingAloudDrawable by lazy {
        AppCompatResources.getDrawable(
            context,
            R.drawable.translucent_current_reading_text_background
        )
    }

    private val currentReadingAloudLoadingDrawable by lazy {
        AppCompatResources.getDrawable(
            context,
            R.drawable.translucent_current_reading_loading_text_background
        )
    }

    private fun TextView.updateTextSelectability() {
        val selectableText = appPreferences.READER_SELECTABLE_TEXT.value
        setTextIsSelectable(selectableText)
        if (selectableText) {
            setTextSelectionAwareClick { onClick() }
        }
    }

    private fun getItemReadingStateBackground(item: ReaderItem): Drawable? {
        val textSynthesis = readerSpeaker.currentTextPlaying.value
        val isReadingItem = item is ReaderItem.Position &&
                textSynthesis.chapterIndex == item.chapterIndex &&
                textSynthesis.chapterItemIndex == item.chapterItemIndex

        if (!isReadingItem) return null

        return when (textSynthesis.state) {
            TextSynthesisState.PLAYING -> currentReadingAloudDrawable
            TextSynthesisState.LOADING -> currentReadingAloudLoadingDrawable
            TextSynthesisState.FINISHED -> null
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
        when (val item = getItem(position)) {
            is ReaderItem.GoogleTranslateAttribution -> viewTranslateAttribution(
                item,
                convertView,
                parent
            )
            is ReaderItem.Body -> viewBody(item, convertView, parent)
            is ReaderItem.Image -> viewImage(item, convertView, parent)
            is ReaderItem.BookEnd -> viewBookEnd(item, convertView, parent)
            is ReaderItem.BookStart -> viewBookStart(item, convertView, parent)
            is ReaderItem.Divider -> viewDivider(item, convertView, parent)
            is ReaderItem.Error -> viewError(item, convertView, parent)
            is ReaderItem.Padding -> viewPadding(item, convertView, parent)
            is ReaderItem.Progressbar -> viewProgressbar(item, convertView, parent)
            is ReaderItem.Translating -> viewTranslating(item, convertView, parent)
            is ReaderItem.Title -> viewTitle(item, convertView, parent)
        }
}

private fun View.setTextSelectionAwareClick(action: () -> Unit) {
    setOnClickListener { action() }
    setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_UP && !this.isFocused) {
            performClick()
        }
        false
    }
}