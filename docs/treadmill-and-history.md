# Treadmill and history

How the **Treadmill** tab records a session and how the **History** tab shows what was done. Both read the same session records ([architecture](./architecture.md#devices)).

## Treadmill (Bluetooth FTMS)

The **Treadmill** tab finds a treadmill that speaks the Bluetooth Fitness Machine Service, shows its live readings and records a session, either free or following a treadmill workout (a built-in demo workout is there until the workout builder exists). Recording is read-only: the app never changes the treadmill's speed or incline yet, though `device/ftms` already encodes those commands.

Testing on a new machine: the bottom of the tab lists what the treadmill says it can report and counts any packets the decoder could not read. Treadmills differ in which fields they send, so a metric that never shows up is usually one the machine does not send.

## History

The **History** tab has two views. **Calendar** shows a month, Monday first, with the days you trained filled in; swipe or use the arrows to go back through earlier months. Under it, that month's sessions are grouped by day, newest first, and tapping a day scrolls the list to it (a day without sessions shows as a rest day). **Stats** shows this week's and this month's totals, charts of the last eight weeks (active minutes, treadmill kilometres), the total number of sessions, the longest run and Poulpy's full octopuses. Each session's totals (`core/stats`) are saved next to it when it ends, so the history opens without replaying the event log; sessions recorded before the history existed are summed up once, the first time the tab opens. Sessions can be deleted from their detail screen.
