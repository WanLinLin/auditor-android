package com.example.auditor;

/**
 * Created by wanlin on 15/7/21.
 * Stores the frequencies for equal-tempered scale, A4 = 440 Hz.
 */
public enum NotesFrequency {
    Pause       ("Pause",       -1.0f),

    C0          ("C0",          16.35f),
    C0sAndD0f   ("C0sAndD0f",   17.32f),
    D0          ("D0",          18.45f),
    D0sAndE0f   ("D0sAndE0f",   19.45f),
    E0          ("E0",          20.60f),
    F0          ("F0",          21.83f),
    F0sAndG0f   ("F0sAndG0f",   23.12f),
    G0          ("G0",          24.50f),
    G0sAndA0b   ("G0sAndA0b",   25.96f),
    A0          ("A0",          27.50f),
    A0sAndB0f   ("A0sAndB0f",   29.14f),
    B0          ("B0",          30.87f),

    C1          ("C1",          32.70f),
    C1sAndD1f   ("C1sAndD1f",   34.65f),
    D1          ("D1",          36.71f),
    D1sAndE1f   ("D1sAndE1f",   38.89f),
    E1          ("E1",          41.20f),
    F1          ("F1",          43.65f),
    F1sAndG1f   ("F1sAndG1f",   46.25f),
    G1          ("G1",          49.00f),
    G1sAndA1b   ("G1sAndA1b",   51.91f),
    A1          ("A1",          55.00f),
    A1sAndB1f   ("A1sAndB1f",   58.27f),
    B1          ("B1",          61.74f),

    C2          ("C2",          65.41f),
    C2sAndD2f   ("C2sAndD2f",   69.30f),
    D2          ("D2",          73.42f),
    D2sAndE2f   ("D2sAndE2f",   77.78f),
    E2          ("E2",          82.41f),
    F2          ("F2",          87.31f),
    F2sAndG2f   ("F2sAndG2f",   92.50f),
    G2          ("G2",          98.00f),
    G2sAndA2b   ("G2sAndA2b",   103.83f),
    A2          ("A2",          110.00f),
    A2sAndB2f   ("A2sAndB2f",   116.54f),
    B2          ("B2",          123.47f),

    C3          ("C3",          130.81f),
    C3sAndD3f   ("C3sAndD3f",   138.59f),
    D3          ("D3",          146.83f),
    D3sAndE3f   ("D3sAndE3f",   155.56f),
    E3          ("E3",          164.81f),
    F3          ("F3",          174.61f),
    F3sAndG3f   ("F3sAndG3f",   185.00f),
    G3          ("G3",          196.00f),
    G3sAndA3b   ("G3sAndA3b",   207.65f),
    A3          ("A3",          220.00f),
    A3sAndB3f   ("A3sAndB3f",   233.08f),
    B3          ("B3",          246.94f),

    C4          ("C4",          261.63f),
    C4sAndD4f   ("C4sAndD4f",   277.18f),
    D4          ("D4",          293.66f),
    D4sAndE4f   ("D4sAndE4f",   311.13f),
    E4          ("E4",          329.63f),
    F4          ("F4",          349.23f),
    F4sAndG4f   ("F4sAndG4f",   369.99f),
    G4          ("G4",          392.00f),
    G4sAndA4b   ("G4sAndA4b",   415.30f),
    A4          ("A4",          440.00f),
    A4sAndB4f   ("A4sAndB4f",   466.16f),
    B4          ("B4",          493.88f),

    C5          ("C5",          523.25f),
    C5sAndD5f   ("C5sAndD5f",   554.37f),
    D5          ("D5",          587.33f),
    D5sAndE5f   ("D5sAndE5f",   622.25f),
    E5          ("E5",          659.25f),
    F5          ("F5",          698.46f),
    F5sAndG5f   ("F5sAndG5f",   739.99f),
    G5          ("G5",          783.99f),
    G5sAndA5b   ("G5sAndA5b",   830.61f),
    A5          ("A5",          880.00f),
    A5sAndB5f   ("A5sAndB5f",   932.33f),
    B5          ("B5",          987.77f),

    C6          ("C6",          1046.50f),
    C6sAndD6f   ("C6sAndD6f",   1108.73f),
    D6          ("D6",          1174.66f),
    D6sAndE6f   ("D6sAndE6f",   1244.51f),
    E6          ("E6",          1318.51f),
    F6          ("F6",          1396.91f),
    F6sAndG6f   ("F6sAndG6f",   1479.98f),
    G6          ("G6",          1567.98f),
    G6sAndA6b   ("G6sAndA6b",   1661.22f),
    A6          ("A6",          1760.00f),
    A6sAndB6f   ("A6sAndB6f",   1864.66f),
    B6          ("B6",          1975.53f),

    C7          ("C7",          2093.00f),
    C7sAndD7f   ("C7sAndD7f",   2217.46f),
    D7          ("D7",          2349.32f),
    D7sAndE7f   ("D7sAndE7f",   2489.02f),
    E7          ("E7",          2637.02f),
    F7          ("F7",          2793.83f),
    F7sAndG7f   ("F7sAndG7f",   2959.96f),
    G7          ("G7",          3135.96f),
    G7sAndA7b   ("G7sAndA7b",   3322.44f),
    A7          ("A7",          3520.00f),
    A7sAndB7f   ("A7sAndB7f",   3729.31f),
    B7          ("B7",          3951.07f),

    C8          ("C8",          4186.01f),
    C8sAndD8f   ("C8sAndD8f",   4434.92f),
    D8          ("D8",          4698.63f),
    D8sAndE8f   ("D8sAndE8f",   4978.03f),
    E8          ("E8",          5274.04f),
    F8          ("F8",          5587.65f),
    F8sAndG8f   ("F8sAndG8f",   5919.91f),
    G8          ("G8",          6271.93f),
    G8sAndA8b   ("G8sAndA8b",   6644.88f),
    A8          ("A8",          7040.00f),
    A8sAndB8f   ("A8sAndB8f",   7458.62f),
    B8          ("B8",          7902.13f);

    private String note;
    private float frequency; // int Hertz
    private static NotesFrequency[] notesFrequencies = values();

    NotesFrequency(String note, float frequency) {
        this.note = note;
        this.frequency = frequency;
    }

    public static String getNote(float frequency) {
        // is pause
        if (frequency == NotesFrequency.Pause.frequency)
            return NotesFrequency.Pause.note;

        // is a note
        for (NotesFrequency cur : notesFrequencies) {
            if (cur.frequency == frequency) {
                return cur.note;
            }

            else if (cur.frequency > frequency) {
                NotesFrequency pre = cur.previous();
                float middleFrequency = (pre.frequency + cur.frequency) / 2;

                if(frequency < middleFrequency) {
                    return pre.note;
                }
                else {
                    return cur.note;
                }
            }
        }

        return null;
    }

    /* getter and setter */
    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public float getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    /* get previous or next Object */
    private NotesFrequency next() {
        return notesFrequencies[(this.ordinal() + 1) % notesFrequencies.length];
    }

    private NotesFrequency previous() {
        return notesFrequencies[(this.ordinal() - 1) % notesFrequencies.length];
    }
}