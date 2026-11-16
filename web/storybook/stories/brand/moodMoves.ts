// The moves of Poulpy's moods, shared by the page of drafts (PoulpyMoodMoves.vue) and the Home mockup (MobilePoulpyMood.vue), and baked for the phones
// (design/build-poulpy-pictures.mjs): they are written once, in plain JavaScript, in design/poulpy-3d/moods.mjs.
export { MOOD_MOVES, spinning, tucking } from '../../../../design/poulpy-3d/moods.mjs'

/** A move is anything that gives a pose for a time step. */
export interface Move { advance: (seconds: number) => unknown }
