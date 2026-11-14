// The account's workouts, read from the API (docs/the-api.md, ADR 0027). Every call asks for `include=text`: the words
// (labels, counts, prescriptions, muscles) are said by the server, the same the phone shows, so nothing here words a workout.
import { authorized } from './session'

export type WorkoutFormat = 'CIRCUIT' | 'SETS'

export type WorkoutRow = {
  id: string
  name: string
  format: WorkoutFormat
  exerciseCount: number
  minutes: number
  timesDone: number
  lastDoneAt: string | null
  text: { format: string; details: string; exercises: string; length: string }
}

export type WorkoutList = { items: WorkoutRow[]; total: number; page: number; size: number; text: { count: string } }

export type WorkoutsQuery = { q: string; format: '' | 'circuit' | 'sets'; page: number; size: number }

export type StepText = { key: string; slug: string; name: string; prescription: string; roleLabel: string | null; isRest: boolean }
export type PartStepText = { key: string; row: StepText | null; repeat: { key: string; times: string; rows: StepText[] } | null }
export type PartText = { key: string; kind: 'WARM_UP' | 'WORK' | 'COOL_DOWN'; name: string; summary: string; time: string; steps: PartStepText[] }

export type Workout = {
  workout: { id: string; name: string; format: WorkoutFormat }
  history: { timesDone: number; lastDoneAt: string | null; usualMinutes: number | null }
  text: {
    format: string
    details: string
    equipment: string[]
    parts: PartText[]
    muscles: { group: string; label: string; sets: number; setsLabel: string; heat: number; percent: number }[]
    heat: Record<string, number>
    timesDone: string
    usual: string
  }
}

/** One page of workouts, or nothing when it can't be loaded. */
export async function listWorkouts(query: WorkoutsQuery): Promise<WorkoutList | undefined> {
  const params = new URLSearchParams({ include: 'text', page: String(query.page), size: String(query.size) })
  if (query.q) params.set('q', query.q)
  if (query.format) params.set('format', query.format)
  try {
    const response = await authorized(`/workouts?${params}`)
    return response.ok ? ((await response.json()) as WorkoutList) : undefined
  } catch {
    return undefined
  }
}

/** One workout: `missing` when the account has none by that id, nothing when it can't be loaded. */
export async function getWorkout(id: string): Promise<Workout | 'missing' | undefined> {
  try {
    const response = await authorized(`/workouts/${encodeURIComponent(id)}?include=text`)
    if (response.status === 404) return 'missing'
    return response.ok ? ((await response.json()) as Workout) : undefined
  } catch {
    return undefined
  }
}
