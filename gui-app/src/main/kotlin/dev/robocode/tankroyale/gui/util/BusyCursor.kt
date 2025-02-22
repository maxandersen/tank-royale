package dev.robocode.tankroyale.gui.util

import dev.robocode.tankroyale.gui.ui.MainFrame
import java.awt.Cursor
import java.util.concurrent.atomic.AtomicInteger

object BusyCursor {

    private var count = AtomicInteger(0)

    fun activate() {
        count.incrementAndGet()
        MainFrame.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    }

    fun deactivate() {
        if (count.decrementAndGet() == 0) MainFrame.cursor = Cursor.getDefaultCursor()
    }
}