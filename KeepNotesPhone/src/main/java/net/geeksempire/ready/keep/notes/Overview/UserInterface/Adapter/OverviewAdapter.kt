package net.geeksempire.ready.keep.notes.Overview.UserInterface.Adapter

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import net.geeksempire.ready.keep.notes.Database.DataStructure.NotesDatabaseModel
import net.geeksempire.ready.keep.notes.Notes.Taking.TakeNote
import net.geeksempire.ready.keep.notes.Overview.UserInterface.KeepNoteOverview
import net.geeksempire.ready.keep.notes.Preferences.Theme.ThemeType
import net.geeksempire.ready.keep.notes.R
import kotlin.math.abs


class OverviewAdapter(val context: KeepNoteOverview) : RecyclerView.Adapter<OverviewViewHolder>() {

    val notesDataStructureList: ArrayList<NotesDatabaseModel> = ArrayList<NotesDatabaseModel>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int) : OverviewViewHolder {

        return OverviewViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.overview_saved_notes_item,
                viewGroup,
                false
            )
        )
    }

    override fun getItemCount() : Int {

        return notesDataStructureList.size
    }

    override fun onBindViewHolder(
        overviewViewHolder: OverviewViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        super.onBindViewHolder(overviewViewHolder, position, payloads)

        when (context.themePreferences.checkThemeLightDark()) {
            ThemeType.ThemeLight -> {

                val tintedBackgroundDrawable =
                    context.getDrawable(R.drawable.round_corner_background)
                tintedBackgroundDrawable?.setTint(context.getColor(R.color.dark_transparent))

                overviewViewHolder.rootItemContentView.background = tintedBackgroundDrawable

                val imageContentBackground = context.getDrawable(R.drawable.round_corner_background)
                imageContentBackground?.setTint(context.getColor(R.color.dark_transparent))

                overviewViewHolder.contentImageView.background = imageContentBackground

                overviewViewHolder.titleTextView.setTextColor(context.getColor(R.color.dark))
                overviewViewHolder.contentTextView.setTextColor(context.getColor(R.color.dark))

            }
            ThemeType.ThemeDark -> {

                val tintedBackgroundDrawable =
                    context.getDrawable(R.drawable.round_corner_background)
                tintedBackgroundDrawable?.setTint(context.getColor(R.color.light_transparent))

                overviewViewHolder.rootItemContentView.background = tintedBackgroundDrawable

                val imageContentBackground = context.getDrawable(R.drawable.round_corner_background)
                imageContentBackground?.setTint(context.getColor(R.color.light_transparent))

                overviewViewHolder.contentImageView.background = imageContentBackground

                overviewViewHolder.titleTextView.setTextColor(context.getColor(R.color.light))
                overviewViewHolder.contentTextView.setTextColor(context.getColor(R.color.light))

            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(overviewViewHolder: OverviewViewHolder, position: Int) {

        context.firebaseUser?.let {

            overviewViewHolder.titleTextView.text = context.contentEncryption.decryptEncodedData(
                notesDataStructureList[position].noteTile.toString(),
                context.firebaseUser.uid
            )

            overviewViewHolder.contentTextView.text = context.contentEncryption.decryptEncodedData(
                notesDataStructureList[position].noteTextContent.toString(),
                context.firebaseUser.uid
            )

        }

        when (context.themePreferences.checkThemeLightDark()) {
            ThemeType.ThemeLight -> {

                val tintedBackgroundDrawable =
                    context.getDrawable(R.drawable.round_corner_background)
                tintedBackgroundDrawable?.setTint(context.getColor(R.color.dark_transparent))

                overviewViewHolder.rootItemContentView.background = tintedBackgroundDrawable

                val imageContentBackground = context.getDrawable(R.drawable.round_corner_background)
                imageContentBackground?.setTint(context.getColor(R.color.dark_transparent))

                overviewViewHolder.contentImageView.background = imageContentBackground

                overviewViewHolder.titleTextView.setTextColor(context.getColor(R.color.dark))
                overviewViewHolder.contentTextView.setTextColor(context.getColor(R.color.dark))

            }
            ThemeType.ThemeDark -> {

                val tintedBackgroundDrawable =
                    context.getDrawable(R.drawable.round_corner_background)
                tintedBackgroundDrawable?.setTint(context.getColor(R.color.light_transparent))

                overviewViewHolder.rootItemContentView.background = tintedBackgroundDrawable

                val imageContentBackground = context.getDrawable(R.drawable.round_corner_background)
                imageContentBackground?.setTint(context.getColor(R.color.light_transparent))

                overviewViewHolder.contentImageView.background = imageContentBackground

                overviewViewHolder.titleTextView.setTextColor(context.getColor(R.color.light))
                overviewViewHolder.contentTextView.setTextColor(context.getColor(R.color.light))

            }
        }

        if (notesDataStructureList[position].noteHandwritingSnapshotLink == null) {

            overviewViewHolder.contentImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            overviewViewHolder.contentImageView.setColorFilter(context.getColor(R.color.pink_transparent))
            overviewViewHolder.contentImageView.setImageDrawable(context.getDrawable(R.drawable.icon_no_content))

        } else {

            overviewViewHolder.contentImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            overviewViewHolder.contentImageView.clearColorFilter()

            Glide.with(context)
                .load(notesDataStructureList[position].noteHandwritingSnapshotLink)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(overviewViewHolder.contentImageView)

        }

        overviewViewHolder.rootItemContentView.setOnClickListener {

            if (!overviewViewHolder.optionsView.isShown) {

                overviewViewHolder.waitingViewLoading.visibility = View.VISIBLE

                val paintingPathsJsonArray = notesDataStructureList[position].noteHandwritingPaintingPaths

                if (paintingPathsJsonArray.isNullOrEmpty()) {

                    overviewViewHolder.waitingViewLoading.visibility = View.GONE

                    context.startActivity(Intent(context, TakeNote::class.java).apply {
                        putExtra(TakeNote.NoteTakingWritingType.DocumentId, notesDataStructureList[position].uniqueNoteId)
                        putExtra(TakeNote.NoteTakingWritingType.TitleText, notesDataStructureList[position].noteTile)
                        putExtra(TakeNote.NoteTakingWritingType.ContentText, notesDataStructureList[position].noteTextContent)
                    }, ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, 0).toBundle())

                } else {

                    overviewViewHolder.waitingViewLoading.visibility = View.GONE

                    context.startActivity(Intent(context, TakeNote::class.java).apply {
                        putExtra(TakeNote.NoteTakingWritingType.DocumentId, notesDataStructureList[position].uniqueNoteId)
                        putExtra(TakeNote.NoteTakingWritingType.TitleText, notesDataStructureList[position].noteTile.toString())
                        putExtra(TakeNote.NoteTakingWritingType.ContentText, notesDataStructureList[position].noteTextContent.toString())
                        putExtra(TakeNote.NoteTakingWritingType.PaintingPath, paintingPathsJsonArray)
                    }, ActivityOptions.makeCustomAnimation(context, R.anim.fade_in, 0).toBundle())

                }

            }

        }

        var differentiateX: Float = 0f

        overviewViewHolder.rootItemContentView.setOnTouchListener { view, motionEvent ->

            when (motionEvent.action) {

                MotionEvent.ACTION_DOWN -> {

                    differentiateX = overviewViewHolder.rootItemContentView.x - motionEvent.rawX

                }
                MotionEvent.ACTION_MOVE -> {

                    val dxAbsolute = abs(motionEvent.rawX + differentiateX)

                    if (motionEvent.rawX < abs(differentiateX)) {

                        if (dxAbsolute < overviewViewHolder.optionsView.width) {

                            overviewViewHolder.rootItemContentView.x = motionEvent.rawX + differentiateX

                            if (!overviewViewHolder.optionsView.isShown) {

                                overviewViewHolder.optionsView.visibility = View.VISIBLE
                                overviewViewHolder.optionsView.startAnimation(AnimationUtils.loadAnimation(context, R.anim.fade_in))

                            }

                        }

                    } else {

                        overviewViewHolder.rootItemContentView.x = 0f

                    }

                }
                MotionEvent.ACTION_UP -> {

                    overviewViewHolder.rootItemContentView.x = -overviewViewHolder.optionsView.width.toFloat()

                }
                MotionEvent.ACTION_CANCEL -> {

                    overviewViewHolder.rootItemContentView.x = -overviewViewHolder.optionsView.width.toFloat()

                }
                else -> {


                }
            }

            false
        }


    }

}