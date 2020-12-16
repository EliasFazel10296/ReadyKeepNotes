package net.geeksempire.keepnotes.Overview.UserInterface

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import net.geeksempire.keepnotes.databinding.OverviewLayoutBinding

class KeepNoteOverview : AppCompatActivity() {

    lateinit var overviewLayoutBinding: OverviewLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overviewLayoutBinding = OverviewLayoutBinding.inflate(layoutInflater)
        setContentView(overviewLayoutBinding.root)



    }

}