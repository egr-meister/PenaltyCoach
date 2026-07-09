package com.penaltycoach.app

import android.app.Application

/**
 * Minimal Application subclass. No SDK initialization happens here — the app has
 * no Firebase, analytics, ads, or background services to set up. It exists only
 * so the manifest can reference a stable application name.
 */
class PenaltyCoachApp : Application()
